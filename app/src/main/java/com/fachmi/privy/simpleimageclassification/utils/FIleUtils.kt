package com.fachmi.privy.simpleimageclassification.utils

import android.content.Context
import android.os.Environment
import android.widget.ImageView
import android.widget.Toast
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

const val COUNTRY_CODE_INDONESIA = "ID"
const val LANGUAGE_CODE_INDONESIA = "in"

val indonesiaLocale = Locale(LANGUAGE_CODE_INDONESIA, COUNTRY_CODE_INDONESIA)

fun createImageFile(context: Context?): File {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", indonesiaLocale).format(Date())
    val storageDir = context?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    return File.createTempFile(
        "IMG_${timeStamp}_",
        ".jpg",
        storageDir
    )
}

fun Context?.showToast(message: Any?) {
    Toast.makeText(this?.applicationContext, message.toString(), Toast.LENGTH_LONG).show()
}
