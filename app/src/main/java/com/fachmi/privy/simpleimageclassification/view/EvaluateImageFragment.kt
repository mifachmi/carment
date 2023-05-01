package com.fachmi.privy.simpleimageclassification.view

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.net.toFile
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.fachmi.privy.simpleimageclassification.R
import com.fachmi.privy.simpleimageclassification.TFLiteHelper
import com.fachmi.privy.simpleimageclassification.databinding.DialogPickImageBinding
import com.fachmi.privy.simpleimageclassification.databinding.FragmentEvaluateImageBinding
import com.fachmi.privy.simpleimageclassification.model.CarDamageModel
import com.fachmi.privy.simpleimageclassification.utils.*
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date

class EvaluateImageFragment : Fragment(), ImagePickerListener {

    private lateinit var binding: FragmentEvaluateImageBinding
    private lateinit var tfLiteHelper: TFLiteHelper
    private var bitmap: Bitmap? = null
    private lateinit var finalImageUri: Uri
    private val cameraOutputFile by lazy { createImageFile(requireContext()) }

    private var listener: ImagePickerListener? = null

    private var finalData = CarDamageModel(
        carImage = Uri.EMPTY,
        date = "",
        merkMobil = "",
        modelMobil = "",
        tahunMobil = "",
        varianMobil = "",
        jenisKerusakan = "",
        tingkatKerusakan = "",
        tindakanReparasi = "",
        estimasiHarga = ""
    )

