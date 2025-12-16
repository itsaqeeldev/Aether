package com.devsphere.aether.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devsphere.aether.R
import com.devsphere.aether.data.remote.dto.weather.DailyBlock
import com.devsphere.aether.data.remote.dto.weather.WeatherResponse
import com.devsphere.aether.data.repository.AetherRepository
import com.devsphere.aether.models.DailyForecastUi
import com.devsphere.aether.network.ApiResult
import com.devsphere.aether.utils.WeatherImageMapper
import com.devsphere.aether.utils.WeatherUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject
import kotlin.math.roundToInt

@HiltViewModel
class CityWeatherViewModel @Inject constructor(
    private val repository: AetherRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // Get navigation arguments
    private val locationId: Int = savedStateHandle.get<Int>("locationId") ?: 0
    private val cityName: String = savedStateHandle.get<String>("cityName") ?: "Unknown"
    private val country: String = savedStateHandle.get<String>("country") ?: ""
    private val countryCode: String? = savedStateHandle.get<String>("countryCode")
    private val latitude: Double =
        savedStateHandle.get<Float>("latitude")?.toDouble() ?: 0.0

    private val longitude: Double =
        savedStateHandle.get<Float>("longitude")?.toDouble() ?: 0.0

    private val timezone: String? = savedStateHandle.get<String>("timezone")

    private val _uiState = MutableStateFlow(CityWeatherUiState(
        cityName = cityName,
        country = country,
        countryCode = countryCode
    ))
    val uiState: StateFlow<CityWeatherUiState> = _uiState.asStateFlow()

    init {
        loadWeatherData()
    }

    /**
     * Load weather data for the city
     */
    fun loadWeatherData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            when (val result = repository.getWeather(
                latitude = latitude,
                longitude = longitude,
                timezone = timezone ?: "auto",
                forecastDays = 7 // Request 7 days but show 5
            )) {
                is ApiResult.Success -> {
                    val weather = result.data
                    processWeatherData(weather)
                }
                is ApiResult.Error -> {
                    _uiState.update { it.copy(
                        isLoading = false,
                        errorMessage = result.message
                    ) }
                }
            }
        }
    }

    private fun processWeatherData(weather: WeatherResponse) {
        val current = weather.current
        val daily = weather.daily

        // Build daily forecast (5 days)
        val dailyForecast = buildDailyForecast(daily)

        _uiState.update { state ->
            state.copy(
                isLoading = false,

                // Hero data
                heroImageUrl = WeatherImageMapper.getImageUrl(current?.weatherCode, current?.isDay),
                currentTemp = current?.temperature?.roundToInt()?.toString() ?: "--",
                currentCondition = WeatherImageMapper.getConditionText(current?.weatherCode),
                feelsLike = current?.apparentTemperature?.roundToInt()?.let { "Feels like ${it}°C" } ?: "",

                // Sun times
                sunriseTime = WeatherUtils.formatTime(daily?.sunrise?.firstOrNull()),
                sunsetTime = WeatherUtils.formatTime(daily?.sunset?.firstOrNull()),

                // Metrics
                humidity = WeatherUtils.formatHumidity(current?.humidity),
                windSpeed = WeatherUtils.formatWindSpeed(current?.windSpeed),
                visibility = WeatherUtils.formatVisibility(current?.visibility),
                pressure = WeatherUtils.formatPressure(current?.pressure),

                // Daily forecast
                dailyForecast = dailyForecast
            )
        }
    }

    private fun buildDailyForecast(daily: DailyBlock?): List<DailyForecastUi> {
        if (daily == null) return emptyList()

        val times = daily.time ?: return emptyList()
        val maxTemps = daily.tempMax ?: return emptyList()
        val minTemps = daily.tempMin ?: return emptyList()
        val weatherCodes = daily.weatherCodes ?: emptyList()
        val precipProbs = daily.precipitationProbMax ?: emptyList()

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val dayNameFormat = SimpleDateFormat("EEE", Locale.US)
        val monthDayFormat = SimpleDateFormat("MMM d", Locale.US)
        val today = Calendar.getInstance()

        // Take only first 5 days
        val count = minOf(5, times.size)

        return (0 until count).mapNotNull { index ->
            val timeStr = times.getOrNull(index) ?: return@mapNotNull null
            val highTemp = maxTemps.getOrNull(index)?.roundToInt() ?: return@mapNotNull null
            val lowTemp = minTemps.getOrNull(index)?.roundToInt() ?: return@mapNotNull null
            val weatherCode = weatherCodes.getOrNull(index)
            val precipProb = precipProbs.getOrNull(index)

            val date = try {
                dateFormat.parse(timeStr)
            } catch (e: Exception) {
                null
            }

            val isToday = date?.let {
                val cal = Calendar.getInstance().apply { time = it }
                cal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR) &&
                        cal.get(Calendar.YEAR) == today.get(Calendar.YEAR)
            } ?: false

            val dayName = when {
                isToday -> "Today"
                index == 1 -> "Tomorrow"
                else -> date?.let { dayNameFormat.format(it) } ?: "Day $index"
            }

            val dateStr = date?.let { monthDayFormat.format(it) } ?: ""

            DailyForecastUi(
                dayName = dayName,
                date = dateStr,
                highTemp = highTemp,
                lowTemp = lowTemp,
                condition = WeatherImageMapper.getConditionText(weatherCode),
                iconResId = getWeatherIconRes(weatherCode),
                precipitationProb = precipProb,
                isToday = isToday
            )
        }
    }

    private fun getWeatherIconRes(weatherCode: Int?): Int {
        return when (weatherCode) {
            0 -> R.drawable.ic_sun
            1, 2, 3 -> R.drawable.ic_sun
            45, 48 -> R.drawable.ic_sun
            51, 53, 55, 61, 63, 65, 80, 81, 82 -> R.drawable.ic_rain
            71, 73, 75, 77, 85, 86 -> R.drawable.ic_rain
            95, 96, 99 -> R.drawable.ic_rain
            else -> R.drawable.ic_sun
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}

/**
 * UI State for City Weather Screen
 */
data class CityWeatherUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,

    // Location info
    val cityName: String = "",
    val country: String = "",
    val countryCode: String? = null,

    // Hero data
    val heroImageUrl: String? = null,
    val currentTemp: String = "--",
    val currentCondition: String = "--",
    val feelsLike: String = "",

    // Sun times
    val sunriseTime: String = "--",
    val sunsetTime: String = "--",

    // Metrics
    val humidity: String = "--",
    val windSpeed: String = "--",
    val visibility: String = "--",
    val pressure: String = "--",

    // Daily forecast
    val dailyForecast: List<DailyForecastUi> = emptyList()
)