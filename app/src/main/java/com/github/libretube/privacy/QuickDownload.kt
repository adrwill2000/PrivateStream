package com.github.libretube.privacy

import android.content.Context
import android.widget.Toast
import com.github.libretube.api.obj.Streams
import com.github.libretube.helpers.DownloadHelper
import com.github.libretube.parcelable.DownloadData

object QuickDownload {
    fun start(context: Context, id: String, streams: Streams, audioOnly: Boolean) {
        val audio = streams.audioStreams.filter { !it.url.isNullOrEmpty() }
            .maxByOrNull { it.bitrate ?: 0 }
        val videos = streams.videoStreams.filter { !it.url.isNullOrEmpty() && !it.format.orEmpty().contains("HLS") }
        val video = if (audioOnly) null else videos.filter { (it.height ?: 0) <= 1080 }
            .maxByOrNull { it.height ?: 0 } ?: videos.minByOrNull { it.height ?: Int.MAX_VALUE }
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
}
