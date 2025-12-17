package com.devsphere.aether.utils

import com.devsphere.aether.data.remote.dto.weather.WeatherResponse
import com.devsphere.aether.data.repository.WhatToWearRuleRepository
import com.devsphere.aether.models.WeatherMood


/**
 * Extension functions for HomeViewModel to generate weather mood
 */
object MoodGenerator {

    /**
     * Generate weather mood from current weather data
     */
    fun generateMood(
        weather: WeatherResponse?,
        aqi: Int?
    ): WeatherMood? {
        if (weather == null) return null

        val current = weather.current ?: return null
        val daily = weather.daily

        // Create weather context for rule evaluation
        val context = WeatherContext(
            tempCurrent = current.temperature ?: 0.0,
            tempFeelsLike = current.apparentTemperature ?: current.temperature ?: 0.0,
            tempMin = daily?.tempMin?.firstOrNull() ?: current.temperature ?: 0.0,
            tempMax = daily?.tempMax?.firstOrNull() ?: current.temperature ?: 0.0,
            weatherCondition = getWeatherConditionFromCode(current.weatherCode),
            weatherDescription = WeatherImageMapper.getConditionText(current.weatherCode),
            windSpeed = current.windSpeed ?: 0.0, // Already in km/h from API
            humidity = current.humidity ?: 50.0,
            uvIndex = daily?.uvIndexMax?.firstOrNull()?.toDouble() ?: 0.0,
            aqi = aqi ?: 50,
            pop = daily?.precipitationProbMax?.firstOrNull() ?: 0, // Use probability from daily forecast
            currentTime = System.currentTimeMillis() / 1000,
            sunrise = 0L, // Not used - rely on isDay instead
            sunset = 0L, // Not used - rely on isDay instead
            isDay = current.isDay == 1 // Use isDay flag from API
        )

        return WhatToWearRuleRepository.getWeatherMood(context)
    }

    /**
     * Map WMO weather code to condition string
     */
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
}