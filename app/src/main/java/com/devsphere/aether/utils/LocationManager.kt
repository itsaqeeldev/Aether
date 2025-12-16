package com.devsphere.aether.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/**
 * Manager for handling device location detection
 */
@Singleton
class LocationManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    /**
     * Check if location permission is granted
     */
    fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Get current location
     * @return LocationResult with coordinates or error
     */
    suspend fun getCurrentLocation(): LocationResult {
        if (!hasLocationPermission()) {
            return LocationResult.Error("Location permission not granted")
        }

        return try {
            val location = getCurrentLocationInternal()
            if (location != null) {
                LocationResult.Success(
                    latitude = location.latitude,
                    longitude = location.longitude
                )
            } else {
                LocationResult.Error("Unable to determine location")
            }
        } catch (e: Exception) {
            LocationResult.Error("Location error: ${e.localizedMessage}")
        }
    }

    /**
     * Internal method to get location using FusedLocationProviderClient
     */
    private suspend fun getCurrentLocationInternal(): Location? =
        suspendCancellableCoroutine { continuation ->
            if (!hasLocationPermission()) {
                continuation.resume(null)
                return@suspendCancellableCoroutine
            }

            val cancellationTokenSource = CancellationTokenSource()

            try {
                fusedLocationClient.getCurrentLocation(
                    Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                    cancellationTokenSource.token
                ).addOnSuccessListener { location ->
                    continuation.resume(location)
                }.addOnFailureListener { exception ->
                    continuation.resume(null)
                }
            } catch (e: SecurityException) {
                continuation.resume(null)
            }

            continuation.invokeOnCancellation {
                cancellationTokenSource.cancel()
            }
        }

    /**
     * Get last known location (faster but may be outdated)
     */
    suspend fun getLastKnownLocation(): LocationResult {
        if (!hasLocationPermission()) {
            return LocationResult.Error("Location permission not granted")
        }

        return try {
            val location = getLastKnownLocationInternal()
            if (location != null) {
                LocationResult.Success(
                    latitude = location.latitude,
                    longitude = location.longitude
                )
            } else {
                // If no last known location, get current location
                getCurrentLocation()
            }
        } catch (e: Exception) {
            LocationResult.Error("Location error: ${e.localizedMessage}")
        }
    }

    /**
     * Internal method to get last known location
     */
    private suspend fun getLastKnownLocationInternal(): Location? =
        suspendCancellableCoroutine { continuation ->
            if (!hasLocationPermission()) {
                continuation.resume(null)
                return@suspendCancellableCoroutine
            }

            try {
                fusedLocationClient.lastLocation
                    .addOnSuccessListener { location ->
                        continuation.resume(location)
                    }
                    .addOnFailureListener {
                        continuation.resume(null)
                    }
            } catch (e: SecurityException) {
                continuation.resume(null)
            }
        }
}

/**
 * Sealed class representing location result
 */
sealed class LocationResult {
    data class Success(
        val latitude: Double,
        val longitude: Double
    ) : LocationResult()

    data class Error(val message: String) : LocationResult()
}