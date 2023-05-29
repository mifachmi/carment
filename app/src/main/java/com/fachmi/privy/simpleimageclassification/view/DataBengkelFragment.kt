package com.fachmi.privy.simpleimageclassification.view

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.fachmi.privy.simpleimageclassification.databinding.FragmentDataBengkelBinding

class DataBengkelFragment : Fragment() {
    private lateinit var binding: FragmentDataBengkelBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupClickListeners()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentDataBengkelBinding.inflate(inflater, container, false)
        return binding.root
    }

    private fun setupClickListeners() {
        binding.apply {
            alamatToyota.setOnClickListener { openLink("https://goo.gl/maps/1ENDyannVBpp4op48") }
            webToyota.setOnClickListener { openLink(webToyota.text.toString()) }

            alamatDaihatsu.setOnClickListener { openLink("https://goo.gl/maps/WF1dqZptY1SprZ119") }
            webDaihatsu.setOnClickListener { openLink(webDaihatsu.text.toString()) }

            alamatHonda1.setOnClickListener { openLink("https://goo.gl/maps/aUBDDCEWATHTjyLM7") }
            webHonda1.setOnClickListener { openLink(webHonda1.text.toString()) }
            alamatHonda2.setOnClickListener { openLink("https://goo.gl/maps/63RRrHxavZ7n6MB3A") }
            webHonda2.setOnClickListener { openLink(webHonda2.text.toString()) }
            alamatHonda3.setOnClickListener { openLink("https://goo.gl/maps/rFKRMnM5RQDYvYNc9") }
            webHonda3.setOnClickListener { openLink(webHonda3.text.toString()) }

            alamatMitsubishi1.setOnClickListener { openLink("https://goo.gl/maps/kTwGjRLw5t4H2Sh38") }
            webMitsubishi1.setOnClickListener { openLink(webMitsubishi1.text.toString()) }
            alamatMitsubishi2.setOnClickListener { openLink("https://goo.gl/maps/15JWhAQW8nLbc4HHA") }
            webMitsubishi2.setOnClickListener { openLink(webMitsubishi2.text.toString()) }

            alamatSuzuki1.setOnClickListener { openLink("https://goo.gl/maps/R7JXVidRe8QGQ6xm7") }
            webSuzuki1.setOnClickListener { openLink(webSuzuki1.text.toString()) }
            alamatSuzuki2.setOnClickListener { openLink("https://goo.gl/maps/P4KHK2ewEkz5yiiGA") }
            webSuzuki2.setOnClickListener { openLink(webSuzuki2.text.toString()) }
        }
    }

    private fun openLink(link: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
        startActivity(intent)
    }
}