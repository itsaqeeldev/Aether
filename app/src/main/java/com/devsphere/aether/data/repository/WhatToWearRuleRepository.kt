package com.devsphere.aether.data.repository

import com.devsphere.aether.models.*
import com.devsphere.aether.utils.RuleCondition
import kotlin.random.Random

/**
 * Repository containing all rules for wearable items, tips, activities, insights, and mood
 */
object WhatToWearRuleRepository {

    // ==================== WEARABLE ITEMS RULES ====================

    private data class WearableItemRule(
        val item: WearableItem,
        val condition: RuleCondition
    )

    private val wearableItemRules = listOf(
        // FORMAL - Very Cold
        WearableItemRule(
            WearableItem("Heavy Overcoat", WearCategory.FORMAL, 10),
            { it.tempRange == TempRange.VERY_COLD }
        ),
        WearableItemRule(
            WearableItem("Wool Suit", WearCategory.FORMAL, 9),
            { it.tempRange == TempRange.VERY_COLD || it.tempRange == TempRange.COLD }
        ),
        WearableItemRule(
            WearableItem("Dress Scarf", WearCategory.FORMAL, 8),
            { it.tempRange == TempRange.VERY_COLD || it.tempRange == TempRange.COLD }
        ),
        WearableItemRule(
            WearableItem("Leather Gloves", WearCategory.FORMAL, 7),
            { it.tempRange == TempRange.VERY_COLD || it.tempRange == TempRange.COLD }
        ),

        // FORMAL - Cold/Cool
        WearableItemRule(
            WearableItem("Blazer", WearCategory.FORMAL, 8),
            { it.tempRange == TempRange.COLD || it.tempRange == TempRange.COOL }
        ),
        WearableItemRule(
            WearableItem("Suit Jacket", WearCategory.FORMAL, 8),
            { it.tempRange == TempRange.COOL || it.tempRange == TempRange.MILD }
        ),
        WearableItemRule(
            WearableItem("Dress Shirt", WearCategory.FORMAL, 7),
            { it.tempRange in listOf(TempRange.COOL, TempRange.MILD, TempRange.WARM) }
        ),
        WearableItemRule(
            WearableItem("Tie", WearCategory.FORMAL, 6),
            { it.tempCurrent < 30 }
        ),
        WearableItemRule(
            WearableItem("Dress Pants", WearCategory.FORMAL, 7),
            { true } // Always applicable
        ),

        // FORMAL - Warm/Hot
        WearableItemRule(
            WearableItem("Light Dress Shirt", WearCategory.FORMAL, 8),
            { it.tempRange == TempRange.WARM || it.tempRange == TempRange.HOT }
        ),
        WearableItemRule(
            WearableItem("Linen Suit", WearCategory.FORMAL, 8),
            { it.tempRange == TempRange.HOT }
        ),

        // FORMAL - Weather Specific
        WearableItemRule(
            WearableItem("Formal Umbrella", WearCategory.FORMAL, 9),
            { it.conditionType in listOf(WeatherConditionType.RAIN, WeatherConditionType.DRIZZLE) }
        ),
        WearableItemRule(
            WearableItem("Trench Coat", WearCategory.FORMAL, 9),
            { it.conditionType == WeatherConditionType.RAIN && it.tempCurrent < 20 }
        ),

        // CASUAL - Very Cold
        WearableItemRule(
            WearableItem("Heavy Winter Jacket", WearCategory.CASUAL, 10),
            { it.tempRange == TempRange.VERY_COLD }
        ),
        WearableItemRule(
            WearableItem("Thermal Wear", WearCategory.CASUAL, 9),
            { it.tempRange == TempRange.VERY_COLD }
        ),
        WearableItemRule(
            WearableItem("Beanie", WearCategory.CASUAL, 8),
            { it.tempRange == TempRange.VERY_COLD || it.tempRange == TempRange.COLD }
        ),
        WearableItemRule(
            WearableItem("Wool Scarf", WearCategory.CASUAL, 8),
            { it.tempRange == TempRange.VERY_COLD || it.tempRange == TempRange.COLD }
        ),
        WearableItemRule(
            WearableItem("Winter Gloves", WearCategory.CASUAL, 7),
            { it.tempRange == TempRange.VERY_COLD || it.tempRange == TempRange.COLD }
        ),
        WearableItemRule(
            WearableItem("Winter Boots", WearCategory.CASUAL, 8),
            { it.tempRange == TempRange.VERY_COLD || it.conditionType == WeatherConditionType.SNOW }
        ),

        // CASUAL - Cold
        WearableItemRule(
            WearableItem("Jacket", WearCategory.CASUAL, 8),
            { it.tempRange == TempRange.COLD || it.tempRange == TempRange.COOL }
        ),
        WearableItemRule(
            WearableItem("Sweater", WearCategory.CASUAL, 7),
            { it.tempRange in listOf(TempRange.COLD, TempRange.COOL) }
        ),
        WearableItemRule(
            WearableItem("Hoodie", WearCategory.CASUAL, 7),
            { it.tempRange in listOf(TempRange.COLD, TempRange.COOL, TempRange.MILD) }
        ),
        WearableItemRule(
            WearableItem("Jeans", WearCategory.CASUAL, 6),
            { it.tempCurrent < 28 }
        ),
        WearableItemRule(
            WearableItem("Long Pants", WearCategory.CASUAL, 6),
            { it.tempCurrent < 25 }
        ),

        // CASUAL - Cool/Mild
        WearableItemRule(
            WearableItem("Light Jacket", WearCategory.CASUAL, 7),
            { it.tempRange == TempRange.COOL || (it.tempRange == TempRange.MILD && it.windLevel >= WindLevel.MODERATE) }
        ),
        WearableItemRule(
            WearableItem("Long Sleeve Shirt", WearCategory.CASUAL, 6),
            { it.tempRange in listOf(TempRange.COOL, TempRange.MILD) }
        ),
        WearableItemRule(
            WearableItem("T-Shirt", WearCategory.CASUAL, 7),
            { it.tempRange in listOf(TempRange.MILD, TempRange.WARM, TempRange.HOT) }
        ),

        // CASUAL - Warm/Hot
        WearableItemRule(
            WearableItem("Shorts", WearCategory.CASUAL, 8),
            { it.tempRange == TempRange.WARM || it.tempRange == TempRange.HOT }
        ),
        WearableItemRule(
            WearableItem("Tank Top", WearCategory.CASUAL, 7),
            { it.tempRange == TempRange.HOT }
        ),
        WearableItemRule(
            WearableItem("Sunglasses", WearCategory.CASUAL, 8),
            { it.isDaytime && (it.conditionType == WeatherConditionType.CLEAR || it.uvLevel >= UVLevel.HIGH) }
        ),
        WearableItemRule(
            WearableItem("Cap", WearCategory.CASUAL, 7),
            { it.isDaytime && it.tempRange in listOf(TempRange.WARM, TempRange.HOT) }
        ),
        WearableItemRule(
            WearableItem("Sandals", WearCategory.CASUAL, 6),
            { it.tempRange == TempRange.HOT && it.conditionType != WeatherConditionType.RAIN }
        ),
        WearableItemRule(
            WearableItem("Light Cotton Clothes", WearCategory.CASUAL, 7),
            { it.tempRange == TempRange.HOT || (it.tempRange == TempRange.WARM && it.isHighHumidity) }
        ),

        // CASUAL - Weather Specific
        WearableItemRule(
            WearableItem("Raincoat", WearCategory.CASUAL, 9),
            { it.conditionType in listOf(WeatherConditionType.RAIN, WeatherConditionType.DRIZZLE, WeatherConditionType.THUNDERSTORM) }
        ),
        WearableItemRule(
            WearableItem("Umbrella", WearCategory.CASUAL, 9),
            { it.conditionType in listOf(WeatherConditionType.RAIN, WeatherConditionType.DRIZZLE) || it.precipLevel >= PrecipitationLevel.MODERATE }
        ),
        WearableItemRule(
            WearableItem("Waterproof Shoes", WearCategory.CASUAL, 8),
            { it.conditionType in listOf(WeatherConditionType.RAIN, WeatherConditionType.SNOW) }
        ),
        WearableItemRule(
            WearableItem("Face Mask", WearCategory.CASUAL, 9),
            { it.aqiLevel >= AQILevel.UNHEALTHY_SG }
        ),

        // SPORTS - Very Cold/Cold
        WearableItemRule(
            WearableItem("Thermal Running Jacket", WearCategory.SPORTS, 10),
            { it.tempRange == TempRange.VERY_COLD || it.tempRange == TempRange.COLD }
        ),
        WearableItemRule(
            WearableItem("Compression Tights", WearCategory.SPORTS, 8),
            { it.tempRange in listOf(TempRange.VERY_COLD, TempRange.COLD, TempRange.COOL) }
        ),
        WearableItemRule(
            WearableItem("Sports Gloves", WearCategory.SPORTS, 7),
            { it.tempRange == TempRange.VERY_COLD || it.tempRange == TempRange.COLD }
        ),
        WearableItemRule(
            WearableItem("Thermal Headband", WearCategory.SPORTS, 6),
            { it.tempRange == TempRange.VERY_COLD || it.tempRange == TempRange.COLD }
        ),

        // SPORTS - Cool/Mild
        WearableItemRule(
            WearableItem("Running Jacket", WearCategory.SPORTS, 8),
            { it.tempRange == TempRange.COOL || (it.tempRange == TempRange.MILD && it.windLevel >= WindLevel.MODERATE) }
        ),
        WearableItemRule(
            WearableItem("Long Sleeve Sports Shirt", WearCategory.SPORTS, 7),
            { it.tempRange in listOf(TempRange.COOL, TempRange.MILD) }
        ),
        WearableItemRule(
            WearableItem("Track Pants", WearCategory.SPORTS, 7),
            { it.tempCurrent < 25 }
        ),

        // SPORTS - Warm/Hot
        WearableItemRule(
            WearableItem("Moisture-Wicking Shirt", WearCategory.SPORTS, 9),
            { it.tempRange in listOf(TempRange.WARM, TempRange.HOT) || it.isHighHumidity }
        ),
        WearableItemRule(
            WearableItem("Athletic Shorts", WearCategory.SPORTS, 8),
            { it.tempRange in listOf(TempRange.MILD, TempRange.WARM, TempRange.HOT) }
        ),
        WearableItemRule(
            WearableItem("Sports Tank Top", WearCategory.SPORTS, 7),
            { it.tempRange == TempRange.HOT }
        ),
        WearableItemRule(
            WearableItem("Sweatband", WearCategory.SPORTS, 6),
            { it.tempRange == TempRange.HOT || it.isHighHumidity }
        ),
        WearableItemRule(
            WearableItem("Sports Cap", WearCategory.SPORTS, 7),
            { it.isDaytime && it.tempRange in listOf(TempRange.WARM, TempRange.HOT) }
        ),

        // SPORTS - General
        WearableItemRule(
            WearableItem("Running Shoes", WearCategory.SPORTS, 8),
            { true } // Always applicable
        ),
        WearableItemRule(
            WearableItem("Sports Watch", WearCategory.SPORTS, 5),
            { true } // Always applicable
        ),
        WearableItemRule(
            WearableItem("Sports Sunglasses", WearCategory.SPORTS, 8),
            { it.isDaytime && (it.conditionType == WeatherConditionType.CLEAR || it.uvLevel >= UVLevel.MODERATE) }
        ),

        // SPORTS - Weather Specific
        WearableItemRule(
            WearableItem("Waterproof Running Jacket", WearCategory.SPORTS, 9),
            { it.conditionType in listOf(WeatherConditionType.RAIN, WeatherConditionType.DRIZZLE) }
        ),
        WearableItemRule(
            WearableItem("Reflective Gear", WearCategory.SPORTS, 8),
            { !it.isDaytime || it.conditionType in listOf(WeatherConditionType.FOG, WeatherConditionType.MIST) }
        )
    )

