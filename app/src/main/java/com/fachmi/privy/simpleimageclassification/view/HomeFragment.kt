package com.fachmi.privy.simpleimageclassification.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.navigation.fragment.findNavController
import com.fachmi.privy.simpleimageclassification.R
import com.fachmi.privy.simpleimageclassification.databinding.FragmentHomeBinding
import com.fachmi.privy.simpleimageclassification.utils.showToast

class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onViewCreated(view, savedInstanceState)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        context?.let { ctx ->
            binding.apply {
                btnEvaluateImage.setOnClickListener {
                    findNavController().navigate(R.id.action_homeFragment_to_evaluateImageFragment)
                }
                btnEvaluateHistory.setOnClickListener {
                    ctx.showToast("Fitur ini belum tersedia")
                }
                btnDataBengkel.setOnClickListener {
                    findNavController().navigate(R.id.action_homeFragment_to_dataBengkelFragment)
                }
                btnPetunjukPenggunaan.setOnClickListener {
                    findNavController().navigate(R.id.action_homeFragment_to_userGuideFragment)
                }
            }
        }
    }
}