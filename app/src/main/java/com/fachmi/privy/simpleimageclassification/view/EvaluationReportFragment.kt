package com.fachmi.privy.simpleimageclassification.view

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.navigation.fragment.findNavController
import com.fachmi.privy.simpleimageclassification.R
import com.fachmi.privy.simpleimageclassification.databinding.DialogQuitConfirmationBinding
import com.fachmi.privy.simpleimageclassification.databinding.FragmentEvaluationReportBinding
import com.fachmi.privy.simpleimageclassification.databinding.ScreenshotInformationBinding
import com.fachmi.privy.simpleimageclassification.model.CarDamageModel

class EvaluationReportFragment : Fragment() {

    private lateinit var binding: FragmentEvaluationReportBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentEvaluationReportBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onViewCreated(view, savedInstanceState)

        showProgressBar()
        handleClickListeners()
        receiveParcelable()
    }

    private fun showProgressBar() {
        binding.apply {
            nsvReportEvaluation.visibility = View.GONE
            pbLoading.visibility = View.VISIBLE
            Handler(Looper.getMainLooper()).postDelayed({
                pbLoading.visibility = View.GONE
                nsvReportEvaluation.visibility = View.VISIBLE
            }, 1000)
        }
    }

    private fun handleClickListeners() {
        binding.apply {
            btnSimpanBuktiEvaluasi.setOnClickListener {
                showScreenshotInformation()
            }
            btnBack.setOnClickListener {
                showQuitConfirmation()
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun receiveParcelable() {
        val result = arguments?.getParcelable<CarDamageModel>("dataEvaluation")
        result?.let { data ->
            Log.d("receiveParcelable", "receiveParcelable: $data")
            binding.apply {
                tvTanggalEvaluasi.text = "${data.date} WIB"
                tvMerkMobil.text = data.merkMobil
                tvModelMobil.text = data.modelMobil
                tvTahunMobil.text = data.tahunMobil
                tvVarianMobil.text = data.varianMobil
                tvJenisKerusakan.text = data.jenisKerusakan
                tvTingkatKerusakan.text = data.tingkatKerusakan
                tvTindakanReparasi.text = data.tindakanReparasi
                tvEstimasiReparasi.text = data.estimasiHarga
                ivEvaluatedImage.setImageURI(data.carImage)
            }
        }
    }

    private fun showQuitConfirmation() {
        context?.let { ctx ->
            val dialog = Dialog(ctx)
            val bindingView = DialogQuitConfirmationBinding.inflate(layoutInflater)
            dialog.window?.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
            )
            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
            dialog.setContentView(bindingView.root)
            dialog.show()

            bindingView.apply {
                btnTidak.setOnClickListener {
                    dialog.dismiss()
                }
                btnYa.setOnClickListener {
                    findNavController().popBackStack(R.id.homeFragment, false)
                    dialog.dismiss()
                }
            }
        }
    }

    private fun showScreenshotInformation() {
        context?.let { ctx ->
            val dialog = Dialog(ctx)
            val bindingView = ScreenshotInformationBinding.inflate(layoutInflater)
            dialog.window?.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
            )
            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
            dialog.setContentView(bindingView.root)
            dialog.show()

            bindingView.apply {
                btnMengerti.setOnClickListener {
                    dialog.dismiss()
                }
            }
        }
    }

}