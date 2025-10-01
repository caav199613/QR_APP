package com.example.qr_app.ui.scanner

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.ViewGroup
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.qr_app.model.Estudiante
import com.example.qr_app.ui.selection.SelectionViewModel
import com.google.mlkit.vision.barcode.Barcode
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import org.json.JSONObject
import java.util.concurrent.Executors
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import com.google.mlkit.vision.common.InputImage


@SuppressLint("UnsafeOptInUsageError")
@Composable
fun QrScannerScreen(
    navController: NavController,
    viewModel: SelectionViewModel = viewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var isProcessing by remember { mutableStateOf(false) }

    // 📌 Picker para galería
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            uri?.let {
                processGalleryQrCode(
                    uri = it,
                    context = context,
                    viewModel = viewModel,
                    navController = navController
                ) {
                    isProcessing = true
                }
            }
        }
    )

    Box(modifier = Modifier.fillMaxSize()) {
        if (!isProcessing) {
            Column {
                // Cámara con CameraX
                AndroidView(
                    factory = { ctx ->
                        val previewView = PreviewView(ctx).apply {
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                        }

                        val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                        cameraProviderFuture.addListener({
                            val cameraProvider = cameraProviderFuture.get()

                            val preview = Preview.Builder().build().also {
                                it.setSurfaceProvider(previewView.surfaceProvider)
                            }

                            val scanner = BarcodeScanning.getClient()
                            val executor = Executors.newSingleThreadExecutor()

                            val imageAnalyzer = ImageAnalysis.Builder()
                                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                .build()
                                .also {
                                    it.setAnalyzer(executor) { imageProxy ->
                                        processQrCode(scanner, imageProxy, viewModel, navController) {
                                            isProcessing = true
                                        }
                                    }
                                }

                            try {
                                cameraProvider.unbindAll()
                                cameraProvider.bindToLifecycle(
                                    lifecycleOwner,
                                    CameraSelector.DEFAULT_BACK_CAMERA,
                                    preview,
                                    imageAnalyzer
                                )
                            } catch (e: Exception) {
                                Log.e("QrScanner", "Error al iniciar cámara", e)
                            }
                        }, ContextCompat.getMainExecutor(ctx))

                        previewView
                    },
                    modifier = Modifier.weight(1f)
                )

                // 📌 Botón galería
                Button(
                    onClick = { galleryLauncher.launch("image/*") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Seleccionar QR desde galería")
                }
            }
        } else {
            CircularProgressIndicator(modifier = Modifier.fillMaxSize())
        }
    }
}


private fun processGalleryQrCode(
    uri: Uri,
    context: Context,
    viewModel: SelectionViewModel,
    navController: NavController,
    onResult: () -> Unit
) {
    try {
        val image = InputImage.fromFilePath(context, uri)
        val scanner = BarcodeScanning.getClient()
        scanner.process(image)
            .addOnSuccessListener { barcodes ->
                for (barcode in barcodes) {
                    if (barcode.valueType == Barcode.TYPE_TEXT) {
                        val rawValue = barcode.rawValue
                        try {
                            val json = JSONObject(rawValue!!)
                            val estudiante = Estudiante(
                                nombre = json.getString("nombre"),
                                tipo_identificacion = json.getString("tipo_identificacion"),
                                numero_identificacion = json.getString("numero_identificacion"),
                                correo = json.getString("correo"),
                                telefono = json.getString("telefono"),
                                jornada = json.getString("jornada"),
                                grado = json.getString("grado"),
                                codigo_grado = json.getInt("codigo_grado"),
                                acudiente = json.getString("acudiente"),
                                numero_acudiente = json.getString("numero_acudiente")
                            )
                            viewModel.setEstudiante(estudiante)
                            onResult()
                            navController.popBackStack()
                            break
                        } catch (e: Exception) {
                            Log.e("QrScanner", "Error parsing QR desde galería: ${e.message}")
                        }
                    }
                }
            }
            .addOnFailureListener {
                Log.e("QrScanner", "Error al procesar QR desde galería: ${it.message}")
            }
    } catch (e: Exception) {
        Log.e("QrScanner", "Error cargando imagen de galería: ${e.message}")
    }
}

