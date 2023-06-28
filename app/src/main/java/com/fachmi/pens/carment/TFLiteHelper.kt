package com.fachmi.pens.carment

import android.app.Activity
import android.graphics.Bitmap
import android.util.Log
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.common.TensorOperator
import org.tensorflow.lite.support.common.TensorProcessor
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.image.ops.ResizeWithCropOrPadOp
import org.tensorflow.lite.support.label.TensorLabel
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.util.*

class TFLiteHelper constructor(private val activity: Activity) {

    companion object {
        private const val MODEL_NAME = "efficientnetv2b0_try2_80.tflite"
//        private const val MODEL_NAME = "10classes_mobilenetv2.tflite"
//        private const val MODEL_NAME = "car_damage_model.tflite"
    }

    private lateinit var tflite: Interpreter
    private var labels: List<String>? = null

    private lateinit var inputImageBuffer: TensorImage
    private lateinit var outputProbabilityBuffer: TensorBuffer
    private lateinit var probabilityProcessor: TensorProcessor

    private var imageSizeX = 224
    private var imageSizeY = 224

    private val IMAGE_MEAN = 0.0f
    private val IMAGE_STD = 1.0f

    private val PROBABILITY_MEAN = 0.0f
    private val PROBABILITY_STD = 255.0f

    fun init() {
        try {
            val opt: Interpreter.Options = Interpreter.Options()
            tflite = Interpreter(loadModelFile(activity), opt)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // to load the model file
    private fun loadModelFile(activity: Activity): MappedByteBuffer {
        val fileDescriptor = activity.assets.openFd(MODEL_NAME)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    // to doing preprocessing on the image
    private fun loadImage(bitmap: Bitmap): TensorImage {
        inputImageBuffer.load(bitmap)

        // Creates processor for the TensorImage
        val cropSize = bitmap.width.coerceAtMost(bitmap.height)

        // TODO(b/143564309): Fuse ops inside ImageProcessor.
        val imageProcessor = ImageProcessor.Builder()
            .add(ResizeWithCropOrPadOp(cropSize, cropSize))
            .add(ResizeOp(imageSizeX, imageSizeY, ResizeOp.ResizeMethod.NEAREST_NEIGHBOR))
            .add(getPreprocessNormalizeOp())
//            .add(Rot90Op(-1))
            .build()

        return imageProcessor.process(inputImageBuffer)
    }

    private fun getPreprocessNormalizeOp(): TensorOperator {
        return NormalizeOp(IMAGE_MEAN, IMAGE_STD)
    }

    fun classifyImage(bitmap: Bitmap) {
        val imageTensorIndex = 0
        val imageShape = tflite.getInputTensor(imageTensorIndex).shape() // {1, height, width, 3}

        imageSizeY = imageShape[1]
        imageSizeX = imageShape[2]
        val imageDataType = tflite.getInputTensor(imageTensorIndex).dataType()

        val probabilityTensorIndex = 0
        val probabilityShape =
            tflite.getOutputTensor(probabilityTensorIndex).shape() // {1, NUM_CLASSES}

        val probabilityDataType = tflite.getOutputTensor(probabilityTensorIndex).dataType()

        inputImageBuffer = TensorImage(imageDataType)
        outputProbabilityBuffer =
            TensorBuffer.createFixedSize(probabilityShape, probabilityDataType)
        probabilityProcessor = TensorProcessor.Builder().add(getPostProcessNormalizeOp()).build()

        inputImageBuffer = loadImage(bitmap)

        tflite.run(inputImageBuffer.buffer, outputProbabilityBuffer.buffer.rewind())
    }

    private fun getPostProcessNormalizeOp(): TensorOperator {
        return NormalizeOp(PROBABILITY_MEAN, PROBABILITY_STD)
    }

    fun showResult(): String {
        try {
            labels = FileUtil.loadLabels(activity.applicationContext, "10classes_label.txt")
//            labels = FileUtil.loadLabels(activity.applicationContext, "vegs.txt")
        } catch (e: Exception) {
            e.printStackTrace()
            return "Error reading label file"
        }

        val labeledProbability = TensorLabel(
            labels!!, probabilityProcessor.process(outputProbabilityBuffer)
        ).mapWithFloatValue
        Log.d("labeledProbability", "showResult: ${labeledProbability.values}")
        val maxValueInMap = Collections.max(labeledProbability.values)
        var result: String? = null
        for ((key, value) in labeledProbability.entries) {
            if (value == maxValueInMap) {
                result = key
            }
        }

        return result!!
    }
}