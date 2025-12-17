package com.devsphere.aether.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.devsphere.aether.R
import com.devsphere.aether.models.WearableItemUi

/**
 * Adapter for recommended wearable items with gradient rotation
 */
class RecommendedItemsAdapter : ListAdapter<WearableItemUi, RecommendedItemsAdapter.ViewHolder>(ItemDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_reccomended, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val background: LinearLayout = itemView.findViewById(R.id.itemBackground)
        private val icon: ImageView = itemView.findViewById(R.id.icItem)
        private val title: TextView = itemView.findViewById(R.id.txtItemTitle)
        private val reason: TextView = itemView.findViewById(R.id.txtItemReason)
        private val priority: TextView = itemView.findViewById(R.id.txtItemPriority)

        fun bind(item: WearableItemUi) {
            background.setBackgroundResource(item.gradientRes)
            icon.setImageResource(item.iconRes)
            title.text = item.name
            reason.text = item.reason

            if (item.priority != null) {
                priority.visibility = View.VISIBLE
                priority.text = item.priority
            } else {
                priority.visibility = View.GONE
            }
        }
    }

    private class ItemDiffCallback : DiffUtil.ItemCallback<WearableItemUi>() {
        override fun areItemsTheSame(oldItem: WearableItemUi, newItem: WearableItemUi): Boolean {
            return oldItem.name == newItem.name
        }

        override fun areContentsTheSame(oldItem: WearableItemUi, newItem: WearableItemUi): Boolean {
            return oldItem == newItem
        }
    }
}

