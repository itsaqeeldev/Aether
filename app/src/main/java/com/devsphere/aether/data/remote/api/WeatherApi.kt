package com.devsphere.aether.data.remote.api

import com.devsphere.aether.data.remote.dto.weather.WeatherResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApi {

    /**
     * Get comprehensive weather forecast including current, hourly, and daily data
     *
     * @param latitude Geographic coordinate (required)
     * @param longitude Geographic coordinate (required)
     * @param current Current weather variables (comma-separated)
     * @param hourly Hourly forecast variables (comma-separated)
     * @param daily Daily forecast variables (comma-separated)
     * @param timezone Timezone for time values (e.g., "auto", "America/New_York")
     * @param temperatureUnit Temperature unit: "celsius" or "fahrenheit"
     * @param windspeedUnit Wind speed unit: "kmh", "ms", "mph", or "kn"
     * @param precipitationUnit Precipitation unit: "mm" or "inch"
     * @param forecastDays Number of days to forecast (0-16)
     */
    @GET("forecast")
    suspend fun getWeather(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current") current: String =
            "temperature_2m,relative_humidity_2m,apparent_temperature,weather_code,wind_speed_10m,wind_direction_10m,pressure_msl,visibility,is_day,precipitation",

        @Query("hourly") hourly: String =
            "temperature_2m,relative_humidity_2m,precipitation_probability,weather_code,wind_speed_10m,visibility,uv_index,uv_index_clear_sky",
        @Query("daily") daily: String =
            "temperature_2m_max,temperature_2m_min,sunrise,sunset,uv_index_max,precipitation_sum,precipitation_probability_max,weather_code",
        @Query("timezone") timezone: String = "auto",
        @Query("temperature_unit") temperatureUnit: String = "celsius",
        @Query("windspeed_unit") windspeedUnit: String = "kmh",
        @Query("precipitation_unit") precipitationUnit: String = "mm",
        @Query("forecast_days") forecastDays: Int = 7
    ): WeatherResponse

    /**
     * Get minutely precipitation forecast (15-minute intervals)
     * Replaces the non-existent "nowcast" endpoint
     * Perfect for "Rain expected in X minutes" feature
     *
     * @param latitude Geographic coordinate (required)
     * @param longitude Geographic coordinate (required)
     * @param minutely Minutely variables (comma-separated)
     * @param forecastDays Number of days to forecast (1 recommended for minutely)
     * @param timezone Timezone for time values
     */
    @GET("forecast")
    suspend fun getMinutelyForecast(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("minutely_15") minutely: String = "precipitation,weather_code",
        @Query("forecast_days") forecastDays: Int = 1,
        @Query("timezone") timezone: String = "auto"
    ): WeatherResponse
}