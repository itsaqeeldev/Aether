package com.devsphere.aether.data.remote.api

import com.devsphere.aether.data.remote.dto.air.AirQualityResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface AirQualityApi {

    @GET("air-quality")
    suspend fun getAirQuality(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,

        // ✅ ADD THIS LINE
        @Query("current") current: String = "european_aqi",

        @Query("hourly") hourly: String =
            "pm10,pm2_5,nitrogen_dioxide,sulphur_dioxide,ozone,carbon_monoxide,european_aqi",

        @Query("timezone") timezone: String = "auto"
    ): AirQualityResponse

}
