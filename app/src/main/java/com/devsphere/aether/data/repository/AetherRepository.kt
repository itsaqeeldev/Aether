package com.devsphere.aether.data.repository

import com.devsphere.aether.cache.WeatherStore
import com.devsphere.aether.data.remote.api.AirQualityApi
import com.devsphere.aether.data.remote.api.GeocodingApi
import com.devsphere.aether.data.remote.api.WeatherApi
import com.devsphere.aether.data.remote.dto.air.AirQualityResponse
import com.devsphere.aether.data.remote.dto.geocoding.GeocodingResponse
import com.devsphere.aether.data.remote.dto.weather.WeatherResponse
import com.devsphere.aether.network.ApiHandler
import com.devsphere.aether.network.ApiResult
import com.devsphere.aether.utils.CachePolicy
import com.devsphere.aether.utils.LocationInfo
import com.devsphere.aether.utils.ReverseGeocoder
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AetherRepository @Inject constructor(
    private val weatherApi: WeatherApi,
    private val airQualityApi: AirQualityApi,
    private val geocodingApi: GeocodingApi,
    private val reverseGeocoder: ReverseGeocoder,
    private val weatherStore: WeatherStore
) {

    /**
     * Get weather with TTL-based caching
     * Repository is the single source of truth - ViewModels should not decide when to fetch
     *
     * @param forceRefresh Bypass cache and force network fetch (for pull-to-refresh)
     */
    suspend fun getWeather(
        latitude: Double,
        longitude: Double,
        timezone: String = "auto",
        temperatureUnit: String = "celsius",
        windspeedUnit: String = "kmh",
        forecastDays: Int = 7,
        forceRefresh: Boolean = false
    ): ApiResult<WeatherResponse> {
        // Check cache first (unless forced refresh)
        if (!forceRefresh) {
            val cached = weatherStore.getWeather(latitude, longitude)
            if (cached != null) {
                // Check if cache is still valid based on TTL
                val shouldRefresh = CachePolicy.shouldRefreshForecast(cached.timestamp)
                if (!shouldRefresh) {
                    // Cache is valid, return cached data
                    return ApiResult.Success(cached.data)
                }
            }
        }

        // Cache miss or stale - fetch from network
        val result = ApiHandler.execute {
            weatherApi.getWeather(
                latitude = latitude,
                longitude = longitude,
                timezone = timezone,
                temperatureUnit = temperatureUnit,
                windspeedUnit = windspeedUnit,
                forecastDays = forecastDays
            )
        }

        // Update cache on successful fetch
        if (result is ApiResult.Success) {
            weatherStore.putWeather(latitude, longitude, result.data)
        }

        // If network fails but we have stale cache, return stale data (never show empty state)
        if (result is ApiResult.Error && !forceRefresh) {
            val staleCache = weatherStore.getWeather(latitude, longitude)
            if (staleCache != null) {
                return ApiResult.Success(staleCache.data)
            }
        }

        return result
    }

    /**
     * Get minutely forecast (no caching for now as it's very time-sensitive)
     */
    suspend fun getMinutelyForecast(
        latitude: Double,
        longitude: Double,
        timezone: String = "auto"
    ): ApiResult<WeatherResponse> =
        ApiHandler.execute {
            weatherApi.getMinutelyForecast(
                latitude = latitude,
                longitude = longitude,
                timezone = timezone
            )
        }

    /**
     * Get air quality with separate TTL-based caching
     * AQI has its own refresh cycle independent of weather
     *
     * @param forceRefresh Bypass cache and force network fetch
     */
    suspend fun getAirQuality(
        latitude: Double,
        longitude: Double,
        timezone: String = "auto",
        forceRefresh: Boolean = false
    ): ApiResult<AirQualityResponse> {
        // Check cache first (unless forced refresh)
        if (!forceRefresh) {
            val cached = weatherStore.getAqi(latitude, longitude)
            if (cached != null) {
                // Check if cache is still valid based on AQI TTL (30 minutes)
                val shouldRefresh = CachePolicy.shouldRefreshAqi(cached.timestamp)
                if (!shouldRefresh) {
                    // Cache is valid, return cached data
                    return ApiResult.Success(cached.data)
                }
            }
        }

        // Cache miss or stale - fetch from network
        val result = ApiHandler.execute {
            airQualityApi.getAirQuality(
                latitude = latitude,
                longitude = longitude,
                timezone = timezone
            )
        }

        // Update cache on successful fetch
        if (result is ApiResult.Success) {
            weatherStore.putAqi(latitude, longitude, result.data)
        }

        // If network fails but we have stale cache, return stale data
        if (result is ApiResult.Error && !forceRefresh) {
            val staleCache = weatherStore.getAqi(latitude, longitude)
            if (staleCache != null) {
                return ApiResult.Success(staleCache.data)
            }
        }

        return result
    }

    /**
     * Search locations (no caching needed - user-initiated search)
     */
    suspend fun searchLocations(
        name: String,
        count: Int = 10,
        language: String = "en"
    ): ApiResult<GeocodingResponse> =
        ApiHandler.execute {
            geocodingApi.searchLocations(
                name = name,
                count = count,
                language = language
            )
        }

    /**
     * Integrated Reverse Geocoding
     */
    suspend fun reverseGeocode(
        latitude: Double,
        longitude: Double
    ): ApiResult<LocationInfo> {
        val result = reverseGeocoder.getLocationName(latitude, longitude)
        return if (result != null) {
            ApiResult.Success(result)
        } else {
            ApiResult.Error("Location not found", 404)
        }
    }
}