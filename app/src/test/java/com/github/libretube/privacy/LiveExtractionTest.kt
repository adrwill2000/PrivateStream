package com.github.libretube.privacy

import com.github.libretube.util.NewPipeDownloaderImpl
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.schabi.newpipe.extractor.NewPipe
import org.schabi.newpipe.extractor.ServiceList

/** Explicit opt-in: real YouTube requests, no recorded fixtures or mocked extraction. */
class LiveExtractionTest {
    @Test fun realSearchAndStreamExtraction() {
        assumeTrue(System.getProperty("privatestream.live") == "true")
        NetworkLedger.foreground = true
        try {
            NewPipe.init(NewPipeDownloaderImpl())
            val youtube = NewPipe.getService(ServiceList.YouTube.serviceId)
            val search = youtube.getSearchExtractor("Big Buck Bunny")
            search.fetchPage()
            assertTrue("Search must return real items", search.initialPage.items.isNotEmpty())
            val stream = youtube.getStreamExtractor("https://www.youtube.com/watch?v=aqz-KE-bpKQ")
            stream.fetchPage()
            assertTrue("Must extract playable audio", stream.audioStreams.isNotEmpty())
            assertTrue("Must extract playable video", stream.videoStreams.isNotEmpty() || stream.videoOnlyStreams.isNotEmpty())
        } finally { NetworkLedger.foreground = false }
    }
}
