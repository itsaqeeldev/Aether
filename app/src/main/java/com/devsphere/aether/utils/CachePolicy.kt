package com.devsphere.aether.utils

/**
 * Cache policy for weather data with TTL-based validation
 */
object CachePolicy {

    // TTL (Time To Live) values in milliseconds
    const val CURRENT_WEATHER_TTL = 10 * 60 * 1000L      // 10 minutes
    const val FORECAST_TTL = 2 * 60 * 60 * 1000L         // 2 hours
    const val AQI_TTL = 30 * 60 * 1000L                  // 30 minutes

    /**
     * Determines if data should be fetched from network based on TTL
     *
     * @param lastUpdated Timestamp of last update (null means never fetched)
     * @param ttlMs Time-to-live in milliseconds
     * @return true if data is stale and should be refetched, false if cache is valid
     */
    fun shouldFetch(lastUpdated: Long?, ttlMs: Long): Boolean {
        // Never fetched before - need to fetch
        if (lastUpdated == null || lastUpdated == 0L) return true

        // Calculate time since last update
        val timeSinceUpdate = System.currentTimeMillis() - lastUpdated

        // Fetch if TTL expired
        return timeSinceUpdate >= ttlMs
    }

    /**
     * Check if weather data should be refreshed
     */
    fun shouldRefreshWeather(lastUpdated: Long?): Boolean {
        return shouldFetch(lastUpdated, CURRENT_WEATHER_TTL)
    }

    /**
     * Check if forecast data should be refreshed
     */
    fun shouldRefreshForecast(lastUpdated: Long?): Boolean {
        return shouldFetch(lastUpdated, FORECAST_TTL)
    }

    /**
     * Check if AQI data should be refreshed
     */
    fun shouldRefreshAqi(lastUpdated: Long?): Boolean {
        return shouldFetch(lastUpdated, AQI_TTL)
    }

    /**
     * Get remaining cache validity time in seconds
     */
    fun getRemainingCacheTime(lastUpdated: Long?, ttlMs: Long): Long {
        if (lastUpdated == null || lastUpdated == 0L) return 0L

        val timeSinceUpdate = System.currentTimeMillis() - lastUpdated
        val remaining = ttlMs - timeSinceUpdate

        return if (remaining > 0) remaining / 1000 else 0L
    }
}