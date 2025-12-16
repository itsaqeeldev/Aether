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
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.devsphere.aether.R
import com.devsphere.aether.databinding.FragmentHomeBinding
import com.devsphere.aether.viewmodels.HomeViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Home Fragment - Main weather display screen
 * Shows current conditions, forecast, AQI, and smart insights
 *
 * Using View Binding for type-safe view access
 */
@AndroidEntryPoint
class HomeFragment : Fragment() {

    // View Binding
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels()
    private lateinit var hourlyAdapter: HourlyForecastAdapter

    // Permission launcher
    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

        if (fineLocationGranted || coarseLocationGranted) {
            // Permission granted, load weather
            viewModel.detectAndLoadWeather()
        } else {
            // Permission denied, show message
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
        observeUiState()
        checkLocationPermission()
    }

    override fun onResume() {
        super.onResume()
        // Check if auto-refresh is needed
        viewModel.checkAutoRefresh()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Prevent memory leaks
    }

    /**
     * Setup RecyclerView for hourly forecast
     */
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

    /**
     * Setup pull-to-refresh
     */
    private fun setupSwipeRefresh() {
        binding.root.setOnRefreshListener {
            viewModel.refreshWeather()
        }

        // Set color scheme
        binding.root.setColorSchemeResources(
            R.color.aether_purple_start,
            R.color.aether_blue_end
        )
    }

    /**
     * Observe UI state changes from ViewModel
     */
    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    updateUI(state)
                }
            }
        }
    }

    /**
     * Update UI based on state
     */
    private fun updateUI(state: com.devsphere.aether.models.HomeUiState) {
        // Loading state
        binding.root.isRefreshing = state.isRefreshing

        // Error handling
        state.errorMessage?.let { message ->
            showSnackbar(message)
            viewModel.clearError()
        }

        // Hero image
        state.heroImageUrl?.let { url ->
            Glide.with(this)
                .load(url)
                .centerCrop()
                .into(binding.imgHero)
        }

        // Location and temperature
        binding.txtLocation.text = state.locationName ?: "Unknown Location"
        binding.txtTemp.text = state.currentTemp ?: "--"
        binding.txtCondition.text = state.currentCondition ?: "--"
        binding.txtHighLow.text = state.highLowTemp ?: "--"

        // Sun times
        binding.txtSunrise.text = state.sunriseTime ?: "--"
        binding.txtSunset.text = state.sunsetTime ?: "--"

        // Rain card
        binding.cardRain.visibility = if (state.showRainCard) View.VISIBLE else View.GONE
        binding.txtRainTitle.text = state.rainMessage ?: "No rain expected"

        binding.cardUvPeak.isVisible = state.showUvCard
        binding.txtUvTitle.text = state.uvTitle ?: "UV"
        binding.txtUvSub.text = state.uvSub ?: "--"


        // Metrics
        binding.txtHumidityValue.text = state.humidity ?: "--"
        binding.txtWindValue.text = state.windSpeed ?: "--"
        binding.txtVisibilityValue.text = state.visibility ?: "--"
        binding.txtPressureValue.text = state.pressure ?: "--"

        // AQI
        binding.txtAqiValue.text = state.aqiValue ?: "--"
        binding.txtAqiStatus.text = state.aqiCategory ?: "Unknown"

        // Set AQI color
        state.aqiColor?.let { colorHex ->
            try {
                val color = Color.parseColor(colorHex)
                binding.aqiIconContainer.setBackgroundColor(color)
                binding.txtAqiStatus.setTextColor(color)
            } catch (e: Exception) {
                // Ignore invalid color
            }
        }

        // Hourly forecast
        if (state.hourlyForecast.isNotEmpty()) {
            hourlyAdapter.submitList(state.hourlyForecast)
        }
    }

    /**
     * Check and request location permission
     */
    private fun checkLocationPermission() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                // Permission already granted
                viewModel.detectAndLoadWeather()
            }

            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                // Show rationale and request permission
                showSnackbar("Location permission is needed to show weather for your area") {
                    requestLocationPermission()
                }
            }

            else -> {
                // Request permission
                requestLocationPermission()
            }
        }
    }

    /**
     * Request location permission
     */
    private fun requestLocationPermission() {
        locationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    /**
     * Show Snackbar with message
     */
    private fun showSnackbar(message: String, action: (() -> Unit)? = null) {
        val snackbar = Snackbar.make(requireView(), message, Snackbar.LENGTH_LONG)

        // Rename 'it' to 'retryAction' to avoid confusion with the View inside setAction
        action?.let { retryAction ->
            snackbar.setAction("RETRY") {
                retryAction.invoke()
            }
        }

        snackbar.show()
    }
}