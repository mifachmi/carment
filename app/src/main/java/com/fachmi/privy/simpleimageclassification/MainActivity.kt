package com.fachmi.privy.simpleimageclassification

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import com.fachmi.privy.simpleimageclassification.databinding.ActivityMainBinding
import com.fachmi.privy.simpleimageclassification.utils.createImageFile
import com.fachmi.privy.simpleimageclassification.utils.hasPermissions
import com.fachmi.privy.simpleimageclassification.utils.log

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var tfLiteHelper: TFLiteHelper
    private var bitmap: Bitmap? = null
    private lateinit var imageUri: Uri

    private val cameraOutputFile by lazy { createImageFile(this) }

    private val PERMISSION_LIST = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        initModel()
//        checkStoragePermissionGranting()
//        setupClickListener()
    }

    private fun initModel() {
        tfLiteHelper = TFLiteHelper(this)
        tfLiteHelper.init()
    }

    private fun checkStoragePermissionGranting() {
        if (!this.hasPermissions(PERMISSION_LIST)) {
            requestPermissionsLauncher.launch(PERMISSION_LIST)
        }
    }

    private val requestPermissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissionList ->
            permissionList.entries.forEach { permission ->
                val isNotGranted = permission.value == false
                if (isNotGranted) {
                    Toast.makeText(this, "Permission is not granted", Toast.LENGTH_SHORT).show()
                }
                log(permission.key, permission.value)
            }
        }

//    private fun setupClickListener() {
//        binding.apply {
//            image.setOnClickListener {
//                Toast.makeText(this@MainActivity, "image", Toast.LENGTH_SHORT).show()
////                selectImageListener
////                cameraLauncher.openCamera(this@MainActivity, cameraOutputFile)
//                galleryLauncher.openImagePicker()
//            }
//
//            classify.setOnClickListener {
////                Toast.makeText(this@MainActivity, "classify", Toast.LENGTH_SHORT).show()
////                classifyImageListener
//                if (bitmap == null) {
//                    Toast.makeText(this@MainActivity, "Please select image", Toast.LENGTH_SHORT)
//                        .show()
//                }
//                bitmap?.let {
//                    tfLiteHelper.classifyImage(it)
//                    setLabel(tfLiteHelper.showResult())
//                }
////                if (bitmap != null) {
////                    tfLiteHelper.classifyImage(bitmap)
////                    setLabel(tfLiteHelper.showResult())
////                } else {
////                    Toast.makeText(this@MainActivity, "Please select image", Toast.LENGTH_SHORT)
////                        .show()
////                }
//            }
//        }
//    }
//
//    private val galleryLauncher =
//        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
//            result.data?.data?.let { imageUri ->
//                val outputUri = createImageFile(this).toUri()
//                binding.apply {
//                    try {
//                        bitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
//                        binding.image.setImageBitmap(bitmap)
//                        Toast.makeText(
//                            this@MainActivity, "Image is loaded: $imageUri", Toast.LENGTH_SHORT
//                        ).show()
//                    } catch (e: IOException) {
//                        e.printStackTrace()
//                    }
//                }
//
//                val listUri = listOf(imageUri, outputUri)
////                cropImageLauncher.launch(listUri)
//            }
//        }

    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val outputUri = createImageFile(this).toUri()
                val listUri = listOf(cameraOutputFile.toUri(), outputUri)
//                cropImageLauncher.launch(listUri)
            }
        }

//    private val cropImageLauncher =
//        registerForActivityResult(getUCropContracts()) { croppedImageUri ->
//            croppedImageUri?.let {
//                val croppedImageFile = croppedImageUri.toFile()
//                binding.apply {
//                    try {
//                        bitmap = MediaStore.Images.Media.getBitmap(contentResolver, it)
//                        binding.image.setImageBitmap(bitmap)
//                        Toast.makeText(
//                            this@MainActivity, "Image is loaded: $it", Toast.LENGTH_SHORT
//                        ).show()
//                    } catch (e: IOException) {
//                        e.printStackTrace()
//                    }
//                }
////                listener?.onImageSelected(croppedImageFile)
//            }
//        }

    var selectImageListener = View.OnClickListener {
        val SELECT_TYPE = "image/*"
        val SELECT_PICTURE = "Select Picture"

        Toast.makeText(this, "Select Image", Toast.LENGTH_SHORT).show()

        val intent = Intent()
        intent.type = SELECT_TYPE
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, SELECT_PICTURE), 12)
    }

//    var classifyImageListener = View.OnClickListener {
//        tfLiteHelper.classifyImage(bitmap)
//        setLabel(tfLiteHelper.showResult())
//    }

//    @Deprecated("Deprecated in Java")
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        if (requestCode == 12 && resultCode == RESULT_OK && data != null) {
//            imageUri = data.data!!
//            try {
//                bitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
//                binding.image.setImageBitmap(bitmap)
//            } catch (e: IOException) {
//                e.printStackTrace()
//            }
//        }
//    }

//    private fun setLabel(label: String?) {
//        binding.classifytext.text = label
//    }

//    override fun onDestroy() {
//        super.onDestroy()
//        listener = null
//    }

}