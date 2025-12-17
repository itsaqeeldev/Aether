package com.devsphere.aether.di

import android.content.Context
import androidx.room.Room
import com.devsphere.aether.data.local.AetherDatabase
import com.devsphere.aether.data.local.dao.SavedLocationDao
import com.devsphere.aether.data.local.dao.WeatherCacheDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for providing database and DAOs
 *
 * ✅ Provides: AetherDatabase, SavedLocationDao, WeatherCacheDao
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAetherDatabase(
        @ApplicationContext context: Context
    ): AetherDatabase {
        return Room.databaseBuilder(
            context,
            AetherDatabase::class.java,
            "aether_db"
        )
            .fallbackToDestructiveMigration() // For development - removes data on schema change
            // .addMigrations(MIGRATION_1_2) // For production - use proper migration
            .build()
    }

    @Provides
    @Singleton
    fun provideSavedLocationDao(database: AetherDatabase): SavedLocationDao {
        return database.savedLocationDao()
    }

    /**
     * ✅ CRITICAL: This provider is required for WeatherStore and WhatToWearRepository
     */
    @Provides
    @Singleton
    fun provideWeatherCacheDao(database: AetherDatabase): WeatherCacheDao {
        return database.weatherCacheDao()
    }
}