package com.devsphere.aether.data.repository

import com.devsphere.aether.data.local.dao.WeatherCacheDao
import com.devsphere.aether.data.local.entity.WhatToWearCacheEntity
import com.devsphere.aether.data.remote.dto.air.AirQualityResponse
import com.devsphere.aether.data.remote.dto.weather.WeatherResponse
import com.devsphere.aether.models.*
import com.devsphere.aether.network.ApiResult
import com.devsphere.aether.utils.CachePolicy
import com.devsphere.aether.utils.WeatherContext
import com.devsphere.aether.utils.WhatToWearUiMapper
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for What To Wear suggestions
 * Handles caching, data fetching, and business logic
 *
 * ✅ Follows clean architecture: ViewModel → Repository → DAO/API
 */
@Singleton
class WhatToWearRepository @Inject constructor(
    private val aetherRepository: AetherRepository,
    private val weatherCacheDao: WeatherCacheDao,
    private val gson: Gson
) {

    /**
     * Get What To Wear suggestions for a location
     *
     * Flow:
     * 1. Check cache first (instant if available and fresh)
     * 2. If cache stale or missing, fetch weather from AetherRepository
     * 3. Generate new suggestions
     * 4. Save to cache
     * 5. Return suggestions
     */
    suspend fun getSuggestions(
        latitude: Double,
        longitude: Double,
        forceRefresh: Boolean = false
    ): WhatToWearSuggestions {

        // 1. Try loading from cache if not forcing refresh
        if (!forceRefresh) {
            val cached = loadFromCache(latitude, longitude)
            if (cached != null && !CachePolicy.shouldRefreshSuggestions(cached.timestamp)) {
                // Cache is fresh, return immediately
                return cached
            }
        }

        // 2. Fetch weather data (uses AetherRepository's cache)
        val weatherResult = aetherRepository.getWeather(
            latitude = latitude,
            longitude = longitude,
            forecastDays = 1,
            forceRefresh = false  // Let repository decide caching
        )

        val airQualityResult = aetherRepository.getAirQuality(
            latitude = latitude,
            longitude = longitude,
            forceRefresh = false
        )

        // 3. Generate suggestions from weather data
        val suggestions = when (weatherResult) {
            is ApiResult.Success -> {
                generateSuggestions(
                    weather = weatherResult.data,
                    airQuality = (airQualityResult as? ApiResult.Success)?.data
                )
            }
            is ApiResult.Error -> {
                // Return error suggestions or fallback to cached if available
                loadFromCache(latitude, longitude) ?: createErrorSuggestions(weatherResult.message)
            }
        }

        // 4. Save to cache for next time
        saveToCache(latitude, longitude, suggestions)

        return suggestions
    }

    /**
     * Load suggestions from persistent cache
     */
    private suspend fun loadFromCache(
        latitude: Double,
        longitude: Double
    ): WhatToWearSuggestions? = withContext(Dispatchers.IO) {
        try {
            val key = getCacheKey(latitude, longitude)
            val cached = weatherCacheDao.getWhatToWearCache(key) ?: return@withContext null

            // Deserialize JSON to data classes
            val formalItems = gson.fromJson<List<WearableItemUi>>(
                cached.formalItemsJson,
                object : TypeToken<List<WearableItemUi>>() {}.type
            )
            val casualItems = gson.fromJson<List<WearableItemUi>>(
                cached.casualItemsJson,
                object : TypeToken<List<WearableItemUi>>() {}.type
            )
            val sportsItems = gson.fromJson<List<WearableItemUi>>(
                cached.sportsItemsJson,
                object : TypeToken<List<WearableItemUi>>() {}.type
            )
            val tips = gson.fromJson<List<WeatherTipUi>>(
                cached.tipsJson,
                object : TypeToken<List<WeatherTipUi>>() {}.type
            )
            val activities = gson.fromJson<List<ActivityUi>>(
                cached.activitiesJson,
                object : TypeToken<List<ActivityUi>>() {}.type
            )

            WhatToWearSuggestions(
                currentTemp = cached.currentTemp,
                currentCondition = cached.currentCondition,
                weatherIconCode = cached.weatherIconCode,
                formalItems = formalItems,
                casualItems = casualItems,
                sportsItems = sportsItems,
                tips = tips,
                activities = activities,
                smartInsightTitle = cached.insightTitle,
                smartInsightMessage = cached.insightMessage,
                timestamp = cached.timestamp
            )
        } catch (e: Exception) {
            // Cache corrupted or deserialization failed
            null
        }
    }

    /**
     * Save suggestions to persistent cache
     */
    private suspend fun saveToCache(
        latitude: Double,
        longitude: Double,
        suggestions: WhatToWearSuggestions
    ) = withContext(Dispatchers.IO) {
        try {
            val key = getCacheKey(latitude, longitude)
            val entity = WhatToWearCacheEntity(
                locationKey = key,
                latitude = latitude,
                longitude = longitude,
                formalItemsJson = gson.toJson(suggestions.formalItems),
                casualItemsJson = gson.toJson(suggestions.casualItems),
                sportsItemsJson = gson.toJson(suggestions.sportsItems),
                tipsJson = gson.toJson(suggestions.tips),
                activitiesJson = gson.toJson(suggestions.activities),
                insightTitle = suggestions.smartInsightTitle,
                insightMessage = suggestions.smartInsightMessage,
                currentTemp = suggestions.currentTemp,
                currentCondition = suggestions.currentCondition,
                weatherIconCode = suggestions.weatherIconCode,
                timestamp = suggestions.timestamp
            )
            weatherCacheDao.insertWhatToWearCache(entity)
        } catch (e: Exception) {
            // Silently fail - not critical
        }
    }

    /**
     * Generate suggestions from weather data using rules
     * Note: WhatToWearRuleRepository is an object, not a class
     */
    private fun generateSuggestions(
        weather: WeatherResponse,
        airQuality: AirQualityResponse?
    ): WhatToWearSuggestions {
        val current = weather.current
        val daily = weather.daily

        // Create weather context for rule evaluation
        val context = WeatherContext(
            tempCurrent = current?.temperature ?: 20.0,
            tempFeelsLike = current?.apparentTemperature ?: current?.temperature ?: 20.0,
            tempMin = daily?.tempMin?.firstOrNull() ?: 15.0,
            tempMax = daily?.tempMax?.firstOrNull() ?: 25.0,
            weatherCondition = getWeatherConditionFromCode(current?.weatherCode),
            weatherDescription = current?.weatherCode?.toString() ?: "",
            windSpeed = current?.windSpeed ?: 0.0,
            humidity = current?.humidity ?: 50.0,
            uvIndex = daily?.uvIndexMax?.firstOrNull()?.toDouble() ?: 0.0,
            aqi = airQuality?.current?.europeanAqi ?: 50,
            pop = daily?.precipitationProbMax?.firstOrNull() ?: 0,
            currentTime = System.currentTimeMillis() / 1000,
            sunrise = 0L,
            sunset = 0L,
            isDay = current?.isDay == 1
        )

        // Generate items for each category using rule engine
        // Note: WhatToWearRuleRepository is accessed as object, not injected
        val formalItems = WhatToWearRuleRepository.getWearableItems(
            context,
            WearCategory.FORMAL,
            min = 2,
            max = 5
        )

        val casualItems = WhatToWearRuleRepository.getWearableItems(
            context,
            WearCategory.CASUAL,
            min = 2,
            max = 5
        )

        val sportsItems = WhatToWearRuleRepository.getWearableItems(
            context,
            WearCategory.SPORTS,
            min = 2,
            max = 5
        )

        val tips = WhatToWearRuleRepository.getTips(
            context,
            min = 2,
            max = 4
        )

        val activities = WhatToWearRuleRepository.getActivities(
            context,
            min = 2,
            max = 4
        )

        val insights = WhatToWearRuleRepository.getSmartInsights(
            context,
            maxInsights = 1
        )

        // Map to UI models using correct mapper method names
        val tempCurrent = current?.temperature ?: 20.0
        val tempStr = "${tempCurrent.toInt()}°"
        val conditionText = getWeatherConditionFromCode(current?.weatherCode)

        return WhatToWearSuggestions(
            currentTemp = tempStr,
            currentCondition = conditionText,
            weatherIconCode = getWeatherIconCode(current?.weatherCode, current?.isDay),
            // ✅ FIXED: Using correct mapper method names with proper parameters
            formalItems = WhatToWearUiMapper.mapItemsToUi(formalItems, tempCurrent),
            casualItems = WhatToWearUiMapper.mapItemsToUi(casualItems, tempCurrent),
            sportsItems = WhatToWearUiMapper.mapItemsToUi(sportsItems, tempCurrent),
            tips = WhatToWearUiMapper.mapTipsToUi(tips),
            activities = WhatToWearUiMapper.mapActivitiesToUi(activities),
            smartInsightTitle = insights.firstOrNull()?.title,
            smartInsightMessage = insights.firstOrNull()?.message,
            timestamp = System.currentTimeMillis()
        )
    }

    /**
     * Create error suggestions when weather fetch fails
     */
    private fun createErrorSuggestions(errorMessage: String): WhatToWearSuggestions {
        return WhatToWearSuggestions(
            currentTemp = "--",
            currentCondition = "Unable to load",
            weatherIconCode = 0,
            formalItems = emptyList(),
            casualItems = emptyList(),
            sportsItems = emptyList(),
            tips = emptyList(),
            activities = emptyList(),
            smartInsightTitle = null,
            smartInsightMessage = null,
            timestamp = System.currentTimeMillis()
        )
    }

    /**
     * Clear suggestions cache for a location
     */
    suspend fun clearCache(latitude: Double, longitude: Double) = withContext(Dispatchers.IO) {
        try {
            val key = getCacheKey(latitude, longitude)
            weatherCacheDao.deleteWhatToWearCache(key)
        } catch (e: Exception) {
            // Silently fail
        }
    }

    /**
     * Clear all suggestions cache
     */
    suspend fun clearAllCache() = withContext(Dispatchers.IO) {
        try {
            weatherCacheDao.clearAllWhatToWearCache()
        } catch (e: Exception) {
            // Silently fail
        }
    }

    // ==================== Helper Functions ====================

    private fun getCacheKey(latitude: Double, longitude: Double): String {
        val lat = String.format("%.4f", latitude)
        val lon = String.format("%.4f", longitude)
        return "$lat,$lon"
    }

    private fun getWeatherConditionFromCode(code: Int?): String {
        return when (code) {
            0 -> "Clear"
            1, 2, 3 -> "Clouds"
            45, 48 -> "Fog"
            51, 53, 55, 56, 57 -> "Drizzle"
            61, 63, 65, 66, 67 -> "Rain"
            71, 73, 75, 77, 85, 86 -> "Snow"
            80, 81, 82 -> "Rain"
            95, 96, 99 -> "Thunderstorm"
            else -> "Clear"
        }
    }

    private fun getWeatherIconCode(weatherCode: Int?, isDay: Int?): Int {
        return when (weatherCode) {
            0 -> if (isDay == 1) 1 else 2
            1, 2, 3 -> 3
            61, 63, 65, 80, 81, 82 -> 4
            95, 96, 99 -> 5
            else -> 1
        }
    }
}

/**
 * Data class representing What To Wear suggestions
 * Clean separation between data and UI state
 */
data class WhatToWearSuggestions(
    val currentTemp: String,
    val currentCondition: String,
    val weatherIconCode: Int,

    val formalItems: List<WearableItemUi>,
    val casualItems: List<WearableItemUi>,
    val sportsItems: List<WearableItemUi>,

    val tips: List<WeatherTipUi>,
    val activities: List<ActivityUi>,

    val smartInsightTitle: String?,
    val smartInsightMessage: String?,

    val timestamp: Long
)