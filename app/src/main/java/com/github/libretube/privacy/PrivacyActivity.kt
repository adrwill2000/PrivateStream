package com.github.libretube.privacy

import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import com.github.libretube.ui.base.BaseActivity

class PrivacyActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val padding = (24 * resources.displayMetrics.density).toInt()
        val column = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(padding, padding * 2, padding, padding)
        }
        fun label(text: String, size: Float = 16f) = TextView(this).apply {
            this.text = text; textSize = size; setPadding(0, 12, 0, 12)
            column.addView(this)
        }
        label("PrivateStream privacy", 26f)
        label("No analytics or telemetry SDK. Search and watch history, automatic update checks, remote suggestions, sync and subscription polling are off by default. Video extraction and playback connect directly; no video privacy proxy is used.")
        label("Background API and image requests are restricted. Playback and downloads remain allowed. Extraction remains allowed while either service is active. SponsorBlock contacts its service for playback features.")
        label("Session request counts", 21f)
        val report = label("")
        fun refresh() {
            report.text = NetworkLedger.snapshot().joinToString("\n\n") {
                "${it.host} · ${it.purpose}\n${it.allowed} allowed · ${it.blocked} blocked"
            }.ifEmpty { "No observed requests in this session." }
        }
        column.addView(Button(this).apply { text = "Refresh"; setOnClickListener { refresh() } })
        column.addView(Button(this).apply { text = "Clear session counts"; setOnClickListener { NetworkLedger.clear(); refresh() } })
        label("Only host names, request purpose and counts are held in memory (up to 40 entries). No URLs, search terms, video IDs, headers or bodies are collected. No dashboard data leaves this device. Counts cover instrumented OkHttp clients; Media3's separate HTTP transport is not counted. Allowed counts represent attempts, not successful transfers.")
        label("Images use memory caching. Counts and temporary error text clear on normal exit and task removal; process death discards memory. Subscriptions, playlists and downloads remain durable. Android cannot guarantee an exit callback after force-stop.")
        column.addView(Button(this).apply { text = "Back"; setOnClickListener { finish() } })
        setContentView(ScrollView(this).apply { addView(column) })
        refresh()
    }
}
