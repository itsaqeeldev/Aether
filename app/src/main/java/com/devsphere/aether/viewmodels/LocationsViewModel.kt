package com.devsphere.aether.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devsphere.aether.data.repository.LocationRepository
import com.devsphere.aether.models.SavedLocationUi
import com.devsphere.aether.utils.LocationManager
import com.devsphere.aether.utils.LocationResult
import com.devsphere.aether.utils.ReverseGeocoder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LocationsViewModel @Inject constructor(
    private val locationRepository: LocationRepository,
    private val locationManager: LocationManager,
    private val reverseGeocoder: ReverseGeocoder
) : ViewModel() {

    private val _uiState = MutableStateFlow(LocationsUiState())
    val uiState: StateFlow<LocationsUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<LocationsEvent>()
    val events = _events.asSharedFlow()

    init {
        loadLocations()
        loadCurrentLocation()
    }

    /**
     * Load saved locations from database
     * ✅ OPTIMIZATION: Display cached data only, no automatic network fetch
     * Weather is fetched only when user taps a location to view details
     */
    private fun loadLocations() {
        viewModelScope.launch {
            locationRepository.getSavedLocations().collect { entities ->
                val locations = entities.map { entity ->
                    SavedLocationUi.fromEntity(entity)
                }
                _uiState.update { it.copy(
                    savedLocations = locations,
                    isLoading = false
                ) }

                // ❌ REMOVED: No longer auto-fetching weather for all locations
                // This was causing unnecessary API calls every time the screen opened
                // Weather will be fetched when user navigates to CityWeatherFragment
            }
        }
    }

    /**
     * Load current device location
     */
    private fun loadCurrentLocation() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingCurrentLocation = true) }

            when (val result = locationManager.getCurrentLocation()) {
                is LocationResult.Success -> {
                    val locationName = reverseGeocoder.getSimpleLocationString(
                        result.latitude,
                        result.longitude
                    )
                    _uiState.update { it.copy(
                        currentLocationName = locationName,
                        currentLatitude = result.latitude,
                        currentLongitude = result.longitude,
                        isLoadingCurrentLocation = false,
                        hasLocationPermission = true
                    ) }
                }
                is LocationResult.Error -> {
                    _uiState.update { it.copy(
                        currentLocationName = "Enable location",
                        isLoadingCurrentLocation = false,
                        hasLocationPermission = false
                    ) }
                }
            }
        }
    }

    /**
     * Toggle expansion state for a location card
     */
    fun toggleExpansion(locationId: Int) {
        val currentExpandedId = _uiState.value.expandedLocationId
        val newExpandedId = if (currentExpandedId == locationId) null else locationId
        _uiState.update { it.copy(expandedLocationId = newExpandedId) }
    }

    /**
     * Collapse any expanded location
     */
    fun collapseExpanded() {
        _uiState.update { it.copy(expandedLocationId = null) }
    }

    /**
     * Remove a location from saved list
     */
    fun removeLocation(locationId: Int) {
        viewModelScope.launch {
            locationRepository.removeLocation(locationId)
            _uiState.update { it.copy(expandedLocationId = null) }
            _events.emit(LocationsEvent.LocationRemoved)
        }
    }

    /**
     * Navigate to city weather details
     * ✅ Weather will be fetched by CityWeatherViewModel when screen opens
     */
    fun navigateToDetails(location: SavedLocationUi) {
        viewModelScope.launch {
            _events.emit(LocationsEvent.NavigateToDetails(
                locationId = location.id,
                cityName = location.name,
                country = location.country,
                countryCode = location.countryCode,
                latitude = location.latitude ?: 0.0,
                longitude = location.longitude ?: 0.0,
                timezone = location.timezone
            ))
        }
    }

    /**
     * Navigate to current location weather (Home screen)
     */
    fun navigateToCurrentLocation() {
        viewModelScope.launch {
            val state = _uiState.value
            if (state.hasLocationPermission && state.currentLatitude != null && state.currentLongitude != null) {
                _events.emit(LocationsEvent.NavigateToHome)
            } else {
                _events.emit(LocationsEvent.RequestLocationPermission)
            }
        }
    }

    /**
     * Filter locations by search query
     */
    fun filterLocations(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    /**
     * Refresh location permission state
     */
    fun refreshLocationPermission() {
        loadCurrentLocation()
    }

    /**
     * Get filtered locations based on search query
     */
    fun getFilteredLocations(): List<SavedLocationUi> {
        val state = _uiState.value
        return if (state.searchQuery.isBlank()) {
            state.savedLocations
        } else {
            state.savedLocations.filter { location ->
                location.name.contains(state.searchQuery, ignoreCase = true) ||
                        location.country.contains(state.searchQuery, ignoreCase = true)
            }
        }
    }
}

/**
 * UI State for Locations Screen
 */
data class LocationsUiState(
    val savedLocations: List<SavedLocationUi> = emptyList(),
    val isLoading: Boolean = true,
    val expandedLocationId: Int? = null,
    val searchQuery: String = "",

    // Current location
    val currentLocationName: String = "My Location",
    val currentLatitude: Double? = null,
    val currentLongitude: Double? = null,
    val isLoadingCurrentLocation: Boolean = true,
    val hasLocationPermission: Boolean = false
)

/**
 * One-time events from ViewModel
 */
sealed class LocationsEvent {
    object LocationRemoved : LocationsEvent()
    object NavigateToHome : LocationsEvent()
    object RequestLocationPermission : LocationsEvent()
    data class NavigateToDetails(
        val locationId: Int,
        val cityName: String,
        val country: String,
        val countryCode: String?,
        val latitude: Double,
        val longitude: Double,
        val timezone: String?
    ) : LocationsEvent()
}