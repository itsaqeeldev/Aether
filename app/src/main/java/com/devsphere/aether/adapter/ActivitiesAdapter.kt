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
import com.devsphere.aether.models.ActivityUi

/**
 * Adapter for activities in 2-column grid with gradient rotation
 */
class ActivitiesAdapter : ListAdapter<ActivityUi, ActivitiesAdapter.ViewHolder>(ActivityDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_activity_grid, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val iconContainer: FrameLayout = itemView.findViewById(R.id.activityIconContainer)
        private val icon: ImageView = itemView.findViewById(R.id.icActivity)
        private val name: TextView = itemView.findViewById(R.id.txtActivity)

        fun bind(activity: ActivityUi) {
            iconContainer.setBackgroundResource(activity.gradientRes)
            icon.setImageResource(activity.iconRes)
            name.text = activity.name
        }
    }

    private class ActivityDiffCallback : DiffUtil.ItemCallback<ActivityUi>() {
        override fun areItemsTheSame(oldItem: ActivityUi, newItem: ActivityUi): Boolean {
            return oldItem.name == newItem.name
        }

        override fun areContentsTheSame(oldItem: ActivityUi, newItem: ActivityUi): Boolean {
            return oldItem == newItem
        }
    }
}