package com.devsphere.aether.utils

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/**
 * Helper class for reverse geocoding (coordinates → location name)
 * Uses Android's built-in Geocoder which is:
 * - Free (no API key needed)
 * - Works offline with cached data
 * - Uses device's location service
 *
 * This replaces the non-existent reverse geocoding in Open-Meteo API
 */
@Singleton
class ReverseGeocoder @Inject constructor(
    @ApplicationContext private val context: Context
) {

    /**
     * Convert coordinates to a location name
     *
     * @param latitude Geographic latitude
     * @param longitude Geographic longitude
     * @param locale Locale for result (default: device locale)
     * @return Location name or null if geocoding fails
     */
    suspend fun getLocationName(
        latitude: Double,
        longitude: Double,
        locale: Locale = Locale.getDefault()
    ): LocationInfo? = withContext(Dispatchers.IO) {
        try {
            if (!Geocoder.isPresent()) {
                return@withContext null
            }

            val geocoder = Geocoder(context, locale)
            val addresses = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // Use new async API for Android 13+
                getAddressesAsync(geocoder, latitude, longitude)
            } else {
                // Use legacy synchronous API for older versions
                @Suppress("DEPRECATION")
                geocoder.getFromLocation(latitude, longitude, 1)
            }

            addresses?.firstOrNull()?.let { address ->
                LocationInfo(
                    cityName = address.locality ?: address.subAdminArea ?: address.adminArea,
                    countryName = address.countryName,
                    countryCode = address.countryCode,
                    adminArea = address.adminArea,
                    subAdminArea = address.subAdminArea,
                    postalCode = address.postalCode,
                    fullAddress = address.getAddressLine(0)
                )
            }
        } catch (e: IOException) {
            // Network error or service unavailable
            null
        } catch (e: IllegalArgumentException) {
            // Invalid coordinates
            null
        } catch (e: Exception) {
            // Other errors
            null
        }
    }

    /**
     * Get addresses using new async API (Android 13+)
     */
    private suspend fun getAddressesAsync(
        geocoder: Geocoder,
        latitude: Double,
        longitude: Double
    ): List<Address>? = suspendCancellableCoroutine { continuation ->
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            geocoder.getFromLocation(
                latitude,
                longitude,
                1
            ) { addresses ->
                continuation.resume(addresses)
            }
        } else {
            continuation.resume(null)
        }
    }

    /**
     * Get a simple formatted location string
     * Format: "City, Country" or "Region, Country" or just "Country"
     */
    suspend fun getSimpleLocationString(
        latitude: Double,
        longitude: Double
    ): String {
        val location = getLocationName(latitude, longitude) ?: return "Unknown Location"

        return when {
            location.cityName != null && location.countryName != null ->
                "${location.cityName}, ${location.countryName}"
            location.adminArea != null && location.countryName != null ->
                "${location.adminArea}, ${location.countryName}"
            location.countryName != null ->
                location.countryName
            else ->
                "Unknown Location"
        }
    }
}

/**
 * Data class representing location information from reverse geocoding
 */
data class LocationInfo(
    val cityName: String?,
    val countryName: String?,
    val countryCode: String?,
    val adminArea: String?,
    val subAdminArea: String?,
    val postalCode: String?,
    val fullAddress: String?
)