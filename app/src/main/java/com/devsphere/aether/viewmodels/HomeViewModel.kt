package com.devsphere.aether.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devsphere.aether.data.repository.AetherRepository
import com.devsphere.aether.models.HomeUiState
import com.devsphere.aether.models.HourlyForecastUi
import com.devsphere.aether.network.ApiResult
import com.devsphere.aether.utils.LocationManager
import com.devsphere.aether.utils.LocationResult
import com.devsphere.aether.utils.ReverseGeocoder
import com.devsphere.aether.utils.WeatherImageMapper
import com.devsphere.aether.utils.WeatherUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.devsphere.aether.R
import com.devsphere.aether.data.remote.dto.weather.CurrentBlock
import com.devsphere.aether.data.remote.dto.weather.HourlyBlock
import com.devsphere.aether.data.remote.dto.weather.WeatherResponse

/**
 * ViewModel for Home Screen
 * Handles weather data fetching, location detection, and UI state management
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: AetherRepository,
    private val locationManager: LocationManager,
    private val reverseGeocoder: ReverseGeocoder
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    // Auto-refresh interval (30 minutes in milliseconds)
    private val AUTO_REFRESH_INTERVAL = 30 * 60 * 1000L
    private var lastRefreshTime = 0L

    init {
        // Auto-detect location on initialization
        detectAndLoadWeather()
    }

    /**
     * Detect current location and load weather data
     */
    fun detectAndLoadWeather() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            // Get current location
            when (val locationResult = locationManager.getCurrentLocation()) {
                is LocationResult.Success -> {
                    loadWeatherData(
                        latitude = locationResult.latitude,
                        longitude = locationResult.longitude
                    )
                }
                is LocationResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = locationResult.message
                        )
                    }
                }
            }
        }
    }

    /**
     * Load weather data for specific coordinates
     */
    fun loadWeatherData(
        latitude: Double,
        longitude: Double,
        isRefreshing: Boolean = false
    ) {
        viewModelScope.launch {
            if (isRefreshing) {
                _uiState.update { it.copy(isRefreshing = true) }
            } else {
                _uiState.update { it.copy(isLoading = true) }
            }

            // Store coordinates
            _uiState.update {
                it.copy(
                    latitude = latitude,
                    longitude = longitude
                )
            }

            // Fetch location name using reverse geocoding
            val locationName = reverseGeocoder.getSimpleLocationString(latitude, longitude)

            // Fetch weather data
            val weatherResult = repository.getWeather(
                latitude = latitude,
                longitude = longitude,
                forecastDays = 7
            )

            // Fetch air quality data
            val airQualityResult = repository.getAirQuality(
                latitude = latitude,
                longitude = longitude
            )

            // Fetch minutely forecast for rain prediction
            val minutelyResult = repository.getMinutelyForecast(
                latitude = latitude,
                longitude = longitude
            )

            // Process results and update UI state
            when (weatherResult) {
                is ApiResult.Success -> {
                    val weather = weatherResult.data
                    val airQuality = (airQualityResult as? ApiResult.Success)?.data
                    val minutely = (minutelyResult as? ApiResult.Success)?.data

                    // Process and format data
                    val current = weather.current
                    val daily = weather.daily
                    val hourly = weather.hourly

                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            isRefreshing = false,
                            errorMessage = null,
                            weather = weather,
                            airQuality = airQuality,
                            minutelyForecast = minutely,
                            locationName = locationName,

                            // Hero section
                            heroImageUrl = WeatherImageMapper.getImageUrl(
                                current?.weatherCode,
                                current?.isDay
                            ),
                            currentTemp = current?.temperature?.toInt()?.toString() ?: "--",
                            currentCondition = WeatherImageMapper.getConditionText(
                                current?.weatherCode
                            ),
                            highLowTemp = WeatherUtils.formatHighLow(
                                daily?.tempMax?.firstOrNull(),
                                daily?.tempMin?.firstOrNull()
                            ),

                            // Sun times
                            sunriseTime = WeatherUtils.formatTime(daily?.sunrise?.firstOrNull()),
                            sunsetTime = WeatherUtils.formatTime(daily?.sunset?.firstOrNull()),

                            // Rain prediction
                            rainMessage = calculateRainMessage(current, minutely, hourly),
                            showRainCard = shouldShowRainCard(current, minutely, hourly),

                            // Metrics
                            humidity = WeatherUtils.formatHumidity(current?.humidity),
                            windSpeed = WeatherUtils.formatWindSpeed(current?.windSpeed),
                            visibility = WeatherUtils.formatVisibility(current?.visibility),
                            pressure = WeatherUtils.formatPressure(current?.pressure),

                            // AQI
                            aqiValue = airQuality?.current?.europeanAqi?.toString() ?: "--",
                            aqiCategory = WeatherUtils.formatAqiCategory(
                                airQuality?.current?.europeanAqi
                            ),
                            aqiColor = WeatherUtils.getAqiColorHex(
                                airQuality?.current?.europeanAqi
                            ),

                            // Hourly forecast
                            hourlyForecast = buildHourlyForecast(hourly, current?.isDay),

                            // Update timestamp
                            lastUpdateTime = System.currentTimeMillis()
                        )
                    }

                    lastRefreshTime = System.currentTimeMillis()
                }

                is ApiResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            errorMessage = weatherResult.message
                        )
                    }
                }
            }
        }
    }

    /**
     * Refresh weather data (for pull-to-refresh)
     */
    fun refreshWeather() {
        val latitude = _uiState.value.latitude
        val longitude = _uiState.value.longitude

        if (latitude != null && longitude != null) {
            loadWeatherData(latitude, longitude, isRefreshing = true)
        } else {
            // If no location stored, detect again
            detectAndLoadWeather()
        }
    }

    /**
     * Check if auto-refresh is needed
     */
    fun checkAutoRefresh() {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastRefreshTime > AUTO_REFRESH_INTERVAL) {
            refreshWeather()
        }
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    /**
     * Calculate rain message based on current conditions and forecast
     */
    private fun calculateRainMessage(
        current: CurrentBlock?,
        minutely: WeatherResponse?,
        hourly: HourlyBlock?
    ): String {
        // Check if currently raining
        val isRaining = WeatherUtils.isCurrentlyRaining(current?.precipitation)

        // Calculate minutes until rain from minutely data
        val minutesUntilRain = WeatherUtils.calculateMinutesUntilRain(
            minutely?.minutely?.time,
            minutely?.minutely?.precipitation
        )

        // Get precipitation probability from hourly data
        val precipitationProb = hourly?.precipitationProbabilities?.firstOrNull()

        return WeatherUtils.getRainMessage(isRaining, minutesUntilRain, precipitationProb)
    }

    /**
     * Determine if rain card should be shown
     */
    private fun shouldShowRainCard(
        current: CurrentBlock?,
        minutely: WeatherResponse?,
        hourly: HourlyBlock?
    ): Boolean {
        val isRaining = WeatherUtils.isCurrentlyRaining(current?.precipitation)
        val minutesUntilRain = WeatherUtils.calculateMinutesUntilRain(
            minutely?.minutely?.time,
            minutely?.minutely?.precipitation
        )
        val precipitationProb = hourly?.precipitationProbabilities?.firstOrNull() ?: 0

        return isRaining || minutesUntilRain != null || precipitationProb > 30
    }

    /**
     * Build hourly forecast list for RecyclerView
     */
    private fun buildHourlyForecast(
        hourly: HourlyBlock?,
        currentIsDay: Int?
    ): List<HourlyForecastUi> {
        if (hourly == null) return emptyList()

        val times = hourly.time ?: return emptyList()
        val temps = hourly.temperatures ?: return emptyList()
        val codes = hourly.weatherCodes ?: return emptyList()

        // Take first 24 hours
        val count = minOf(24, times.size)

        return (0 until count).mapNotNull { index ->
            val time = times.getOrNull(index) ?: return@mapNotNull null
            val temp = temps.getOrNull(index) ?: return@mapNotNull null
            val code = codes.getOrNull(index)

            val timeLabel = if (index == 0) {
                "Now"
            } else {
                WeatherUtils.formatTime(time)
            }

            HourlyForecastUi(
                time = timeLabel,
                temperatureC = temp.toInt(),
                iconResId = getWeatherIconRes(code, currentIsDay)
            )
        }
    }

    /**
     * Get weather icon resource based on weather code
     * TODO: Update with actual drawable resources
     */
    private fun getWeatherIconRes(weatherCode: Int?, isDay: Int?): Int {
        return when (weatherCode) {
            0 -> if (isDay == 1) R.drawable.ic_sun else R.drawable.ic_sun
            1, 2, 3 -> R.drawable.ic_sun // Use cloud icon when available
            61, 63, 65, 80, 81, 82 -> R.drawable.ic_rain
            95, 96, 99 -> R.drawable.ic_rain // Use thunder icon when available
            else -> R.drawable.ic_sun
        }
    }
}