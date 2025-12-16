package com.devsphere.aether.data.remote.dto.weather

import com.google.gson.annotations.SerializedName

/**
 * Main weather response from Open-Meteo Forecast API
 * Endpoint: https://api.open-meteo.com/v1/forecast
 *
 * Supports:
 * - Current weather conditions
 * - Hourly forecasts (up to 16 days)
 * - Daily forecasts (up to 16 days)
 * - Minutely precipitation (15-minute intervals, up to 24 hours)
 */
data class WeatherResponse(
    val latitude: Double,
    val longitude: Double,

    @SerializedName("generationtime_ms")
    val generationTimeMs: Double?,

    @SerializedName("utc_offset_seconds")
    val utcOffsetSeconds: Int?,

    val timezone: String,

    @SerializedName("timezone_abbreviation")
    val timezoneAbbreviation: String?,

    val elevation: Double?,

    // Current weather block
    @SerializedName("current")
    val current: CurrentBlock?,

    // Hourly forecast block
    @SerializedName("hourly")
    val hourly: HourlyBlock?,

    // Daily forecast block
    @SerializedName("daily")
    val daily: DailyBlock?,

    // Minutely precipitation (15-minute intervals)
    // Use this for "Rain expected in X minutes" feature
    @SerializedName("minutely_15")
    val minutely: MinutelyBlock?,

    // Current weather (legacy format, available with current_weather=true param)
    @SerializedName("current_weather")
    val currentWeather: CurrentWeatherLegacy?
)

/**
 * Current weather conditions
 * Provides real-time weather data
 */
data class CurrentBlock(
    @SerializedName("time")
    val time: String?,  // ISO 8601 format

    @SerializedName("interval")
    val interval: Int?,  // Update interval in seconds

    // Temperature
    @SerializedName("temperature_2m")
    val temperature: Double?,  // Temperature at 2 meters height (°C or °F)

    @SerializedName("apparent_temperature")
    val apparentTemperature: Double?,  // Feels like temperature

    // Humidity
    @SerializedName("relative_humidity_2m")
    val humidity: Double?,  // Relative humidity (%)

    @SerializedName("dew_point_2m")
    val dewPoint: Double?,  // Dew point temperature

    // Precipitation
    @SerializedName("precipitation")
    val precipitation: Double?,  // Total precipitation (mm)

    @SerializedName("rain")
    val rain: Double?,  // Rain (mm)

    @SerializedName("showers")
    val showers: Double?,  // Showers (mm)

    @SerializedName("snowfall")
    val snowfall: Double?,  // Snowfall (cm)

    @SerializedName("snow_depth")
    val snowDepth: Double?,  // Snow depth on ground (m)

    // Weather condition
    @SerializedName("weather_code")
    val weatherCode: Int?,  // WMO weather code

    // Cloud cover
    @SerializedName("cloud_cover")
    val cloudCover: Double?,  // Total cloud cover (%)

    @SerializedName("cloud_cover_low")
    val cloudCoverLow: Double?,  // Low level clouds (%)

    @SerializedName("cloud_cover_mid")
    val cloudCoverMid: Double?,  // Mid level clouds (%)

    @SerializedName("cloud_cover_high")
    val cloudCoverHigh: Double?,  // High level clouds (%)

    // Pressure
    @SerializedName("pressure_msl")
    val pressure: Double?,  // Pressure at sea level (hPa)

    @SerializedName("surface_pressure")
    val surfacePressure: Double?,  // Surface pressure (hPa)

    // Wind
    @SerializedName("wind_speed_10m")
    val windSpeed: Double?,  // Wind speed at 10m (km/h, m/s, mph, or kn)

    @SerializedName("wind_direction_10m")
    val windDirection: Double?,  // Wind direction (degrees)

    @SerializedName("wind_gusts_10m")
    val windGusts: Double?,  // Wind gusts

    // Visibility
    @SerializedName("visibility")
    val visibility: Double?,  // Visibility (meters)

    // Solar radiation
    @SerializedName("shortwave_radiation")
    val shortwaveRadiation: Double?,  // Shortwave solar radiation (W/m²)

    @SerializedName("direct_radiation")
    val directRadiation: Double?,  // Direct solar radiation (W/m²)

    @SerializedName("diffuse_radiation")
    val diffuseRadiation: Double?,  // Diffuse solar radiation (W/m²)

    // Additional
    @SerializedName("is_day")
    val isDay: Int?,  // 1 = day, 0 = night

    @SerializedName("cape")
    val cape: Double?,  // Convective Available Potential Energy (J/kg)

    @SerializedName("evapotranspiration")
    val evapotranspiration: Double?,  // Evapotranspiration (mm)

    @SerializedName("et0_fao_evapotranspiration")
    val et0FaoEvapotranspiration: Double?  // Reference evapotranspiration (mm)
)

/**
 * Hourly forecast data
 * Provides detailed forecast for each hour
 */
