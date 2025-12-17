package com.devsphere.aether.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.devsphere.aether.data.local.dao.SavedLocationDao
import com.devsphere.aether.data.local.dao.WeatherCacheDao
import com.devsphere.aether.data.local.entity.*

/**
 * Main Room Database for Aether app
 *
 * Version 2: Added WeatherCacheEntity and WhatToWearCacheEntity
 */
@Database(
    entities = [
        SavedLocationEntity::class,
        WeatherCacheEntity::class,
        WhatToWearCacheEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AetherDatabase : RoomDatabase() {
    abstract fun savedLocationDao(): SavedLocationDao
    abstract fun weatherCacheDao(): WeatherCacheDao
    companion object {
        const val DATABASE_NAME = "aether_database"
    }
}