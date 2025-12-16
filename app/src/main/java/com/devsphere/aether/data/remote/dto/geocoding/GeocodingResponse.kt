package com.devsphere.aether.data.remote.dto.geocoding

import com.google.gson.annotations.SerializedName

/**
 * Geocoding response from Open-Meteo Geocoding API
 * Endpoint: https://geocoding-api.open-meteo.com/v1/search
 *
 * Provides:
 * - Location search by name
 * - Coordinates (latitude, longitude)
 * - Administrative divisions
 * - Country information
 * - Timezone
 * - Population data
 *
 * Note: Reverse geocoding (coordinates → name) is NOT supported.
 * Use Android's Geocoder class for reverse geocoding instead.
 */
data class GeocodingResponse(
    val results: List<GeocodingResult>?,

    @SerializedName("generationtime_ms")
    val generationTimeMs: Double?
)

/**
 * Individual location result from geocoding search
 */
data class GeocodingResult(
    // Unique identifier for the location
    val id: Int?,

    // Location name (city, town, etc.)
    val name: String?,

    // Coordinates
    val latitude: Double?,
    val longitude: Double?,

    // Elevation above sea level in meters
    val elevation: Double?,

    // Feature code (type of location)
    // Examples: "PPLA" (seat of first-order admin division),
    //           "PPLC" (capital), "PPL" (populated place)
    @SerializedName("feature_code")
    val featureCode: String?,

    // Country information
    val country: String?,  // Full country name

    @SerializedName("country_code")
    val countryCode: String?,  // ISO 3166-1 alpha-2 country code (e.g., "US", "GB", "DE")

    @SerializedName("country_id")
    val countryId: Int?,  // Unique country identifier

    // Administrative divisions (hierarchical)
    @SerializedName("admin1")
    val admin1: String?,  // First-level admin division (e.g., state, province)

    @SerializedName("admin2")
    val admin2: String?,  // Second-level admin division (e.g., county)

    @SerializedName("admin3")
    val admin3: String?,  // Third-level admin division

    @SerializedName("admin4")
    val admin4: String?,  // Fourth-level admin division

    @SerializedName("admin1_id")
    val admin1Id: Int?,

    @SerializedName("admin2_id")
    val admin2Id: Int?,

    @SerializedName("admin3_id")
    val admin3Id: Int?,

    @SerializedName("admin4_id")
    val admin4Id: Int?,

    // Timezone
    val timezone: String?,  // IANA timezone identifier (e.g., "America/New_York")

    // Population
    val population: Long?,  // Population of the location

    // Postal codes
    val postcodes: List<String>?  // List of postal/ZIP codes for this location
)

/**
 * Extension function to get a formatted display name
 * Format: "City, State, Country" or "City, Country"
 */
fun GeocodingResult.getDisplayName(): String {
    val parts = mutableListOf<String>()

    name?.let { parts.add(it) }
    admin1?.let { parts.add(it) }
    country?.let { parts.add(it) }

    return if (parts.isNotEmpty()) {
        parts.joinToString(", ")
    } else {
        "Unknown Location"
    }
}

/**
 * Extension function to get a short display name
 * Format: "City, Country" or "City, State" (for large countries)
 */
fun GeocodingResult.getShortDisplayName(): String {
    val cityName = name ?: "Unknown"

    // For large countries, show state/province
    val showAdmin1 = countryCode in listOf("US", "CA", "AU", "BR", "CN", "IN", "RU")

    return when {
        showAdmin1 && admin1 != null -> "$cityName, $admin1"
        country != null -> "$cityName, $country"
        else -> cityName
    }
}

/**
 * Extension function to check if location is a capital city
 */
fun GeocodingResult.isCapital(): Boolean {
    return featureCode == "PPLC"
}

/**
 * Extension function to get location type description
 */
fun GeocodingResult.getLocationTypeDescription(): String {
    return when (featureCode) {
        "PPLC" -> "Capital City"
        "PPLA" -> "State/Province Capital"
        "PPLA2" -> "County Capital"
        "PPLA3" -> "Third-order Administrative Capital"
        "PPLA4" -> "Fourth-order Administrative Capital"
        "PPL" -> "Populated Place"
        "PPLX" -> "Section of Populated Place"
        "AIRP" -> "Airport"
        "MT" -> "Mountain"
        "ISL" -> "Island"
        "LK" -> "Lake"
        "STM" -> "Stream"
        else -> "Location"
    }
}

/**
 * Extension function to format population
 */
fun Long?.formatPopulation(): String {
    return when {
        this == null -> "Unknown"
        this >= 1_000_000 -> String.format("%.1fM", this / 1_000_000.0)
        this >= 1_000 -> String.format("%.1fK", this / 1_000.0)
        else -> this.toString()
    }
}

/**
 * Extension function to get region name (admin1 with fallback)
 */
fun GeocodingResult.getRegionName(): String? {
    return admin1 ?: admin2 ?: admin3
}

/**
 * Extension function to validate coordinates
 */
fun GeocodingResult.hasValidCoordinates(): Boolean {
    return latitude != null && longitude != null &&
            latitude >= -90.0 && latitude <= 90.0 &&
            longitude >= -180.0 && longitude <= 180.0
}

/**
 * Extension function to convert to a simpler Location model
 * (if you have a domain Location model)
 */
data class SimpleLocation(
    val id: Int,
    val name: String,
    val displayName: String,
    val latitude: Double,
    val longitude: Double,
    val country: String?,
    val countryCode: String?,
    val timezone: String?
)

fun GeocodingResult.toSimpleLocation(): SimpleLocation? {
    return if (id != null && name != null && latitude != null && longitude != null) {
        SimpleLocation(
            id = id,
            name = name,
            displayName = getDisplayName(),
            latitude = latitude,
            longitude = longitude,
            country = country,
            countryCode = countryCode,
            timezone = timezone
        )
    } else {
        null
    }
}