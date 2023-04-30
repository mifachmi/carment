package com.fachmi.privy.simpleimageclassification.utils

import java.io.File

interface ImagePickerListener {
    fun onImageSelected(imageFile: File)
}