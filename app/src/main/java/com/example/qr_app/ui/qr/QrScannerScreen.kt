package com.example.qr_app.ui.qr

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.qr_app.model.Estudiante
import com.example.qr_app.ui.selection.SelectionViewModel
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import org.json.JSONObject
import java.util.concurrent.Executors

@SuppressLint("UnsafeOptInUsageError")
@Composable
fun QrScannerScreen(
    navController: NavController,
    viewModel: SelectionViewModel,
    onBack: () -> Unit,
    onResult: (Estudiante) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var isProcessing by remember { mutableStateOf(false) }
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA) ==
                    PackageManager.PERMISSION_GRANTED
        )
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            // procesa imagen de galería y regresa Estudiante vía callback
            processGalleryQrCode(
                uri = it,
                context = context,
                onSuccess = { estudiante ->
                    // actualizar VM y devolver resultado a pantalla anterior
                    isProcessing = true
                    viewModel.setEstudiante(estudiante)
                    onResult(estudiante)
                    navController.popBackStack()
                },
                onFailure = { err ->
                    Log.e("QrScanner", "Error procesando galería", err)
                    Toast.makeText(context, "No se pudo leer QR desde galería", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    // Executor con DisposableEffect para cerrarlo al salir
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    DisposableEffect(Unit) {
        onDispose { cameraExecutor.shutdown() }
    }

    LaunchedEffect(Unit) {
        if (!hasPermission) cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (!isProcessing) {
            Column(modifier = Modifier.fillMaxSize()) {
                if (hasPermission) {
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
                                try {
                                    val cameraProvider = cameraProviderFuture.get()
                                    val preview = Preview.Builder().build().also {
                                        it.setSurfaceProvider(previewView.surfaceProvider)
                                    }

                                    val scanner = BarcodeScanning.getClient()

                                    val imageAnalyzer = ImageAnalysis.Builder()
                                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                        .build()
                                        .also { analysis ->
                                            analysis.setAnalyzer(cameraExecutor) { imageProxy ->
                                                processQrCode(
                                                    scanner = scanner,
                                                    imageProxy = imageProxy,
                                                    onSuccess = { estudiante ->
                                                        // Cuando se detecta correctamente:
                                                        isProcessing = true
                                                        viewModel.setEstudiante(estudiante)
                                                        onResult(estudiante)
                                                        navController.popBackStack()
                                                    },
                                                    onFailure = { err ->
                                                        Log.e("QrScanner", "No se pudo procesar QR", err)
                                                        // solo loguear: el imageProxy se cierra en processQrCode
                                                    }
                                                )
                                            }
                                        }

                                    cameraProvider.unbindAll()
                                    cameraProvider.bindToLifecycle(
                                        lifecycleOwner,
                                        CameraSelector.DEFAULT_BACK_CAMERA,
                                        preview,
                                        imageAnalyzer
                                    )
                                    Log.d("QrScanner", "CameraX bindToLifecycle OK")
                                } catch (e: Exception) {
                                    Log.e("QrScanner", "Error inicializando CameraX", e)
                                }
                            }, ContextCompat.getMainExecutor(ctx))

                            previewView
                        },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    )
                } else {
                    Button(onClick = { cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA) }) {
                        Text("Conceder permiso de cámara")
                    }
                }

                // Botón galería
                Button(onClick = { galleryLauncher.launch("image/*") }, modifier = Modifier.fillMaxWidth()) {
                    Text("Seleccionar QR desde galería")
                }
            }
        } else {
            // Spinner pequeño y centrado
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
    }
}

private fun parseQrValue(rawValue: String): Estudiante? {
    return try {
        val json = JSONObject(rawValue)
        Estudiante(
            nombre = json.optString("nombre"),
            tipo_identificacion = json.optString("tipo_identificacion"),
            numero_identificacion = json.optString("numero_identificacion"),
            correo = json.optString("correo"),
            telefono = json.optString("telefono"),
            jornada = json.optString("jornada"),
            grado = json.optString("grado"),
            codigo_grado = json.optInt("codigo_grado"),
            acudiente = json.optString("acudiente"),
            numero_acudiente = json.optString("numero_acudiente"),
            id = json.optString("id")
        )
    } catch (e: Exception) {
        Log.w("QrScanner", "QR no es JSON válido: ${e.message}")
        null
    }
}

