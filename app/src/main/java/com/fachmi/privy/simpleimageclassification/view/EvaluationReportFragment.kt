package com.fachmi.privy.simpleimageclassification.view

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageFormat
import android.hardware.display.DisplayManager
import android.media.ImageReader
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.navigation.fragment.findNavController
import com.fachmi.privy.simpleimageclassification.databinding.FragmentEvaluationReportBinding
import com.fachmi.privy.simpleimageclassification.utils.showToast
import java.io.File
import java.io.FileOutputStream

class EvaluationReportFragment : Fragment() {

    private lateinit var binding: FragmentEvaluationReportBinding
    private lateinit var mediaProjectionManager: MediaProjectionManager

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
    }

    private fun showProgressBar() {
        binding.pbLoading.visibility = View.VISIBLE
        Handler(Looper.getMainLooper()).postDelayed({
            binding.apply {
                pbLoading.visibility = View.GONE
                nsvReportEvaluation.visibility = View.VISIBLE
            }
        }, 1000)
    }

    private fun handleClickListeners() {
        binding.apply {
            btnSimpanBuktiEvaluasi.setOnClickListener {
                context?.showToast("coming soon feature")
            }
            btnBack.setOnClickListener {
                findNavController().navigateUp()
            }
        }
    }

    private fun takeScreenshot() {
        context?.let { ctx ->
            mediaProjectionManager =
                ctx.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
            val captureIntent = mediaProjectionManager.createScreenCaptureIntent()
            startActivityForResult(captureIntent, REQUEST_CODE_SCREENSHOT)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        context?.let { ctx ->
            val displayMetrics = DisplayMetrics()
            requireActivity().windowManager.defaultDisplay.getMetrics(displayMetrics)
            val screenWidth = displayMetrics.widthPixels
            val screenHeight = displayMetrics.heightPixels
            val screenDensity = displayMetrics.densityDpi

            if (requestCode == REQUEST_CODE_SCREENSHOT && resultCode == Activity.RESULT_OK && data != null) {
                val mediaProjection = mediaProjectionManager.getMediaProjection(resultCode, data)
                val imageReader = ImageReader.newInstance(
                    screenWidth,
                    screenHeight,
                    ImageFormat.FLEX_RGBA_8888,
                    2
                )
                val virtualDisplay = mediaProjection.createVirtualDisplay(
                    "ScreenCapture",
                    screenWidth, screenHeight, screenDensity,
                    DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                    imageReader.surface, null, null
                )
                val handler = Handler(Looper.getMainLooper())
                handler.postDelayed({
                    val image = imageReader.acquireLatestImage()
                    val bitmap =
                        Bitmap.createBitmap(image.width, image.height, Bitmap.Config.ARGB_8888)
                    bitmap.copyPixelsFromBuffer(image.planes[0].buffer)
                    image.close()
                    mediaProjection.stop()
                    val file = File(
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                        "screenshot.png"
                    )
                    val outputStream = FileOutputStream(file)
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                    outputStream.flush()
                    outputStream.close()
                    ctx.showToast("Screenshot saved to ${file.absolutePath}")
                }, 1000)
            }
        }
    }

    companion object {
        private const val REQUEST_CODE_SCREENSHOT = 1
    }
}