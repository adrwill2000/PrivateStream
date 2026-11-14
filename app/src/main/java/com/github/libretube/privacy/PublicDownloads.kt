package com.github.libretube.privacy

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.provider.MediaStore
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import com.github.libretube.db.DatabaseHolder
import com.github.libretube.enums.FileType
import com.github.libretube.extensions.toAndroidUri
import com.github.libretube.util.MediaExporter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/** Publish completed downloads to normal shared storage, retaining offline originals. */
object PublicDownloads {
    private val mutex = Mutex()
    private val writing = java.util.concurrent.ConcurrentHashMap.newKeySet<Int>()
    fun started(id: Int) { writing.add(id) }

    @OptIn(UnstableApi::class)
    suspend fun finished(context: Context, id: Int, videoId: String) {
        writing.remove(id)
        if (Build.VERSION.SDK_INT < 29) return // Older devices retain the upstream SAF export action.
        mutex.withLock {
            val download = DatabaseHolder.Database.downloadDao().getDownloadById(videoId) ?: return
            val items = download.downloadItems.filter { it.type != FileType.SUBTITLE }
            if (items.isEmpty() || items.any { !it.isFinished || it.id in writing }) return
            val prefs = context.getSharedPreferences("public_downloads", Context.MODE_PRIVATE)
            val key = items.joinToString("_") { it.id.toString() }
            if (prefs.contains(key)) return
            val video = items.firstOrNull { it.type == FileType.VIDEO }
            val audio = items.firstOrNull { it.type == FileType.AUDIO }
            var temporary: File? = null
            try {
                val source = if (video != null && audio != null) {
                    val temp = File.createTempFile("export-", ".mp4", context.cacheDir)
                    temporary = temp
                    withContext(Dispatchers.Main) {
                        suspendCancellableCoroutine<Unit> { continuation ->
                            MediaExporter.muxMedia(context, audio.path.toAndroidUri(), video.path.toAndroidUri(), temp.path,
                                onCompletion = { if (continuation.isActive) continuation.resume(Unit) },
                                onError = { if (continuation.isActive) continuation.resumeWithException(it) })
                        }
                    }
                    temp
                } else (video ?: audio!!).path.toFile()
                val extension = if (temporary != null) "mp4" else source.extension.ifEmpty { "bin" }
                val values = ContentValues().apply {
                    put(MediaStore.Downloads.DISPLAY_NAME, "PrivateStream-$videoId.$extension")
                    put(MediaStore.Downloads.MIME_TYPE, if (video != null) "video/$extension" else if (extension == "mp4" || extension == "m4a") "audio/mp4" else "audio/$extension")
                    put(MediaStore.Downloads.RELATIVE_PATH, "Download/PrivateStream")
                    put(MediaStore.Downloads.IS_PENDING, 1)
                }
                val resolver = context.contentResolver
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                    ?: error("Unable to create shared download")
                try {
                    resolver.openOutputStream(uri)?.use { output -> source.inputStream().use { it.copyTo(output) } }
                        ?: error("Unable to open shared download")
                    resolver.update(uri, ContentValues().apply { put(MediaStore.Downloads.IS_PENDING, 0) }, null, null)
                    prefs.edit().putString(key, uri.toString()).apply()
                } catch (error: Exception) {
                    resolver.delete(uri, null, null)
                    throw error
                }
            } finally { temporary?.delete() }
        }
    }
}
