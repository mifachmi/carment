package com.fachmi.pens.carment.view

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
import com.fachmi.pens.carment.R
import com.fachmi.pens.carment.TFLiteHelper
import com.fachmi.pens.carment.databinding.DialogPickImageBinding
import com.fachmi.pens.carment.databinding.FragmentEvaluateImageBinding
import com.fachmi.pens.carment.model.CarDamageModel
import com.fachmi.pens.carment.utils.*
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date

class EvaluateImageFragment : Fragment(), ImagePickerListener {

    private var _binding: FragmentEvaluateImageBinding? = null
    private val binding get() = _binding!!

    private lateinit var tfLiteHelper: TFLiteHelper
    private var bitmap: Bitmap? = null
    private var finalImageUri: Uri? = null
    private val cameraOutputFile by lazy { createImageFile(requireContext()) }

    private var listener: ImagePickerListener? = null

    private var finalData = CarDamageModel(
        carImage = Uri.EMPTY,
        date = "",
        merkMobil = "",
        modelMobil = "",
        tahunMobil = "",
        warnaMobil = "",
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
        _binding = FragmentEvaluateImageBinding.inflate(inflater, container, false)
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
                if (ivUploadedImage.alpha != 1f && etInputMerkMobil.text.isEmpty() && etInputModelMobil.text.isEmpty() && etInputTahunKeluaranMobil.text.isEmpty() && etInputWarnaMobil.text.isEmpty()) {
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
                Log.d("btnFromCamera", "showDialogPickImage: $cameraOutputFile")
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
            cameraLauncher.openCamera(requireContext(), cameraOutputFile)
            /*val outputUri = createImageFile(context).toUri()
            val listUri = listOf(cameraOutputFile.toUri(), outputUri)
            cropImageLauncher.launch(listUri)*/
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
                Log.d("cameraLauncher", "outputUri: $outputUri")
                Log.d("cameraLauncher", "listUri: $listUri")
                cropImageLauncher.launch(listUri)
                requireContext().showToast("kesini")
            }
        }

