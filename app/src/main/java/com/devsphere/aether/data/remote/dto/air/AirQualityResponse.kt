package com.devsphere.aether.data.remote.dto.air

import com.google.gson.annotations.SerializedName

/**
 * Air Quality response from Open-Meteo Air Quality API
 * Endpoint: https://api.open-meteo.com/v1/air-quality
 *
 * Provides:
 * - Current air quality metrics
 * - Hourly air quality forecasts (up to 5 days)
 * - Various pollutant concentrations
 * - Air Quality Index (AQI) - European standard
 */
data class AirQualityResponse(
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

    // Current air quality
    @SerializedName("current")
    val current: AirQualityCurrentBlock?,

    // Hourly air quality forecast
    @SerializedName("hourly")
    val hourly: AirQualityHourlyBlock?
)

/**
 * Current air quality measurements
 */
data class AirQualityCurrentBlock(
    @SerializedName("time")
    val time: String?,  // ISO 8601 format

    @SerializedName("interval")
    val interval: Int?,

    // Particulate Matter
    @SerializedName("pm10")
    val pm10: Double?,  // Particulate Matter < 10 µm (μg/m³)

    @SerializedName("pm2_5")
    val pm25: Double?,  // Particulate Matter < 2.5 µm (μg/m³)

    // Gases
    @SerializedName("carbon_monoxide")
    val carbonMonoxide: Double?,  // CO (μg/m³)

    @SerializedName("nitrogen_dioxide")
    val nitrogenDioxide: Double?,  // NO₂ (μg/m³)

    @SerializedName("sulphur_dioxide")
    val sulphurDioxide: Double?,  // SO₂ (μg/m³)

    @SerializedName("ozone")
    val ozone: Double?,  // O₃ (μg/m³)

    // Additional pollutants
    @SerializedName("ammonia")
    val ammonia: Double?,  // NH₃ (μg/m³)

    @SerializedName("aerosol_optical_depth")
    val aerosolOpticalDepth: Double?,  // Aerosol Optical Depth at 550nm

    @SerializedName("dust")
    val dust: Double?,  // Dust concentration (μg/m³)

    // UV Index
    @SerializedName("uv_index")
    val uvIndex: Double?,

    @SerializedName("uv_index_clear_sky")
    val uvIndexClearSky: Double?,

    // Air Quality Indices
    @SerializedName("european_aqi")
    val europeanAqi: Int?,  // European AQI (0-100+)

    @SerializedName("european_aqi_pm2_5")
    val europeanAqiPm25: Int?,  // European AQI for PM2.5

    @SerializedName("european_aqi_pm10")
    val europeanAqiPm10: Int?,  // European AQI for PM10

    @SerializedName("european_aqi_nitrogen_dioxide")
    val europeanAqiNo2: Int?,  // European AQI for NO₂

    @SerializedName("european_aqi_ozone")
    val europeanAqiOzone: Int?,  // European AQI for O₃

    @SerializedName("european_aqi_sulphur_dioxide")
    val europeanAqiSo2: Int?,  // European AQI for SO₂

    // US AQI (if available)
    @SerializedName("us_aqi")
    val usAqi: Int?,  // US AQI (0-500)

    @SerializedName("us_aqi_pm2_5")
    val usAqiPm25: Int?,

    @SerializedName("us_aqi_pm10")
    val usAqiPm10: Int?,

    @SerializedName("us_aqi_nitrogen_dioxide")
    val usAqiNo2: Int?,

    @SerializedName("us_aqi_ozone")
    val usAqiOzone: Int?,

    @SerializedName("us_aqi_sulphur_dioxide")
    val usAqiSo2: Int?,

    @SerializedName("us_aqi_carbon_monoxide")
    val usAqiCo: Int?
)

/**
 * Hourly air quality forecast
 */
