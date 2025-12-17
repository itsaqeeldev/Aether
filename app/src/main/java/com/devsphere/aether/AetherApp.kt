package com.devsphere.aether

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.devsphere.aether.cache.WeatherStore
import com.devsphere.aether.utils.LocationManager
import com.devsphere.aether.utils.LocationResult
import com.devsphere.aether.workers.WeatherRefreshScheduler
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class AetherApp : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory
    @Inject lateinit var weatherStore: WeatherStore
    @Inject lateinit var locationManager: LocationManager

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()

        // Schedule periodic weather refresh
        WeatherRefreshScheduler.schedule(this)

        // Preload cache from database for instant display
        preloadCache()
    }

    /**
     * Preload weather cache from database on app start
     * This makes cached data available immediately when app opens
     */
    private fun preloadCache() {
        applicationScope.launch(Dispatchers.IO) {
            try {
                // Get last known location
                val location = locationManager.getLastKnownLocation()
                if (location is LocationResult.Success) {
                    // Preload weather cache from database into memory
                    weatherStore.loadCacheFromDatabase(
                        location.latitude,
                        location.longitude
                    )
                }
            } catch (e: Exception) {
                // Silently fail - not critical
                // Fresh data will be fetched when user opens the app
            }
        }
    }
}
