package com.fachmi.privy.simpleimageclassification.model

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CarDamageModel(
    val carImage: Uri,
    val date: String,
    val tingkatKerusakan: String,
    val tindakanReparasi: String,
    val estimasiHarga: String
) : Parcelable
