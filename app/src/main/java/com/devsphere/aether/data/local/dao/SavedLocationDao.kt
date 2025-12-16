package com.devsphere.aether.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.devsphere.aether.data.local.entity.SavedLocationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedLocationDao {

    /**
     * Get all saved locations ordered by when they were added
     */
    @Query("SELECT * FROM saved_locations ORDER BY addedAt DESC")
    fun getAllLocations(): Flow<List<SavedLocationEntity>>

    /**
     * Get all saved locations as a one-shot query (for validation)
     */
    @Query("SELECT * FROM saved_locations ORDER BY addedAt DESC")
    suspend fun getAllLocationsOnce(): List<SavedLocationEntity>

    /**
     * Get count of saved locations
     */
    @Query("SELECT COUNT(*) FROM saved_locations")
    suspend fun getLocationCount(): Int

    /**
     * Get a specific location by ID
     */
    @Query("SELECT * FROM saved_locations WHERE id = :locationId")
    suspend fun getLocationById(locationId: Int): SavedLocationEntity?

    /**
     * Check if a location exists
     */
    @Query("SELECT EXISTS(SELECT 1 FROM saved_locations WHERE id = :locationId)")
    suspend fun locationExists(locationId: Int): Boolean

    /**
     * Insert a new location
     * Will replace if already exists
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocation(location: SavedLocationEntity)

    /**
     * Update an existing location (e.g., for cached weather)
     */
    @Update
    suspend fun updateLocation(location: SavedLocationEntity)

    /**
     * Delete a location
     */
    @Delete
    suspend fun deleteLocation(location: SavedLocationEntity)

    /**
     * Delete a location by ID
     */
    @Query("DELETE FROM saved_locations WHERE id = :locationId")
    suspend fun deleteLocationById(locationId: Int)

    /**
     * Delete all locations
     */
    @Query("DELETE FROM saved_locations")
    suspend fun deleteAllLocations()

    /**
     * Update cached weather for a location
     */
    @Query("""
        UPDATE saved_locations 
        SET cachedTemp = :temp, 
            cachedCondition = :condition, 
            cachedWeatherCode = :weatherCode,
            cachedImageUrl = :imageUrl,
            lastWeatherUpdate = :updateTime
        WHERE id = :locationId
    """)
    suspend fun updateCachedWeather(
        locationId: Int,
        temp: Int?,
        condition: String?,
        weatherCode: Int?,
        imageUrl: String?,
        updateTime: Long
    )
}