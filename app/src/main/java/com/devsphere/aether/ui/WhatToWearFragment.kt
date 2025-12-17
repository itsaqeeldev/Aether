package com.devsphere.aether.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.devsphere.aether.R
import com.devsphere.aether.adapter.ActivitiesAdapter
import com.devsphere.aether.adapter.RecommendedItemsAdapter
import com.devsphere.aether.adapter.TipsAdapter
import com.devsphere.aether.models.WearCategory
import com.devsphere.aether.viewmodels.HomeViewModel
import com.devsphere.aether.viewmodels.WhatToWearUiState
import com.devsphere.aether.viewmodels.WhatToWearViewModel
import com.google.android.material.tabs.TabLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

@AndroidEntryPoint
class WhatToWearFragment : Fragment() {

    private val whatToWearViewModel: WhatToWearViewModel by viewModels()
    private val homeViewModel: HomeViewModel by activityViewModels()

    private lateinit var recommendedAdapter: RecommendedItemsAdapter
    private lateinit var tipsAdapter: TipsAdapter
    private lateinit var activitiesAdapter: ActivitiesAdapter

    private var currentCategory = WearCategory.CASUAL

    // Cache the latest state to avoid re-collecting on tab changes
    private var cachedUiState: WhatToWearUiState? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_what_to_wear, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViews(view)
        setupRecyclerViews(view)
        setupTabLayout(view)

        // Observe both ViewModels with proper lifecycle scope
        observeViewModels(view)

        // Load data once using location from HomeViewModel
        loadWeatherDataOnce()
    }

    private fun setupViews(view: View) {
        // Views are already set up in XML
    }

    private fun setupRecyclerViews(view: View) {
        // Recommended Items RecyclerView
        recommendedAdapter = RecommendedItemsAdapter()
        view.findViewById<RecyclerView>(R.id.recyclerRecommendedItems).apply {
            layoutManager = LinearLayoutManager(context)
            adapter = recommendedAdapter
        }

        // Tips RecyclerView
        tipsAdapter = TipsAdapter()
        view.findViewById<RecyclerView>(R.id.recyclerTips).apply {
            layoutManager = LinearLayoutManager(context)
            adapter = tipsAdapter
        }

        // Activities RecyclerView (2-column grid)
        activitiesAdapter = ActivitiesAdapter()
        view.findViewById<RecyclerView>(R.id.recyclerActivities).apply {
            layoutManager = GridLayoutManager(context, 2)
            adapter = activitiesAdapter
        }
    }

    private fun setupTabLayout(view: View) {
        val tabLayout = view.findViewById<TabLayout>(R.id.tabStyle)

        // Set default selection to Casual (first tab)
        tabLayout.selectTab(tabLayout.getTabAt(0))

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> { // Casual
                        currentCategory = WearCategory.CASUAL
                        updateRecommendedItemsFromCache()
                    }
                    1 -> { // Formal
                        currentCategory = WearCategory.FORMAL
                        updateRecommendedItemsFromCache()
                    }
                    2 -> { // Sport
                        currentCategory = WearCategory.SPORTS
                        updateRecommendedItemsFromCache()
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun observeViewModels(view: View) {
        // Use repeatOnLifecycle to properly handle lifecycle
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Collect WhatToWear state
                launch {
                    whatToWearViewModel.uiState.collect { state ->
                        // Cache the state for tab switching
                        cachedUiState = state

                        // Show/hide loading
                        view.findViewById<View>(R.id.progressBar).visibility =
                            if (state.isLoading) View.VISIBLE else View.GONE

                        if (state.error != null) {
                            // TODO: Show error message
                            return@collect
                        }

                        // Update temperature card
                        view.findViewById<TextView>(R.id.txtCurrentTemp).text = state.currentTemp
                        view.findViewById<TextView>(R.id.txtCurrentCondition).text = state.currentCondition

                        // Update weather icon
                        state.weatherIconRes?.let { iconRes ->
                            view.findViewById<ImageView>(R.id.icCurrentWeather).setImageResource(iconRes)
                        }

                        // Update recommended items based on selected category
                        updateRecommendedItemsFromCache()

                        // Update tips
                        tipsAdapter.submitList(state.tips)

                        // Update activities
                        activitiesAdapter.submitList(state.activities)

                        // Update smart insight
                        if (state.smartInsightTitle != null && state.smartInsightMessage != null) {
                            view.findViewById<TextView>(R.id.txtSmartInsightTitle).text =
                                state.smartInsightTitle
                            view.findViewById<TextView>(R.id.txtSmartInsightBody).text =
                                state.smartInsightMessage

                            state.smartInsightIconRes?.let { iconRes ->
                                view.findViewById<ImageView>(R.id.icSmartInsight).setImageResource(iconRes)
                            }
                            view.findViewById<View>(R.id.cardSmartInsight).visibility = View.VISIBLE
                        } else {
                            // Hide smart insight card if no insight
                            view.findViewById<View>(R.id.cardSmartInsight).visibility = View.GONE
                        }
                    }
                }
            }
        }
    }

    /**
     * Update recommended items from cached state without starting new collector
     */
    private fun updateRecommendedItemsFromCache() {
        cachedUiState?.let { state ->
            val items = when (currentCategory) {
                WearCategory.FORMAL -> state.formalItems
                WearCategory.CASUAL -> state.casualItems
                WearCategory.SPORTS -> state.sportsItems
            }
            recommendedAdapter.submitList(items)
        }
    }

    /**
     * Load weather data once when fragment starts
     */
    // FIXED CODE:
    private fun loadWeatherDataOnce() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Use firstOrNull() to get a single emission
                val homeState = homeViewModel.uiState.firstOrNull()
                val lat = homeState?.latitude
                val lon = homeState?.longitude

                // Load suggestions if we have valid coordinates
                if (lat != null && lon != null) {
                    whatToWearViewModel.loadSuggestionsForLocation(lat, lon)
                } else {
                    // Handle case where location is unavailable
                    whatToWearViewModel.loadSuggestionsForLocation(0.0, 0.0)
                }
            }
        }
    }
}