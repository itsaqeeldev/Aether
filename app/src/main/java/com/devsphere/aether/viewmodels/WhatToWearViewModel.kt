package com.devsphere.aether.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devsphere.aether.data.repository.WhatToWearRepository
import com.devsphere.aether.models.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI State for What to Wear screen
 */
data class WhatToWearUiState(
    val currentTemp: String = "--",
    val currentCondition: String = "--",
    val weatherIconRes: Int? = null,

    val formalItems: List<WearableItemUi> = emptyList(),
    val casualItems: List<WearableItemUi> = emptyList(),
    val sportsItems: List<WearableItemUi> = emptyList(),

    val tips: List<WeatherTipUi> = emptyList(),
    val activities: List<ActivityUi> = emptyList(),

    val smartInsightTitle: String? = null,
    val smartInsightMessage: String? = null,
    val smartInsightIconRes: Int? = null,

    val isLoading: Boolean = true,
    val error: String? = null,

    val lastUpdated: Long? = null
)

/**
 * WhatToWearViewModel - Uses SharedWeatherViewModel for weather data
 * Fragment will observe both ViewModels and coordinate updates
 */
@HiltViewModel
class WhatToWearViewModel @Inject constructor(
    private val whatToWearRepository: WhatToWearRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WhatToWearUiState())
    val uiState: StateFlow<WhatToWearUiState> = _uiState.asStateFlow()

    /**
     * Generate suggestions when weather data becomes available
     * Called from Fragment when SharedWeatherState changes
     */
    fun generateSuggestionsFromSharedState(sharedState: SharedWeatherState) {
        viewModelScope.launch {
            // Only generate suggestions when we have valid weather data
            if (sharedState.weather != null &&
                sharedState.latitude != null &&
                sharedState.longitude != null) {

                // Show loading only initially
                if (_uiState.value.currentTemp == "--") {
                    _uiState.update { it.copy(isLoading = true) }
                }

                generateSuggestions(sharedState.latitude, sharedState.longitude)
            } else if (sharedState.isLoading) {
                // Weather is still loading
                _uiState.update { it.copy(isLoading = true, error = null) }
            } else if (sharedState.errorMessage != null) {
                // Weather fetch failed
                _uiState.update { it.copy(
                    isLoading = false,
                    error = sharedState.errorMessage
                )}
            }
        }
    }

    /**
     * Generate suggestions using coordinates
     */
    private suspend fun generateSuggestions(lat: Double, lon: Double) {
        try {
            val suggestions = whatToWearRepository.getSuggestions(
                latitude = lat,
                longitude = lon,
                forceRefresh = false
            )

            _uiState.update {
                WhatToWearUiState(
                    currentTemp = suggestions.currentTemp,
                    currentCondition = suggestions.currentCondition,
                    weatherIconRes = mapIconCodeToResource(suggestions.weatherIconCode),
                    formalItems = suggestions.formalItems,
                    casualItems = suggestions.casualItems,
                    sportsItems = suggestions.sportsItems,
                    tips = suggestions.tips,
                    activities = suggestions.activities,
                    smartInsightTitle = suggestions.smartInsightTitle,
                    smartInsightMessage = suggestions.smartInsightMessage,
                    smartInsightIconRes = getInsightIconRes(suggestions.smartInsightTitle),
                    isLoading = false,
                    error = null,
                    lastUpdated = suggestions.timestamp
                )
            }

        } catch (e: Exception) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    error = "Failed to load suggestions: ${e.message}"
                )
            }
        }
    }

    /**
     * Clear error state
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    private fun mapIconCodeToResource(iconCode: Int): Int {
        return when (iconCode) {
            1 -> com.devsphere.aether.R.drawable.ic_sun
            2 -> com.devsphere.aether.R.drawable.ic_sun
            3 -> com.devsphere.aether.R.drawable.ic_sun
            4 -> com.devsphere.aether.R.drawable.ic_rain
            5 -> com.devsphere.aether.R.drawable.ic_rain
            else -> com.devsphere.aether.R.drawable.ic_sun
        }
    }

    private fun getInsightIconRes(title: String?): Int? {
        return when {
            title?.contains("warning", ignoreCase = true) == true ->
                com.devsphere.aether.R.drawable.ic_rain
            title?.contains("perfect", ignoreCase = true) == true ->
                com.devsphere.aether.R.drawable.ic_sun
            else -> com.devsphere.aether.R.drawable.ic_sun
        }
    }
}