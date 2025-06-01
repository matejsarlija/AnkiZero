package com.example.ankizero.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.core.content.ContextCompat
// import android.util.Log // Replaced by AppLogger
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.Executor

object CameraUtils {

    fun hasCameraPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun createFileForImage(context: Context): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(System.currentTimeMillis())
        val storageDir: File? = context.externalCacheDir ?: context.cacheDir // Prefer external cache
        return File(storageDir, "JPEG_${timeStamp}_.jpg")
    }

    fun takePhoto(
        imageCapture: ImageCapture?,
        context: Context,
        executor: Executor,
        onImageCaptured: (Uri) -> Unit,
        onError: (ImageCaptureException) -> Unit
    ) {
        val imageCaptureInstance = imageCapture ?: run {
            AppLogger.e("CameraUtils", "ImageCapture instance is null.")
            onError(ImageCaptureException(ImageCapture.ERROR_INVALID_CAMERA, "ImageCapture is null", null))
            return
        }

        val photoFile = createFileForImage(context)
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCaptureInstance.takePicture(
            outputOptions,
            executor, // Use the provided executor for callbacks
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val savedUri = output.savedUri ?: Uri.fromFile(photoFile)
                    AppLogger.d("CameraUtils", "Photo capture succeeded: $savedUri")
                    onImageCaptured(savedUri)
                }

                override fun onError(exc: ImageCaptureException) {
                    AppLogger.e("CameraUtils", "Photo capture failed: ${exc.message}", exc)
                    onError(exc)
                }
            }
        )
    }
}
