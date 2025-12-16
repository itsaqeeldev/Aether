package com.devsphere.aether.models

import com.devsphere.aether.data.local.entity.SavedLocationEntity

/**
 * UI model for displaying saved locations in RecyclerView
 * Includes expansion state for the detail card
 */
data class SavedLocationUi(
    val id: Int,
    val name: String,
    val country: String,
    val countryCode: String?,
    val latitude: Double,
    val longitude: Double,
    val timezone: String?,
    val admin1: String?,

    // Weather display
    val temperature: String,
    val condition: String,
    val weatherCode: Int?,
    val imageUrl: String?,

    // UI state
    val isExpanded: Boolean = false,
    val isLoading: Boolean = false
) {
    companion object {
        fun fromEntity(entity: SavedLocationEntity): SavedLocationUi {
            return SavedLocationUi(
                id = entity.id,
                name = entity.name,
                country = entity.country,
                countryCode = entity.countryCode,
                latitude = entity.latitude,
                longitude = entity.longitude,
                timezone = entity.timezone,
                admin1 = entity.admin1,
                temperature = entity.cachedTemp?.let { "${it}°" } ?: "--°",
                condition = entity.cachedCondition ?: "Loading...",
                weatherCode = entity.cachedWeatherCode,
                imageUrl = entity.cachedImageUrl
            )
        }
    }

    /**
     * Get formatted coordinates string
     */
    fun getFormattedCoordinates(): String {
        val latDir = if (latitude >= 0) "N" else "S"
        val lonDir = if (longitude >= 0) "E" else "W"
        return String.format("%.2f°%s, %.2f°%s",
            kotlin.math.abs(latitude), latDir,
            kotlin.math.abs(longitude), lonDir)
    }
}