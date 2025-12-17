package com.devsphere.aether.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devsphere.aether.data.remote.dto.air.AirQualityResponse
import com.devsphere.aether.data.remote.dto.weather.WeatherResponse
import com.devsphere.aether.data.repository.AetherRepository
import com.devsphere.aether.network.ApiResult
import com.devsphere.aether.utils.LocationManager
import com.devsphere.aether.utils.LocationResult
import com.devsphere.aether.utils.ReverseGeocoder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Shared ViewModel for weather data across multiple fragments
 * Prevents duplicate API calls and ensures consistent state
 *
 * Strategy: Stale-While-Revalidate
 * 1. Load cached data first (instant display)
 * 2. Fetch fresh data in background (invisible to user)
 * 3. Update UI when fresh data arrives (smooth transition)
 *
 * Usage:
 * - HomeFragment observes this for display
 * - WhatToWearFragment uses this data instead of fetching separately
 */
@HiltViewModel
class SharedWeatherViewModel @Inject constructor(
    private val repository: AetherRepository,
    private val locationManager: LocationManager,
    private val reverseGeocoder: ReverseGeocoder,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _weatherState = MutableStateFlow(SharedWeatherState())
    val weatherState: StateFlow<SharedWeatherState> = _weatherState.asStateFlow()

    // Guard flag to prevent multiple initial fetches
    private var hasFetchedInitially: Boolean
        get() = savedStateHandle.get<Boolean>(KEY_HAS_FETCHED) ?: false
        set(value) = savedStateHandle.set(KEY_HAS_FETCHED, value)

    init {
        // Only fetch on first initialization
        if (!hasFetchedInitially) {
            detectAndLoadWeather()
        }
    }

    /**
     * Detect current location and load weather
     */
    fun detectAndLoadWeather() {
        viewModelScope.launch {
            _weatherState.update { it.copy(
                isLoading = true,
                errorMessage = null
            )}

            when (val locationResult = locationManager.getCurrentLocation()) {
                is LocationResult.Success -> {
                    loadWeatherData(
                        latitude = locationResult.latitude,
                        longitude = locationResult.longitude
                    )
                    hasFetchedInitially = true
                }
                is LocationResult.Error -> {
                    _weatherState.update { it.copy(
                        isLoading = false,
                        errorMessage = locationResult.message
                    )}
                }
            }
        }
    }

    /**
     * Load weather data for specific location
     *
     * Strategy: Stale-While-Revalidate
     * 1. Load cached data first (instant display)
     * 2. Fetch fresh data in background (invisible update)
     */
    fun loadWeatherData(
        latitude: Double,
        longitude: Double,
        isRefreshing: Boolean = false
    ) {
        viewModelScope.launch {
            if (isRefreshing) {
                _weatherState.update { it.copy(isRefreshing = true) }
            } else {
                _weatherState.update { it.copy(isLoading = true) }
            }

            // Get location name
            val locationName = reverseGeocoder.getSimpleLocationString(latitude, longitude)

            // STEP 1: Load cached data first (instant display)
            if (!isRefreshing) {
                val cachedWeatherResult = repository.getWeather(
                    latitude = latitude,
                    longitude = longitude,
                    forecastDays = 7,
                    forceRefresh = false,
                    returnStaleCache = true // Return cache immediately
                )

                val cachedAqiResult = repository.getAirQuality(
                    latitude = latitude,
                    longitude = longitude,
                    forceRefresh = false
                )

                // If we have cached data, show it immediately
                if (cachedWeatherResult is ApiResult.Success) {
                    val weather = cachedWeatherResult.data
                    val airQuality = (cachedAqiResult as? ApiResult.Success)?.data

                    _weatherState.update {
                        SharedWeatherState(
                            isLoading = false,
                            isRefreshing = false,
                            errorMessage = null,
                            latitude = latitude,
                            longitude = longitude,
                            locationName = locationName,
                            weather = weather,
                            airQuality = airQuality,
                            minutelyForecast = null, // Will fetch in background
                            lastUpdateTime = System.currentTimeMillis()
                        )
                    }
                }
            }

            // STEP 2: Fetch fresh data in background
            val freshWeatherResult = repository.getWeather(
                latitude = latitude,
                longitude = longitude,
                forecastDays = 7,
                forceRefresh = true, // Force fresh fetch
                returnStaleCache = false
            )

            val freshAqiResult = repository.getAirQuality(
                latitude = latitude,
                longitude = longitude,
                forceRefresh = true
            )

            val minutelyResult = repository.getMinutelyForecast(latitude, longitude)

            when (freshWeatherResult) {
                is ApiResult.Success -> {
                    val weather = freshWeatherResult.data
                    val airQuality = (freshAqiResult as? ApiResult.Success)?.data
                    val minutely = (minutelyResult as? ApiResult.Success)?.data

                    _weatherState.update {
                        SharedWeatherState(
                            isLoading = false,
                            isRefreshing = false,
                            errorMessage = null,
                            latitude = latitude,
                            longitude = longitude,
                            locationName = locationName,
                            weather = weather,
                            airQuality = airQuality,
                            minutelyForecast = minutely,
                            lastUpdateTime = System.currentTimeMillis()
                        )
                    }
                }
                is ApiResult.Error -> {
                    // If fresh fetch fails but we already showed cached data, keep it
                    val currentState = _weatherState.value
                    if (currentState.weather != null) {
                        // Keep cached data, just update loading state
                        _weatherState.update { it.copy(
                            isLoading = false,
                            isRefreshing = false
                            // Don't show error message if we have cached data
                        )}
                    } else {
                        // No cached data, show error
                        _weatherState.update { it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            errorMessage = freshWeatherResult.message
                        )}
                    }
                }
            }
        }
    }

    /**
     * Manual refresh - always bypasses cache
     */
    fun refreshWeather() {
        val state = _weatherState.value
        val lat = state.latitude
        val lon = state.longitude

        if (lat != null && lon != null) {
            loadWeatherData(lat, lon, isRefreshing = true)
        } else {
            detectAndLoadWeather()
        }
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _weatherState.update { it.copy(errorMessage = null) }
    }

    /**
     * Check if weather data is available
     */
    fun hasWeatherData(): Boolean {
        return _weatherState.value.weather != null
    }

    companion object {
        private const val KEY_HAS_FETCHED = "shared_weather_has_fetched"
    }
}

/**
 * Shared weather state - single source of truth
 */
data class SharedWeatherState(
    // Loading states
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,

    // Location
    val latitude: Double? = null,
    val longitude: Double? = null,
    val locationName: String? = null,

    // Weather data
    val weather: WeatherResponse? = null,
    val airQuality: AirQualityResponse? = null,
    val minutelyForecast: WeatherResponse? = null,

    // Metadata
    val lastUpdateTime: Long = 0L
)