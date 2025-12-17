package com.devsphere.aether.cache

import com.devsphere.aether.data.local.dao.WeatherCacheDao
import com.devsphere.aether.data.local.entity.WeatherCacheEntity
import com.devsphere.aether.data.remote.dto.air.AirQualityResponse
import com.devsphere.aether.data.remote.dto.weather.WeatherResponse
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Hybrid cache for weather data
 *
 * Architecture:
 * ViewModel → Repository → WeatherStore → DAO (✅ Correct!)
 * ViewModel → WeatherStore (❌ Wrong - don't do this!)
 *
 * This is part of the DATA LAYER, called by repositories only.
 *
 * Level 1: In-memory cache (fast, volatile)
 * Level 2: Room database (persistent, survives app restart)
 *
 * Benefits:
 * - Memory cache for instant access (no I/O)
 * - Database cache survives app restarts
 * - Automatic cache population from DB to memory
 * - TTL-based expiration
 */
@Singleton
class WeatherStore @Inject constructor(
    private val weatherCacheDao: WeatherCacheDao,
    private val gson: Gson
) {

    // Level 1: In-memory cache for instant access
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
     * Load cached data from database into memory on initialization
     * This populates in-memory cache from persistent storage for instant access
     */
    suspend fun loadCacheFromDatabase(latitude: Double, longitude: Double) {
        val key = getCacheKey(latitude, longitude)

        withContext(Dispatchers.IO) {
            weatherCacheDao.getWeatherCache(key)?.let { entity ->
                try {
                    val weatherData = gson.fromJson(entity.weatherDataJson, WeatherResponse::class.java)
                    val entry = WeatherCacheEntry(
                        data = weatherData,
                        timestamp = entity.timestamp
                    )
                    // Populate memory cache for instant access
                    weatherCache[key] = entry
                } catch (e: Exception) {
                    // Invalid JSON, delete from DB
                    weatherCacheDao.deleteWeatherCache(key)
                }
            }
        }
    }

    /**
     * Get cached weather data (checks memory first, then database)
     * Called by: AetherRepository only
     */
    suspend fun getWeather(latitude: Double, longitude: Double): WeatherCacheEntry? {
        val key = getCacheKey(latitude, longitude)

        // Level 1: Check in-memory cache (instant)
        weatherCache[key]?.let { return it }

        // Level 2: Check database (persists across restarts)
        return withContext(Dispatchers.IO) {
            weatherCacheDao.getWeatherCache(key)?.let { entity ->
                try {
                    val weatherData = gson.fromJson(entity.weatherDataJson, WeatherResponse::class.java)
                    val entry = WeatherCacheEntry(
                        data = weatherData,
                        timestamp = entity.timestamp
                    )
                    // Populate memory cache for next time
                    weatherCache[key] = entry
                    entry
                } catch (e: Exception) {
                    // Invalid JSON, delete from DB
                    weatherCacheDao.deleteWeatherCache(key)
                    null
                }
            }
        }
    }

    /**
     * Store weather data in cache (both memory and database)
     * Called by: AetherRepository only
     */
    suspend fun putWeather(
        latitude: Double,
        longitude: Double,
        data: WeatherResponse,
        timestamp: Long = System.currentTimeMillis()
    ) {
        val key = getCacheKey(latitude, longitude)

        // Level 1: Store in memory (instant access)
        weatherCache[key] = WeatherCacheEntry(
            data = data,
            timestamp = timestamp
        )

        // Level 2: Store in database (persists across restarts)
        withContext(Dispatchers.IO) {
            try {
                val entity = WeatherCacheEntity(
                    locationKey = key,
                    latitude = latitude,
                    longitude = longitude,
                    weatherDataJson = gson.toJson(data),
                    timestamp = timestamp,
                    currentTemp = data.current?.temperature,
                    currentCondition = data.current?.weatherCode?.toString(),
                    weatherCode = data.current?.weatherCode
                )
                weatherCacheDao.insertWeatherCache(entity)
            } catch (e: Exception) {
                // Silently fail - at least we have memory cache
            }
        }
    }

    /**
     * Check if weather cache exists for location
     */
    suspend fun hasWeather(latitude: Double, longitude: Double): Boolean {
        val key = getCacheKey(latitude, longitude)
        return weatherCache.containsKey(key) ||
                withContext(Dispatchers.IO) {
                    weatherCacheDao.getWeatherCache(key) != null
                }
    }

    /**
     * Clear weather cache for specific location
     */
    suspend fun clearWeather(latitude: Double, longitude: Double) {
        val key = getCacheKey(latitude, longitude)
        weatherCache.remove(key)
        withContext(Dispatchers.IO) {
            weatherCacheDao.deleteWeatherCache(key)
        }
    }

    // ==================== AQI Cache (In-Memory Only) ====================
    // AQI changes frequently, so we only cache in memory for the session

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
     * Clear all cached data (both memory and database)
     */
    suspend fun clearAll() {
        weatherCache.clear()
        aqiCache.clear()
        withContext(Dispatchers.IO) {
            weatherCacheDao.clearAllWeatherCache()
            weatherCacheDao.clearAllWhatToWearCache()
        }
    }

    /**
     * Clear stale entries older than TTL
     * Should be called periodically by WorkManager
     */
    suspend fun clearStaleEntries(weatherTtl: Long, aqiTtl: Long) {
        val now = System.currentTimeMillis()

        // Clear stale weather from memory
        weatherCache.entries.removeAll { (_, entry) ->
            now - entry.timestamp > weatherTtl
        }

        // Clear stale AQI from memory
        aqiCache.entries.removeAll { (_, entry) ->
            now - entry.timestamp > aqiTtl
        }

        // Clear stale from database
        withContext(Dispatchers.IO) {
            val weatherCutoff = now - weatherTtl
            weatherCacheDao.deleteStaleWeatherCache(weatherCutoff)
            weatherCacheDao.deleteStaleWhatToWearCache(weatherCutoff)
        }
    }

    /**
     * Get cache statistics (for debugging)
     */
    suspend fun getCacheStats(): CacheStats {
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