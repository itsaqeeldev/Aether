package com.devsphere.aether.models

data class HourlyForecastUi(
    val time: String,        // "14:00" from Open-Meteo hourly.time
    val temperatureC: Int,   // temperature_2m
    val iconResId: Int       // mapped from Open-Meteo weather_code
)