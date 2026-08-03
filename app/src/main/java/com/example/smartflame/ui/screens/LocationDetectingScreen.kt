package com.example.smartflame.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.smartflame.theme.DarkBackground
import com.example.smartflame.theme.InteractiveTeal
import com.example.smartflame.theme.TextPrimary
import com.example.smartflame.theme.TextSecondary
import com.example.smartflame.viewmodel.EmergencyFlowState
import com.example.smartflame.viewmodel.EmergencyViewModel

@Composable
fun LocationDetectingScreen(
    viewModel: EmergencyViewModel,
    onResolved: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    // Pulse animation for the Pin Icon
    val infiniteTransition = rememberInfiniteTransition(label = "PinPulse")
    val pinScale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PinScale"
    )

    // Check permissions and resolve location
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        if (fineGranted || coarseGranted) {
            viewModel.resolveLocationAndDispatch(onResolved = {})
        } else {
            // Even if denied, fall back to mock location flow so it is testable
            viewModel.resolveLocationAndDispatch(onResolved = {})
        }
    }

    LaunchedEffect(Unit) {
        val hasFine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (hasFine || hasCoarse) {
            viewModel.resolveLocationAndDispatch(onResolved = {})
        } else {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    // Determine state values
    val isResolved = uiState is EmergencyFlowState.AlertSent
    val addressText = if (uiState is EmergencyFlowState.AlertSent) {
        (uiState as EmergencyFlowState.AlertSent).location.address
    } else {
        ""
    }

    // Progress bar animation once resolved
    var progressTarget by remember { mutableFloatStateOf(0f) }
    val animatedProgress by animateFloatAsState(
        targetValue = progressTarget,
        animationSpec = tween(durationMillis = 1500, easing = LinearEasing),
        label = "ResolutionProgress"
    )

    LaunchedEffect(isResolved) {
        if (isResolved) {
            progressTarget = 1f
        }
    }

    // Auto-navigate once progress hits 100%
    LaunchedEffect(animatedProgress) {
        if (animatedProgress >= 1.0f) {
            onResolved()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        ) {
            // Pulsing pin icon
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = "Pulsing Location Pin",
                tint = InteractiveTeal,
                modifier = Modifier
                    .size(80.dp)
                    .scale(pinScale)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // State Title
            Text(
                text = if (isResolved) "Location Resolved" else "Detecting your location...",
                style = MaterialTheme.typography.headlineMedium.copy(color = TextPrimary),
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Subtitle / Address details
            Text(
                text = if (isResolved) addressText else "Securing GPS coordinates and finding nearby stations...",
                style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary),
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Progress Bar
            if (isResolved) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    LinearProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp),
                        color = InteractiveTeal,
                        trackColor = Color(0xFF2A2A2A)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Broadcasting Emergency Signal... ${(animatedProgress * 100).toInt()}%",
                        style = MaterialTheme.typography.labelMedium.copy(color = InteractiveTeal),
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                // Indeterminate bar while fetching GPS
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp),
                    color = InteractiveTeal,
                    trackColor = Color(0xFF2A2A2A)
                )
            }
        }
    }
}
