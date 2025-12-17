package com.devsphere.aether.models

data class WearableItemUi(
    val name: String,
    val reason: String = "",
    val iconRes: Int,
    val gradientRes: Int,
    val priority: String? = null // "Essential", "Recommended", "Optional" or null
)