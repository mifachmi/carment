package com.fachmi.privy.simpleimageclassification.view

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
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
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toFile
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.fachmi.privy.simpleimageclassification.R
import com.fachmi.privy.simpleimageclassification.TFLiteHelper
import com.fachmi.privy.simpleimageclassification.databinding.DialogPickImageBinding
import com.fachmi.privy.simpleimageclassification.databinding.FragmentEvaluateImageBinding
import com.fachmi.privy.simpleimageclassification.utils.*
import java.io.File

class EvaluateImageFragment : Fragment(), ImagePickerListener {

    private lateinit var binding: FragmentEvaluateImageBinding
    private lateinit var tfLiteHelper: TFLiteHelper
    private var bitmap: Bitmap? = null
    private lateinit var imageUri: Uri
    private val cameraOutputFile by lazy { createImageFile(requireContext()) }

    private var listener: ImagePickerListener? = null

    companion object {
        private val PERMISSION_LIST = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
        )
    }

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

        showProgressBar()
        setupClickListener()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentEvaluateImageBinding.inflate(inflater, container, false)
        return binding.root
    }

    private fun showProgressBar() {
        binding.pbLoading.visibility = View.VISIBLE
        Handler(Looper.getMainLooper()).postDelayed({
            binding.apply {
                pbLoading.visibility = View.GONE
                nsvEvaluateImage.visibility = View.VISIBLE
                btnEvaluasiGambar.visibility = View.VISIBLE
            }
        }, 1000)
    }

    private fun setupClickListener() {
        binding.apply {
            btnPickImage.setOnClickListener {
                showDialogPickImage()
            }
            btnEvaluasiGambar.setOnClickListener {
                if (ivUploadedImage.alpha != 1f && etInputMerkMobil.text.isEmpty() && etInputTipeMobil.text.isEmpty() && etInputTahunKeluaranMobil.text.isEmpty()) {
                    requireContext().showToast("Mohon isi semua data terlebih dahulu")
                } else {
                    findNavController().navigate(R.id.action_evaluateImageFragment_to_evaluationReportFragment)
                }
            }
        }
    }

    private fun showDialogPickImage() {
        val dialog = Dialog(requireContext())
        val bindingView = DialogPickImageBinding.inflate(layoutInflater)
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
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
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED
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
                listener?.onImageSelected(croppedImageFile)
            }
        }

    override fun onImageSelected(imageFile: File) {
        binding.apply {
            binding.apply {
                ivUploadedImage.setImageURI(imageFile.toUri())
                ivUploadedImage.alpha = 1f
                btnPickImage.visibility = View.GONE
            }
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