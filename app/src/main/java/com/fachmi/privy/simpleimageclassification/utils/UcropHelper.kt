package com.fachmi.privy.simpleimageclassification.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.activity.result.contract.ActivityResultContract
import com.yalantis.ucrop.UCrop

fun getUCropContracts(): ActivityResultContract<List<Uri>, Uri> {
    return object : ActivityResultContract<List<Uri>, Uri>() {
        override fun createIntent(context: Context, input: List<Uri>): Intent {
            val inputUri = input[0]
            val outputUri = input[1]

            val uCrop = UCrop.of(inputUri, outputUri)
                .withAspectRatio(1f, 1f)
            return uCrop.getIntent(context)
        }

        override fun parseResult(resultCode: Int, intent: Intent?): Uri {
            return intent.let { data ->
                (if (resultCode == Activity.RESULT_OK) {
                    data?.let { UCrop.getOutput(it) }
                } else {
                    val t = data?.let { UCrop.getError(it) }
                    Log.e("UCrop Contracts", t?.message.toString(), t)
                    null
                })!!
            }
        }
    }
}