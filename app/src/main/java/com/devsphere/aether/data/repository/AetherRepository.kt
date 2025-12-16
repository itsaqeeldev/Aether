package com.devsphere.aether.data.repository

import com.devsphere.aether.data.remote.api.AirQualityApi
import com.devsphere.aether.data.remote.api.GeocodingApi
import com.devsphere.aether.data.remote.api.WeatherApi
import com.devsphere.aether.data.remote.dto.air.AirQualityResponse
import com.devsphere.aether.data.remote.dto.geocoding.GeocodingResponse
import com.devsphere.aether.data.remote.dto.weather.WeatherResponse
import com.devsphere.aether.network.ApiHandler
import com.devsphere.aether.network.ApiResult
import com.devsphere.aether.utils.LocationInfo
import com.devsphere.aether.utils.ReverseGeocoder
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AetherRepository @Inject constructor(
    private val weatherApi: WeatherApi,
    private val airQualityApi: AirQualityApi,
    private val geocodingApi: GeocodingApi,
    private val reverseGeocoder: ReverseGeocoder // ✅ Inject the new utility
) {

    // ... (Your existing getWeather implementation) ...
    suspend fun getWeather(
        latitude: Double,
        longitude: Double,
        timezone: String = "auto",
        temperatureUnit: String = "celsius",
        windspeedUnit: String = "kmh",
        forecastDays: Int = 7
    ): ApiResult<WeatherResponse> =
        ApiHandler.execute {
            weatherApi.getWeather(
                latitude = latitude,
                longitude = longitude,
                timezone = timezone,
                temperatureUnit = temperatureUnit,
                windspeedUnit = windspeedUnit,
                forecastDays = forecastDays
            )
        }

    // ... (Your existing getMinutelyForecast implementation) ...
    suspend fun getMinutelyForecast(
        latitude: Double,
        longitude: Double,
        timezone: String = "auto"
    ): ApiResult<WeatherResponse> =
        ApiHandler.execute {
            weatherApi.getMinutelyForecast(
                latitude = latitude,
                longitude = longitude,
                timezone = timezone
            )
        }

    // ... (Your existing getAirQuality implementation) ...
    suspend fun getAirQuality(
        latitude: Double,
        longitude: Double,
        timezone: String = "auto"
    ): ApiResult<AirQualityResponse> =
        ApiHandler.execute {
            airQualityApi.getAirQuality(
                latitude = latitude,
                longitude = longitude,
                timezone = timezone
            )
        }

    // ... (Your existing searchLocations implementation) ...
    suspend fun searchLocations(
        name: String,
        count: Int = 10,
        language: String = "en"
    ): ApiResult<GeocodingResponse> =
        ApiHandler.execute {
            geocodingApi.searchLocations(
                name = name,
                count = count,
                language = language
            )
        }

    /**
     * ✅ Integrated Reverse Geocoding using your new utility class
     */
    suspend fun reverseGeocode(
        latitude: Double,
        longitude: Double
    ): ApiResult<LocationInfo> {
        val result = reverseGeocoder.getLocationName(latitude, longitude)
        return if (result != null) {
            ApiResult.Success(result)
        } else {
            ApiResult.Error("Location not found",404, )
        }
    }
}