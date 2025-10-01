package com.example.qr_app.ui.qr

import android.annotation.SuppressLint
import android.util.Log
import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors

@SuppressLint("UnsafeOptInUsageError")
@Composable
fun QrCameraPreview(
    modifier: Modifier = Modifier,
    lifecycleOwner: LifecycleOwner,
    onQrScanned: (String) -> Unit
) {
    val context = lifecycleOwner as android.content.Context
    val cameraProviderFuture = remember {
        ProcessCameraProvider.getInstance(context)
    }

    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            }

            val cameraExecutor = Executors.newSingleThreadExecutor()

            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()

                // Preview
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                // Cámara trasera
                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                // Analizador de imágenes para QR
                val imageAnalyzer = ImageAnalysis.Builder().build().also {
                    it.setAnalyzer(cameraExecutor) { imageProxy ->
                        val mediaImage = imageProxy.image
                        if (mediaImage != null) {
                            val image = InputImage.fromMediaImage(
                                mediaImage,
                                imageProxy.imageInfo.rotationDegrees
                            )
                            val scanner = BarcodeScanning.getClient()
                            scanner.process(image)
                                .addOnSuccessListener { barcodes ->
                                    for (barcode in barcodes) {
                                        barcode.rawValue?.let { qrValue ->
                                            Log.d("QR", "Detectado: $qrValue")
                                            onQrScanned(qrValue)
                                        }
                                    }
                                }
                                .addOnFailureListener { e ->
                                    Log.e("QR", "Error al leer código", e)
                                }
                                .addOnCompleteListener {
                                    imageProxy.close()
                                }
                        } else {
                            imageProxy.close()
                        }
                    }
                }

                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageAnalyzer
                    )
                } catch (e: Exception) {
                    Log.e("QR", "Error inicializando cámara", e)
                }
            }, ContextCompat.getMainExecutor(ctx))

            previewView
        },
        modifier = modifier
    )
}
