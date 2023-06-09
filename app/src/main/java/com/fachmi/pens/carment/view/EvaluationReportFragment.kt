package com.fachmi.pens.carment.view

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
import com.fachmi.pens.carment.R
import com.fachmi.pens.carment.databinding.DialogQuitConfirmationBinding
import com.fachmi.pens.carment.databinding.FragmentEvaluationReportBinding
import com.fachmi.pens.carment.databinding.ScreenshotInformationBinding
import com.fachmi.pens.carment.model.CarDamageModel

class EvaluationReportFragment : Fragment() {

    private var _binding: FragmentEvaluationReportBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEvaluationReportBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onViewCreated(view, savedInstanceState)

        showProgressBar()
        handleClickListeners()
        receiveSafeParcelable()
        /*receiveParcelable()*/
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
    private fun receiveSafeParcelable() {
        val receivedData = EvaluationReportFragmentArgs.fromBundle(arguments as Bundle).dataEvaluation
        binding.apply {
            tvTanggalEvaluasi.text = "${receivedData.date} WIB"
            tvMerkMobil.text = receivedData.merkMobil.capitalize()
            tvModelMobil.text = receivedData.modelMobil.capitalize()
            tvTahunMobil.text = receivedData.tahunMobil
            tvWarnaMobil.text = receivedData.warnaMobil.capitalize()
            tvJenisKerusakan.text = receivedData.jenisKerusakan
            tvTingkatKerusakan.text = receivedData.tingkatKerusakan
            tvTindakanReparasi.text = receivedData.tindakanReparasi
            tvEstimasiReparasi.text = receivedData.estimasiHarga
            ivEvaluatedImage.setImageURI(receivedData.carImage)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun receiveParcelable() {
        val result = arguments?.getParcelable<CarDamageModel>("dataEvaluation")
        result?.let { data ->
            Log.d("receiveParcelable", "receiveParcelable: $data")
            binding.apply {
                tvTanggalEvaluasi.text = "${data.date} WIB"
                tvMerkMobil.text = data.merkMobil.capitalize()
                tvModelMobil.text = data.modelMobil.capitalize()
                tvTahunMobil.text = data.tahunMobil
                tvWarnaMobil.text = data.warnaMobil.capitalize()
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

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}