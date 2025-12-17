package com.devsphere.aether.ui

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.devsphere.aether.R
import com.devsphere.aether.databinding.FragmentHomeBinding
import com.devsphere.aether.viewmodels.HomeViewModel
import com.devsphere.aether.viewmodels.SharedWeatherViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * HomeFragment - Observes SharedWeatherViewModel and transforms data via HomeViewModel
 */
@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    // Activity-scoped SharedWeatherViewModel
    private val sharedWeatherViewModel: SharedWeatherViewModel by activityViewModels()

    // Fragment-scoped HomeViewModel for UI transformations
    private val homeViewModel: HomeViewModel by viewModels()

    private lateinit var hourlyAdapter: HourlyForecastAdapter

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

        if (fineLocationGranted || coarseLocationGranted) {
            sharedWeatherViewModel.detectAndLoadWeather()
        } else {
            showSnackbar("Location permission is required to show weather for your area")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSwipeRefresh()
        observeViewModels()
        checkLocationPermission()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupRecyclerView() {
        hourlyAdapter = HourlyForecastAdapter(emptyList())
        binding.rvHourly.apply {
            layoutManager = LinearLayoutManager(
                requireContext(),
                LinearLayoutManager.HORIZONTAL,
                false
            )
            adapter = hourlyAdapter
        }
    }

    private fun setupSwipeRefresh() {
        binding.root.setOnRefreshListener {
            sharedWeatherViewModel.refreshWeather()
        }

        binding.root.setColorSchemeResources(
            R.color.aether_purple_start,
            R.color.aether_blue_end
        )
    }

    /**
     * Observe both ViewModels:
     * 1. SharedWeatherViewModel - for raw weather data
     * 2. HomeViewModel - for transformed UI state
     */
    private fun observeViewModels() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Launch both collectors in parallel
                launch {
                    // Observe shared weather state and update HomeViewModel
                    sharedWeatherViewModel.weatherState.collect { sharedState ->
                        homeViewModel.updateFromSharedState(sharedState)
                    }
                }

                launch {
                    // Observe transformed Home UI state
                    homeViewModel.uiState.collect { state ->
                        updateUI(state)
                    }
                }
            }
        }
    }

    private fun updateUI(state: com.devsphere.aether.models.HomeUiState) {
        binding.root.isRefreshing = state.isRefreshing

        state.errorMessage?.let { message ->
            showSnackbar(message)
        }

        state.heroImageUrl?.let { url ->
            Glide.with(this)
                .load(url)
                .centerCrop()
                .into(binding.imgHero)
        }

        binding.txtLocation.text = state.locationName ?: "Unknown Location"
        binding.txtTemp.text = state.currentTemp ?: "--"
        binding.txtCondition.text = state.currentCondition ?: "--"
        binding.txtHighLow.text = state.highLowTemp ?: "--"

        binding.txtSunrise.text = state.sunriseTime ?: "--"
        binding.txtSunset.text = state.sunsetTime ?: "--"

        binding.cardMood.isVisible = state.showMoodCard
        if (state.showMoodCard) {
            binding.txtMoodTitle.text = state.moodTitle ?: "Mood"
            binding.txtMoodSub.text = state.moodDescription ?: "Based on current weather conditions"
            state.moodIconRes?.let { iconRes ->
                binding.icMood.setImageResource(iconRes)
            }
        }

        binding.cardRain.visibility = if (state.showRainCard) View.VISIBLE else View.GONE
        binding.txtRainTitle.text = state.rainMessage ?: "No rain expected"

        binding.cardUvPeak.isVisible = state.showUvCard
        binding.txtUvTitle.text = state.uvTitle ?: "UV"
        binding.txtUvSub.text = state.uvSub ?: "--"

        binding.txtHumidityValue.text = state.humidity ?: "--"
        binding.txtWindValue.text = state.windSpeed ?: "--"
        binding.txtVisibilityValue.text = state.visibility ?: "--"
        binding.txtPressureValue.text = state.pressure ?: "--"

        binding.txtAqiValue.text = state.aqiValue ?: "--"
        binding.txtAqiStatus.text = state.aqiCategory ?: "Unknown"

        state.aqiColor?.let { colorHex ->
            try {
                val color = Color.parseColor(colorHex)
                binding.aqiIconContainer.setBackgroundColor(color)
                binding.txtAqiStatus.setTextColor(color)
            } catch (e: Exception) {
                // Ignore invalid color
            }
        }

        if (state.hourlyForecast.isNotEmpty()) {
            hourlyAdapter.submitList(state.hourlyForecast)
        }
    }

    private fun checkLocationPermission() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                sharedWeatherViewModel.detectAndLoadWeather()
            }

            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                showSnackbar("Location permission is needed to show weather for your area") {
                    requestLocationPermission()
                }
            }

            else -> {
                requestLocationPermission()
            }
        }
    }

    private fun requestLocationPermission() {
        locationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    private fun showSnackbar(message: String, action: (() -> Unit)? = null) {
        val snackbar = Snackbar.make(requireView(), message, Snackbar.LENGTH_LONG)

        action?.let { retryAction ->
            snackbar.setAction("RETRY") {
                retryAction.invoke()
            }
        }

        snackbar.show()
    }
}