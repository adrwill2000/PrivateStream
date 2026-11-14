package com.github.libretube.privacy

import org.junit.After
import org.junit.Assert.*
import org.junit.Test

class NetworkLedgerTest {
    @After fun reset() {
        NetworkLedger.foreground = false
        NetworkLedger.playbackActive = false
        NetworkLedger.downloadActive = false
        NetworkLedger.clear()
    }
    @Test fun backgroundBrowsingIsBlockedWhileUserTransfersRemainAllowed() {
        reset()
        assertFalse(NetworkLedger.allows("api"))
        assertFalse(NetworkLedger.allows("images"))
        assertFalse(NetworkLedger.allows("extractor"))
        assertTrue(NetworkLedger.allows("download", true))
        assertTrue(NetworkLedger.allows("playback", true))
        NetworkLedger.playbackActive = true
        assertTrue(NetworkLedger.allows("extractor"))
        assertTrue(NetworkLedger.allows("playback-support"))
        assertFalse(NetworkLedger.allows("api"))
        NetworkLedger.playbackActive = false
        NetworkLedger.downloadActive = true
        assertTrue(NetworkLedger.allows("extractor"))
        assertFalse(NetworkLedger.allows("images"))
        NetworkLedger.foreground = true
        assertTrue(NetworkLedger.allows("api"))
    }
    @Test fun ledgerIsBoundedAndSnapshotsCannotMutateLiveCounts() {
        reset()
        repeat(100) { NetworkLedger.record("host$it.example", "images", true) }
        assertEquals(40, NetworkLedger.snapshot().size)
        NetworkLedger.record("host99.example", "images", false)
        val snapshot = NetworkLedger.snapshot().last()
        assertEquals(1, snapshot.allowed)
        assertEquals(1, snapshot.blocked)
        snapshot.allowed = 500
        assertEquals(1, NetworkLedger.snapshot().last().allowed)
        NetworkLedger.clear()
        assertTrue(NetworkLedger.snapshot().isEmpty())
    }
}
