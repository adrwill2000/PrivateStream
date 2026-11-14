package com.github.libretube.privacy

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.github.libretube.constants.PreferenceKeys
import com.github.libretube.helpers.PreferenceHelper

object PrivateSession {
    fun initialize(app: Application) {
        // Clear leftovers after OS process death; durable data lives outside the image cache.
        app.cacheDir.resolve("coil").deleteRecursively()
        PreferenceHelper.remove(PreferenceKeys.ERROR_LOG)
        val settings = PreferenceHelper.settings
        val defaults = mapOf(
            PreferenceKeys.SEARCH_HISTORY_TOGGLE to false,
            PreferenceKeys.WATCH_HISTORY_TOGGLE to false,
            PreferenceKeys.AUTOMATIC_UPDATE_CHECKS to false,
            PreferenceKeys.NOTIFICATION_ENABLED to false,
            PreferenceKeys.LOCAL_RYD to false,
            PreferenceKeys.SEARCH_SUGGESTIONS to false,
            PreferenceKeys.DEARROW to false,
            PreferenceKeys.WELCOME_ACTIVITY_FINISHED to true
        )
        settings.edit().apply {
            defaults.forEach { (key, value) -> if (!settings.contains(key)) putBoolean(key, value) }
            if (!settings.contains(PreferenceKeys.SYNC_SERVER_TYPE)) putString(PreferenceKeys.SYNC_SERVER_TYPE, "none")
        }.commit()
        app.registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {
            private var started = 0
            override fun onActivityStarted(activity: Activity) { started++; NetworkLedger.foreground = true }
            override fun onActivityStopped(activity: Activity) {
                started = (started - 1).coerceAtLeast(0)
                NetworkLedger.foreground = started > 0
                if (activity.isFinishing && started == 0) clearTemporaryData()
            }
            override fun onActivityCreated(activity: Activity, state: Bundle?) {}
            override fun onActivityResumed(activity: Activity) {}
            override fun onActivityPaused(activity: Activity) {}
            override fun onActivitySaveInstanceState(activity: Activity, state: Bundle) {}
            override fun onActivityDestroyed(activity: Activity) {}
        })
    }
    fun clearTemporaryData() {
        NetworkLedger.clear()
        com.github.libretube.helpers.ImageHelper.clearSessionCache()
        PreferenceHelper.remove(PreferenceKeys.ERROR_LOG)
        // Images use memory caching only. No subscription, playlist or download data is deleted.
    }
}
