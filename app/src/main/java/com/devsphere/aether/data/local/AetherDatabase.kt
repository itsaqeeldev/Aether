package com.devsphere.aether.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.devsphere.aether.data.local.dao.SavedLocationDao
import com.devsphere.aether.data.local.entity.SavedLocationEntity

@Database(
    entities = [SavedLocationEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AetherDatabase : RoomDatabase() {

    abstract fun savedLocationDao(): SavedLocationDao

    companion object {
        const val DATABASE_NAME = "aether_database"
    }
}