package com.devsphere.aether.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Utility functions for formatting weather data
 */
object WeatherUtils {

    /**
     * Format temperature with unit
     * @param temp Temperature value
     * @param unit "celsius" or "fahrenheit"
     */
    fun formatTemperature(temp: Double?, unit: String = "celsius"): String {
        if (temp == null) return "--"
        val rounded = temp.roundToInt()
        val symbol = if (unit == "celsius") "°C" else "°F"
        return "$rounded$symbol"
    }

    /**
     * Format high/low temperatures
     */
    fun formatHighLow(high: Double?, low: Double?, unit: String = "celsius"): String {
        if (high == null || low == null) return "--"
        val highRounded = high.roundToInt()
        val lowRounded = low.roundToInt()
        val symbol = if (unit == "celsius") "°" else "°"
        return "H:$highRounded$symbol  L:$lowRounded$symbol"
    }

    /**
     * Format humidity
     */
    fun formatHumidity(humidity: Double?): String {
        if (humidity == null) return "--"
        return "${humidity.roundToInt()}%"
    }

    /**
     * Format wind speed
     * @param speed Wind speed value
     * @param unit "kmh", "ms", "mph", "kn"
     */
    fun formatWindSpeed(speed: Double?, unit: String = "kmh"): String {
        if (speed == null) return "--"
        val rounded = speed.roundToInt()
        val unitText = when (unit) {
            "kmh" -> "km/h"
            "ms" -> "m/s"
            "mph" -> "mph"
            "kn" -> "kn"
            else -> "km/h"
        }
        return "$rounded $unitText"
    }

    /**
     * Format visibility
     * @param visibility Visibility in meters
     * @param unit "km" or "miles"
     */
    fun formatVisibility(visibility: Double?, unit: String = "km"): String {
        if (visibility == null) return "--"

        return if (unit == "km") {
            val km = visibility / 1000.0
            if (km >= 10) {
                "${km.roundToInt()} km"
            } else {
                String.format(Locale.US, "%.1f km", km)
            }
        } else {
            val miles = visibility / 1609.34
            if (miles >= 10) {
                "${miles.roundToInt()} mi"
            } else {
                String.format(Locale.US, "%.1f mi", miles)
            }
        }
    }

    /**
     * Format pressure
     */
    fun formatPressure(pressure: Double?): String {
        if (pressure == null) return "--"
        return "${pressure.roundToInt()} mb"
    }

    /**
     * Format time from ISO 8601 string
     * @param isoTime ISO 8601 datetime string
     * @param pattern Time format pattern (default: "HH:mm")
     */
    fun formatTime(isoTime: String?, pattern: String = "HH:mm"): String {
        if (isoTime == null) return "--"

        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.US)
            val outputFormat = SimpleDateFormat(pattern, Locale.US)
            val date = inputFormat.parse(isoTime)
            date?.let { outputFormat.format(it) } ?: "--"
        } catch (e: Exception) {
            "--"
        }
    }

    /**
     * Calculate minutes until rain from minutely forecast
     * @param times List of time strings (ISO 8601)
     * @param precipitation List of precipitation values (mm)
     * @return Minutes until rain, or null if no rain expected
     */
    fun calculateMinutesUntilRain(
        times: List<String>?,
        precipitation: List<Double>?
    ): Int? {
        if (times.isNullOrEmpty() || precipitation.isNullOrEmpty()) return null

        val currentTime = System.currentTimeMillis()
        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.US)

        for (i in precipitation.indices) {
            if (precipitation[i] > 0.1) { // Rain threshold: 0.1mm
                try {
                    val rainTime = format.parse(times[i])?.time ?: continue
                    val diffMinutes = ((rainTime - currentTime) / 60000).toInt()

                    if (diffMinutes > 0) {
                        return diffMinutes
                    }
                } catch (e: Exception) {
                    continue
                }
            }
        }

        return null
    }

    /**
     * Check if it's currently raining
     * @param currentPrecipitation Current precipitation in mm
     */
    fun isCurrentlyRaining(currentPrecipitation: Double?): Boolean {
        return (currentPrecipitation ?: 0.0) > 0.1
    }

    /**
     * Calculate rain probability message
     */
    fun getRainMessage(
        isRaining: Boolean,
        minutesUntilRain: Int?,
        precipitationProbability: Int?
    ): String {
        return when {
            isRaining -> "Raining now"
            minutesUntilRain != null && minutesUntilRain < 120 -> {
                "Rain expected in $minutesUntilRain minutes"
            }
            precipitationProbability != null && precipitationProbability > 70 -> {
                "High chance of rain ($precipitationProbability%)"
            }
            precipitationProbability != null && precipitationProbability > 30 -> {
                "Possible rain ($precipitationProbability%)"
            }
            else -> "Low chance of rain"
        }
    }

    /**
     * Format AQI category
     */
    fun formatAqiCategory(aqi: Int?): String {
        return when (aqi) {
            null -> "Unknown"
            in 0..20 -> "Good"
            in 21..40 -> "Fair"
            in 41..60 -> "Moderate"
            in 61..80 -> "Poor"
            in 81..100 -> "Very Poor"
            else -> "Extremely Poor"
        }
    }

    /**
     * Get AQI color hex
     */
    fun getAqiColorHex(aqi: Int?): String {
        return when (aqi) {
            null -> "#9E9E9E"
            in 0..20 -> "#50F0E6"
            in 21..40 -> "#50CCAA"
            in 41..60 -> "#F0E641"
            in 61..80 -> "#FF5050"
            in 81..100 -> "#960032"
            else -> "#7D2181"
        }
    }

    fun uvLevel(uv: Double?): String {
        if (uv == null) return "Unknown"
        return when {
            uv < 3 -> "Low"
            uv < 6 -> "Moderate"
            uv < 8 -> "High"
            uv < 11 -> "Very High"
            else -> "Extreme"
        }
    }

    fun uvAdvice(uv: Double?): String {
        if (uv == null) return "UV data unavailable."
        return when {
            uv < 3 -> "Low risk. Sunglasses optional."
            uv < 6 -> "Use SPF 30+. Limit long exposure."
            uv < 8 -> "SPF 50+ recommended. Seek shade midday."
            uv < 11 -> "Avoid midday sun. Cover up + SPF 50+."
            else -> "Extreme. Stay indoors if possible."
        }
    }

    /**
     * Format last update time
     */
    fun formatLastUpdate(timestamp: Long): String {
        if (timestamp == 0L) return "Never"

        val now = System.currentTimeMillis()
        val diffMinutes = ((now - timestamp) / 60000).toInt()

        return when {
            diffMinutes < 1 -> "Just now"
            diffMinutes < 60 -> "$diffMinutes min ago"
            else -> {
                val hours = diffMinutes / 60
                "$hours hour${if (hours > 1) "s" else ""} ago"
            }
        }
    }
}