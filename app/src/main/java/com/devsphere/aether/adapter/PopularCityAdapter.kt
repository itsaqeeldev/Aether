package com.devsphere.aether.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.devsphere.aether.databinding.ItemPopularCityBinding
import com.devsphere.aether.models.PopularCity

/**
 * Adapter for displaying popular cities and search results
 * in the Add Location bottom sheet
 */
class PopularCityAdapter(
    private val onCityClick: (PopularCity) -> Unit
) : ListAdapter<PopularCity, PopularCityAdapter.CityViewHolder>(CityDiffCallback()) {

    private val savedLocationIds = mutableSetOf<Int>()

    fun updateSavedLocations(ids: Set<Int>) {
        savedLocationIds.clear()
        savedLocationIds.addAll(ids)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CityViewHolder {
        val binding = ItemPopularCityBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CityViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CityViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class CityViewHolder(
        private val binding: ItemPopularCityBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(city: PopularCity) {
            binding.apply {
                txtCityName.text = city.name
                txtCountry.text = city.country

                // Check if already saved
                val isAlreadySaved = savedLocationIds.contains(city.id)
                icAdded.visibility = if (isAlreadySaved) View.VISIBLE else View.GONE

                // Temperature loading state
                if (city.isLoading) {
                    progressTemp.visibility = View.VISIBLE
                    txtTemperature.visibility = View.GONE
                } else {
                    progressTemp.visibility = View.GONE
                    txtTemperature.visibility = View.VISIBLE
                    txtTemperature.text = city.temperature?.let { "${it}°" } ?: "--°"
                }

                // Click handling
                root.setOnClickListener {
                    if (!isAlreadySaved) {
                        onCityClick(city)
                    }
                }

                // Visual feedback for already saved
                root.alpha = if (isAlreadySaved) 0.6f else 1.0f
            }
        }
    }

    class CityDiffCallback : DiffUtil.ItemCallback<PopularCity>() {
        override fun areItemsTheSame(oldItem: PopularCity, newItem: PopularCity): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: PopularCity, newItem: PopularCity): Boolean {
            return oldItem == newItem
        }
    }
}