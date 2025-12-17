package com.devsphere.aether.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devsphere.aether.R
import com.devsphere.aether.data.remote.dto.weather.CurrentBlock
import com.devsphere.aether.data.remote.dto.weather.HourlyBlock
import com.devsphere.aether.data.remote.dto.weather.WeatherResponse
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
import kotlin.math.roundToInt

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: AetherRepository,
    private val locationManager: LocationManager,
    private val reverseGeocoder: ReverseGeocoder,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    // Guard flag to prevent multiple initial fetches
    private var hasFetchedInitially: Boolean
        get() = savedStateHandle.get<Boolean>(KEY_HAS_FETCHED) ?: false
        set(value) = savedStateHandle.set(KEY_HAS_FETCHED, value)

    init {
        // Only fetch on first initialization, not on configuration changes or re-navigation
        if (!hasFetchedInitially) {
            detectAndLoadWeather()
        }
    }

    fun detectAndLoadWeather() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            when (val locationResult = locationManager.getCurrentLocation()) {
                is LocationResult.Success -> {
                    loadWeatherData(
                        latitude = locationResult.latitude,
                        longitude = locationResult.longitude
                    )
                    hasFetchedInitially = true
                }
                is LocationResult.Error -> _uiState.update {
                    it.copy(isLoading = false, errorMessage = locationResult.message)
                }
            }
        }
    }

    fun loadWeatherData(
        latitude: Double,
        longitude: Double,
        isRefreshing: Boolean = false
    ) {
        viewModelScope.launch {
            if (isRefreshing) _uiState.update { it.copy(isRefreshing = true) }
            else _uiState.update { it.copy(isLoading = true) }

            _uiState.update { it.copy(latitude = latitude, longitude = longitude) }

            val locationName = reverseGeocoder.getSimpleLocationString(latitude, longitude)

            // Repository decides whether to use cache or fetch fresh data
            val weatherResult = repository.getWeather(
                latitude = latitude,
                longitude = longitude,
                forecastDays = 7,
                forceRefresh = isRefreshing
            )

            // AQI has separate TTL - only refresh if needed or forced
            val airQualityResult = repository.getAirQuality(
                latitude = latitude,
                longitude = longitude,
                forceRefresh = isRefreshing
            )

            val minutelyResult = repository.getMinutelyForecast(latitude, longitude)

            when (weatherResult) {
                is ApiResult.Success -> {
                    val weather = weatherResult.data
                    val airQuality = (airQualityResult as? ApiResult.Success)?.data
                    val minutely = (minutelyResult as? ApiResult.Success)?.data

                    val current = weather.current
                    val daily = weather.daily
                    val hourly = weather.hourly

                    // AQI fallback logic (CURRENT → HOURLY)
                    val aqi = airQuality?.current?.europeanAqi
                        ?: airQuality?.hourly?.europeanAqi?.firstOrNull()

                    // UV peak (HOURLY UV INDEX)
                    val uvUi = buildUvUi(hourly, daily?.uvIndexMax?.firstOrNull())

                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            isRefreshing = false,
                            errorMessage = null,

                            weather = weather,
                            airQuality = airQuality,
                            minutelyForecast = minutely,
                            locationName = locationName,

                            // Hero
                            heroImageUrl = WeatherImageMapper.getImageUrl(
                                current?.weatherCode,
                                current?.isDay
                            ),
                            currentTemp = current?.temperature?.toInt()?.toString() ?: "--",
                            currentCondition = WeatherImageMapper.getConditionText(current?.weatherCode),
                            highLowTemp = WeatherUtils.formatHighLow(
                                daily?.tempMax?.firstOrNull(),
                                daily?.tempMin?.firstOrNull()
                            ),

                            // Sun
                            sunriseTime = WeatherUtils.formatTime(daily?.sunrise?.firstOrNull()),
                            sunsetTime = WeatherUtils.formatTime(daily?.sunset?.firstOrNull()),

                            // Rain
                            rainMessage = calculateRainMessage(current, minutely, hourly),
                            showRainCard = shouldShowRainCard(current, minutely, hourly),

                            // Metrics
                            humidity = WeatherUtils.formatHumidity(current?.humidity),
                            windSpeed = WeatherUtils.formatWindSpeed(current?.windSpeed),
                            visibility = WeatherUtils.formatVisibility(current?.visibility),
                            pressure = WeatherUtils.formatPressure(current?.pressure),

                            // AQI
                            aqiValue = aqi?.toString() ?: "--",
                            aqiCategory = WeatherUtils.formatAqiCategory(aqi),
                            aqiColor = WeatherUtils.getAqiColorHex(aqi),

                            // UV card
                            showUvCard = uvUi.show,
                            uvTitle = uvUi.title,
                            uvSub = uvUi.sub,

                            // Hourly forecast
                            hourlyForecast = buildHourlyForecast(hourly, current?.isDay),

                            lastUpdateTime = System.currentTimeMillis()
                        )
                    }
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
     * Manual refresh - always bypasses cache
     */
    fun refreshWeather() {
        val lat = _uiState.value.latitude
        val lon = _uiState.value.longitude
        if (lat != null && lon != null) {
            loadWeatherData(lat, lon, isRefreshing = true)
        } else {
            detectAndLoadWeather()
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    private fun calculateRainMessage(
        current: CurrentBlock?,
        minutely: WeatherResponse?,
        hourly: HourlyBlock?
    ): String {
        val isRaining = WeatherUtils.isCurrentlyRaining(current?.precipitation)
        val minutesUntilRain = WeatherUtils.calculateMinutesUntilRain(
            minutely?.minutely?.time,
            minutely?.minutely?.precipitation
        )
        val precipitationProb = hourly?.precipitationProbabilities?.firstOrNull()
        return WeatherUtils.getRainMessage(isRaining, minutesUntilRain, precipitationProb)
    }

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

    private fun buildHourlyForecast(
        hourly: HourlyBlock?,
        currentIsDay: Int?
    ): List<HourlyForecastUi> {
        if (hourly == null) return emptyList()
        val times = hourly.time ?: return emptyList()
        val temps = hourly.temperatures ?: return emptyList()
        val codes = hourly.weatherCodes ?: return emptyList()

        val count = minOf(24, times.size)
        return (0 until count).mapNotNull { index ->
            val time = times.getOrNull(index) ?: return@mapNotNull null
            val temp = temps.getOrNull(index) ?: return@mapNotNull null
            val code = codes.getOrNull(index)

            val timeLabel = if (index == 0) "Now" else WeatherUtils.formatTime(time)

            HourlyForecastUi(
                time = timeLabel,
                temperatureC = temp.toInt(),
                iconResId = getWeatherIconRes(code, currentIsDay)
            )
        }
    }

    private fun getWeatherIconRes(weatherCode: Int?, isDay: Int?): Int {
        return when (weatherCode) {
            0 -> if (isDay == 1) R.drawable.ic_sun else R.drawable.ic_sun
            1, 2, 3 -> R.drawable.ic_sun
            61, 63, 65, 80, 81, 82 -> R.drawable.ic_rain
            95, 96, 99 -> R.drawable.ic_rain
            else -> R.drawable.ic_sun
        }
    }

    private data class UvUi(
        val show: Boolean,
        val title: String?,
        val sub: String?
    )

    private fun buildUvUi(hourly: HourlyBlock?, dailyUvMax: Double?): UvUi {
        val peak = findTodayPeakUv(hourly)
        val peakUv = peak?.uv ?: dailyUvMax
        if (peakUv == null) return UvUi(show = false, title = null, sub = null)

        val peakTimeText = peak?.timeIso?.let { WeatherUtils.formatTime(it) }
        val uvInt = peakUv.roundToInt()

        val title = if (peakTimeText != null && peakTimeText != "--") {
            "UV peaks at $peakTimeText"
        } else {
            "Peak UV today"
        }

        val level = WeatherUtils.uvLevel(peakUv)
        val advice = WeatherUtils.uvAdvice(peakUv)
        val sub = "UV $uvInt • $level\n$advice"

        return UvUi(show = true, title = title, sub = sub)
    }

    private data class PeakUv(val timeIso: String, val uv: Double)

    private fun findTodayPeakUv(hourly: HourlyBlock?): PeakUv? {
        val times = hourly?.time ?: return null
        val uvs = hourly.uvIndex ?: return null
        if (times.isEmpty() || uvs.isEmpty()) return null

        val today = times.firstOrNull()?.take(10) ?: return null
        val n = minOf(times.size, uvs.size)

        var bestUv = Double.NEGATIVE_INFINITY
        var bestTime: String? = null

        for (i in 0 until n) {
            val t = times[i]
            if (!t.startsWith(today)) continue
            val uv = uvs[i]
            if (uv > bestUv) {
                bestUv = uv
                bestTime = t
            }
        }

        return if (bestTime != null && bestUv.isFinite()) PeakUv(bestTime, bestUv) else null
    }

    companion object {
        private const val KEY_HAS_FETCHED = "has_fetched_initially"
    }
}