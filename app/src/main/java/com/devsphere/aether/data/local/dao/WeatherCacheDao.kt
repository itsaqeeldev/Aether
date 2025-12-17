package com.devsphere.aether.data.local.dao

import androidx.room.*
import com.devsphere.aether.data.local.entity.WeatherCacheEntity
import com.devsphere.aether.data.local.entity.WhatToWearCacheEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WeatherCacheDao {

    // ==================== Weather Cache ====================

    @Query("SELECT * FROM weather_cache WHERE locationKey = :locationKey")
    suspend fun getWeatherCache(locationKey: String): WeatherCacheEntity?

    @Query("SELECT * FROM weather_cache WHERE locationKey = :locationKey")
    fun getWeatherCacheFlow(locationKey: String): Flow<WeatherCacheEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeatherCache(cache: WeatherCacheEntity)

    @Query("DELETE FROM weather_cache WHERE locationKey = :locationKey")
    suspend fun deleteWeatherCache(locationKey: String)

    @Query("DELETE FROM weather_cache WHERE timestamp < :cutoffTime")
    suspend fun deleteStaleWeatherCache(cutoffTime: Long)

    @Query("DELETE FROM weather_cache")
    suspend fun clearAllWeatherCache()

    // ==================== What To Wear Cache ====================

    @Query("SELECT * FROM what_to_wear_cache WHERE locationKey = :locationKey")
    suspend fun getWhatToWearCache(locationKey: String): WhatToWearCacheEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWhatToWearCache(cache: WhatToWearCacheEntity)

    @Query("DELETE FROM what_to_wear_cache WHERE locationKey = :locationKey")
    suspend fun deleteWhatToWearCache(locationKey: String)

    @Query("DELETE FROM what_to_wear_cache WHERE timestamp < :cutoffTime")
    suspend fun deleteStaleWhatToWearCache(cutoffTime: Long)

    @Query("DELETE FROM what_to_wear_cache")
    suspend fun clearAllWhatToWearCache()
}