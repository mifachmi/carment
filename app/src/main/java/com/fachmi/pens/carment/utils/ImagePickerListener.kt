package com.fachmi.pens.carment.utils

import java.io.File

interface ImagePickerListener {
    fun onImageSelected(imageFile: File)
}