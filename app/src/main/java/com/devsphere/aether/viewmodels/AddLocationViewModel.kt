package com.devsphere.aether.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devsphere.aether.data.remote.dto.geocoding.GeocodingResult
import com.devsphere.aether.data.repository.LocationRepository
import com.devsphere.aether.models.PopularCity
import com.devsphere.aether.network.ApiResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddLocationViewModel @Inject constructor(
    private val locationRepository: LocationRepository
) : ViewModel() {

    // UI State
    private val _uiState = MutableStateFlow(AddLocationUiState())
    val uiState: StateFlow<AddLocationUiState> = _uiState.asStateFlow()

    // Events
    private val _events = MutableSharedFlow<AddLocationEvent>()
    val events = _events.asSharedFlow()

    // Search debounce job
    private var searchJob: Job? = null

    init {
        loadPopularCities()
        loadSavedLocationIds()
    }

    /**
     * Load popular cities WITHOUT fetching temperatures
     * ✅ OPTIMIZATION: Zero network calls when bottom sheet opens
     * Temperature will only be fetched when user taps a city to add it
     */
    private fun loadPopularCities() {
        viewModelScope.launch {
            val cities = PopularCity.getPopularCities().map { city ->
                // Set isLoading = false and temperature = null
                // No network fetch here
                city.copy(temperature = null, isLoading = false)
            }
            _uiState.update { it.copy(popularCities = cities) }

            // ❌ REMOVED: No longer fetching temperatures for all cities
            // This was causing 10+ API calls every time the bottom sheet opened
            // Temperature will be fetched only when user taps a city (if needed)
        }
    }

    /**
     * Load IDs of already saved locations
     */
    private fun loadSavedLocationIds() {
        viewModelScope.launch {
            locationRepository.getSavedLocations().collect { locations ->
                val ids = locations.map { it.id }.toSet()
                _uiState.update { it.copy(
                    savedLocationIds = ids,
                    isMaxLocationsReached = locations.size >= LocationRepository.MAX_SAVED_LOCATIONS
                ) }
            }
        }
    }

    /**
     * Handle search query changes with debounce
     */
    fun onSearchQueryChanged(query: String) {
        searchJob?.cancel()

        if (query.isBlank()) {
            _uiState.update { it.copy(
                searchQuery = "",
                searchResults = emptyList(),
                isSearching = false,
                showSearchResults = false
            ) }
            return
        }

        _uiState.update { it.copy(searchQuery = query, isSearching = true, showSearchResults = true) }

        searchJob = viewModelScope.launch {
            delay(300) // Debounce 300ms
            searchLocations(query)
        }
    }

    /**
     * Perform location search
     */
    private suspend fun searchLocations(query: String) {
        when (val result = locationRepository.searchLocations(query)) {
            is ApiResult.Success -> {
                _uiState.update { it.copy(
                    searchResults = result.data,
                    isSearching = false,
                    searchError = null
                ) }
            }
            is ApiResult.Error -> {
                _uiState.update { it.copy(
                    searchResults = emptyList(),
                    isSearching = false,
                    searchError = result.message
                ) }
            }
        }
    }

    /**
     * Clear search and show popular cities
     */
    fun clearSearch() {
        searchJob?.cancel()
        _uiState.update { it.copy(
            searchQuery = "",
            searchResults = emptyList(),
            isSearching = false,
            showSearchResults = false
        ) }
    }

    /**
     * Fetch temperature for a specific popular city (called when user taps it)
     * ✅ OPTIMIZATION: Only fetch when actually needed
     */
    fun fetchCityTemperature(cityIndex: Int) {
        viewModelScope.launch {
            val cities = _uiState.value.popularCities.toMutableList()
            if (cityIndex !in cities.indices) return@launch

            val city = cities[cityIndex]
            if (city.temperature != null) return@launch // Already fetched

            // Mark as loading
            cities[cityIndex] = city.copy(isLoading = true)
            _uiState.update { it.copy(popularCities = cities) }

            // Fetch temperature
            val temp = locationRepository.fetchCurrentTemperature(
                city.latitude,
                city.longitude
            )

            // Update with result
            cities[cityIndex] = city.copy(temperature = temp, isLoading = false)
            _uiState.update { it.copy(popularCities = cities) }
        }
    }

    /**
     * Add a popular city to saved locations
     * ✅ Now only makes ONE API call when user actually adds the city
     */
    fun addPopularCity(city: PopularCity) {
        if (_uiState.value.isMaxLocationsReached) {
            viewModelScope.launch {
                _events.emit(AddLocationEvent.MaxLocationsReached)
            }
            return
        }

        viewModelScope.launch {
            val success = locationRepository.saveLocationFromPopularCity(city)
            if (success) {
                _events.emit(AddLocationEvent.LocationAdded(city.name))
            } else {
                _events.emit(AddLocationEvent.MaxLocationsReached)
            }
        }
    }

    /**
     * Add a search result to saved locations
     */
    fun addSearchResult(result: GeocodingResult) {
        if (_uiState.value.isMaxLocationsReached) {
            viewModelScope.launch {
                _events.emit(AddLocationEvent.MaxLocationsReached)
            }
            return
        }

        viewModelScope.launch {
            val success = locationRepository.saveLocationFromSearchResult(result)
            if (success) {
                _events.emit(AddLocationEvent.LocationAdded(result.name ?: "Location"))
            } else {
                _events.emit(AddLocationEvent.MaxLocationsReached)
            }
        }
    }
}

/**
 * UI State for Add Location Bottom Sheet
 */
data class AddLocationUiState(
    val popularCities: List<PopularCity> = emptyList(),
    val searchQuery: String = "",
    val searchResults: List<GeocodingResult> = emptyList(),
    val isSearching: Boolean = false,
    val showSearchResults: Boolean = false,
    val searchError: String? = null,
    val savedLocationIds: Set<Int> = emptySet(),
    val isMaxLocationsReached: Boolean = false
)

/**
 * One-time events from ViewModel
 */
sealed class AddLocationEvent {
    data class LocationAdded(val cityName: String) : AddLocationEvent()
    object MaxLocationsReached : AddLocationEvent()
    data class Error(val message: String) : AddLocationEvent()
}