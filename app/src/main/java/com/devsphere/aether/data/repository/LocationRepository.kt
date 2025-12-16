package com.devsphere.aether.data.repository

import com.devsphere.aether.data.local.dao.SavedLocationDao
import com.devsphere.aether.data.local.entity.SavedLocationEntity
import com.devsphere.aether.data.remote.api.GeocodingApi
import com.devsphere.aether.data.remote.api.WeatherApi
import com.devsphere.aether.data.remote.dto.geocoding.GeocodingResult
import com.devsphere.aether.models.PopularCity
import com.devsphere.aether.network.ApiHandler
import com.devsphere.aether.network.ApiResult
import com.devsphere.aether.utils.WeatherImageMapper
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationRepository @Inject constructor(
    private val savedLocationDao: SavedLocationDao,
    private val geocodingApi: GeocodingApi,
    private val weatherApi: WeatherApi
) {
    companion object {
        const val MAX_SAVED_LOCATIONS = 3
    }

    // ==================== Local Database Operations ====================

    /**
     * Get all saved locations as a Flow
     */
    fun getSavedLocations(): Flow<List<SavedLocationEntity>> {
        return savedLocationDao.getAllLocations()
    }

    /**
     * Get count of saved locations
     */
    suspend fun getSavedLocationCount(): Int {
        return savedLocationDao.getLocationCount()
    }

    /**
     * Check if location is already saved
     */
    suspend fun isLocationSaved(locationId: Int): Boolean {
        return savedLocationDao.locationExists(locationId)
    }

    /**
     * Save a new location
     * Returns false if max limit reached
     */
    suspend fun saveLocation(location: SavedLocationEntity): Boolean {
        val currentCount = savedLocationDao.getLocationCount()
        if (currentCount >= MAX_SAVED_LOCATIONS) {
            return false
        }
        savedLocationDao.insertLocation(location)
        return true
    }

    /**
     * Save location from PopularCity
     */
    suspend fun saveLocationFromPopularCity(city: PopularCity): Boolean {
        val entity = SavedLocationEntity(
            id = city.id,
            name = city.name,
            country = city.country,
            countryCode = city.countryCode,
            latitude = city.latitude,
            longitude = city.longitude,
            timezone = city.timezone,
            admin1 = null,
            cachedTemp = city.temperature,
            cachedCondition = null,
            cachedWeatherCode = null,
            cachedImageUrl = null
        )
        return saveLocation(entity)
    }

    /**
     * Save location from GeocodingResult (search result)
     */
    suspend fun saveLocationFromSearchResult(result: GeocodingResult): Boolean {
        if (result.id == null || result.name == null ||
            result.latitude == null || result.longitude == null) {
            return false
        }

        val entity = SavedLocationEntity(
            id = result.id,
            name = result.name,
            country = result.country ?: "Unknown",
            countryCode = result.countryCode,
            latitude = result.latitude,
            longitude = result.longitude,
            timezone = result.timezone,
            admin1 = result.admin1
        )
        return saveLocation(entity)
    }

    /**
     * Remove a saved location
     */
    suspend fun removeLocation(locationId: Int) {
        savedLocationDao.deleteLocationById(locationId)
    }

    /**
     * Update cached weather for a location
     */
    suspend fun updateLocationWeather(
        locationId: Int,
        temp: Int?,
        condition: String?,
        weatherCode: Int?,
        imageUrl: String?
    ) {
        savedLocationDao.updateCachedWeather(
            locationId = locationId,
            temp = temp,
            condition = condition,
            weatherCode = weatherCode,
            imageUrl = imageUrl,
            updateTime = System.currentTimeMillis()
        )
    }

    // ==================== Remote API Operations ====================

    /**
     * Search for locations by name
     */
    suspend fun searchLocations(query: String): ApiResult<List<GeocodingResult>> {
        return ApiHandler.execute {
            val response = geocodingApi.searchLocations(name = query, count = 10)
            response.results ?: emptyList()
        }
    }

    /**
     * Fetch current temperature for a location
     * Used for popular cities list
     */
    suspend fun fetchCurrentTemperature(latitude: Double, longitude: Double): Int? {
        return try {
            val response = weatherApi.getWeather(
                latitude = latitude,
                longitude = longitude,
                forecastDays = 1
            )
            response.current?.temperature?.toInt()
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Fetch weather data for a location and update cache
     */
    suspend fun fetchAndCacheWeather(location: SavedLocationEntity) {
        try {
            val response = weatherApi.getWeather(
                latitude = location.latitude,
                longitude = location.longitude,
                forecastDays = 1
            )

            val current = response.current
            val temp = current?.temperature?.toInt()
            val condition = WeatherImageMapper.getConditionText(current?.weatherCode)
            val imageUrl = WeatherImageMapper.getImageUrl(current?.weatherCode, current?.isDay)

            updateLocationWeather(
                locationId = location.id,
                temp = temp,
                condition = condition,
                weatherCode = current?.weatherCode,
                imageUrl = imageUrl
            )
        } catch (e: Exception) {
            // Silently fail - will use cached data
        }
    }
}