    override fun onAttach(context: Context) {
        super.onAttach(context)

        try {
            listener = this
        } catch (e: ClassCastException) {
            throw ClassCastException(
                "$this must implement ${ImagePickerListener::class.java.simpleName}"
            )
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onViewCreated(view, savedInstanceState)

        initModel()
        showProgressBar()
        setupClickListener()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentEvaluateImageBinding.inflate(inflater, container, false)
        return binding.root
    }

    private fun initModel() {
        tfLiteHelper = TFLiteHelper(requireActivity())
        tfLiteHelper.init()
    }

    private fun showProgressBar() {
        binding.apply {
            nsvEvaluateImage.visibility = View.GONE
            pbLoading.visibility = View.VISIBLE
            Handler(Looper.getMainLooper()).postDelayed({
                pbLoading.visibility = View.GONE
                nsvEvaluateImage.visibility = View.VISIBLE
            }, 1000)
        }
    }

    private fun setupClickListener() {
        binding.apply {
            btnPickImage.setOnClickListener {
                showDialogPickImage()
            }
            btnEvaluasiGambar.setOnClickListener {
                if (ivUploadedImage.alpha != 1f && etInputMerkMobil.text.isEmpty() && etInputModelMobil.text.isEmpty() && etInputTahunKeluaranMobil.text.isEmpty() && etInputVarianMobil.text.isEmpty()) {
                    requireContext().showToast("Mohon isi semua data terlebih dahulu")
                } else {
                    runTheModel()
                }
            }
        }
    }

    private fun showDialogPickImage() {
        val dialog = Dialog(requireContext())
        val bindingView = DialogPickImageBinding.inflate(layoutInflater)
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setContentView(bindingView.root)
        dialog.show()

        bindingView.btnFromGallery.setOnClickListener {
            galleryLauncher.openImagePicker()
            dialog.dismiss()
        }

        bindingView.btnFromCamera.setOnClickListener {
            // In your code where you want to start the camera activity, call the cameraPermissionLauncher
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                cameraLauncher.openCamera(requireContext(), cameraOutputFile)
            } else {
                // Permission is not granted, request the permission
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
            dialog.dismiss()
        }
    }

    private val galleryLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            result.data?.data?.let { imageUri ->
                val outputUri = createImageFile(context).toUri()
                val listUri = listOf(imageUri, outputUri)
                cropImageLauncher.launch(listUri)
            }
        }

    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            val outputUri = createImageFile(context).toUri()
            val listUri = listOf(cameraOutputFile.toUri(), outputUri)
            cropImageLauncher.launch(listUri)
        } else {
            // Permission is not granted, show a message to the user
            Toast.makeText(
                requireContext(),
                "Camera permission is required to take pictures",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val outputUri = createImageFile(context).toUri()
                val listUri = listOf(cameraOutputFile.toUri(), outputUri)
                cropImageLauncher.launch(listUri)
            }
        }

    private val cropImageLauncher =
        registerForActivityResult(getUCropContracts()) { croppedImageUri ->
            croppedImageUri?.let {
                val croppedImageFile = croppedImageUri.toFile()
                context?.let { ctx ->
                    try {
                        bitmap = MediaStore.Images.Media.getBitmap(ctx.contentResolver, it)
                        Log.d("cropImageLauncher", "Image is loaded: $it")
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
                listener?.onImageSelected(croppedImageFile)
            }
        }

    override fun onImageSelected(imageFile: File) {
        binding.apply {
            binding.apply {
                ivUploadedImage.setImageURI(imageFile.toUri())
                finalImageUri = imageFile.toUri()
                ivUploadedImage.alpha = 1f
                btnPickImage.visibility = View.GONE
            }
        }
    }

    private fun runTheModel() {
        context?.let { ctx ->
            if (bitmap == null) {
                ctx.showToast("Please select image")
            }
            bitmap?.let {
                tfLiteHelper.classifyImage(it)
                Log.d("runTheModel", "runTheModel: ${setLabel(tfLiteHelper.showResult())}")
                finalData = determineOutput(tfLiteHelper.showResult())
                Log.d("finaldata", "runTheModel: $finalData")
                goToEvaluationReportFragment(finalData)
            }
        }
    }

    private fun goToEvaluationReportFragment(finalData: CarDamageModel) {
        val bundle = Bundle().apply {
            putParcelable("dataEvaluation", finalData)
        }
        findNavController().navigate(
            R.id.action_evaluateImageFragment_to_evaluationReportFragment, bundle
        )
    }

    private fun setLabel(label: String?) {
        context?.showToast(label.toString())
    }

    private fun determineOutput(label: String?): CarDamageModel {
        return CarDamageModel(
            carImage = finalImageUri,
            date = getCurrentDate(),
            merkMobil = binding.etInputMerkMobil.text.toString(),
            modelMobil = binding.etInputModelMobil.text.toString(),
            tahunMobil = binding.etInputTahunKeluaranMobil.text.toString(),
            varianMobil = binding.etInputVarianMobil.text.toString(),
            jenisKerusakan = determineDamageType(label),
            tingkatKerusakan = determineLevelDamage(label),
            tindakanReparasi = determineReparationAction(label),
            estimasiHarga = determineReparationCost(label)
        )
    }

    @SuppressLint("SimpleDateFormat")
    private fun getCurrentDate(): String {
        val sdf = SimpleDateFormat("dd MMMM yyyy hh:mm:ss")
        return sdf.format(Date())
    }

    private fun determineDamageType(label: String?): String {
        return when (label) {
            "head_lamp" -> "Lampu depan"
            "bumper_dent" -> "Bumper Penyok"
            else -> "Kaca depan"
        }
    }

    private fun determineLevelDamage(label: String?): String {
        return when (label) {
            "head_lamp" -> "Ringan"
            "bumper_dent" -> "Sedang"
            else -> "Berat"
        }
    }

    private fun determineReparationAction(label: String?): String {
        return when (label) {
            "head_lamp" -> "Dibawa ke bengkel"
            "bumper_dent" -> "Dicat ulang"
            else -> "Beli mobil baru"
        }
    }

    private fun determineReparationCost(label: String?): String {
        return when (label) {
            "head_lamp" -> "Rp 500.000"
            "bumper_dent" -> "Rp 1.000.000"
            else -> "Rp 100.000.000"
        }
    }

    override fun onResume() {
        super.onResume()
        if (binding.ivUploadedImage.alpha == 1f) {
            binding.ivUploadedImage.setOnClickListener {
                showDialogPickImage()
            }
        }
    }

}