data class AirQualityHourlyBlock(
    val time: List<String>?,  // ISO 8601 timestamps

    // Particulate Matter
    @SerializedName("pm10")
    val pm10: List<Double>?,  // PM10 (μg/m³)

    @SerializedName("pm2_5")
    val pm25: List<Double>?,  // PM2.5 (μg/m³)

    // Gases
    @SerializedName("carbon_monoxide")
    val carbonMonoxide: List<Double>?,  // CO (μg/m³)

    @SerializedName("nitrogen_dioxide")
    val nitrogenDioxide: List<Double>?,  // NO₂ (μg/m³)

    @SerializedName("sulphur_dioxide")
    val sulphurDioxide: List<Double>?,  // SO₂ (μg/m³)

    @SerializedName("ozone")
    val ozone: List<Double>?,  // O₃ (μg/m³)

    // Additional pollutants
    @SerializedName("ammonia")
    val ammonia: List<Double>?,  // NH₃ (μg/m³)

    @SerializedName("aerosol_optical_depth")
    val aerosolOpticalDepth: List<Double>?,

    @SerializedName("dust")
    val dust: List<Double>?,

    // UV Index
    @SerializedName("uv_index")
    val uvIndex: List<Double>?,

    @SerializedName("uv_index_clear_sky")
    val uvIndexClearSky: List<Double>?,

    // Air Quality Indices - European
    @SerializedName("european_aqi")
    val europeanAqi: List<Int>?,

    @SerializedName("european_aqi_pm2_5")
    val europeanAqiPm25: List<Int>?,

    @SerializedName("european_aqi_pm10")
    val europeanAqiPm10: List<Int>?,

    @SerializedName("european_aqi_nitrogen_dioxide")
    val europeanAqiNo2: List<Int>?,

    @SerializedName("european_aqi_ozone")
    val europeanAqiOzone: List<Int>?,

    @SerializedName("european_aqi_sulphur_dioxide")
    val europeanAqiSo2: List<Int>?,

    // Air Quality Indices - US
    @SerializedName("us_aqi")
    val usAqi: List<Int>?,

    @SerializedName("us_aqi_pm2_5")
    val usAqiPm25: List<Int>?,

    @SerializedName("us_aqi_pm10")
    val usAqiPm10: List<Int>?,

    @SerializedName("us_aqi_nitrogen_dioxide")
    val usAqiNo2: List<Int>?,

    @SerializedName("us_aqi_ozone")
    val usAqiOzone: List<Int>?,

    @SerializedName("us_aqi_sulphur_dioxide")
    val usAqiSo2: List<Int>?,

    @SerializedName("us_aqi_carbon_monoxide")
    val usAqiCo: List<Int>?,

    // Pollen (seasonal availability)
    @SerializedName("alder_pollen")
    val alderPollen: List<Double>?,

    @SerializedName("birch_pollen")
    val birchPollen: List<Double>?,

    @SerializedName("grass_pollen")
    val grassPollen: List<Double>?,

    @SerializedName("mugwort_pollen")
    val mugwortPollen: List<Double>?,

    @SerializedName("olive_pollen")
    val olivePollen: List<Double>?,

    @SerializedName("ragweed_pollen")
    val ragweedPollen: List<Double>?
)

/**
 * Extension function to get AQI category and description
 */
fun Int?.getAqiCategory(): AqiCategory {
    return when (this) {
        null -> AqiCategory.UNKNOWN
        in 0..20 -> AqiCategory.GOOD
        in 21..40 -> AqiCategory.FAIR
        in 41..60 -> AqiCategory.MODERATE
        in 61..80 -> AqiCategory.POOR
        in 81..100 -> AqiCategory.VERY_POOR
        else -> AqiCategory.EXTREMELY_POOR
    }
}

/**
 * Air Quality Index categories (European standard)
 */
enum class AqiCategory(
    val label: String,
    val description: String,
    val colorHex: String,
    val healthAdvice: String
) {
    GOOD(
        label = "Good",
        description = "Air quality is excellent",
        colorHex = "#50F0E6",
        healthAdvice = "Perfect conditions for outdoor activities"
    ),
    FAIR(
        label = "Fair",
        description = "Air quality is acceptable",
        colorHex = "#50CCAA",
        healthAdvice = "Enjoy outdoor activities"
    ),
    MODERATE(
        label = "Moderate",
        description = "Air quality is acceptable for most",
        colorHex = "#F0E641",
        healthAdvice = "Sensitive individuals should consider reducing prolonged outdoor exertion"
    ),
    POOR(
        label = "Poor",
        description = "Some pollutants may affect sensitive groups",
        colorHex = "#FF5050",
        healthAdvice = "Sensitive groups should reduce outdoor activities"
    ),
    VERY_POOR(
        label = "Very Poor",
        description = "Health effects may be experienced by all",
        colorHex = "#960032",
        healthAdvice = "Everyone should reduce outdoor activities"
    ),
    EXTREMELY_POOR(
        label = "Extremely Poor",
        description = "Serious health effects for everyone",
        colorHex = "#7D2181",
        healthAdvice = "Avoid outdoor activities"
    ),
    UNKNOWN(
        label = "Unknown",
        description = "No data available",
        colorHex = "#9E9E9E",
        healthAdvice = "Unable to provide advice"
    )
}

/**
 * Extension function to get US AQI category
 */
fun Int?.getUsAqiCategory(): UsAqiCategory {
    return when (this) {
        null -> UsAqiCategory.UNKNOWN
        in 0..50 -> UsAqiCategory.GOOD
        in 51..100 -> UsAqiCategory.MODERATE
        in 101..150 -> UsAqiCategory.UNHEALTHY_SENSITIVE
        in 151..200 -> UsAqiCategory.UNHEALTHY
        in 201..300 -> UsAqiCategory.VERY_UNHEALTHY
        else -> UsAqiCategory.HAZARDOUS
    }
}

/**
 * US Air Quality Index categories
 */
enum class UsAqiCategory(
    val label: String,
    val description: String,
    val colorHex: String
) {
    GOOD("Good", "Air quality is satisfactory", "#00E400"),
    MODERATE("Moderate", "Acceptable for most people", "#FFFF00"),
    UNHEALTHY_SENSITIVE("Unhealthy for Sensitive Groups", "Sensitive groups may experience health effects", "#FF7E00"),
    UNHEALTHY("Unhealthy", "Everyone may begin to experience health effects", "#FF0000"),
    VERY_UNHEALTHY("Very Unhealthy", "Health alert: everyone may experience serious effects", "#8F3F97"),
    HAZARDOUS("Hazardous", "Health warnings of emergency conditions", "#7E0023"),
    UNKNOWN("Unknown", "No data available", "#9E9E9E")
}