    // ==================== TIPS RULES ====================

    private data class TipRule(
        val tip: WeatherTip,
        val condition: RuleCondition
    )

    private val tipRules = listOf(
        // HEALTH TIPS
        TipRule(
            WeatherTip("Wear sunscreen (SPF 30+)", TipType.HEALTH, 10),
            { it.uvLevel >= UVLevel.HIGH && it.isDaytime }
        ),
        TipRule(
            WeatherTip("Stay hydrated, drink plenty of water", TipType.HEALTH, 10),
            { it.tempRange == TempRange.HOT || (it.tempRange == TempRange.WARM && it.isHighHumidity) }
        ),
        TipRule(
            WeatherTip("Take breaks in shade or indoors", TipType.HEALTH, 9),
            { it.tempRange == TempRange.HOT && it.isDaytime }
        ),
        TipRule(
            WeatherTip("Avoid strenuous outdoor activities", TipType.HEALTH, 9),
            { it.tempRange == TempRange.HOT || it.aqiLevel >= AQILevel.UNHEALTHY }
        ),
        TipRule(
            WeatherTip("Keep skin moisturized", TipType.HEALTH, 7),
            { it.tempRange in listOf(TempRange.VERY_COLD, TempRange.COLD) || it.isLowHumidity }
        ),
        TipRule(
            WeatherTip("Watch for signs of frostbite", TipType.HEALTH, 10),
            { it.tempRange == TempRange.VERY_COLD && it.windLevel >= WindLevel.MODERATE }
        ),
        TipRule(
            WeatherTip("Limit time outdoors", TipType.HEALTH, 9),
            { it.aqiLevel >= AQILevel.VERY_UNHEALTHY || it.tempRange == TempRange.VERY_COLD }
        ),
        TipRule(
            WeatherTip("Use lip balm to prevent chapped lips", TipType.HEALTH, 6),
            { it.tempRange in listOf(TempRange.VERY_COLD, TempRange.COLD) || it.isLowHumidity }
        ),
        TipRule(
            WeatherTip("Stay in air-conditioned areas when possible", TipType.HEALTH, 8),
            { it.tempRange == TempRange.HOT && it.isHighHumidity }
        ),

        // SAFETY TIPS
        TipRule(
            WeatherTip("Check air quality before outdoor activities", TipType.SAFETY, 10),
            { it.aqiLevel >= AQILevel.UNHEALTHY_SG }
        ),
        TipRule(
            WeatherTip("Drive carefully, visibility may be reduced", TipType.SAFETY, 10),
            { it.conditionType in listOf(WeatherConditionType.FOG, WeatherConditionType.MIST, WeatherConditionType.RAIN, WeatherConditionType.SNOW) }
        ),
        TipRule(
            WeatherTip("Avoid outdoor activities during thunderstorms", TipType.SAFETY, 10),
            { it.conditionType == WeatherConditionType.THUNDERSTORM }
        ),
        TipRule(
            WeatherTip("Wear mask to protect from air pollution", TipType.SAFETY, 9),
            { it.aqiLevel >= AQILevel.UNHEALTHY }
        ),
        TipRule(
            WeatherTip("Keep emergency supplies in car", TipType.SAFETY, 8),
            { it.conditionType == WeatherConditionType.SNOW || it.tempRange == TempRange.VERY_COLD }
        ),
        TipRule(
            WeatherTip("Watch for icy patches on roads", TipType.SAFETY, 9),
            { it.tempRange == TempRange.VERY_COLD || (it.tempCurrent < 5 && it.humidity > 80) }
        ),
        TipRule(
            WeatherTip("Secure outdoor items due to strong winds", TipType.SAFETY, 9),
            { it.windLevel >= WindLevel.STRONG }
        ),
        TipRule(
            WeatherTip("Stay away from windows during storms", TipType.SAFETY, 10),
            { it.conditionType == WeatherConditionType.THUNDERSTORM || it.windLevel == WindLevel.VERY_STRONG }
        ),
        TipRule(
            WeatherTip("Carry flashlight in case of power outage", TipType.SAFETY, 7),
            { it.conditionType == WeatherConditionType.THUNDERSTORM }
        ),

        // COMFORT TIPS
        TipRule(
            WeatherTip("Dress in layers for temperature changes", TipType.COMFORT, 8),
            { it.tempMax - it.tempMin > 10 }
        ),
        TipRule(
            WeatherTip("Bring extra clothes in case of rain", TipType.COMFORT, 8),
            { it.precipLevel >= PrecipitationLevel.MODERATE }
        ),
        TipRule(
            WeatherTip("Keep a light jacket handy", TipType.COMFORT, 7),
            { it.tempRange == TempRange.MILD && (it.tempMax - it.tempMin > 8 || it.windLevel >= WindLevel.MODERATE) }
        ),
        TipRule(
            WeatherTip("Protect electronics from moisture", TipType.COMFORT, 7),
            { it.conditionType in listOf(WeatherConditionType.RAIN, WeatherConditionType.DRIZZLE) || it.humidity > 85 }
        ),
        TipRule(
            WeatherTip("Use dehumidifier or AC for comfort", TipType.COMFORT, 7),
            { it.isHighHumidity && it.tempRange in listOf(TempRange.WARM, TempRange.HOT) }
        ),
        TipRule(
            WeatherTip("Carry water bottle", TipType.COMFORT, 8),
            { it.tempRange in listOf(TempRange.WARM, TempRange.HOT) }
        ),
        TipRule(
            WeatherTip("Warm up indoors before going out", TipType.COMFORT, 7),
            { it.tempRange == TempRange.VERY_COLD }
        ),
        TipRule(
            WeatherTip("Plan for indoor alternative activities", TipType.COMFORT, 7),
            { it.conditionType in listOf(WeatherConditionType.THUNDERSTORM, WeatherConditionType.SNOW) || it.aqiLevel >= AQILevel.UNHEALTHY }
        ),
        TipRule(
            WeatherTip("Keep windows closed to maintain temperature", TipType.COMFORT, 6),
            { it.tempRange in listOf(TempRange.VERY_COLD, TempRange.HOT) }
        )
    )

