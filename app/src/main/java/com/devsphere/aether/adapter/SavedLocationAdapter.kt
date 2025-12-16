package com.devsphere.aether.ui.adapter

import android.animation.ValueAnimator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.devsphere.aether.R
import com.devsphere.aether.databinding.ItemLocationCardExpandableBinding
import com.devsphere.aether.models.SavedLocationUi
import com.devsphere.aether.utils.WeatherImageMapper

/**
 * Adapter for displaying saved locations with expandable detail cards
 */
class SavedLocationsAdapter(
    private val onItemClick: (SavedLocationUi) -> Unit,
    private val onViewDetailsClick: (SavedLocationUi) -> Unit,
    private val onRemoveClick: (SavedLocationUi) -> Unit
) : ListAdapter<SavedLocationUi, SavedLocationsAdapter.LocationViewHolder>(LocationDiffCallback()) {

    private var expandedPosition: Int = RecyclerView.NO_POSITION

    fun setExpandedPosition(position: Int) {
        val previousExpanded = expandedPosition
        expandedPosition = position

        if (previousExpanded != RecyclerView.NO_POSITION) {
            notifyItemChanged(previousExpanded)
        }
        if (position != RecyclerView.NO_POSITION) {
            notifyItemChanged(position)
        }
    }

    fun getExpandedPosition(): Int = expandedPosition

    fun collapseAll() {
        val previous = expandedPosition
        expandedPosition = RecyclerView.NO_POSITION
        if (previous != RecyclerView.NO_POSITION) {
            notifyItemChanged(previous)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationViewHolder {
        val binding = ItemLocationCardExpandableBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return LocationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LocationViewHolder, position: Int) {
        holder.bind(getItem(position), position == expandedPosition)
    }

    inner class LocationViewHolder(
        private val binding: ItemLocationCardExpandableBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(location: SavedLocationUi, isExpanded: Boolean) {
            binding.apply {
                // City and country
                txtCity.text = location.name
                txtCountry.text = location.countryCode ?: location.country

                // Weather
                txtTemp.text = location.temperature
                txtCondition.text = location.condition

                // Weather icon
                val iconRes = getWeatherIconRes(location.weatherCode)
                icWeather.setImageResource(iconRes)

                // Background image
                location.imageUrl?.let { url ->
                    Glide.with(imgBackground.context)
                        .load(url)
                        .centerCrop()
                        .placeholder(R.drawable.default_img)
                        .into(imgBackground)
                } ?: run {
                    imgBackground.setImageResource(R.drawable.default_img)
                }

                // Coordinates
                txtCoordinates.text = location.getFormattedCoordinates()

                // Expansion state
                cardDetails.isVisible = isExpanded
                icExpand.rotation = if (isExpanded) 180f else 0f

                // Adjust main card corners when expanded
                if (isExpanded) {
                    cardLocation.radius = cardLocation.context.resources.getDimension(R.dimen.card_radius_top_only)
                } else {
                    cardLocation.radius = cardLocation.context.resources.getDimension(R.dimen.card_radius_full)
                }

                // Click handlers
                cardLocation.setOnClickListener {
                    val position = bindingAdapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        val newExpandedPos = if (expandedPosition == position) {
                            RecyclerView.NO_POSITION
                        } else {
                            position
                        }
                        setExpandedPosition(newExpandedPos)
                        onItemClick(location)

                        // Animate chevron
                        animateChevron(icExpand, newExpandedPos == position)
                    }
                }

                btnViewDetails.setOnClickListener {
                    onViewDetailsClick(location)
                }

                btnUnfavorite.setOnClickListener {
                    onRemoveClick(location)
                }
            }
        }

        private fun animateChevron(view: View, expand: Boolean) {
            val startRotation = if (expand) 0f else 180f
            val endRotation = if (expand) 180f else 0f

            ValueAnimator.ofFloat(startRotation, endRotation).apply {
                duration = 200
                interpolator = AccelerateDecelerateInterpolator()
                addUpdateListener { animator ->
                    view.rotation = animator.animatedValue as Float
                }
                start()
            }
        }

        private fun getWeatherIconRes(weatherCode: Int?): Int {
            return when (weatherCode) {
                0 -> R.drawable.ic_sun
                1, 2, 3 -> R.drawable.ic_sun // Partly cloudy
                45, 48 -> R.drawable.ic_sun // Fog - use cloud icon if available
                51, 53, 55, 61, 63, 65, 80, 81, 82 -> R.drawable.ic_rain
                71, 73, 75, 77, 85, 86 -> R.drawable.ic_rain // Snow - use snow icon if available
                95, 96, 99 -> R.drawable.ic_rain // Thunderstorm
                else -> R.drawable.ic_sun
            }
        }
    }

    class LocationDiffCallback : DiffUtil.ItemCallback<SavedLocationUi>() {
        override fun areItemsTheSame(oldItem: SavedLocationUi, newItem: SavedLocationUi): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: SavedLocationUi, newItem: SavedLocationUi): Boolean {
            return oldItem == newItem
        }
    }
}