@OptIn(ExperimentalGetImage::class)
private fun processQrCode(
    scanner: com.google.mlkit.vision.barcode.BarcodeScanner,
    imageProxy: ImageProxy,
    onSuccess: (Estudiante) -> Unit,
    onFailure: (Throwable?) -> Unit
) {
    val mediaImage = imageProxy.image
    if (mediaImage == null) {
        try { imageProxy.close() } catch (_: Throwable) {}
        Log.w("QrScanner", "ImageProxy.image es null")
        onFailure(null)
        return
    }

    val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
    scanner.process(image)
        .addOnSuccessListener { barcodes ->
            var handled = false
            for (barcode in barcodes) {
                val raw = barcode.rawValue
                Log.d("QrScanner", "Barcode found: format=${barcode.format}, type=${barcode.valueType}, raw=${raw?.take(200)}")
                if (raw.isNullOrBlank()) continue

                // Intentar parsear JSON -> Estudiante
                val estudiante = parseQrValue(raw)
                if (estudiante != null) {
                    Log.i("QrScanner", "QR parsed as Estudiante: ${estudiante.numero_identificacion}")
                    onSuccess(estudiante)
                    handled = true
                    break
                } else {
                    // Si no es JSON válido, opcionalmente podrías aceptar raw como número
                    // Ejemplo: si raw es solo el número de identificación:
                    val posibleId = raw.trim()
                    if (posibleId.all { it.isDigit() }) {
                        // crear Estudiante mínimo con número identificacion y devolverlo
                        val estMin = Estudiante(
                            nombre = "",
                            tipo_identificacion = "",
                            numero_identificacion = posibleId,
                            correo = "",
                            telefono = "",
                            jornada = "",
                            grado = "",
                            codigo_grado = 0,
                            acudiente = "",
                            numero_acudiente = "",
                            id = ""
                        )
                        onSuccess(estMin)
                        handled = true
                        break
                    }
                    Log.w("QrScanner", "QR no es JSON y no es solo número: raw=${raw.take(200)}")
                }
            }

            if (!handled) {
                Log.d("QrScanner", "Se detectaron códigos, pero ninguno parseó a Estudiante")
                onFailure(null)
            }
        }
        .addOnFailureListener { e ->
            Log.e("QrScanner", "Error en scanner.process", e)
            onFailure(e)
        }
        .addOnCompleteListener {
            try { imageProxy.close() } catch (t: Throwable) { Log.w("QrScanner", "Error cerrando imageProxy", t) }
        }
}

private fun processGalleryQrCode(
    uri: Uri,
    context: Context,
    onSuccess: (Estudiante) -> Unit,
    onFailure: (Throwable?) -> Unit
) {
    try {
        val image = InputImage.fromFilePath(context, uri)
        val scanner = BarcodeScanning.getClient()
        scanner.process(image)
            .addOnSuccessListener { barcodes ->
                var handled = false
                for (barcode in barcodes) {
                    val raw = barcode.rawValue
                    Log.d("QrScanner", "Gallery barcode raw=${raw?.take(200)}")
                    if (raw.isNullOrBlank()) continue
                    val estudiante = parseQrValue(raw)
                    if (estudiante != null) {
                        onSuccess(estudiante)
                        handled = true
                        break
                    } else {
                        // same fallback: numeric-only raw
                        val posibleId = raw.trim()
                        if (posibleId.all { it.isDigit() }) {
                            val estMin = Estudiante(
                                nombre = "",
                                tipo_identificacion = "",
                                numero_identificacion = posibleId,
                                correo = "",
                                telefono = "",
                                jornada = "",
                                grado = "",
                                codigo_grado = 0,
                                acudiente = "",
                                numero_acudiente = "",
                                id = ""
                            )
                            onSuccess(estMin)
                            handled = true
                            break
                        }
                    }
                }
                if (!handled) onFailure(null)
            }
            .addOnFailureListener { e -> onFailure(e) }
    } catch (e: Exception) {
        Log.e("QrScanner", "Error creando InputImage desde URI", e)
        onFailure(e)
    }
}
