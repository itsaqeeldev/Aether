package com.devsphere.aether.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.devsphere.aether.viewmodels.SharedWeatherViewModel
import com.devsphere.aether.viewmodels.WhatToWearUiState
import com.devsphere.aether.viewmodels.WhatToWearViewModel
import com.google.android.material.tabs.TabLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * WhatToWearFragment - Observes SharedWeatherViewModel and generates suggestions via WhatToWearViewModel
 */
@AndroidEntryPoint
class WhatToWearFragment : Fragment() {

    // Activity-scoped SharedWeatherViewModel
    private val sharedWeatherViewModel: SharedWeatherViewModel by activityViewModels()

    // Fragment-scoped WhatToWearViewModel
    private val whatToWearViewModel: WhatToWearViewModel by viewModels()

    private lateinit var recommendedAdapter: RecommendedItemsAdapter
    private lateinit var tipsAdapter: TipsAdapter
    private lateinit var activitiesAdapter: ActivitiesAdapter

    private var currentCategory = WearCategory.CASUAL
    private var cachedUiState: WhatToWearUiState? = null

    // Views
    private lateinit var progressBar: View
    private lateinit var txtCurrentTemp: TextView
    private lateinit var txtCurrentCondition: TextView
    private lateinit var icCurrentWeather: ImageView
    private lateinit var recyclerRecommendedItems: RecyclerView
    private lateinit var recyclerTips: RecyclerView
    private lateinit var recyclerActivities: RecyclerView
    private lateinit var cardSmartInsight: View
    private lateinit var txtSmartInsightTitle: TextView
    private lateinit var txtSmartInsightBody: TextView
    private lateinit var icSmartInsight: ImageView
    private lateinit var tabStyle: TabLayout

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_what_to_wear, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bindViews(view)
        setupRecyclerViews()
        setupTabLayout()
        observeViewModels()
    }

    private fun bindViews(view: View) {
        progressBar = view.findViewById(R.id.progressBar)
        txtCurrentTemp = view.findViewById(R.id.txtCurrentTemp)
        txtCurrentCondition = view.findViewById(R.id.txtCurrentCondition)
        icCurrentWeather = view.findViewById(R.id.icCurrentWeather)
        recyclerRecommendedItems = view.findViewById(R.id.recyclerRecommendedItems)
        recyclerTips = view.findViewById(R.id.recyclerTips)
        recyclerActivities = view.findViewById(R.id.recyclerActivities)
        cardSmartInsight = view.findViewById(R.id.cardSmartInsight)
        txtSmartInsightTitle = view.findViewById(R.id.txtSmartInsightTitle)
        txtSmartInsightBody = view.findViewById(R.id.txtSmartInsightBody)
        icSmartInsight = view.findViewById(R.id.icSmartInsight)
        tabStyle = view.findViewById(R.id.tabStyle)
    }

    private fun setupRecyclerViews() {
        recommendedAdapter = RecommendedItemsAdapter()
        recyclerRecommendedItems.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = recommendedAdapter
        }

        tipsAdapter = TipsAdapter()
        recyclerTips.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = tipsAdapter
        }

        activitiesAdapter = ActivitiesAdapter()
        recyclerActivities.apply {
            layoutManager = GridLayoutManager(context, 2)
            adapter = activitiesAdapter
        }
    }

    private fun setupTabLayout() {
        tabStyle.selectTab(tabStyle.getTabAt(0))

        tabStyle.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> currentCategory = WearCategory.CASUAL
                    1 -> currentCategory = WearCategory.FORMAL
                    2 -> currentCategory = WearCategory.SPORTS
                }
                updateRecommendedItemsFromCache()
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    /**
     * Observe both ViewModels:
     * 1. SharedWeatherViewModel - triggers suggestion generation
     * 2. WhatToWearViewModel - displays suggestions
     */
    private fun observeViewModels() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    // Observe shared weather and trigger suggestion generation
                    sharedWeatherViewModel.weatherState.collect { sharedState ->
                        whatToWearViewModel.generateSuggestionsFromSharedState(sharedState)
                    }
                }

                launch {
                    // Observe and display suggestions
                    whatToWearViewModel.uiState.collect { state ->
                        cachedUiState = state

                        progressBar.visibility =
                            if (state.isLoading) View.VISIBLE else View.GONE

                        if (state.error != null) {
                            // TODO: Show error
                            return@collect
                        }

                        txtCurrentTemp.text = state.currentTemp
                        txtCurrentCondition.text = state.currentCondition

                        state.weatherIconRes?.let { iconRes ->
                            icCurrentWeather.setImageResource(iconRes)
                        }

                        updateRecommendedItemsFromCache()
                        tipsAdapter.submitList(state.tips)
                        activitiesAdapter.submitList(state.activities)

                        if (state.smartInsightTitle != null && state.smartInsightMessage != null) {
                            txtSmartInsightTitle.text = state.smartInsightTitle
                            txtSmartInsightBody.text = state.smartInsightMessage
                            state.smartInsightIconRes?.let { iconRes ->
                                icSmartInsight.setImageResource(iconRes)
                            }
                            cardSmartInsight.visibility = View.VISIBLE
                        } else {
                            cardSmartInsight.visibility = View.GONE
                        }
                    }
                }
            }
        }
    }

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
}