    // ==================== ACTIVITIES RULES ====================

    private data class ActivityRule(
        val activity: Activity,
        val condition: RuleCondition
    )

    private val activityRules = listOf(
        // Outdoor Activities - Good Weather
        ActivityRule(
            Activity("Running", "Perfect conditions for a refreshing run", 10),
            { it.tempRange in listOf(TempRange.COOL, TempRange.MILD) &&
                    it.conditionType == WeatherConditionType.CLEAR &&
                    it.aqiLevel <= AQILevel.MODERATE }
        ),
        ActivityRule(
            Activity("Cycling", "Great weather for a bike ride", 10),
            { it.tempRange in listOf(TempRange.COOL, TempRange.MILD, TempRange.WARM) &&
                    it.conditionType in listOf(WeatherConditionType.CLEAR, WeatherConditionType.CLOUDS) &&
                    it.windLevel <= WindLevel.MODERATE &&
                    it.aqiLevel <= AQILevel.MODERATE }
        ),
        ActivityRule(
            Activity("Walking", "Ideal for a leisurely walk", 9),
            { it.tempRange in listOf(TempRange.COOL, TempRange.MILD, TempRange.WARM) &&
                    it.conditionType != WeatherConditionType.THUNDERSTORM &&
                    it.aqiLevel <= AQILevel.MODERATE }
        ),
        ActivityRule(
            Activity("Picnic", "Perfect day for outdoor dining", 10),
            { it.tempRange in listOf(TempRange.MILD, TempRange.WARM) &&
                    it.conditionType == WeatherConditionType.CLEAR &&
                    it.windLevel <= WindLevel.LIGHT &&
                    it.aqiLevel <= AQILevel.MODERATE }
        ),
        ActivityRule(
            Activity("Photography", "Excellent lighting for outdoor photos", 9),
            { (it.conditionType in listOf(WeatherConditionType.CLEAR, WeatherConditionType.CLOUDS) ||
                    (it.conditionType == WeatherConditionType.RAIN && it.isDaytime)) &&
                    it.tempRange in listOf(TempRange.COOL, TempRange.MILD, TempRange.WARM) }
        ),
        ActivityRule(
            Activity("Hiking", "Great conditions for a hike", 10),
            { it.tempRange in listOf(TempRange.COOL, TempRange.MILD) &&
                    it.conditionType in listOf(WeatherConditionType.CLEAR, WeatherConditionType.CLOUDS) &&
                    it.aqiLevel <= AQILevel.MODERATE &&
                    it.precipLevel <= PrecipitationLevel.LOW }
        ),
        ActivityRule(
            Activity("Outdoor Yoga", "Peaceful weather for outdoor practice", 9),
            { it.tempRange in listOf(TempRange.MILD, TempRange.WARM) &&
                    it.conditionType == WeatherConditionType.CLEAR &&
                    it.windLevel <= WindLevel.LIGHT &&
                    it.aqiLevel <= AQILevel.MODERATE }
        ),
        ActivityRule(
            Activity("Gardening", "Good time for outdoor gardening", 8),
            { it.tempRange in listOf(TempRange.COOL, TempRange.MILD, TempRange.WARM) &&
                    it.conditionType != WeatherConditionType.THUNDERSTORM &&
                    it.isDaytime }
        ),
        ActivityRule(
            Activity("Beach Day", "Perfect beach weather", 10),
            { it.tempRange in listOf(TempRange.WARM, TempRange.HOT) &&
                    it.conditionType == WeatherConditionType.CLEAR &&
                    it.windLevel <= WindLevel.MODERATE }
        ),
        ActivityRule(
            Activity("Stargazing", "Clear skies for observing stars", 9),
            { !it.isDaytime &&
                    it.conditionType == WeatherConditionType.CLEAR &&
                    it.tempRange >= TempRange.COOL }
        ),

        // Indoor Activities - Poor Weather
        ActivityRule(
            Activity("Indoor Gym", "Better to exercise indoors today", 9),
            { it.conditionType in listOf(WeatherConditionType.RAIN, WeatherConditionType.THUNDERSTORM, WeatherConditionType.SNOW) ||
                    it.tempRange in listOf(TempRange.VERY_COLD, TempRange.HOT) ||
                    it.aqiLevel >= AQILevel.UNHEALTHY_SG }
        ),
        ActivityRule(
            Activity("Reading Indoors", "Cozy weather for reading", 8),
            { it.conditionType in listOf(WeatherConditionType.RAIN, WeatherConditionType.SNOW) ||
                    it.tempRange in listOf(TempRange.VERY_COLD, TempRange.HOT) }
        ),
        ActivityRule(
            Activity("Indoor Swimming", "Good alternative to outdoor activities", 8),
            { it.conditionType in listOf(WeatherConditionType.RAIN, WeatherConditionType.THUNDERSTORM) ||
                    it.aqiLevel >= AQILevel.UNHEALTHY_SG }
        ),
        ActivityRule(
            Activity("Museum Visit", "Ideal day for indoor exploration", 8),
            { it.conditionType in listOf(WeatherConditionType.RAIN, WeatherConditionType.SNOW, WeatherConditionType.THUNDERSTORM) ||
                    it.tempRange in listOf(TempRange.VERY_COLD, TempRange.HOT) }
        ),
        ActivityRule(
            Activity("Movie Marathon", "Perfect weather to stay in and watch movies", 7),
            { it.conditionType in listOf(WeatherConditionType.THUNDERSTORM, WeatherConditionType.SNOW) ||
                    it.tempRange in listOf(TempRange.VERY_COLD, TempRange.HOT) }
        ),
        ActivityRule(
            Activity("Cooking", "Great day for indoor cooking projects", 7),
            { it.conditionType in listOf(WeatherConditionType.RAIN, WeatherConditionType.SNOW) }
        ),

        // Moderate Weather Activities
        ActivityRule(
            Activity("Window Shopping", "Comfortable for mall visits", 8),
            { it.tempRange in listOf(TempRange.VERY_COLD, TempRange.HOT) ||
                    it.conditionType == WeatherConditionType.RAIN }
        ),
        ActivityRule(
            Activity("Coffee Shop Visit", "Nice weather for a café outing", 8),
            { it.tempRange in listOf(TempRange.COOL, TempRange.MILD, TempRange.WARM) ||
                    it.conditionType == WeatherConditionType.RAIN }
        )
    )

