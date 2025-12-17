package com.devsphere.aether.workers

import android.content.Context
import androidx.hilt.work.HiltWorker

import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.devsphere.aether.cache.WeatherStore
import com.devsphere.aether.data.repository.AetherRepository
import com.devsphere.aether.data.repository.LocationRepository
import com.devsphere.aether.network.ApiResult
import com.devsphere.aether.utils.LocationManager
import com.devsphere.aether.utils.LocationResult
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Background worker to periodically refresh weather data
 * Runs every 3-6 hours to keep cache fresh
 * Updates cache silently without notifying UI
 */
@HiltWorker
class WeatherRefreshWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val aetherRepository: AetherRepository,
    private val locationRepository: LocationRepository,
    private val locationManager: LocationManager,
    private val weatherStore: WeatherStore
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            // 1. Refresh current location weather
            refreshCurrentLocation()

            // 2. Refresh saved locations weather
            refreshSavedLocations()

            // 3. Clean up stale cache entries
            cleanupStaleCache()

            Result.success()
        } catch (e: Exception) {
            // Retry on failure (with exponential backoff)
            Result.retry()
        }
    }

    /**
     * Refresh weather for current device location
     */
    private suspend fun refreshCurrentLocation() {
        when (val locationResult = locationManager.getCurrentLocation()) {
            is LocationResult.Success -> {
                // Force refresh to update cache
                aetherRepository.getWeather(
                    latitude = locationResult.latitude,
                    longitude = locationResult.longitude,
                    forceRefresh = true
                )

                // Also refresh AQI
                aetherRepository.getAirQuality(
                    latitude = locationResult.latitude,
                    longitude = locationResult.longitude,
                    forceRefresh = true
                )
            }
            is LocationResult.Error -> {
                // Location not available, skip
            }
        }
    }

    /**
     * Refresh weather for all saved locations
     */
    private suspend fun refreshSavedLocations() {
        // Get saved locations from database
        locationRepository.getSavedLocations().collect { locations ->
            locations.forEach { location ->
                try {
                    // Fetch and cache weather for each location
                    locationRepository.fetchAndCacheWeather(
                        location = location,
                        forceRefresh = true
                    )

                    // Also update in-memory cache
                    val result = aetherRepository.getWeather(
                        latitude = location.latitude,
                        longitude = location.longitude,
                        forceRefresh = true
                    )

                    // If successful, weather is already cached by repository
                    if (result is ApiResult.Success) {
                        // Success - cache updated
                    }
                } catch (e: Exception) {
                    // Skip failed locations, continue with others
                }
            }
        }
    }

    /**
     * Clean up old cache entries to free memory
     */
    private suspend fun cleanupStaleCache() {
        weatherStore.clearStaleEntries(
            weatherTtl = 6 * 60 * 60 * 1000L, // 6 hours
            aqiTtl = 6 * 60 * 60 * 1000L       // 6 hours
        )
    }

    companion object {
        const val WORK_NAME = "weather_refresh_work"
    }
}