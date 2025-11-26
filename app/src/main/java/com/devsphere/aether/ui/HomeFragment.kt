package com.devsphere.aether.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.devsphere.aether.R
import com.devsphere.aether.models.HourlyForecastUi

class HomeFragment : Fragment() {

    private lateinit var hourlyAdapter: HourlyForecastAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // You can handle fragment args here if needed later
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Hero image with Glide (you can swap URL based on weather/time of day)
        val imgHero: ImageView = view.findViewById(R.id.imgHero)
        Glide.with(this)
            .load("https://images.unsplash.com/photo-1627817783271-1b8d21266a74?auto=format&fit=crop&w=1080&q=80")
            .into(imgHero)

        // Current weather text views (map from Open-Meteo current & daily endpoints)
        val txtLocation: TextView = view.findViewById(R.id.txtLocation)
        val txtTemp: TextView = view.findViewById(R.id.txtTemp)
        val txtTempUnit: TextView = view.findViewById(R.id.txtTempUnit)
        val txtCondition: TextView = view.findViewById(R.id.txtCondition)
        val txtHighLow: TextView = view.findViewById(R.id.txtHighLow)

        // Example static for now – later bind from API:
        txtLocation.text = "San Francisco"
        txtTemp.text = "24"
        txtTempUnit.text = "°C"
        txtCondition.text = "Partly cloudy"
        txtHighLow.text = "H:28°  L:18°"

        // Setup hourly RecyclerView
        val rvHourly: RecyclerView = view.findViewById(R.id.rvHourly)
        hourlyAdapter = HourlyForecastAdapter(emptyList())
        rvHourly.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        rvHourly.adapter = hourlyAdapter

        // TODO: Replace with real Open-Meteo data
        val demoHourly = listOf(
            HourlyForecastUi("Now", 24, R.drawable.ic_sun),
            HourlyForecastUi("14:00", 26, R.drawable.ic_sun),
            HourlyForecastUi("15:00", 28, R.drawable.ic_sun),
            HourlyForecastUi("16:00", 27, R.drawable.ic_rain),
            HourlyForecastUi("17:00", 25, R.drawable.ic_rain),
            HourlyForecastUi("18:00", 23, R.drawable.ic_rain)
        )
        hourlyAdapter.submitList(demoHourly)


    }
}
