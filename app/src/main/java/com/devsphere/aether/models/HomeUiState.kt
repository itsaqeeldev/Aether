package com.devsphere.aether.models

import com.devsphere.aether.data.remote.dto.air.AirQualityResponse
import com.devsphere.aether.data.remote.dto.weather.WeatherResponse

/**
 * UI State for Home Screen
 * Represents all possible states of the Home screen
 */
data class HomeUiState(
    // Loading states
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,

    // Error state
    val errorMessage: String? = null,

    // Data
    val weather: WeatherResponse? = null,
    val airQuality: AirQualityResponse? = null,
    val minutelyForecast: WeatherResponse? = null,

    // Location
    val locationName: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,

    // Derived/Formatted data for UI
    val heroImageUrl: String? = null,
    val currentTemp: String? = null,
    val currentCondition: String? = null,
    val highLowTemp: String? = null,
    val sunriseTime: String? = null,
    val sunsetTime: String? = null,
    val rainMessage: String? = null, // "Rain expected in 42 minutes" or "Raining now"
    val showRainCard: Boolean = false,

    // Metrics
    val humidity: String? = null,
    val windSpeed: String? = null,
    val visibility: String? = null,
    val pressure: String? = null,

    // AQI
    val aqiValue: String? = null,
    val aqiCategory: String? = null,
    val aqiColor: String? = null,

    // Hourly forecast
    val hourlyForecast: List<HourlyForecastUi> = emptyList(),

    // Last update timestamp
    val lastUpdateTime: Long = 0L,

    // UV
    val showUvCard: Boolean = false,
    val uvTitle: String? = null,
    val uvSub: String? = null,

    )