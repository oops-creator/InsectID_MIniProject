package com.farms.insectid

import android.content.Context
import android.content.res.AssetFileDescriptor
import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.RectF
import android.util.Log
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class InsectClassifier(private val context: Context) {

    private var interpreter: Interpreter? = null
    var isInitialized = false
        private set

    companion object {
        private const val TAG = "DigitClassifier"

        private const val FLOAT_TYPE_SIZE = 4
        private const val PIXEL_SIZE = 3

        private const val OUTPUT_CLASSES_COUNT = 4
    }


    private var inputImageWidth: Int = 0
    private var inputImageHeight: Int = 0
    private var modelInputSize: Int = 0

    @Throws(IOException::class)
    fun initializeInterpreter() {


        // Load the TF Lite model from asset folder
        val assetManager = context.assets
        val model = loadModelFile(assetManager, "modelInsect.tflite")
        val options = Interpreter.Options()
        options.setUseNNAPI(true)
        val interpreter = Interpreter(model, options)

        // Read input shape from model file.
        val inputShape = interpreter.getInputTensor(0).shape()
        inputImageWidth = inputShape[1]
        inputImageHeight = inputShape[2]
        modelInputSize = FLOAT_TYPE_SIZE * inputImageWidth *
                inputImageHeight * PIXEL_SIZE

        // Finish interpreter initialization.
        this.interpreter = interpreter

        isInitialized = true
        Log.d(TAG, "Initialized interpreter.")
    }


    @Throws(IOException::class)
    private fun loadModelFile(assetManager: AssetManager, filename: String): ByteBuffer {
        val fileDescriptor = assetManager.openFd(filename)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)

    }

    fun classify(bitmap: Bitmap): String {
        check(isInitialized) { "TF Lite not initialized" }


        val resizedImage = Bitmap.createScaledBitmap(
            bitmap,
            inputImageWidth,
            inputImageHeight,
            true
        )
        val byteBuffer = convertBitmapToByteBuffer(resizedImage)


        //Array to store the model output.
        val output = Array(1) { FloatArray(OUTPUT_CLASSES_COUNT) }

        // Run inference
        interpreter?.run(byteBuffer, output)


        val result = output[0]
        val maxIndex = result.indices.maxBy { result[it] } ?: -1

        val resultString =
            "Prediction Result: %d\nConfidence: %2f"
                .format(maxIndex, result[maxIndex])

        val res = result[maxIndex].toString()
        Log.e("result", resultString)
        return resultString
    }

//    private fun convertBitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
//        val byteBuffer = ByteBuffer.allocateDirect(modelInputSize)
//        byteBuffer.order(ByteOrder.nativeOrder())
//
//        val pixels = IntArray(inputImageWidth * inputImageHeight)
//        bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
//
//
//        for (pixelValue in pixels) {
//
//            // Normalize pixel value to 0 to 1
//            val normalizedPixelValue = pixelValue / 255.0f
//
//            byteBuffer.putFloat(normalizedPixelValue)
//
//        }
//
//        return byteBuffer
//    }

    fun convertBitmapToByteBuffer(bitmapIn: Bitmap,
                           width: Int = 224, height: Int = 224,
                           mean: Float = 0.0f,std: Float = 255.0f ): ByteBuffer {
        val bitmap = bitmapIn
        val inputImage = ByteBuffer.allocateDirect(1 * width * height * 3 * 4)
        inputImage.order(ByteOrder.nativeOrder())
        inputImage.rewind()

        val intValues = IntArray(width * height)
        bitmap.getPixels(intValues, 0, width, 0, 0, width, height)
        var pixel = 0
        for (y in 0 until height) {
            for (x in 0 until width) {
                val value = intValues[pixel++]

                // Normalize channel values
                inputImage.putFloat(((value shr 16 and 0xFF) - mean) / std)

                inputImage.putFloat(((value shr 8 and 0xFF) - mean) / std)

                inputImage.putFloat(((value and 0xFF) - mean) / std)
            }
        }

        inputImage.rewind()
        return inputImage
    }



}