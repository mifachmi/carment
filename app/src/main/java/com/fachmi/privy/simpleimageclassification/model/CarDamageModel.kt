package com.fachmi.privy.simpleimageclassification.model

import android.net.Uri

data class CarDamageModel(
    val carImage: Uri,
    val confidence: Float
)
