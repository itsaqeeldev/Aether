package com.devsphere.aether.data.remote.api

import com.devsphere.aether.data.remote.dto.geocoding.GeocodingResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface GeocodingApi {

    /**
     * Search for locations by name
     *
     * @param name Location name to search for (city, town, etc.)
     * @param count Number of results to return (max 100)
     * @param language Result language (ISO 639-1 codes: "en", "de", "fr", etc.)
     * @param format Response format (default: "json")
     */
    @GET("search")
    suspend fun searchLocations(
        @Query("name") name: String,
        @Query("count") count: Int = 10,
        @Query("language") language: String = "en",
        @Query("format") format: String = "json"
    ): GeocodingResponse

    /**
     * Note: Open-Meteo Geocoding API does NOT support reverse geocoding
     * (converting coordinates to location names).
     *
     * For reverse geocoding, use one of these alternatives:
     * 1. Android's built-in Geocoder class (recommended, free, offline)
     * 2. Nominatim API (OpenStreetMap, free)
     * 3. Google Geocoding API (requires API key and billing)
     * 4. Mapbox Geocoding API (free tier available)
     *
     * Example using Android Geocoder:
     *
     * suspend fun getLocationName(lat: Double, lon: Double): String? = withContext(Dispatchers.IO) {
     *     try {
     *         val geocoder = Geocoder(context, Locale.getDefault())
     *         val addresses = geocoder.getFromLocation(lat, lon, 1)
     *         addresses?.firstOrNull()?.let { address ->
     *             address.locality ?: address.subAdminArea ?: address.adminArea ?: "Unknown"
     *         }
     *     } catch (e: Exception) {
     *         null
     *     }
     * }
     */
}