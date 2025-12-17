package com.devsphere.aether.utils

import com.devsphere.aether.R
import com.devsphere.aether.models.*

/**
 * Mapper to convert rule-based models to UI models with gradients and icons
 */
object WhatToWearUiMapper {

    // Available gradients for rotation
    private val itemGradients = listOf(
        R.drawable.gradient_ocean,
        R.drawable.gradient_amber,
        R.drawable.gradient_cosmic,
        R.drawable.gradient_emerald,
        R.drawable.gradient_fire,
        R.drawable.gradient_sky
    )

    private val tipGradients = listOf(
        R.drawable.gradient_ocean,
        R.drawable.gradient_fire,
        R.drawable.gradient_cosmic,
        R.drawable.gradient_emerald
    )

    private val activityGradients = listOf(
        R.drawable.gradient_emerald,
        R.drawable.gradient_ocean,
        R.drawable.gradient_sky,
        R.drawable.gradient_fire
    )

    /**
     * Map wearable items to UI models with gradient rotation
     */
    fun mapItemsToUi(items: List<WearableItem>, tempCurrent: Double): List<WearableItemUi> {
        return items.mapIndexed { index, item ->
            WearableItemUi(
                name = item.name,
                reason = generateReason(item, tempCurrent),
                iconRes = getIconForItem(item.name),
                gradientRes = itemGradients[index % itemGradients.size],
                priority = if (index == 0 && item.priority >= 9) "Essential"
                else if (index <= 1 && item.priority >= 7) "Recommended"
                else null
            )
        }
    }

    /**
     * Map tips to UI models with gradient rotation
     */
    fun mapTipsToUi(tips: List<WeatherTip>): List<WeatherTipUi> {
        return tips.mapIndexed { index, tip ->
            WeatherTipUi(
                title = generateTipTitle(tip),
                description = tip.text,
                iconRes = getIconForTip(tip),
                gradientRes = tipGradients[index % tipGradients.size]
            )
        }
    }

    /**
     * Map activities to UI models with gradient rotation
     */
    fun mapActivitiesToUi(activities: List<Activity>): List<ActivityUi> {
        return activities.mapIndexed { index, activity ->
            ActivityUi(
                name = activity.name,
                iconRes = getIconForActivity(activity.name),
                gradientRes = activityGradients[index % activityGradients.size]
            )
        }
    }

    /**
     * Generate reason text for wearable items
     */
    private fun generateReason(item: WearableItem, temp: Double): String {
        return when {
            item.name.contains("Jacket", ignoreCase = true) ||
                    item.name.contains("Coat", ignoreCase = true) ->
                "Perfect for ${temp.toInt()}°C"

            item.name.contains("Sunglasses", ignoreCase = true) ->
                "UV protection needed"

            item.name.contains("Umbrella", ignoreCase = true) ||
                    item.name.contains("Raincoat", ignoreCase = true) ->
                "Rain expected"

            item.name.contains("Scarf", ignoreCase = true) ||
                    item.name.contains("Gloves", ignoreCase = true) ||
                    item.name.contains("Beanie", ignoreCase = true) ->
                "Stay warm"

            item.name.contains("Shorts", ignoreCase = true) ||
                    item.name.contains("Tank", ignoreCase = true) ->
                "Stay cool"

            item.name.contains("Mask", ignoreCase = true) ->
                "Air quality protection"

            item.name.contains("Waterproof", ignoreCase = true) ->
                "Keep dry"

            item.name.contains("Moisture", ignoreCase = true) ->
                "Comfort during activity"

            else -> "Recommended for today"
        }
    }

    /**
     * Generate tip title based on type
     */
    private fun generateTipTitle(tip: WeatherTip): String {
        return when (tip.type) {
            TipType.HEALTH -> when {
                tip.text.contains("sunscreen", ignoreCase = true) -> "UV Protection"
                tip.text.contains("hydrat", ignoreCase = true) -> "Hydration Alert"
                tip.text.contains("moistur", ignoreCase = true) -> "Skin Care"
                tip.text.contains("frostbite", ignoreCase = true) -> "Cold Warning"
                tip.text.contains("break", ignoreCase = true) -> "Take Breaks"
                else -> "Health Tip"
            }
            TipType.SAFETY -> when {
                tip.text.contains("air quality", ignoreCase = true) ||
                        tip.text.contains("aqi", ignoreCase = true) -> "Air Quality Alert"
                tip.text.contains("drive", ignoreCase = true) ||
                        tip.text.contains("visibility", ignoreCase = true) -> "Drive Safely"
                tip.text.contains("thunderstorm", ignoreCase = true) -> "Storm Warning"
                tip.text.contains("mask", ignoreCase = true) -> "Protection Needed"
                tip.text.contains("wind", ignoreCase = true) -> "Wind Alert"
                else -> "Safety Alert"
            }
            TipType.COMFORT -> when {
                tip.text.contains("layer", ignoreCase = true) -> "Dress Smart"
                tip.text.contains("water", ignoreCase = true) -> "Stay Prepared"
                tip.text.contains("extra clothes", ignoreCase = true) -> "Pack Extra"
                tip.text.contains("indoor", ignoreCase = true) -> "Plan Ahead"
                else -> "Comfort Tip"
            }
        }
    }

