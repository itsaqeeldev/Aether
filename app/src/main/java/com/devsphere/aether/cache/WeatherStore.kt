package com.devsphere.aether.cache

import com.devsphere.aether.data.remote.dto.air.AirQualityResponse
import com.devsphere.aether.data.remote.dto.weather.WeatherResponse
import javax.inject.Inject
import javax.inject.Singleton

/**
 * In-memory cache for weather and air quality data
 * Prevents duplicate API calls across different screens/ViewModels
 */
@Singleton
class WeatherStore @Inject constructor() {

    private val weatherCache = mutableMapOf<String, WeatherCacheEntry>()
    private val aqiCache = mutableMapOf<String, AqiCacheEntry>()

    /**
     * Generate cache key from location coordinates
     */
    private fun getCacheKey(latitude: Double, longitude: Double): String {
        // Round to 4 decimal places (~11m precision) for cache key
        val lat = String.format("%.4f", latitude)
        val lon = String.format("%.4f", longitude)
        return "$lat,$lon"
    }

    // ==================== Weather Cache ====================

    /**
     * Get cached weather data
     */
    fun getWeather(latitude: Double, longitude: Double): WeatherCacheEntry? {
        val key = getCacheKey(latitude, longitude)
        return weatherCache[key]
    }

    /**
     * Store weather data in cache
     */
    fun putWeather(
        latitude: Double,
        longitude: Double,
        data: WeatherResponse,
        timestamp: Long = System.currentTimeMillis()
    ) {
        val key = getCacheKey(latitude, longitude)
        weatherCache[key] = WeatherCacheEntry(
            data = data,
            timestamp = timestamp
        )
    }

    /**
     * Check if weather cache exists for location
     */
    fun hasWeather(latitude: Double, longitude: Double): Boolean {
        val key = getCacheKey(latitude, longitude)
        return weatherCache.containsKey(key)
    }

    /**
     * Clear weather cache for specific location
     */
    fun clearWeather(latitude: Double, longitude: Double) {
        val key = getCacheKey(latitude, longitude)
        weatherCache.remove(key)
    }

    // ==================== AQI Cache ====================

    /**
     * Get cached AQI data
     */
    fun getAqi(latitude: Double, longitude: Double): AqiCacheEntry? {
        val key = getCacheKey(latitude, longitude)
        return aqiCache[key]
    }

    /**
     * Store AQI data in cache
     */
    fun putAqi(
        latitude: Double,
        longitude: Double,
        data: AirQualityResponse,
        timestamp: Long = System.currentTimeMillis()
    ) {
        val key = getCacheKey(latitude, longitude)
        aqiCache[key] = AqiCacheEntry(
            data = data,
            timestamp = timestamp
        )
    }

    /**
     * Check if AQI cache exists for location
     */
    fun hasAqi(latitude: Double, longitude: Double): Boolean {
        val key = getCacheKey(latitude, longitude)
        return aqiCache.containsKey(key)
    }

    /**
     * Clear AQI cache for specific location
     */
    fun clearAqi(latitude: Double, longitude: Double) {
        val key = getCacheKey(latitude, longitude)
        aqiCache.remove(key)
    }

    // ==================== Cache Management ====================

    /**
     * Clear all cached data
     */
    fun clearAll() {
        weatherCache.clear()
        aqiCache.clear()
    }

    /**
     * Clear stale entries older than TTL
     */
    fun clearStaleEntries(weatherTtl: Long, aqiTtl: Long) {
        val now = System.currentTimeMillis()

        // Clear stale weather
        weatherCache.entries.removeAll { (_, entry) ->
            now - entry.timestamp > weatherTtl
        }

        // Clear stale AQI
        aqiCache.entries.removeAll { (_, entry) ->
            now - entry.timestamp > aqiTtl
        }
    }

    /**
     * Get cache statistics (for debugging)
     */
    fun getCacheStats(): CacheStats {
        return CacheStats(
            weatherCacheSize = weatherCache.size,
            aqiCacheSize = aqiCache.size
        )
    }
}

/**
 * Weather cache entry with timestamp
 */
data class WeatherCacheEntry(
    val data: WeatherResponse,
    val timestamp: Long
)

/**
 * AQI cache entry with timestamp
 */
data class AqiCacheEntry(
    val data: AirQualityResponse,
    val timestamp: Long
)

/**
 * Cache statistics
 */
data class CacheStats(
    val weatherCacheSize: Int,
    val aqiCacheSize: Int
)