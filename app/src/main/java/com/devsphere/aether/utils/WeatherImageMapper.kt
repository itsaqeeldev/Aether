package com.devsphere.aether.utils

/**
 * Maps Open-Meteo weather codes to Unsplash image URLs
 * Weather codes reference: https://www.noaa.gov/weather
 */
object WeatherImageMapper {

    // Default fallback image
    private const val DEFAULT_IMAGE = "https://images.unsplash.com/photo-1534088568595-a066f410bcda?auto=format&fit=crop&w=1080&q=80"

    /**
     * Get hero image URL based on weather code and time of day
     * @param weatherCode WMO weather code (0-99)
     * @param isDay 1 for day, 0 for night
     * @return Unsplash image URL
     */
    fun getImageUrl(weatherCode: Int?, isDay: Int?): String {
        val isDaytime = isDay == 1

        return when (weatherCode) {
            // Clear sky
            0 -> if (isDaytime) {
                "https://images.unsplash.com/photo-1601297183305-6df142704ea2?auto=format&fit=crop&w=1080&q=80"
            } else {
                "https://images.unsplash.com/photo-1519681393784-d120267933ba?auto=format&fit=crop&w=1080&q=80"
            }

            // Partly cloudy
            1, 2, 3 -> if (isDaytime) {
                "https://images.unsplash.com/photo-1534088568595-a066f410bcda?auto=format&fit=crop&w=1080&q=80"
            } else {
                "https://images.unsplash.com/photo-1532693322450-2cb5c511067d?auto=format&fit=crop&w=1080&q=80"
            }

            // Fog
            45, 48 -> "https://images.unsplash.com/photo-1487621167305-5d248087c724?auto=format&fit=crop&w=1080&q=80"

            // Drizzle
            51, 53, 55 -> "https://images.unsplash.com/photo-1515694346937-94d85e41e6f0?auto=format&fit=crop&w=1080&q=80"

            // Freezing drizzle
            56, 57 -> "https://images.unsplash.com/photo-1551582045-6ec9c11d8697?auto=format&fit=crop&w=1080&q=80"

            // Rain (slight, moderate, heavy)
            61, 63, 65 -> "https://images.unsplash.com/photo-1515694346937-94d85e41e6f0?auto=format&fit=crop&w=1080&q=80"

            // Freezing rain
            66, 67 -> "https://images.unsplash.com/photo-1551582045-6ec9c11d8697?auto=format&fit=crop&w=1080&q=80"

            // Snow (slight, moderate, heavy)
            71, 73, 75, 77 -> "https://images.unsplash.com/photo-1491002052546-bf38f186af56?auto=format&fit=crop&w=1080&q=80"

            // Rain showers
            80, 81, 82 -> "https://images.unsplash.com/photo-1534274988757-a28bf1a57c17?auto=format&fit=crop&w=1080&q=80"

            // Snow showers
            85, 86 -> "https://images.unsplash.com/photo-1491002052546-bf38f186af56?auto=format&fit=crop&w=1080&q=80"

            // Thunderstorm
            95, 96, 99 -> "https://images.unsplash.com/photo-1605727216801-e27ce1d0cc28?auto=format&fit=crop&w=1080&q=80"

            // Default
            else -> DEFAULT_IMAGE
        }
    }

    /**
     * Get weather condition text from code
     */
    fun getConditionText(weatherCode: Int?): String {
        return when (weatherCode) {
            0 -> "Clear sky"
            1 -> "Mainly clear"
            2 -> "Partly cloudy"
            3 -> "Overcast"
            45, 48 -> "Foggy"
            51 -> "Light drizzle"
            53 -> "Moderate drizzle"
            55 -> "Dense drizzle"
            56, 57 -> "Freezing drizzle"
            61 -> "Slight rain"
            63 -> "Moderate rain"
            65 -> "Heavy rain"
            66, 67 -> "Freezing rain"
            71 -> "Slight snow"
            73 -> "Moderate snow"
            75 -> "Heavy snow"
            77 -> "Snow grains"
            80 -> "Slight rain showers"
            81 -> "Moderate rain showers"
            82 -> "Violent rain showers"
            85, 86 -> "Snow showers"
            95 -> "Thunderstorm"
            96, 99 -> "Thunderstorm with hail"
            else -> "Unknown"
        }
    }

    /**
     * Get weather icon resource ID from code
     */
    fun getIconResourceId(weatherCode: Int?, isDay: Int?): Int {
        // Import R at the top when using this
        // For now, return a placeholder - you'll need to import R.drawable
        // This is just the logic structure
        return when (weatherCode) {
            0 -> if (isDay == 1) {
                // R.drawable.ic_sun
                0 // placeholder
            } else {
                // R.drawable.ic_moon
                0
            }
            1, 2, 3 -> {
                // R.drawable.ic_cloud
                0
            }
            61, 63, 65, 80, 81, 82 -> {
                // R.drawable.ic_rain
                0
            }
            71, 73, 75, 77, 85, 86 -> {
                // R.drawable.ic_snow
                0
            }
            95, 96, 99 -> {
                // R.drawable.ic_thunder
                0
            }
            else -> {
                // R.drawable.ic_sun
                0
            }
        }
    }
}