package com.fachmi.pens.carment.utils

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.MediaStore
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File

private const val TAG = "mydebug"

fun log(code: Any?, message: Any?) {
    Log.d("$TAG $code", message.toString())
}

fun Context.hasPermissions(permissionList: Array<String>): Boolean {
    val isGranted = permissionList.all { permission ->
        ContextCompat.checkSelfPermission(
            this, permission
        ) == PackageManager.PERMISSION_GRANTED
    }
    return isGranted
}

fun ActivityResultLauncher<Intent>.openCamera(context: Context?, outputFile: File) {
    val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { intent ->
        context?.let { context ->
            intent.resolveActivity(context.packageManager)?.also {
                val photoUri = FileProvider.getUriForFile(
                    context,
                    "com.fachmi.pens.carment.android.fileprovider",
                    outputFile
                )
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
            }
        }
    }
    launch(intent)
}

fun ActivityResultLauncher<Intent>.openImagePicker() {
    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).also { intent ->
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "image/*"
    }
    launch(intent)
}