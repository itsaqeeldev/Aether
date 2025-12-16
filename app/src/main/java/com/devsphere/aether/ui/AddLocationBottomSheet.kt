package com.devsphere.aether.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.devsphere.aether.R
import com.devsphere.aether.adapter.PopularCityAdapter
import com.devsphere.aether.databinding.FragmentAddLocationBottomSheetBinding

import com.devsphere.aether.adapter.SearchResultAdapter
import com.devsphere.aether.viewmodels.AddLocationEvent
import com.devsphere.aether.viewmodels.AddLocationViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AddLocationBottomSheet : BottomSheetDialogFragment() {

    private var _binding: FragmentAddLocationBottomSheetBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AddLocationViewModel by viewModels()

    private lateinit var popularCityAdapter: PopularCityAdapter
    private lateinit var searchResultAdapter: SearchResultAdapter

    // Callback when location is added successfully
    var onLocationAdded: (() -> Unit)? = null

    override fun getTheme(): Int = R.style.AetherBottomSheetDialog

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddLocationBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerViews()
        setupSearch()
        setupClickListeners()
        observeUiState()
        observeEvents()
    }

    override fun onStart() {
        super.onStart()
        // Expand bottom sheet fully
        val behavior = BottomSheetBehavior.from(requireView().parent as View)
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
        behavior.skipCollapsed = true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupRecyclerViews() {
        // Popular cities adapter
        popularCityAdapter = PopularCityAdapter { city ->
            viewModel.addPopularCity(city)
        }

        // Search results adapter
        searchResultAdapter = SearchResultAdapter { result ->
            viewModel.addSearchResult(result)
        }

        binding.rvCities.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = popularCityAdapter
        }
    }

    private fun setupSearch() {
        binding.edtSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val query = s?.toString() ?: ""
                viewModel.onSearchQueryChanged(query)
                binding.btnClearSearch.isVisible = query.isNotEmpty()
            }
        })

        binding.edtSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                // Search is already triggered by text watcher with debounce
                true
            } else {
                false
            }
        }

        binding.btnClearSearch.setOnClickListener {
            binding.edtSearch.text?.clear()
            viewModel.clearSearch()
        }
    }

    private fun setupClickListeners() {
        binding.btnClose.setOnClickListener {
            dismiss()
        }
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

    private fun updateUi(state: com.devsphere.aether.viewmodels.AddLocationUiState) {
        // Update warning card visibility
        binding.cardWarning.isVisible = state.isMaxLocationsReached

        // Update section title
        binding.txtSectionTitle.text = if (state.showSearchResults) {
            "SEARCH RESULTS"
        } else {
            "POPULAR CITIES"
        }

        // Update loading indicator
        binding.progressLoading.isVisible = state.isSearching

        // Switch between adapters based on search state
        if (state.showSearchResults) {
            if (binding.rvCities.adapter != searchResultAdapter) {
                binding.rvCities.adapter = searchResultAdapter
            }
            searchResultAdapter.updateSavedLocations(state.savedLocationIds)
            searchResultAdapter.submitList(state.searchResults)

            // Show empty state if no results
            binding.emptyState.isVisible = !state.isSearching && state.searchResults.isEmpty()
            binding.txtEmptyMessage.text = state.searchError ?: "No cities found for \"${state.searchQuery}\""
            binding.rvCities.isVisible = state.searchResults.isNotEmpty()
        } else {
            if (binding.rvCities.adapter != popularCityAdapter) {
                binding.rvCities.adapter = popularCityAdapter
            }
            popularCityAdapter.updateSavedLocations(state.savedLocationIds)
            popularCityAdapter.submitList(state.popularCities)

            binding.emptyState.isVisible = false
            binding.rvCities.isVisible = true
        }
    }

    private fun observeEvents() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.events.collect { event ->
                    when (event) {
                        is AddLocationEvent.LocationAdded -> {
                            showSnackbar("${event.cityName} added to your locations")
                            onLocationAdded?.invoke()
                            // Don't dismiss - allow adding more if under limit
                        }
                        is AddLocationEvent.MaxLocationsReached -> {
                            showSnackbar("Maximum 3 locations allowed. Remove one to add more.")
                        }
                        is AddLocationEvent.Error -> {
                            showSnackbar(event.message)
                        }
                    }
                }
            }
        }
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }

    companion object {
        const val TAG = "AddLocationBottomSheet"

        fun newInstance(): AddLocationBottomSheet {
            return AddLocationBottomSheet()
        }
    }
}