    // ==================== SMART INSIGHTS RULES ====================

    private data class InsightRule(
        val insight: SmartInsight,
        val condition: RuleCondition
    )

    private val insightRules = listOf(
        // Critical Insights
        InsightRule(
            SmartInsight(
                "Hazardous Air Quality",
                "Air quality is hazardous. Stay indoors and use air purifiers. Avoid all outdoor activities.",
                100,
                InsightSeverity.CRITICAL
            ),
            { it.aqiLevel == AQILevel.HAZARDOUS }
        ),
        InsightRule(
            SmartInsight(
                "Extreme UV Alert",
                "UV index is extreme. Avoid sun exposure during 10 AM - 4 PM. Wear protective clothing and SPF 50+ sunscreen.",
                95,
                InsightSeverity.CRITICAL
            ),
            { it.uvLevel == UVLevel.EXTREME && it.isDaytime }
        ),
        InsightRule(
            SmartInsight(
                "Severe Cold Warning",
                "Extremely cold temperatures. Limit outdoor exposure and dress in multiple layers. Watch for frostbite symptoms.",
                90,
                InsightSeverity.CRITICAL
            ),
            { it.tempRange == TempRange.VERY_COLD && it.windLevel >= WindLevel.MODERATE }
        ),
        InsightRule(
            SmartInsight(
                "Extreme Heat Alert",
                "Dangerous heat conditions. Stay hydrated, avoid strenuous activity, and take frequent breaks in shade or AC.",
                90,
                InsightSeverity.CRITICAL
            ),
            { it.tempRange == TempRange.HOT && it.isHighHumidity }
        ),

        // Important Insights
        InsightRule(
            SmartInsight(
                "Poor Air Quality",
                "Air quality is unhealthy. Sensitive groups should limit outdoor exposure. Consider wearing a mask.",
                80,
                InsightSeverity.IMPORTANT
            ),
            { it.aqiLevel == AQILevel.UNHEALTHY || it.aqiLevel == AQILevel.VERY_UNHEALTHY }
        ),
        InsightRule(
            SmartInsight(
                "High UV Index",
                "UV index is high. Wear sunscreen, sunglasses, and protective clothing if outdoors for extended periods.",
                75,
                InsightSeverity.IMPORTANT
            ),
            { it.uvLevel >= UVLevel.HIGH && it.isDaytime && it.uvLevel < UVLevel.EXTREME }
        ),
        InsightRule(
            SmartInsight(
                "Large Temperature Swing",
                "Temperature will vary significantly today. Dress in layers.",
                70,
                InsightSeverity.IMPORTANT
            ),
            { it.tempMax - it.tempMin > 12 }
        ),
        InsightRule(
            SmartInsight(
                "Strong Winds Expected",
                "Strong winds today. Secure outdoor items and drive carefully.",
                70,
                InsightSeverity.IMPORTANT
            ),
            { it.windLevel >= WindLevel.STRONG }
        ),
        InsightRule(
            SmartInsight(
                "Thunderstorm Alert",
                "Thunderstorms expected. Avoid outdoor activities and stay away from windows. Carry rain gear.",
                85,
                InsightSeverity.IMPORTANT
            ),
            { it.conditionType == WeatherConditionType.THUNDERSTORM }
        ),
        InsightRule(
            SmartInsight(
                "Rain Expected",
                "Rain likely today. Don't forget your umbrella and waterproof gear.",
                65,
                InsightSeverity.IMPORTANT
            ),
            { it.conditionType in listOf(WeatherConditionType.RAIN, WeatherConditionType.DRIZZLE) ||
                    it.precipLevel >= PrecipitationLevel.HIGH }
        ),
        InsightRule(
            SmartInsight(
                "Snow Advisory",
                "Snow expected. Allow extra time for travel and dress warmly.",
                80,
                InsightSeverity.IMPORTANT
            ),
            { it.conditionType == WeatherConditionType.SNOW }
        ),

        // Info Insights
        InsightRule(
            SmartInsight(
                "Perfect Weather",
                "Ideal conditions today! Great for outdoor activities and enjoying time outside.",
                60,
                InsightSeverity.INFO
            ),
            { it.tempRange in listOf(TempRange.MILD, TempRange.WARM) &&
                    it.conditionType == WeatherConditionType.CLEAR &&
                    it.aqiLevel <= AQILevel.MODERATE &&
                    it.windLevel <= WindLevel.LIGHT }
        ),
        InsightRule(
            SmartInsight(
                "Feels Colder Than Expected",
                "Wind chill makes it feel colder. Dress warmer than the temperature suggests.",
                55,
                InsightSeverity.INFO
            ),
            { it.tempFeelsLike < it.tempCurrent - 5 && it.tempRange <= TempRange.COOL }
        ),
        InsightRule(
            SmartInsight(
                "Feels Warmer Than Expected",
                "Humidity makes it feel warmer. You may feel hotter than the temperature indicates.",
                55,
                InsightSeverity.INFO
            ),
            { it.tempFeelsLike > it.tempCurrent + 3 && it.tempRange >= TempRange.MILD }
        ),
        InsightRule(
            SmartInsight(
                "Low Humidity",
                "Very dry air today. Keep skin moisturized and stay hydrated.",
                50,
                InsightSeverity.INFO
            ),
            { it.isLowHumidity }
        ),
        InsightRule(
            SmartInsight(
                "High Humidity",
                "High humidity levels. You may feel warmer and less comfortable outdoors.",
                50,
                InsightSeverity.INFO
            ),
            { it.isHighHumidity && it.tempRange >= TempRange.WARM }
        ),
        InsightRule(
            SmartInsight(
                "Pleasant Evening",
                "Beautiful evening weather. Perfect for outdoor dining or a walk.",
                55,
                InsightSeverity.INFO
            ),
            { !it.isDaytime &&
                    it.tempRange in listOf(TempRange.MILD, TempRange.WARM) &&
                    it.conditionType == WeatherConditionType.CLEAR &&
                    it.aqiLevel <= AQILevel.MODERATE }
        ),
        InsightRule(
            SmartInsight(
                "Foggy Conditions",
                "Reduced visibility due to fog. Drive with caution and use headlights.",
                65,
                InsightSeverity.INFO
            ),
            { it.conditionType in listOf(WeatherConditionType.FOG, WeatherConditionType.MIST) }
        )
    )

