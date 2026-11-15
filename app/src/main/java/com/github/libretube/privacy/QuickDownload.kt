package com.github.libretube.privacy

import android.content.Context
import android.widget.Toast
import com.github.libretube.api.MediaServiceRepository
import com.github.libretube.api.obj.Streams
import com.github.libretube.helpers.DownloadHelper
import com.github.libretube.parcelable.DownloadData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object QuickDownload {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    fun start(context: Context, id: String, streams: Streams, audioOnly: Boolean) {
        val appContext = context.applicationContext
        scope.launch { startDownload(appContext, id, streams, audioOnly) }
    }

    private suspend fun startDownload(context: Context, id: String, streams: Streams, audioOnly: Boolean) {
        val needsAudio = selectAudioStream(streams) == null
        val needsVideo = !audioOnly && selectVideoStream(streams) == null
        val resolvedStreams = if (needsAudio || needsVideo) {
            runCatching {
                withContext(Dispatchers.IO) { MediaServiceRepository.instance.getStreams(id) }
            }.getOrElse {
                Toast.makeText(context, "Could not load download formats", Toast.LENGTH_LONG).show()
                return
            }
        } else streams

        val audio = selectAudioStream(resolvedStreams)
        val video = if (audioOnly) null else selectVideoStream(resolvedStreams)
        if ((audioOnly && audio == null) || (!audioOnly && video == null)) {
            Toast.makeText(context, "No compatible stream; try download options", Toast.LENGTH_LONG).show()
            return
        }
        DownloadHelper.startDownloadService(context, DownloadData(
            videoId = id, videoFormat = video?.format, videoQuality = video?.quality,
            audioFormat = audio?.format, audioQuality = audio?.quality,
            audioLanguage = audio?.audioTrackLocale, subtitleCode = null
        ))
        Toast.makeText(context, "Downloading; shared files appear in Download/PrivateStream", Toast.LENGTH_LONG).show()
    }

    internal fun selectAudioStream(streams: Streams) = streams.audioStreams
        .filter { !it.format.isNullOrBlank() && !it.quality.isNullOrBlank() }
        .maxByOrNull { it.bitrate ?: 0 }

    internal fun selectVideoStream(streams: Streams) = streams.videoStreams
        .filter {
            !it.format.isNullOrBlank() && !it.quality.isNullOrBlank() &&
                !it.format.orEmpty().contains("HLS", ignoreCase = true)
        }
        .let { videos ->
            videos.filter { (it.height ?: 0) <= 1080 }.maxByOrNull { it.height ?: 0 }
                ?: videos.minByOrNull { it.height ?: Int.MAX_VALUE }
        }
}
