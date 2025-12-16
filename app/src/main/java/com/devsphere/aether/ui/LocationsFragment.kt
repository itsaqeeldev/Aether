package com.devsphere.aether.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.devsphere.aether.R
import com.devsphere.aether.databinding.FragmentLocationsBinding
import com.devsphere.aether.ui.adapter.SavedLocationsAdapter
import com.devsphere.aether.viewmodels.LocationsEvent
import com.devsphere.aether.viewmodels.LocationsViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LocationsFragment : Fragment() {

    private var _binding: FragmentLocationsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LocationsViewModel by viewModels()
    private lateinit var locationsAdapter: SavedLocationsAdapter

    // Location permission launcher
    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

        if (fineLocationGranted || coarseLocationGranted) {
            viewModel.refreshLocationPermission()
        } else {
            showSnackbar("Location permission is required")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLocationsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSearch()
        setupClickListeners()
        observeUiState()
        observeEvents()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupRecyclerView() {
        locationsAdapter = SavedLocationsAdapter(
            onItemClick = { location ->
                viewModel.toggleExpansion(location.id)
            },
            onViewDetailsClick = { location ->
                viewModel.navigateToDetails(location)
            },
            onRemoveClick = { location ->
                viewModel.removeLocation(location.id)
            }
        )

        binding.rvLocations.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = locationsAdapter
            itemAnimator = null // Disable animations for smoother expansion
        }
    }

    private fun setupSearch() {
        binding.edtSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                viewModel.filterLocations(s?.toString() ?: "")
            }
        })
    }

    private fun setupClickListeners() {
        // Add location button
        binding.btnAddLocation.setOnClickListener {
            showAddLocationBottomSheet()
        }

        // My Location pill
        binding.pillMyLocation.setOnClickListener {
            viewModel.navigateToCurrentLocation()
        }

        // Sort button (optional functionality)
        binding.pillSort.setOnClickListener {
            // Could show sort options dialog
            showSnackbar("Sort options coming soon")
        }
    }

    private fun showAddLocationBottomSheet() {
        val bottomSheet = AddLocationBottomSheet.newInstance()
        bottomSheet.onLocationAdded = {
            // Adapter will update automatically via Flow
            viewModel.collapseExpanded()
        }
        bottomSheet.show(childFragmentManager, AddLocationBottomSheet.TAG)
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    updateUi(state)
                }
            }
        }
    }

    private fun updateUi(state: com.devsphere.aether.viewmodels.LocationsUiState) {
        // Update My Location text
        binding.txtMyLocation.text = if (state.isLoadingCurrentLocation) {
            "Loading..."
        } else {
            state.currentLocationName
        }

        // Filter and display locations
        val filteredLocations = viewModel.getFilteredLocations()
        locationsAdapter.submitList(filteredLocations)

        // Update expanded state
        val expandedIndex = filteredLocations.indexOfFirst { it.id == state.expandedLocationId }
        if (expandedIndex != locationsAdapter.getExpandedPosition()) {
            locationsAdapter.setExpandedPosition(expandedIndex)
        }

        // Show empty state if needed
        val showEmpty = !state.isLoading && filteredLocations.isEmpty()
        // You could add an empty state view here if needed
    }

    private fun observeEvents() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.events.collect { event ->
                    when (event) {
                        is LocationsEvent.LocationRemoved -> {
                            showSnackbar("Location removed")
                        }
                        is LocationsEvent.NavigateToHome -> {
                            // Navigate to home tab
                            findNavController().navigate(R.id.homeFragment2)
                        }
                        is LocationsEvent.RequestLocationPermission -> {
                            requestLocationPermission()
                        }
                        is LocationsEvent.NavigateToDetails -> {
                            navigateToCityWeather(event)
                        }
                    }
                }
            }
        }
    }

    private fun navigateToCityWeather(event: LocationsEvent.NavigateToDetails) {
        val navController = findNavController()

        // If we’re already on CityWeatherFragment, ignore the event
        if (navController.currentDestination?.id == R.id.cityWeatherFragment) return

        // Build a single-top NavOptions so a second tap can’t stack another copy
        val options = NavOptions.Builder()
            .setLaunchSingleTop(true)                       // <- core of the fix
            .setEnterAnim(R.anim.slide_in_right)
            .setExitAnim(R.anim.slide_out_left)
            .setPopEnterAnim(R.anim.slide_in_left)
            .setPopExitAnim(R.anim.slide_out_right)
            .build()

        // Pass args as before
        val bundle = Bundle().apply {
            putInt("locationId", event.locationId)
            putString("cityName", event.cityName)
            putString("country", event.country)
            putString("countryCode", event.countryCode)
            putFloat("latitude", event.latitude.toFloat())
            putFloat("longitude", event.longitude.toFloat())
            putString("timezone", event.timezone)
        }

        navController.navigate(R.id.cityWeatherFragment, bundle, options)
    }


    private fun requestLocationPermission() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                viewModel.refreshLocationPermission()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                showSnackbar("Location permission is needed") {
                    launchLocationPermissionRequest()
                }
            }
            else -> {
                launchLocationPermissionRequest()
            }
        }
    }

    private fun launchLocationPermissionRequest() {
        locationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    private fun showSnackbar(message: String, action: (() -> Unit)? = null) {
        val snackbar = Snackbar.make(requireView(), message, Snackbar.LENGTH_SHORT)
        action?.let { actionHandler ->
            snackbar.setAction("OK") { actionHandler() }
        }
        snackbar.show()
    }
}