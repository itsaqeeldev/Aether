package com.devsphere.aether.models

/**
 * Model for popular cities shown in the Add Location bottom sheet
 */
data class PopularCity(
    val id: Int,
    val name: String,
    val country: String,
    val countryCode: String,
    val latitude: Double,
    val longitude: Double,
    val timezone: String,

    // Live weather data (fetched when bottom sheet opens)
    var temperature: Int? = null,
    var isLoading: Boolean = true
) {
    companion object {
        /**
         * Hardcoded list of popular cities
         */
        fun getPopularCities(): List<PopularCity> = listOf(
            PopularCity(
                id = 2988507,
                name = "Paris",
                country = "France",
                countryCode = "FR",
                latitude = 48.8566,
                longitude = 2.3522,
                timezone = "Europe/Paris"
            ),
            PopularCity(
                id = 1850147,
                name = "Tokyo",
                country = "Japan",
                countryCode = "JP",
                latitude = 35.6762,
                longitude = 139.6503,
                timezone = "Asia/Tokyo"
            ),
            PopularCity(
                id = 292223,
                name = "Dubai",
                country = "UAE",
                countryCode = "AE",
                latitude = 25.2048,
                longitude = 55.2708,
                timezone = "Asia/Dubai"
            ),
            PopularCity(
                id = 2147714,
                name = "Sydney",
                country = "Australia",
                countryCode = "AU",
                latitude = -33.8688,
                longitude = 151.2093,
                timezone = "Australia/Sydney"
            ),
            PopularCity(
                id = 2950159,
                name = "Berlin",
                country = "Germany",
                countryCode = "DE",
                latitude = 52.5200,
                longitude = 13.4050,
                timezone = "Europe/Berlin"
            ),
            PopularCity(
                id = 5128581,
                name = "New York",
                country = "USA",
                countryCode = "US",
                latitude = 40.7128,
                longitude = -74.0060,
                timezone = "America/New_York"
            ),
            PopularCity(
                id = 2643743,
                name = "London",
                country = "UK",
                countryCode = "GB",
                latitude = 51.5074,
                longitude = -0.1278,
                timezone = "Europe/London"
            ),
            PopularCity(
                id = 1796236,
                name = "Shanghai",
                country = "China",
                countryCode = "CN",
                latitude = 31.2304,
                longitude = 121.4737,
                timezone = "Asia/Shanghai"
            )
        )
    }
}