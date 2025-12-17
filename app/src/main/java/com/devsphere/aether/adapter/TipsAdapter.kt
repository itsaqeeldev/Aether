package com.devsphere.aether.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.devsphere.aether.R
import com.devsphere.aether.models.WeatherTipUi

/**
 * Adapter for weather tips with gradient rotation
 */
class TipsAdapter : ListAdapter<WeatherTipUi, TipsAdapter.ViewHolder>(TipDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_tip, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val iconContainer: FrameLayout = itemView.findViewById(R.id.iconContainer)
        private val icon: ImageView = itemView.findViewById(R.id.icTip)
        private val title: TextView = itemView.findViewById(R.id.txtTipTitle)
        private val description: TextView = itemView.findViewById(R.id.txtTipDesc)

        fun bind(tip: WeatherTipUi) {
            iconContainer.setBackgroundResource(tip.gradientRes)
            icon.setImageResource(tip.iconRes)
            title.text = tip.title
            description.text = tip.description
        }
    }

    private class TipDiffCallback : DiffUtil.ItemCallback<WeatherTipUi>() {
        override fun areItemsTheSame(oldItem: WeatherTipUi, newItem: WeatherTipUi): Boolean {
            return oldItem.title == newItem.title
        }

        override fun areContentsTheSame(oldItem: WeatherTipUi, newItem: WeatherTipUi): Boolean {
            return oldItem == newItem
        }
    }
}
