package com.devsphere.aether.models

/**
 * UI model for daily forecast display
 * Used in CityWeatherFragment RecyclerView
 */
data class DailyForecastUi(
    val dayName: String,          // "Today", "Tomorrow", "Wed", "Thu", etc.
    val date: String,             // "Dec 15"
    val highTemp: Int,            // High temperature
    val lowTemp: Int,             // Low temperature
    val condition: String,        // "Sunny", "Rainy", etc.
    val iconResId: Int,           // Weather icon resource
    val precipitationProb: Int?,  // Precipitation probability (%)
    val isToday: Boolean = false  // Highlight today's forecast
)