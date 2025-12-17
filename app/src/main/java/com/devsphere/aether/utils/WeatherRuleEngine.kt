package com.devsphere.aether.utils

import com.devsphere.aether.models.*

/**
 * Rule-based engine to evaluate weather conditions and provide suggestions
 */
object WeatherRuleEngine {

    /**
     * Evaluate temperature and return range
     */
    fun getTempRange(temp: Double): TempRange {
        return when {
            temp < 0 -> TempRange.VERY_COLD
            temp < 10 -> TempRange.COLD
            temp < 18 -> TempRange.COOL
            temp < 25 -> TempRange.MILD
            temp < 32 -> TempRange.WARM
            else -> TempRange.HOT
        }
    }

    /**
     * Evaluate weather condition and return type
     */
    fun getWeatherConditionType(condition: String): WeatherConditionType {
        return when (condition.lowercase()) {
            "clear" -> WeatherConditionType.CLEAR
            "clouds" -> WeatherConditionType.CLOUDS
            "rain" -> WeatherConditionType.RAIN
            "drizzle" -> WeatherConditionType.DRIZZLE
            "thunderstorm" -> WeatherConditionType.THUNDERSTORM
            "snow" -> WeatherConditionType.SNOW
            "mist" -> WeatherConditionType.MIST
            "smoke" -> WeatherConditionType.SMOKE
            "haze" -> WeatherConditionType.HAZE
            "dust" -> WeatherConditionType.DUST
            "fog" -> WeatherConditionType.FOG
            "sand" -> WeatherConditionType.SAND
            "ash" -> WeatherConditionType.ASH
            "squall" -> WeatherConditionType.SQUALL
            "tornado" -> WeatherConditionType.TORNADO
            else -> WeatherConditionType.CLEAR
        }
    }

    /**
     * Evaluate UV index and return level
     */
    fun getUVLevel(uvIndex: Double): UVLevel {
        return when {
            uvIndex <= 2 -> UVLevel.LOW
            uvIndex <= 5 -> UVLevel.MODERATE
            uvIndex <= 7 -> UVLevel.HIGH
            uvIndex <= 10 -> UVLevel.VERY_HIGH
            else -> UVLevel.EXTREME
        }
    }

    /**
     * Evaluate AQI and return level
     */
    fun getAQILevel(aqi: Int): AQILevel {
        return when {
            aqi <= 50 -> AQILevel.GOOD
            aqi <= 100 -> AQILevel.MODERATE
            aqi <= 150 -> AQILevel.UNHEALTHY_SG
            aqi <= 200 -> AQILevel.UNHEALTHY
            aqi <= 300 -> AQILevel.VERY_UNHEALTHY
            else -> AQILevel.HAZARDOUS
        }
    }

    /**
     * Evaluate wind speed and return level
     */
    fun getWindLevel(windSpeed: Double): WindLevel {
        return when {
            windSpeed < 5 -> WindLevel.CALM
            windSpeed < 20 -> WindLevel.LIGHT
            windSpeed < 40 -> WindLevel.MODERATE
            windSpeed < 60 -> WindLevel.STRONG
            else -> WindLevel.VERY_STRONG
        }
    }

    /**
     * Evaluate precipitation probability and return level
     */
    fun getPrecipitationLevel(pop: Int): PrecipitationLevel {
        return when {
            pop == 0 -> PrecipitationLevel.NONE
            pop <= 30 -> PrecipitationLevel.LOW
            pop <= 60 -> PrecipitationLevel.MODERATE
            pop <= 80 -> PrecipitationLevel.HIGH
            else -> PrecipitationLevel.VERY_HIGH
        }
    }

    /**
     * Evaluate humidity level
     */
    fun isHighHumidity(humidity: Double): Boolean = humidity > 70

    fun isLowHumidity(humidity: Double): Boolean = humidity < 30

    /**
     * Check if it's daytime based on current time and sunrise/sunset
     */
    fun isDaytime(currentTime: Long, sunrise: Long, sunset: Long): Boolean {
        return currentTime in sunrise..sunset
    }
}

/**
 * Weather context for rule evaluation
 */
data class WeatherContext(
    val tempCurrent: Double,
    val tempFeelsLike: Double,
    val tempMin: Double,
    val tempMax: Double,
    val weatherCondition: String,
    val weatherDescription: String,
    val windSpeed: Double,
    val humidity: Double,
    val uvIndex: Double,
    val aqi: Int,
    val pop: Int = 0, // Precipitation probability
    val currentTime: Long,
    val sunrise: Long,
    val sunset: Long,
    val isDay: Boolean = true // Direct flag from API (1 = day, 0 = night)
) {
    val tempRange: TempRange = WeatherRuleEngine.getTempRange(tempCurrent)
    val feelsLikeTempRange: TempRange = WeatherRuleEngine.getTempRange(tempFeelsLike)
    val conditionType: WeatherConditionType = WeatherRuleEngine.getWeatherConditionType(weatherCondition)
    val uvLevel: UVLevel = WeatherRuleEngine.getUVLevel(uvIndex)
    val aqiLevel: AQILevel = WeatherRuleEngine.getAQILevel(aqi)
    val windLevel: WindLevel = WeatherRuleEngine.getWindLevel(windSpeed)
    val precipLevel: PrecipitationLevel = WeatherRuleEngine.getPrecipitationLevel(pop)
    val isDaytime: Boolean = if (sunrise != 0L && sunset != 0L) WeatherRuleEngine.isDaytime(currentTime, sunrise, sunset) else isDay
    val isHighHumidity: Boolean = WeatherRuleEngine.isHighHumidity(humidity)
    val isLowHumidity: Boolean = WeatherRuleEngine.isLowHumidity(humidity)
}

/**
 * Rule condition for evaluating items, tips, and activities
 */
typealias RuleCondition = (WeatherContext) -> Boolean