data class HourlyBlock(
    val time: List<String>?,  // ISO 8601 timestamps

    // Temperature
    @SerializedName("temperature_2m")
    val temperatures: List<Double>?,

    @SerializedName("apparent_temperature")
    val apparentTemperatures: List<Double>?,

    // Humidity
    @SerializedName("relative_humidity_2m")
    val humidities: List<Double>?,

    @SerializedName("dew_point_2m")
    val dewPoints: List<Double>?,

    // Precipitation
    @SerializedName("precipitation")
    val precipitations: List<Double>?,

    @SerializedName("precipitation_probability")
    val precipitationProbabilities: List<Int>?,

    @SerializedName("rain")
    val rain: List<Double>?,

    @SerializedName("showers")
    val showers: List<Double>?,

    @SerializedName("snowfall")
    val snowfall: List<Double>?,

    @SerializedName("snow_depth")
    val snowDepth: List<Double>?,

    // Weather condition
    @SerializedName("weather_code")
    val weatherCodes: List<Int>?,

    // Cloud cover
    @SerializedName("cloud_cover")
    val cloudCover: List<Double>?,

    @SerializedName("cloud_cover_low")
    val cloudCoverLow: List<Double>?,

    @SerializedName("cloud_cover_mid")
    val cloudCoverMid: List<Double>?,

    @SerializedName("cloud_cover_high")
    val cloudCoverHigh: List<Double>?,

    // Pressure
    @SerializedName("pressure_msl")
    val pressures: List<Double>?,

    @SerializedName("surface_pressure")
    val surfacePressures: List<Double>?,

    // Wind
    @SerializedName("wind_speed_10m")
    val windSpeeds: List<Double>?,

    @SerializedName("wind_direction_10m")
    val windDirections: List<Double>?,

    @SerializedName("wind_gusts_10m")
    val windGusts: List<Double>?,

    // Visibility
    @SerializedName("visibility")
    val visibilities: List<Double>?,

    // UV Index
    @SerializedName("uv_index")
    val uvIndex: List<Double>?,

    @SerializedName("uv_index_clear_sky")
    val uvIndexClearSky: List<Double>?,

    // Solar radiation
    @SerializedName("shortwave_radiation")
    val shortwaveRadiation: List<Double>?,

    @SerializedName("direct_radiation")
    val directRadiation: List<Double>?,

    @SerializedName("diffuse_radiation")
    val diffuseRadiation: List<Double>?,

    // Additional
    @SerializedName("is_day")
    val isDay: List<Int>?,

    @SerializedName("cape")
    val cape: List<Double>?,

    @SerializedName("freezing_level_height")
    val freezingLevelHeight: List<Double>?,

    @SerializedName("soil_temperature_0cm")
    val soilTemperature0cm: List<Double>?,

    @SerializedName("soil_moisture_0_to_1cm")
    val soilMoisture0To1cm: List<Double>?
)

/**
 * Daily forecast data
 * Provides summary for each day
 */
data class DailyBlock(
    val time: List<String>?,  // ISO 8601 dates (YYYY-MM-DD)

    // Temperature
    @SerializedName("temperature_2m_max")
    val tempMax: List<Double>?,

    @SerializedName("temperature_2m_min")
    val tempMin: List<Double>?,

    @SerializedName("apparent_temperature_max")
    val apparentTempMax: List<Double>?,

    @SerializedName("apparent_temperature_min")
    val apparentTempMin: List<Double>?,

    // Precipitation
    @SerializedName("precipitation_sum")
    val precipitationSum: List<Double>?,

    @SerializedName("precipitation_hours")
    val precipitationHours: List<Double>?,

    @SerializedName("precipitation_probability_max")
    val precipitationProbMax: List<Int>?,

    @SerializedName("precipitation_probability_min")
    val precipitationProbMin: List<Int>?,

    @SerializedName("precipitation_probability_mean")
    val precipitationProbMean: List<Int>?,

    @SerializedName("rain_sum")
    val rainSum: List<Double>?,

    @SerializedName("showers_sum")
    val showersSum: List<Double>?,

    @SerializedName("snowfall_sum")
    val snowfallSum: List<Double>?,

    // Weather condition
    @SerializedName("weather_code")
    val weatherCodes: List<Int>?,

    // Sun
    val sunrise: List<String>?,  // ISO 8601 timestamps

    val sunset: List<String>?,  // ISO 8601 timestamps

    @SerializedName("daylight_duration")
    val daylightDuration: List<Double>?,  // Seconds

    @SerializedName("sunshine_duration")
    val sunshineDuration: List<Double>?,  // Seconds

    // UV Index
    @SerializedName("uv_index_max")
    val uvIndexMax: List<Double>?,

    @SerializedName("uv_index_clear_sky_max")
    val uvIndexClearSkyMax: List<Double>?,

    // Wind
    @SerializedName("wind_speed_10m_max")
    val windSpeedMax: List<Double>?,

    @SerializedName("wind_gusts_10m_max")
    val windGustsMax: List<Double>?,

    @SerializedName("wind_direction_10m_dominant")
    val windDirectionDominant: List<Double>?,

    // Solar radiation
    @SerializedName("shortwave_radiation_sum")
    val shortwaveRadiationSum: List<Double>?,

    // Additional
    @SerializedName("et0_fao_evapotranspiration")
    val et0FaoEvapotranspiration: List<Double>?
)

/**
 * Minutely precipitation forecast (15-minute intervals)
 * Perfect for "Rain expected in X minutes" feature
 * Available for up to 24 hours
 */
data class MinutelyBlock(
    val time: List<String>?,  // ISO 8601 timestamps at 15-minute intervals

    @SerializedName("precipitation")
    val precipitation: List<Double>?,  // Precipitation in mm

    @SerializedName("weather_code")
    val weatherCodes: List<Int>?  // WMO weather codes
)

/**
 * Legacy current weather format
 * Available when using current_weather=true parameter
 * Simpler format but less data than CurrentBlock
 */
data class CurrentWeatherLegacy(
    val time: String?,  // ISO 8601

    @SerializedName("temperature")
    val temperature: Double?,

    @SerializedName("weathercode")
    val weatherCode: Int?,

    @SerializedName("windspeed")
    val windSpeed: Double?,

    @SerializedName("winddirection")
    val windDirection: Double?,

    @SerializedName("is_day")
    val isDay: Int?
)