package com.devsphere.aether

import android.app.Application
import com.devsphere.aether.workers.WeatherRefreshScheduler
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class AetherApp : Application() {

    override fun onCreate() {
        super.onCreate()

        // Schedule periodic background weather refresh
        // Runs every 4 hours to keep cache fresh
        WeatherRefreshScheduler.schedule(this)
    }
}