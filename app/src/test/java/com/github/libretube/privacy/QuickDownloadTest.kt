package com.github.libretube.privacy

import com.github.libretube.api.obj.PipedStream
import com.github.libretube.api.obj.Streams
import com.github.libretube.parcelable.DownloadData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class QuickDownloadTest {
    private fun streams(
        audioStreams: List<PipedStream> = emptyList(),
        videoStreams: List<PipedStream> = emptyList()
) = Streams(
        title = "Test video",
        description = "",
        uploader = "Test channel",
        uploaderUrl = null,
        thumbnailUrl = "https://example.org/thumb.jpg",
        category = "Test",
        uploaderVerified = false,
        duration = 60,
        audioStreams = audioStreams,
        videoStreams = videoStreams
    )

    @Test fun selectsUrlLessSabrStreamsAndKeepsThemMatchableForTheDownloader() {
        val audio = PipedStream(format = "webm", quality = "128 bits", bitrate = 128_000)
        val video = PipedStream(format = "webm", quality = "720p", height = 720)
        val video1080 = PipedStream(format = "mp4", quality = "1080p", height = 1080)
        val hls = PipedStream(format = "HLS", quality = "720p", height = 720)
        val source = streams(
            audioStreams = listOf(audio),
            videoStreams = listOf(video, video1080, hls)
        )

        val selectedAudio = assertNotNullAndReturn(QuickDownload.selectAudioStream(source))
        val selectedVideo = assertNotNullAndReturn(QuickDownload.selectVideoStream(source))
        assertEquals("128 bits", selectedAudio.quality)
        assertEquals("1080p", selectedVideo.quality)

        val items = source.toDownloadItems(
            DownloadData(
                videoId = "test-id",
                videoFormat = selectedVideo.format,
                videoQuality = selectedVideo.quality,
                audioFormat = selectedAudio.format,
                audioQuality = selectedAudio.quality,
                audioLanguage = selectedAudio.audioTrackLocale,
                subtitleCode = null
            )
        )
        assertEquals(2, items.size)
    }

    @Test fun fallsBackToLowestAvailableVideoWhenEveryFormatExceeds1080p() {
        val source = streams(videoStreams = listOf(
            PipedStream(format = "webm", quality = "2160p", height = 2160),
            PipedStream(format = "mp4", quality = "1440p", height = 1440)
        ))
        assertEquals("1440p", QuickDownload.selectVideoStream(source)?.quality)
    }

    private fun <T : Any> assertNotNullAndReturn(value: T?): T {
        assertNotNull(value)
        return value!!
    }
}
