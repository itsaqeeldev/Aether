package com.devsphere.aether.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room Entity for saved/favorite locations
 * Maximum 3 locations can be saved
 */
@Entity(tableName = "saved_locations")
data class SavedLocationEntity(
    @PrimaryKey
    val id: Int,                    // Unique ID from geocoding API
    val name: String,               // City name
    val country: String,            // Country name
    val countryCode: String?,       // ISO country code (e.g., "US", "FR")
    val latitude: Double,
    val longitude: Double,
    val timezone: String?,
    val admin1: String?,            // State/Province
    val addedAt: Long = System.currentTimeMillis(),

    // Cached weather data (updated on each view)
    val cachedTemp: Int? = null,
    val cachedCondition: String? = null,
    val cachedWeatherCode: Int? = null,
    val cachedImageUrl: String? = null,
    val lastWeatherUpdate: Long? = null
)