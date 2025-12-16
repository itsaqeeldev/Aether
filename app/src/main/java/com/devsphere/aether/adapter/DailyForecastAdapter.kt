package com.devsphere.aether.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.devsphere.aether.R
import com.devsphere.aether.databinding.ItemDailyForecastBinding
import com.devsphere.aether.models.DailyForecastUi

/**
 * Adapter for displaying 5-day daily forecast in CityWeatherFragment
 */
class DailyForecastAdapter : ListAdapter<DailyForecastUi, DailyForecastAdapter.DailyViewHolder>(DailyDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DailyViewHolder {
        val binding = ItemDailyForecastBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return DailyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DailyViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class DailyViewHolder(
        private val binding: ItemDailyForecastBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(forecast: DailyForecastUi) {
            binding.apply {
                // Day name and date
                txtDayName.text = forecast.dayName
                txtDate.text = forecast.date

                // Weather icon
                icWeather.setImageResource(forecast.iconResId)

                // Condition
                txtCondition.text = forecast.condition

                // Temperatures
                txtHighTemp.text = "${forecast.highTemp}°"
                txtLowTemp.text = "${forecast.lowTemp}°"

                // Precipitation probability
                if (forecast.precipitationProb != null && forecast.precipitationProb > 0) {
                    txtPrecipProb.isVisible = true
                    icRain.isVisible = true
                    txtPrecipProb.text = "${forecast.precipitationProb}%"
                } else {
                    txtPrecipProb.isVisible = false
                    icRain.isVisible = false
                }

                // Highlight today
                if (forecast.isToday) {
                    root.setBackgroundResource(R.drawable.daily_today_bg)
                    txtDayName.setTextColor(ContextCompat.getColor(root.context, R.color.aether_purple_start))
                } else {
                    root.background = null
                    txtDayName.setTextColor(ContextCompat.getColor(root.context, R.color.aether_text_primary))
                }
            }
        }
    }

    class DailyDiffCallback : DiffUtil.ItemCallback<DailyForecastUi>() {
        override fun areItemsTheSame(oldItem: DailyForecastUi, newItem: DailyForecastUi): Boolean {
            return oldItem.dayName == newItem.dayName && oldItem.date == newItem.date
        }

        override fun areContentsTheSame(oldItem: DailyForecastUi, newItem: DailyForecastUi): Boolean {
            return oldItem == newItem
        }
    }
}