package com.devsphere.aether.models

/**
 * Represents a wearable item suggestion
 */
data class WearableItem(
    val name: String,
    val category: WearCategory,
    val priority: Int = 0 // Higher priority shown first
)

enum class WearCategory {
    FORMAL,
    CASUAL,
    SPORTS
}

/**
 * Represents a weather tip
 */
data class WeatherTip(
    val text: String,
    val type: TipType,
    val priority: Int = 0
)

enum class TipType {
    HEALTH,
    SAFETY,
    COMFORT
}

/**
 * Represents an activity suggestion
 */
data class Activity(
    val name: String,
    val description: String,
    val priority: Int = 0
)

/**
 * Represents a smart insight
 */
data class SmartInsight(
    val title: String,
    val message: String,
    val priority: Int = 0, // Higher priority shown first
    val severity: InsightSeverity = InsightSeverity.INFO
)

enum class InsightSeverity {
    CRITICAL, // Red/warning
    IMPORTANT, // Orange/attention
    INFO // Blue/normal
}

/**
 * Represents weather mood
 */
data class WeatherMood(
    val emoji: String,
    val title: String,
    val description: String,
    val color: MoodColor
)

enum class MoodColor {
    SUNNY,      // Bright yellow/orange
    PLEASANT,   // Light blue/green
    CLOUDY,     // Gray
    RAINY,      // Dark blue
    COLD,       // Light blue/white
    HOT         // Red/orange
}

/**
 * Temperature range for rule evaluation
 */
enum class TempRange {
    VERY_COLD,  // < 0°C
    COLD,       // 0-10°C
    COOL,       // 10-18°C
    MILD,       // 18-25°C
    WARM,       // 25-32°C
    HOT         // > 32°C
}

/**
 * Weather condition categories
 */
enum class WeatherConditionType {
    CLEAR,
    CLOUDS,
    RAIN,
    DRIZZLE,
    THUNDERSTORM,
    SNOW,
    MIST,
    SMOKE,
    HAZE,
    DUST,
    FOG,
    SAND,
    ASH,
    SQUALL,
    TORNADO
}

/**
 * UV Index levels
 */
enum class UVLevel {
    LOW,        // 0-2
    MODERATE,   // 3-5
    HIGH,       // 6-7
    VERY_HIGH,  // 8-10
    EXTREME     // 11+
}

/**
 * AQI levels
 */
enum class AQILevel {
    GOOD,           // 0-50
    MODERATE,       // 51-100
    UNHEALTHY_SG,   // 101-150 (Unhealthy for Sensitive Groups)
    UNHEALTHY,      // 151-200
    VERY_UNHEALTHY, // 201-300
    HAZARDOUS       // 301+
}

/**
 * Wind speed levels
 */
enum class WindLevel {
    CALM,       // 0-5 km/h
    LIGHT,      // 5-20 km/h
    MODERATE,   // 20-40 km/h
    STRONG,     // 40-60 km/h
    VERY_STRONG // 60+ km/h
}

/**
 * Precipitation levels
 */
enum class PrecipitationLevel {
    NONE,       // 0%
    LOW,        // 1-30%
    MODERATE,   // 31-60%
    HIGH,       // 61-80%
    VERY_HIGH   // 81-100%
}