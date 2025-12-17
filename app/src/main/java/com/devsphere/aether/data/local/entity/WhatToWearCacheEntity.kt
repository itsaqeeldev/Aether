package com.devsphere.aether.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for caching What To Wear suggestions
 * Prevents re-generating suggestions on every fragment open
 */
@Entity(tableName = "what_to_wear_cache")
data class WhatToWearCacheEntity(
    @PrimaryKey
    val locationKey: String, // Format: "lat,lon" rounded to 4 decimals

    val latitude: Double,
    val longitude: Double,

    // Store suggestions as JSON
    val formalItemsJson: String,
    val casualItemsJson: String,
    val sportsItemsJson: String,
    val tipsJson: String,
    val activitiesJson: String,

    // Smart insight
    val insightTitle: String? = null,
    val insightMessage: String? = null,

    // Current weather summary for display
    val currentTemp: String,
    val currentCondition: String,
    val weatherIconCode: Int,

    val timestamp: Long = System.currentTimeMillis()
)