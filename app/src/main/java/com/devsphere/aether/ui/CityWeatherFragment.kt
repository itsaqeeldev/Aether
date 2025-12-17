package com.devsphere.aether.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.devsphere.aether.R
import com.devsphere.aether.adapter.DailyForecastAdapter
import com.devsphere.aether.viewmodels.CityWeatherUiState
import com.devsphere.aether.viewmodels.CityWeatherViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CityWeatherFragment : Fragment() {

    /* ------------ view refs ------------ */
    private lateinit var root: NestedScrollView
    private lateinit var btnClose: ImageView
    private lateinit var txtLocation: TextView
    private lateinit var txtCountry: TextView
    private lateinit var txtTemp: TextView
    private lateinit var txtCondition: TextView
    private lateinit var txtHighLow: TextView
    private lateinit var txtSunrise: TextView
    private lateinit var txtSunset: TextView
    private lateinit var txtHumidityValue: TextView
    private lateinit var txtWindValue: TextView
    private lateinit var txtVisibilityValue: TextView
    private lateinit var txtPressureValue: TextView
    private lateinit var txtHourlyTitle: TextView
    private lateinit var imgHero: ImageView
    private lateinit var rvHourly: RecyclerView

    /* ------------ VM / adapter ------------ */
    private val viewModel: CityWeatherViewModel by viewModels()
    private val dailyForecastAdapter = DailyForecastAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_city_weather, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews(view)
        setupRecyclerView()
        setupClickListeners()
        observeUiState()
    }

    /* -------------------------------------- */
    /*              helpers                   */
    /* -------------------------------------- */

    private fun bindViews(v: View) {
        root = v as NestedScrollView
        btnClose = v.findViewById(R.id.btnClose)
        txtLocation = v.findViewById(R.id.txtLocation)
        txtCountry = v.findViewById(R.id.txtCountry)
        txtTemp = v.findViewById(R.id.txtTemp)
        txtCondition = v.findViewById(R.id.txtCondition)
        txtHighLow = v.findViewById(R.id.txtHighLow)
        txtSunrise = v.findViewById(R.id.txtSunrise)
        txtSunset = v.findViewById(R.id.txtSunset)
        txtHumidityValue = v.findViewById(R.id.txtHumidityValue)
        txtWindValue = v.findViewById(R.id.txtWindValue)
        txtVisibilityValue = v.findViewById(R.id.txtVisibilityValue)
        txtPressureValue = v.findViewById(R.id.txtPressureValue)
        txtHourlyTitle = v.findViewById(R.id.txtHourlyTitle)
        imgHero = v.findViewById(R.id.imgHero)
        rvHourly = v.findViewById(R.id.rvHourly)
    }

    private fun setupRecyclerView() {
        rvHourly.layoutManager = LinearLayoutManager(requireContext())
        rvHourly.adapter = dailyForecastAdapter
    }

    private fun setupClickListeners() {
        btnClose.setOnClickListener { findNavController().navigateUp() }
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { updateUi(it) }
            }
        }
    }

    private fun updateUi(state: CityWeatherUiState) {
        // errors
        state.errorMessage?.let {
            Snackbar.make(root, it, Snackbar.LENGTH_LONG).show()
            viewModel.clearError()
        }

        // header
        txtLocation.text     = state.cityName
        txtCountry.text      = state.countryCode ?: state.country

        state.heroImageUrl?.let { url ->
            Glide.with(this).load(url).centerCrop().into(imgHero)
        }

        // temps
        txtTemp.text      = state.currentTemp
        txtCondition.text = state.currentCondition
        txtHighLow.text   = state.feelsLike

        // sun times
        txtSunrise.text = state.sunriseTime
        txtSunset.text  = state.sunsetTime

        // metrics
        txtHumidityValue.text   = state.humidity
        txtWindValue.text       = state.windSpeed
        txtVisibilityValue.text = state.visibility
        txtPressureValue.text   = state.pressure

        // daily forecast
        if (state.dailyForecast.isNotEmpty()) {
            dailyForecastAdapter.submitList(state.dailyForecast)
        }

        txtHourlyTitle.text = "5-Day Forecast"
    }
}
