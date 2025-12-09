package com.devsphere.aether.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.devsphere.aether.R
import com.devsphere.aether.databinding.FragmentWhatToWearBinding


class WhatToWearFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_what_to_wear, container, false)
    }


}
