package com.example.firesafe.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.firesafe.theme.*
import com.example.firesafe.viewmodel.EmergencyFlowState
import com.example.firesafe.viewmodel.EmergencyViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Composable
fun PhotoProofScreen(
    viewModel: EmergencyViewModel,
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val uiState by viewModel.uiState.collectAsState()

    // Retrieve state data from AwaitingPhoto or fallback
    val locationData = when (val state = uiState) {
        is EmergencyFlowState.AwaitingPhoto -> state.location
        is EmergencyFlowState.AlertSent -> state.location
        else -> null
    }

    // Camera State
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    var capturedPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var isCapturing by remember { mutableStateOf(false) }

    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val imageCapture = remember { ImageCapture.Builder().build() }
    val cameraExecutor: ExecutorService = remember { Executors.newSingleThreadExecutor() }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    // Launch permission request if not granted
    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // Fallback simulation for emulator testing
    fun simulateCapture() {
        val simulatedFile = File(context.cacheDir, "simulated_fire.jpg")
        // Just write an empty file or dummy file path to pass as URI
        simulatedFile.writeText("simulated_image_content")
        capturedPhotoUri = Uri.fromFile(simulatedFile)
        Toast.makeText(context, "Using Mock Photo (Emulator Mode)", Toast.LENGTH_SHORT).show()
    }

    // Helper to capture photo
    fun takePhoto() {
        if (isCapturing) return
        isCapturing = true

        val photoFile = File(
            context.cacheDir,
            "smartflame_${System.currentTimeMillis()}.jpg"
        )

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    isCapturing = false
                    capturedPhotoUri = Uri.fromFile(photoFile)
                }

                override fun onError(exception: ImageCaptureException) {
                    isCapturing = false
                    // Fallback to simulated photo if physical capture fails or runs in emulator without support
                    simulateCapture()
                }
            }
        )
    }

    val currentTimestamp = remember {
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        if (capturedPhotoUri == null) {
            // CAMERA PREVIEW MODE
            if (hasCameraPermission) {
                // CameraX preview wrapping AndroidView
                AndroidView(
                    factory = { ctx ->
                        val previewView = PreviewView(ctx).apply {
                            scaleType = PreviewView.ScaleType.FILL_CENTER
                        }
                        
                        cameraProviderFuture.addListener({
                            val cameraProvider = cameraProviderFuture.get()
                            val preview = Preview.Builder().build().also {
                                it.surfaceProvider = previewView.surfaceProvider
                            }

                            try {
                                cameraProvider.unbindAll()
                                cameraProvider.bindToLifecycle(
                                    lifecycleOwner,
                                    CameraSelector.DEFAULT_BACK_CAMERA,
                                    preview,
                                    imageCapture
                                )
                            } catch (e: Exception) {
                                // Bind failed, show UI fallback
                            }
                        }, ContextCompat.getMainExecutor(ctx))
                        
                        previewView
                    },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                // Denied or loading permission UI
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Camera Permission Required",
                        style = MaterialTheme.typography.headlineMedium.copy(color = TextPrimary),
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Adding photo proof helps responders identify fire severity and deploy correct gear.",
                        style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                        colors = ButtonDefaults.buttonColors(containerColor = InteractiveTeal)
                    ) {
                        Text("GRANT CAMERA ACCESS", color = DarkBackground, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    TextButton(onClick = { simulateCapture() }) {
                        Text("SIMULATE CAMERA (DEMO)", color = InteractiveTeal)
                    }
                }
            }

            // Overlay controls for Camera view
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                // Shutter button at bottom
                if (hasCameraPermission) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 24.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Dummy spacer to balance skip button
                        Spacer(modifier = Modifier.width(48.dp))

                        // Capture circle
                        Box(
                            modifier = Modifier
                                .size(76.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.3f))
                                .border(4.dp, Color.White, CircleShape)
                                .padding(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                                    .background(Color.White)
                                    .border(2.dp, DarkBackground, CircleShape)
                                    .clip(CircleShape)
                                    .background(if (isCapturing) InteractiveTeal else Color.White)
                                    .align(Alignment.Center)
                                    .clickable { takePhoto() }
                            )
                        }

                        // Simulated mode trigger in case they want it
                        IconButton(
                            onClick = { simulateCapture() },
                            modifier = Modifier
                                .size(48.dp)
                                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Simulate Capture",
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        } else {
            // PHOTO PREVIEW & VERIFY MODE
            val bitmap = remember(capturedPhotoUri) {
                try {
                    val uri = capturedPhotoUri
                    if (uri != null && uri.path?.contains("simulated") != true) {
                        context.contentResolver.openInputStream(uri)?.use { stream ->
                            android.graphics.BitmapFactory.decodeStream(stream)
                        }?.asImageBitmap()
                    } else null
                } catch (e: Exception) {
                    null
                }
            }

            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                // If it is mock, show placeholder image, else load captured image
                if (capturedPhotoUri?.path?.contains("simulated") == true) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.DarkGray),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "MOCK PHOTO PROOF",
                                color = TextPrimary,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "[Active fire/smoke mock demonstration]",
                                color = InteractiveTeal,
                                fontSize = 14.sp
                            )
                        }
                    }
                } else {
                    // Show captured image
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap,
                            contentDescription = "Captured Photo Evidence",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Loading preview...", color = Color.White)
                        }
                    }
                }

                // GPS & Timestamp Overlay (Box on top of Image)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .background(Color.Black.copy(alpha = 0.65f))
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                ) {
                    Column {
                        Text(
                            text = "EVIDENCE TIMESTAMPED & LOCATED",
                            style = TextStyle(
                                color = InteractiveTeal,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Coords: ${locationData?.latitude ?: 37.7749}, ${locationData?.longitude ?: -122.4194}",
                            style = TextStyle(color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        )
                        Text(
                            text = "Time: $currentTimestamp",
                            style = TextStyle(color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        )
                        Text(
                            text = "Addr: ${locationData?.address ?: "Resolving..."}",
                            style = TextStyle(color = TextSecondary, fontSize = 12.sp)
                        )
                    }
                }

                // Confirm and Retake Buttons at bottom
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                            )
                        )
                        .padding(24.dp)
                ) {
                    Button(
                        onClick = {
                            viewModel.savePhotoAndProceed(capturedPhotoUri.toString())
                            onNext()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = InteractiveTeal),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        Text(
                            text = "CONFIRM & SEND EVIDENCE",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = DarkBackground
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = { capturedPhotoUri = null },
                        border = BorderStroke(1.dp, Color.White),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Text(
                            text = "RETAKE PHOTO",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                    }
                }
            }
        }
    }
}