    // ==================== MOOD RULES ====================

    private data class MoodRule(
        val mood: WeatherMood,
        val condition: RuleCondition,
        val priority: Int
    )

    private val moodRules = listOf(
        // Specific Weather Moods - Higher Priority
        MoodRule(
            WeatherMood(
                "❄️",
                "Winter Wonderland",
                "Bundle up and embrace the cold!",
                MoodColor.COLD
            ),
            { it.conditionType == WeatherConditionType.SNOW },
            10
        ),
        MoodRule(
            WeatherMood(
                "⛈️",
                "Stormy Weather",
                "Better stay safe indoors",
                MoodColor.RAINY
            ),
            { it.conditionType == WeatherConditionType.THUNDERSTORM },
            10
        ),
        MoodRule(
            WeatherMood(
                "🌧️",
                "Rainy Day",
                "Perfect for cozy indoor time",
                MoodColor.RAINY
            ),
            { it.conditionType in listOf(WeatherConditionType.RAIN, WeatherConditionType.DRIZZLE) },
            9
        ),

        // Temperature-Based Moods
        MoodRule(
            WeatherMood(
                "🥶",
                "Freezing Cold",
                "Stay warm out there!",
                MoodColor.COLD
            ),
            { it.tempRange == TempRange.VERY_COLD },
            8
        ),
        MoodRule(
            WeatherMood(
                "🥵",
                "Scorching Hot",
                "Stay cool and hydrated!",
                MoodColor.HOT
            ),
            { it.tempRange == TempRange.HOT },
            8
        ),
        MoodRule(
            WeatherMood(
                "☀️",
                "Beautiful Day",
                "Perfect weather to enjoy outside!",
                MoodColor.SUNNY
            ),
            { it.tempRange in listOf(TempRange.MILD, TempRange.WARM) &&
                    it.conditionType == WeatherConditionType.CLEAR &&
                    it.aqiLevel <= AQILevel.MODERATE },
            9
        ),
        MoodRule(
            WeatherMood(
                "😊",
                "Pleasant Weather",
                "Great day to be out and about",
                MoodColor.PLEASANT
            ),
            { it.tempRange in listOf(TempRange.COOL, TempRange.MILD) &&
                    it.conditionType in listOf(WeatherConditionType.CLEAR, WeatherConditionType.CLOUDS) &&
                    it.aqiLevel <= AQILevel.MODERATE },
            7
        ),
        MoodRule(
            WeatherMood(
                "☁️",
                "Cloudy Day",
                "A bit overcast but still nice",
                MoodColor.CLOUDY
            ),
            { it.conditionType == WeatherConditionType.CLOUDS },
            6
        ),

        // Air Quality Moods
        MoodRule(
            WeatherMood(
                "😷",
                "Poor Air Quality",
                "Consider staying indoors",
                MoodColor.CLOUDY
            ),
            { it.aqiLevel >= AQILevel.UNHEALTHY_SG },
            8
        ),

        // Default Fallback
        MoodRule(
            WeatherMood(
                "🌤️",
                "Moderate Weather",
                "Make the most of your day!",
                MoodColor.PLEASANT
            ),
            { true }, // Always matches as fallback
            0
        )
    )

