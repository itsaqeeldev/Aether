package com.devsphere.aether.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.devsphere.aether.data.remote.dto.geocoding.GeocodingResult
import com.devsphere.aether.databinding.ItemPopularCityBinding

/**
 * Adapter for displaying search results in the Add Location bottom sheet
 * Reuses the same layout as PopularCityAdapter
 */
class SearchResultAdapter(
    private val onResultClick: (GeocodingResult) -> Unit
) : ListAdapter<GeocodingResult, SearchResultAdapter.ResultViewHolder>(ResultDiffCallback()) {

    private val savedLocationIds = mutableSetOf<Int>()

    fun updateSavedLocations(ids: Set<Int>) {
        savedLocationIds.clear()
        savedLocationIds.addAll(ids)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResultViewHolder {
        val binding = ItemPopularCityBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ResultViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ResultViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ResultViewHolder(
        private val binding: ItemPopularCityBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(result: GeocodingResult) {
            binding.apply {
                txtCityName.text = result.name ?: "Unknown"

                // Build subtitle with admin1 (state/province) and country
                val subtitle = buildString {
                    result.admin1?.let { append(it) }
                    result.country?.let {
                        if (isNotEmpty()) append(", ")
                        append(it)
                    }
                }
                txtCountry.text = subtitle.ifEmpty { "Unknown location" }

                // Check if already saved
                val isAlreadySaved = result.id?.let { savedLocationIds.contains(it) } ?: false
                icAdded.visibility = if (isAlreadySaved) View.VISIBLE else View.GONE

                // Hide temperature for search results (we don't fetch it for search)
                progressTemp.visibility = View.GONE
                txtTemperature.visibility = View.GONE

                // Click handling
                root.setOnClickListener {
                    if (!isAlreadySaved && result.id != null) {
                        onResultClick(result)
                    }
                }

                // Visual feedback for already saved
                root.alpha = if (isAlreadySaved) 0.6f else 1.0f
            }
        }
    }

    class ResultDiffCallback : DiffUtil.ItemCallback<GeocodingResult>() {
        override fun areItemsTheSame(oldItem: GeocodingResult, newItem: GeocodingResult): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: GeocodingResult, newItem: GeocodingResult): Boolean {
            return oldItem == newItem
        }
    }
}