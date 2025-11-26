package com.devsphere.aether.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.devsphere.aether.R
import com.devsphere.aether.models.HourlyForecastUi


class HourlyForecastAdapter(
    private var items: List<HourlyForecastUi>
) : RecyclerView.Adapter<HourlyForecastAdapter.HourlyViewHolder>() {

    inner class HourlyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtHour: TextView = itemView.findViewById(R.id.txtHour)
        val icHourWeather: ImageView = itemView.findViewById(R.id.icHourWeather)
        val txtHourTemp: TextView = itemView.findViewById(R.id.txtHourTemp)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HourlyViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_hourly, parent, false)
        return HourlyViewHolder(view)
    }

    override fun onBindViewHolder(holder: HourlyViewHolder, position: Int) {
        val item = items[position]
        holder.txtHour.text = item.time
        holder.txtHourTemp.text = "${item.temperatureC}°"
        holder.icHourWeather.setImageResource(item.iconResId)
    }

    override fun getItemCount(): Int = items.size

    fun submitList(newItems: List<HourlyForecastUi>) {
        items = newItems
        notifyDataSetChanged()
    }
}