    private val cropImageLauncher =
        registerForActivityResult(getUCropContracts()) { croppedImageUri ->
            Log.d("cropImageLauncher croppedImageUri", "$croppedImageUri")

            croppedImageUri?.let {
                val croppedImageFile = croppedImageUri.toFile()
                context?.let { ctx ->
                    try {
                        bitmap = MediaStore.Images.Media.getBitmap(
                            ctx.contentResolver,
                            croppedImageUri
                        )
                        Log.d("cropImageLauncher", "Image is loaded: $croppedImageUri")
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
                listener?.onImageSelected(croppedImageFile)
                /*findNavController().popBackStack()*/
            }

            /*try {
                if (croppedImageUri != null) {
                    try {
                        val croppedImageFile = croppedImageUri.toFile()
                        context?.let { ctx ->
                            try {
                                bitmap = MediaStore.Images.Media.getBitmap(
                                    ctx.contentResolver,
                                    croppedImageUri
                                )
                                Log.d("cropImageLauncher", "Image is loaded: $croppedImageUri")
                            } catch (e: IOException) {
                                e.printStackTrace()
                            }
                        }
                        listener?.onImageSelected(croppedImageFile)
                    } catch (e: NullPointerException) {
                        fragmentManager?.popBackStack()
                    }
                } else {
                    print("croppedImageUri di dalem else: $croppedImageUri")
                }
            } catch (e: NullPointerException) {
                findNavController().popBackStack()
            }*/
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
                val scaledImage = Bitmap.createScaledBitmap(it, 224, 224, false)
                tfLiteHelper.classifyImage(scaledImage)
                setLabel(tfLiteHelper.showResult())
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
        val safeBundle =
            EvaluateImageFragmentDirections.actionEvaluateImageFragmentToEvaluationReportFragment(
                finalData
            )
        /*findNavController().navigate(
            R.id.action_evaluateImageFragment_to_evaluationReportFragment, bundle
        )*/
        findNavController().navigate(safeBundle)
    }

    private fun setLabel(label: String?) {
        Log.d("setLabel", "setLabel: $label")
    }

    private fun determineOutput(label: String?): CarDamageModel {
        return CarDamageModel(
            carImage = finalImageUri ?: Uri.EMPTY,
            date = getCurrentDate(),
            merkMobil = binding.etInputMerkMobil.text.toString().lowercase(),
            modelMobil = binding.etInputModelMobil.text.toString().lowercase(),
            tahunMobil = binding.etInputTahunKeluaranMobil.text.toString(),
            warnaMobil = binding.etInputWarnaMobil.text.toString().lowercase(),
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
            "bumper_dent_minor" -> "Bumper Penyok"
            "bumper_dent_severe" -> "Bumper Penyok"
            "bumper_scratch_minor" -> "Bumper Tergores"
            "door_dent_minor" -> "Pintu Penyok"
            "door_dent_severe" -> "Pintu Penyok"
            "door_scratch_minor" -> "Pintu Tergores"
            "glass_shatter_severe" -> "Kaca Pecah"
            "head_lamp_severe" -> "Lampu Depan"
            "tail_lamp_severe" -> "Lampu Belakang"
            else -> "Kerusakan tidak diketahui"
        }
    }

    private fun determineLevelDamage(label: String?): String {
        val listString = label?.split("_")
        return when (listString?.get(2)) {
            "minor" -> "Ringan - Sedang"
            "severe" -> "Sedang - Berat"
            else -> "Tidak diketahui"
        }
    }

    private fun determineReparationAction(label: String?): String {
        return when (label) {
            "door_dent_minor" -> "Dipoles dan didempul"
            "door_dent_severe" -> "Ganti 1 panel"
            "door_scratch_minor" -> "Dipoles, didempul, dan dicat ulang"
            "bumper_dent_minor" -> "Dipoles, didempul dan di cat ulang"
            "bumper_dent_severe" -> "Ganti 1 panel"
            "bumper_scratch_minor" -> "Dipoles, didempul dan di cat ulang"
            "glass_shatter_severe" -> "Ganti 1 panel kaca"
            "head_lamp_severe" -> "Ganti panel dan sparepart"
            "tail_lamp_severe" -> "Ganti panel dan sparepart"
            else -> "Tindakan reparasi tidak bisa ditentukan"
        }
    }

    private fun determineReparationCost(label: String?): String {
        return when (label) {
            "bumper_dent_minor" -> {
                when (binding.etInputMerkMobil.text.toString().lowercase()) {
                    "mitsubishi" -> {
                        when (binding.etInputModelMobil.text.toString().lowercase()) {
                            "mirage" -> createReadableRangePrice(
                                "700",
                                "956"
                            )

                            "xpander", "outlander px", "xpander cross" -> createReadableRangePrice(
                                "805",
                                "1.050"
                            )

                            else -> createReadableRangePrice("908", "1.180")
                        }
                    }

                    "daihatsu" -> {
                        when (binding.etInputModelMobil.text.toString().lowercase()) {
                            "xenia", "sigra", "terios", "rocky", "sirion", "ayla", "luxio" -> createReadableRangePrice(
                                "450",
                                "500"
                            )

                            else -> createReadableRangePrice("516", "645")
                        }
                    }

                    "honda" -> {
                        when (binding.etInputModelMobil.text.toString().lowercase()) {
                            "brio", "jazz", "fit", "city", "freed", "civic", "steam", "hrv", "brv" -> createReadableSinglePrice(
                                "1.270"
                            )

                            "crv", "accord", "odyssey", "crz" -> createReadableSinglePrice("1.355")

                            else -> createReadableRangePrice("1.120", "1.300")
                        }
                    }

                    "toyota" -> {
                        when (binding.etInputModelMobil.text.toString().lowercase()) {
                            "avanza", "calya" -> createReadableSinglePrice("532")

                            "innova" -> createReadableSinglePrice("579")

                            "rush", "sienta" -> createReadableSinglePrice("544")

                            "kijang" -> createReadableSinglePrice("579")

                            "fortuner", "voxy", "chr", "corolla cross" -> createReadableSinglePrice(
                                "615"
                            )

                            "yaris", "etios" -> createReadableSinglePrice("627")

                            "vios", "corolla" -> createReadableSinglePrice("579")

                            "altis", "corona", "ft86" -> createReadableSinglePrice("615")

                            "camry", "mark x" -> createReadableSinglePrice("810")

                            "agya" -> createReadableSinglePrice("627")

                            "alphard" -> createReadableSinglePrice("1.040")

                            "lexus" -> createReadableRangePrice("1.420", "1.540")

                            else -> createReadableRangePrice("500", "800")
                        }
                    }

                    else -> createReadableRangePrice(
                        "600",
                        "1.500"
                    )
                }
            }

            "bumper_dent_severe" -> {
                when (binding.etInputMerkMobil.text.toString().lowercase()) {
                    "mitsubishi" -> {
                        when (binding.etInputModelMobil.text.toString().lowercase()) {
                            "mirage" -> createReadableRangePrice(
                                "700",
                                "956"
                            )

                            "xpander", "outlander px", "xpander cross" -> createReadableRangePrice(
                                "805",
                                "1.050"
                            )

                            else -> createReadableRangePrice("908", "1.180")
                        }
                    }

                    "daihatsu" -> {
                        when (binding.etInputModelMobil.text.toString().lowercase()) {
                            "xenia", "sigra", "terios", "rocky", "sirion", "ayla", "luxio" -> createReadableRangePrice(
                                "580",
                                "600"
                            )

                            else -> createReadableRangePrice("672", "870")
                        }
                    }

                    "honda" -> {
                        when (binding.etInputModelMobil.text.toString().lowercase()) {
                            "brio", "jazz", "fit", "city", "freed", "civic", "steam", "hrv", "brv" -> createReadableSinglePrice(
                                "1.270"
                            )

                            "crv", "accord", "odyssey", "crz" -> createReadableSinglePrice("1.355")

                            else -> createReadableRangePrice("1.120", "1.300")
                        }
                    }

                    "toyota" -> {
                        when (binding.etInputModelMobil.text.toString().lowercase()) {
                            "avanza", "calya" -> createReadableRangePrice(
                                "733",
                                "757"
                            )

                            "innova" -> createReadableRangePrice("816", "828")

                            "rush", "sienta" -> createReadableRangePrice(
                                "745",
                                "769"
                            )

                            "kijang" -> createReadableRangePrice("804", "828")

                            "fortuner", "voxy", "chr", "corolla cross" -> createReadableRangePrice(
                                "851",
                                "875"
                            )

                            "yaris", "etios" -> createReadableRangePrice(
                                "863",
                                "887"
                            )

                            "vios", "corolla" -> createReadableRangePrice(
                                "804",
                                "828"
                            )

                            "altis", "corona", "ft86" -> createReadableRangePrice(
                                "851",
                                "875"
                            )

                            "camry", "mark x" -> createReadableRangePrice(
                                "1.120",
                                "1.150"
                            )

                            "agya" -> createReadableSinglePrice("733")

                            "alphard" -> createReadableRangePrice("1.450", "1.480")

                            "lexus" -> createReadableRangePrice("2.380", "3.170")

                            else -> createReadableRangePrice("804", "1.400")
                        }
                    }

                    else -> createReadableRangePrice(
                        "600",
                        "1.355"
                    )
                }
            }

            "bumper_scratch_minor" -> {
                when (binding.etInputMerkMobil.text.toString().lowercase()) {
                    "mitsubishi" -> {
                        when (binding.etInputModelMobil.text.toString().lowercase()) {
                            "mirage" -> createReadableRangePrice(
                                "700",
                                "956"
                            )

                            "xpander", "outlander px", "xpander cross" -> createReadableRangePrice(
                                "805",
                                "1.050"
                            )

                            else -> createReadableRangePrice("908", "1.180")
                        }
                    }

                    "daihatsu" -> {
                        when (binding.etInputModelMobil.text.toString().lowercase()) {
                            "xenia", "sigra", "terios", "rocky", "sirion", "ayla", "luxio" -> createReadableRangePrice(
                                "450",
                                "500"
                            )

                            else -> createReadableRangePrice("516", "645")
                        }
                    }

                    "honda" -> {
                        when (binding.etInputModelMobil.text.toString().lowercase()) {
                            "brio", "jazz", "fit", "city", "freed", "civic", "steam", "hrv", "brv" -> createReadableSinglePrice(
                                "1.270"
                            )

                            "crv", "accord", "odyssey", "crz" -> createReadableSinglePrice("1.355")

                            else -> createReadableRangePrice("1.120", "1.300")
                        }
                    }

                    "toyota" -> {
                        when (binding.etInputModelMobil.text.toString().lowercase()) {
                            "avanza", "calya" -> createReadableSinglePrice("532")

                            "innova" -> createReadableSinglePrice("579")

                            "rush", "sienta" -> createReadableSinglePrice("544")

                            "kijang" -> createReadableSinglePrice("579")

                            "fortuner", "voxy", "chr", "corolla cross" -> createReadableSinglePrice(
                                "615"
                            )

                            "yaris", "etios" -> createReadableSinglePrice("627")

                            "vios", "corolla" -> createReadableSinglePrice("579")

                            "altis", "corona", "ft86" -> createReadableSinglePrice("615")

                            "camry", "mark x" -> createReadableSinglePrice("810")

                            "agya" -> createReadableSinglePrice("627")

                            "alphard" -> createReadableSinglePrice("1.040")

                            "lexus" -> createReadableRangePrice("1.420", "1.540")

                            else -> createReadableRangePrice("500", "800")
                        }
                    }

                    else -> createReadableRangePrice(
                        "450",
                        "800"
                    )
                }
            }

            "door_dent_minor" -> {
                when (binding.etInputMerkMobil.text.toString().lowercase()) {
                    "mitsubishi" -> {
                        when (binding.etInputModelMobil.text.toString().lowercase()) {
                            "mirage" -> createReadableRangePrice(
                                "883",
                                "1.150"
                            )

                            "xpander", "outlander px", "xpander cross" -> createReadableRangePrice(
                                "908",
                                "1.180"
                            )

                            else -> createReadableRangePrice("1.016", "1.320")
                        }
                    }

                    "daihatsu" -> {
                        when (binding.etInputModelMobil.text.toString().lowercase()) {
                            "xenia", "sigra", "terios", "rocky", "sirion", "ayla", "luxio" -> createReadableRangePrice(
                                "500",
                                "550"
                            )

                            else -> createReadableRangePrice("560", "701")
                        }
                    }

                    "honda" -> {
                        when (binding.etInputModelMobil.text.toString().lowercase()) {
                            "brio", "jazz", "fit", "city", "freed", "civic", "steam", "hrv", "brv" -> createReadableSinglePrice(
                                "1.500"
                            )

                            "crv", "accord", "odyssey", "crz" -> createReadableSinglePrice("1.645")

                            else -> createReadableRangePrice("1.300", "1.520")
                        }
                    }

                    "toyota" -> {
                        when (binding.etInputModelMobil.text.toString().lowercase()) {
                            "avanza", "calya" -> createReadableSinglePrice("615")

                            "innova" -> createReadableSinglePrice("662")

                            "rush", "sienta" -> createReadableSinglePrice("615")

                            "kijang" -> createReadableSinglePrice("639")

                            "fortuner", "voxy", "chr", "corolla cross" -> createReadableSinglePrice(
                                "698"
                            )

                            "yaris", "etios" -> createReadableSinglePrice("627")

                            "vios", "corolla" -> createReadableSinglePrice("639")

                            "altis", "corona", "ft86" -> createReadableSinglePrice("662")

                            "camry", "mark x" -> createReadableSinglePrice("890")

                            "agya" -> createReadableSinglePrice("627")

                            "alphard" -> createReadableSinglePrice("1.120")

                            "lexus" -> createReadableRangePrice("1.530", "1.660")

                            else -> createReadableRangePrice("600", "1.000")
                        }
                    }

                    else -> createReadableRangePrice(
                        "500",
                        "700"
                    )
                }
            }

            "door_dent_severe" -> {
                when (binding.etInputMerkMobil.text.toString().lowercase()) {
                    "mitsubishi" -> {
                        when (binding.etInputModelMobil.text.toString().lowercase()) {
                            "mirage" -> createReadableRangePrice(
                                "883",
                                "1.150"
                            )

                            "xpander", "outlander px", "xpander cross" -> createReadableRangePrice(
                                "908",
                                "1.180"
                            )

                            else -> createReadableRangePrice("1.016", "1.320")
                        }
                    }

                    "daihatsu" -> {
                        when (binding.etInputModelMobil.text.toString().lowercase()) {
                            "xenia", "sigra", "terios", "rocky", "sirion", "ayla", "luxio" -> createReadableRangePrice(
                                "690",
                                "800"
                            )

                            else -> createReadableRangePrice("795", "1.024")
                        }
                    }

                    "honda" -> {
                        when (binding.etInputModelMobil.text.toString().lowercase()) {
                            "brio", "jazz", "fit", "city", "freed", "civic", "steam", "hrv", "brv" -> createReadableSinglePrice(
                                "1.500"
                            )

                            "crv", "accord", "odyssey", "crz" -> createReadableSinglePrice("1.645")

                            else -> createReadableRangePrice("1.300", "1.520")
                        }
                    }

                    "toyota" -> {
                        when (binding.etInputModelMobil.text.toString().lowercase()) {
                            "avanza", "calya" -> createReadableRangePrice(
                                "840",
                                "863"
                            )

                            "innova" -> createReadableRangePrice("922", "946")

                            "rush", "sienta" -> createReadableRangePrice(
                                "851",
                                "875"
                            )

                            "kijang" -> createReadableRangePrice("887", "911")

                            "fortuner", "voxy", "chr", "corolla cross" -> createReadableRangePrice(
                                "958",
                                "993"
                            )

                            "yaris", "etios" -> createReadableRangePrice(
                                "875",
                                "887"
                            )

                            "vios", "corolla" -> createReadableRangePrice(
                                "887",
                                "911"
                            )

                            "altis", "corona", "ft86" -> createReadableRangePrice(
                                "922",
                                "946"
                            )

                            "camry", "mark x" -> createReadableRangePrice(
                                "1.220",
                                "1.250"
                            )

                            "agya" -> createReadableSinglePrice("851")

                            "alphard" -> createReadableRangePrice("1.540", "1.590")

                            "lexus" -> createReadableRangePrice("1.980", "3.290")

                            else -> createReadableRangePrice("800", "1.000")
                        }
                    }

                    else -> createReadableRangePrice(
                        "700",
                        "1.645"
                    )
                }
            }

            "door_scratch_minor" -> {
                when (binding.etInputMerkMobil.text.toString().lowercase()) {
                    "mitsubishi" -> {
                        when (binding.etInputModelMobil.text.toString().lowercase()) {
                            "mirage" -> createReadableRangePrice(
                                "883",
                                "1.150"
                            )

                            "xpander", "outlander px", "xpander cross" -> createReadableRangePrice(
                                "908",
                                "1.180"
                            )

                            else -> createReadableRangePrice("1.016", "1.320")
                        }
                    }

                    "daihatsu" -> {
                        when (binding.etInputModelMobil.text.toString().lowercase()) {
                            "xenia", "sigra", "terios", "rocky", "sirion", "ayla", "luxio" -> createReadableRangePrice(
                                "500",
                                "550"
                            )

                            else -> createReadableRangePrice("560", "701")
                        }
                    }

                    "honda" -> {
                        when (binding.etInputModelMobil.text.toString().lowercase()) {
                            "brio", "jazz", "fit", "city", "freed", "civic", "steam", "hrv", "brv" -> createReadableSinglePrice(
                                "1.500"
                            )

                            "crv", "accord", "odyssey", "crz" -> createReadableSinglePrice("1.645")

                            else -> createReadableRangePrice("1.300", "1.520")
                        }
                    }

                    "toyota" -> {
                        when (binding.etInputModelMobil.text.toString().lowercase()) {
                            "avanza", "calya" -> createReadableSinglePrice("615")

                            "innova" -> createReadableSinglePrice("662")

                            "rush", "sienta" -> createReadableSinglePrice("615")

                            "kijang" -> createReadableSinglePrice("639")

                            "fortuner", "voxy", "chr", "corolla cross" -> createReadableSinglePrice(
                                "698"
                            )

                            "yaris", "etios" -> createReadableSinglePrice("627")

                            "vios", "corolla" -> createReadableSinglePrice("639")

                            "altis", "corona", "ft86" -> createReadableSinglePrice("662")

                            "camry", "mark x" -> createReadableSinglePrice("890")

                            "agya" -> createReadableSinglePrice("627")

                            "alphard" -> createReadableSinglePrice("1.120")

                            "lexus" -> createReadableRangePrice("1.530", "1.660")

                            else -> createReadableRangePrice("600", "1.000")
                        }
                    }

                    else -> createReadableRangePrice(
                        "200",
                        "1.234"
                    )
                }
            }

            "glass_shatter_severe" -> {
                when (binding.etInputMerkMobil.text.toString().lowercase()) {
                    "mitsubishi" -> {
                        when (binding.etInputModelMobil.text.toString().lowercase()) {
                            "mirage" -> createReadableSinglePrice("4.100")

                            "xpander", "outlander px", "xpander cross" -> createReadableSinglePrice(
                                "1.700"
                            )

                            else -> createReadableSinglePrice("3.400")
                        }
                    }

                    "daihatsu" -> {
                        when (binding.etInputModelMobil.text.toString().lowercase()) {
                            "xenia", "sigra", "terios", "rocky", "sirion", "ayla", "luxio" -> createReadableRangePrice(
                                "250",
                                "350"
                            )

                            else -> createReadableRangePrice("392", "490")
                        }
                    }

                    "honda" -> {
                        when (binding.etInputModelMobil.text.toString().lowercase()) {
                            "brio", "jazz", "fit", "city", "freed", "civic", "steam", "hrv", "brv" -> createReadableRangePrice(
                                "1.996.000",
                                "2.178.000"
                            )

                            "crv", "accord", "odyssey", "crz" -> createReadableRangePrice(
                                "2.117.000",
                                "2.359.000"
                            )

                            else -> createReadableRangePrice("326.000", "1.512.000")
                        }
                    }

                    "toyota" -> {
                        when (binding.etInputModelMobil.text.toString().lowercase()) {
                            "avanza", "calya" -> createReadableRangePrice(
                                "414",
                                "650"
                            )

                            "innova" -> createReadableRangePrice("508", "698")

                            "rush", "sienta" -> createReadableRangePrice(
                                "426",
                                "662"
                            )

                            "kijang" -> createReadableRangePrice("426", "662")

                            "fortuner", "voxy", "chr", "corolla cross" -> createReadableRangePrice(
                                "544",
                                "863"
                            )

                            "yaris", "etios" -> createReadableRangePrice(
                                "745",
                                "769"
                            )

                            "vios", "corolla" -> createReadableRangePrice(
                                "840",
                                "863"
                            )

                            "altis", "corona", "ft86" -> createReadableRangePrice(
                                "851",
                                "1.052"
                            )

                            "camry", "mark x" -> createReadableRangePrice(
                                "1.160",
                                "1.500"
                            )

                            "agya" -> createReadableRangePrice("414", "650")

                            "alphard" -> createReadableRangePrice("1.270", "1.720")

                            "lexus" -> createReadableRangePrice("1.820", "2.690")

                            else -> createReadableRangePrice("450", "1.500")
                        }
                    }

                    else -> createReadableRangePrice(
                        "350",
                        "2.359"
                    )
                }
            }

            "head_lamp_severe" -> {
                when (binding.etInputMerkMobil.text.toString().lowercase()) {
                    "mitsubishi" -> {
                        when (binding.etInputModelMobil.text.toString().lowercase()) {
                            "mirage" -> createReadableSinglePrice("2.200")

                            "xpander", "outlander px", "xpander cross" -> createReadableRangePrice(
                                "1.700",
                                "4.100"
                            )

                            else -> createReadableSinglePrice("11.000")
                        }
                    }

                    "daihatsu" -> {
                        when (binding.etInputModelMobil.text.toString().lowercase()) {
                            "xenia", "sigra", "terios", "rocky", "sirion", "ayla", "luxio" -> createReadableSinglePrice(
                                "70"
                            )

                            else -> createReadableRangePrice("78", "98")
                        }
                    }

                    "honda" -> {
                        when (binding.etInputModelMobil.text.toString().lowercase()) {
                            "brio", "jazz", "fit", "city", "freed", "civic", "steam", "hrv", "brv" -> createReadableSinglePrice(
                                "254"
                            )

                            "crv", "accord", "odyssey", "crz" -> createReadableSinglePrice("314")

                            else -> createReadableRangePrice("150", "300")
                        }
                    }

                    "toyota" -> {
                        when (binding.etInputModelMobil.text.toString().lowercase()) {
                            "avanza", "calya" -> createReadableSinglePrice("83")

                            "innova" -> createReadableSinglePrice("95")

                            "rush", "sienta" -> createReadableSinglePrice("83")

                            "kijang" -> createReadableSinglePrice("83")

                            "fortuner", "voxy", "chr", "corolla cross" -> createReadableSinglePrice(
                                "106"
                            )

                            "yaris", "etios" -> createReadableSinglePrice("83")

                            "vios", "corolla" -> createReadableSinglePrice("83")

                            "altis", "corona", "ft86" -> createReadableSinglePrice("83")

                            "camry", "mark x" -> createReadableSinglePrice("160")

                            "agya" -> createReadableSinglePrice("83")

                            "alphard" -> createReadableSinglePrice("190")

                            "lexus" -> createReadableRangePrice("260", "280")

                            else -> createReadableRangePrice("80", "200")
                        }
                    }

                    else -> createReadableRangePrice(
                        "70",
                        "200"
                    )
                }
            }

            "tail_lamp_severe" -> {
                when (binding.etInputMerkMobil.text.toString().lowercase()) {
                    "mitsubishi" -> {
                        when (binding.etInputModelMobil.text.toString().lowercase()) {
                            "mirage" -> createReadableSinglePrice("1.600")

                            "xpander", "outlander px", "xpander cross" -> createReadableSinglePrice(
                                "1.600"
                            )

                            else -> createReadableSinglePrice("3.500")
                        }
                    }

                    "daihatsu" -> {
                        when (binding.etInputModelMobil.text.toString().lowercase()) {
                            "xenia", "sigra", "terios", "rocky", "sirion", "ayla", "luxio" -> createReadableSinglePrice(
                                "70"
                            )

                            else -> createReadableRangePrice("78", "98")
                        }
                    }

                    "honda" -> {
                        when (binding.etInputModelMobil.text.toString().lowercase()) {
                            "brio", "jazz", "fit", "city", "freed", "civic", "steam", "hrv", "brv" -> createReadableSinglePrice(
                                "254"
                            )

                            "crv", "accord", "odyssey", "crz" -> createReadableSinglePrice("314")

                            else -> createReadableRangePrice("150", "300")
                        }
                    }

                    "toyota" -> {
                        when (binding.etInputModelMobil.text.toString().lowercase()) {
                            "avanza", "calya" -> createReadableSinglePrice("83")

                            "innova" -> createReadableSinglePrice("95")

                            "rush", "sienta" -> createReadableSinglePrice("83")

                            "kijang" -> createReadableSinglePrice("83")

                            "fortuner", "voxy", "chr", "corolla cross" -> createReadableSinglePrice(
                                "106"
                            )

                            "yaris", "etios" -> createReadableSinglePrice("83")

                            "vios", "corolla" -> createReadableSinglePrice("83")

                            "altis", "corona", "ft86" -> createReadableSinglePrice("83")

                            "camry", "mark x" -> createReadableSinglePrice("160")

                            "agya" -> createReadableSinglePrice("83")

                            "alphard" -> createReadableSinglePrice("190")

                            "lexus" -> createReadableRangePrice("260", "280")

                            else -> createReadableRangePrice("80", "200")
                        }
                    }

                    else -> createReadableRangePrice(
                        "70",
                        "200"
                    )
                }
            }

            else -> "Harga estimasi tidak bisa ditentukan"
        }
    }

    private fun createReadableRangePrice(lowestPrice: String, highestPrice: String): String {
        return "Rp. $lowestPrice.000 - Rp. $highestPrice.000"
    }

    private fun createReadableSinglePrice(price: String): String {
        return "Rp. $price.000"
    }

    override fun onResume() {
        super.onResume()
        if (binding.ivUploadedImage.alpha == 1f) {
            binding.ivUploadedImage.setOnClickListener {
                showDialogPickImage()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}