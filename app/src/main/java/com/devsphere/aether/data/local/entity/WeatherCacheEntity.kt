package com.devsphere.aether.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for persistent weather data caching
 * Survives app restarts unlike in-memory WeatherStore
 */
@Entity(tableName = "weather_cache")
data class WeatherCacheEntity(
    @PrimaryKey
    val locationKey: String, // Format: "lat,lon" rounded to 4 decimals

    val latitude: Double,
    val longitude: Double,

    // Store as JSON string
    val weatherDataJson: String,

    val timestamp: Long = System.currentTimeMillis(),

    // For quick access without parsing JSON
    val currentTemp: Double? = null,
    val currentCondition: String? = null,
    val weatherCode: Int? = null
)