    /**
     * Get icon resource for item
     */
    private fun getIconForItem(itemName: String): Int {
        return when {
            itemName.contains("Jacket", ignoreCase = true) ||
                    itemName.contains("Coat", ignoreCase = true) -> R.drawable.ic_wind

            itemName.contains("Sunglasses", ignoreCase = true) -> R.drawable.ic_sun

            itemName.contains("Umbrella", ignoreCase = true) ||
                    itemName.contains("Rain", ignoreCase = true) -> R.drawable.ic_cloud

            itemName.contains("Shorts", ignoreCase = true) ||
                    itemName.contains("Shirt", ignoreCase = true) -> R.drawable.ic_sun

            itemName.contains("Scarf", ignoreCase = true) ||
                    itemName.contains("Gloves", ignoreCase = true) ||
                    itemName.contains("Beanie", ignoreCase = true) -> R.drawable.ic_cloud

            else -> R.drawable.ic_wind
        }
    }

    /**
     * Get icon resource for tip
     */
    private fun getIconForTip(tip: WeatherTip): Int {
        return when (tip.type) {
            TipType.HEALTH -> when {
                tip.text.contains("sunscreen", ignoreCase = true) ||
                        tip.text.contains("uv", ignoreCase = true) -> R.drawable.ic_sun

                tip.text.contains("hydrat", ignoreCase = true) -> R.drawable.ic_wind

                tip.text.contains("cold", ignoreCase = true) ||
                        tip.text.contains("frostbite", ignoreCase = true) -> R.drawable.ic_cloud

                else -> R.drawable.ic_wind
            }
            TipType.SAFETY -> when {
                tip.text.contains("air quality", ignoreCase = true) -> R.drawable.ic_cloud

                tip.text.contains("storm", ignoreCase = true) ||
                        tip.text.contains("thunder", ignoreCase = true) -> R.drawable.ic_cloud

                tip.text.contains("wind", ignoreCase = true) -> R.drawable.ic_wind

                else -> R.drawable.ic_cloud
            }
            TipType.COMFORT -> when {
                tip.text.contains("evening", ignoreCase = true) ||
                        tip.text.contains("cool", ignoreCase = true) -> R.drawable.ic_sunset

                else -> R.drawable.ic_wind
            }
        }
    }

    /**
     * Get icon resource for activity
     */
    private fun getIconForActivity(activityName: String): Int {
        return when {
            activityName.contains("Running", ignoreCase = true) ||
                    activityName.contains("Jogging", ignoreCase = true) -> R.drawable.ic_wind

            activityName.contains("Cycling", ignoreCase = true) ||
                    activityName.contains("Bike", ignoreCase = true) -> R.drawable.ic_wind

            activityName.contains("Photo", ignoreCase = true) ||
                    activityName.contains("Camera", ignoreCase = true) -> R.drawable.ic_sunrise

            activityName.contains("Picnic", ignoreCase = true) ||
                    activityName.contains("Outdoor", ignoreCase = true) -> R.drawable.ic_sun

            activityName.contains("Beach", ignoreCase = true) ||
                    activityName.contains("Swimming", ignoreCase = true) -> R.drawable.ic_sun

            activityName.contains("Hiking", ignoreCase = true) ||
                    activityName.contains("Walking", ignoreCase = true) -> R.drawable.ic_wind

            activityName.contains("Indoor", ignoreCase = true) ||
                    activityName.contains("Gym", ignoreCase = true) ||
                    activityName.contains("Museum", ignoreCase = true) -> R.drawable.ic_cloud

            else -> R.drawable.ic_wind
        }
    }
}