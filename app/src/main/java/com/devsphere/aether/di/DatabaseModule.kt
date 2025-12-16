package com.devsphere.aether.di

import android.content.Context
import androidx.room.Room
import com.devsphere.aether.data.local.AetherDatabase
import com.devsphere.aether.data.local.dao.SavedLocationDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

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
            AetherDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideSavedLocationDao(database: AetherDatabase): SavedLocationDao {
        return database.savedLocationDao()
    }
}