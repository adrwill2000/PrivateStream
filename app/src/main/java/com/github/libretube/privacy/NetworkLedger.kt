package com.github.libretube.privacy

import okhttp3.Interceptor
import java.io.IOException

/** Bounded, process-local counts. Never stores URLs, queries, IDs, headers or bodies. */
object NetworkLedger {
    data class Count(val host: String, val purpose: String, var allowed: Int = 0, var blocked: Int = 0)
    private val counts = linkedMapOf<String, Count>()
    @Volatile var foreground = false
    @Volatile var playbackActive = false
    @Volatile var downloadActive = false

    fun interceptor(purpose: String, userTransfer: Boolean = false) = Interceptor { chain ->
        val method = chain.request().tag(retrofit2.Invocation::class.java)?.method()?.name
        val requestPurpose = if (purpose == "api" && method in setOf("botguardRequest", "getSegments", "getVideoLabels"))
            "playback-support" else purpose
        val allowed = allows(requestPurpose, userTransfer)
        record(chain.request().url.host, requestPurpose, allowed)
        if (!allowed) throw IOException("PrivateStream restricted a background $purpose request")
        chain.proceed(chain.request())
    }

    internal fun allows(purpose: String, userTransfer: Boolean = false) = userTransfer || foreground ||
        (purpose in setOf("extractor", "playback-support") && (playbackActive || downloadActive))

    @Synchronized internal fun record(host: String, purpose: String, allowed: Boolean) {
        val key = "$host:$purpose"
        if (key !in counts && counts.size >= 40) counts.remove(counts.keys.first())
        val count = counts.getOrPut(key) { Count(host, purpose) }
        if (allowed) count.allowed++ else count.blocked++
    }
    @Synchronized fun snapshot() = counts.values.map { it.copy() }
    @Synchronized fun clear() = counts.clear()
}