    // ==================== PUBLIC METHODS ====================

    fun getWearableItems(context: com.devsphere.aether.utils.WeatherContext, category: WearCategory, min: Int = 2, max: Int = 5): List<WearableItem> {
        val qualified = wearableItemRules
            .filter { it.item.category == category && it.condition(context) }
            .map { it.item }
            .sortedByDescending { it.priority }

        return when {
            qualified.isEmpty() -> emptyList()
            qualified.size <= max -> qualified
            else -> qualified.shuffled().take(Random.nextInt(min, maxOf(min + 1, minOf(max + 1, qualified.size + 1))))
        }
    }

    fun getTips(context: com.devsphere.aether.utils.WeatherContext, min: Int = 2, max: Int = 4): List<WeatherTip> {
        val qualified = tipRules
            .filter { it.condition(context) }
            .map { it.tip }
            .sortedByDescending { it.priority }

        return when {
            qualified.isEmpty() -> emptyList()
            qualified.size <= max -> qualified
            else -> qualified.shuffled().take(Random.nextInt(min, maxOf(min + 1, minOf(max + 1, qualified.size + 1))))
        }
    }

    fun getActivities(context: com.devsphere.aether.utils.WeatherContext, min: Int = 2, max: Int = 4): List<Activity> {
        val qualified = activityRules
            .filter { it.condition(context) }
            .map { it.activity }
            .sortedByDescending { it.priority }

        return when {
            qualified.isEmpty() -> emptyList()
            qualified.size <= max -> qualified
            else -> qualified.shuffled().take(Random.nextInt(min, maxOf(min + 1, minOf(max + 1, qualified.size + 1))))
        }
    }

    fun getSmartInsights(context: com.devsphere.aether.utils.WeatherContext, maxInsights: Int = 2): List<SmartInsight> {
        return insightRules
            .filter { it.condition(context) }
            .map { it.insight }
            .sortedByDescending { it.priority }
            .take(maxInsights)
    }

    fun getWeatherMood(context: com.devsphere.aether.utils.WeatherContext): WeatherMood {
        return moodRules
            .filter { it.condition(context) }
            .maxByOrNull { it.priority }
            ?.mood
            ?: WeatherMood("🌤️", "Weather", "Check the current conditions", MoodColor.PLEASANT)
    }
}