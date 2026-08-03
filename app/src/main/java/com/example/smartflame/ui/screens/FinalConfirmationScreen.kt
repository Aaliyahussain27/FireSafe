package com.example.smartflame.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartflame.theme.*
import com.example.smartflame.viewmodel.EmergencyFlowState
import com.example.smartflame.viewmodel.EmergencyViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun FinalConfirmationScreen(
    viewModel: EmergencyViewModel,
    onExit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    val state = uiState as? EmergencyFlowState.Confirmed ?: return
    val location = state.location
    val confirmation = state.alertConfirmation
    val station = state.fireStation

    // Parse the photo if it exists
    val bitmap = remember(state.photoPath) {
        try {
            val path = state.photoPath
            if (path != null && !path.contains("simulated")) {
                val uri = Uri.parse(path)
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    android.graphics.BitmapFactory.decodeStream(stream)
                }?.asImageBitmap()
            } else null
        } catch (e: Exception) {
            null
        }
    }

    val currentTimestamp = remember {
        SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault()).format(Date())
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxSize()
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Verified Header
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Success Verified",
                    tint = SuccessGreen,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "VERIFIED & FORWARDED",
                    style = MaterialTheme.typography.headlineMedium.copy(color = TextPrimary),
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Alert ID: ${confirmation.alertId} • Central Dispatch",
                    style = MaterialTheme.typography.labelMedium.copy(color = TextSecondary),
                    fontWeight = FontWeight.Medium
                )
            }

            // Map thumbnail card (Custom Drawn city grid map demonstrating "Live Location Shared")
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(ElevatedCardBackground)
                    .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
            ) {
                // Drawing a premium grid map of the city dispatch route
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    // Draw roads/grids
                    val gridColor = Color(0xFF2A2A2A)
                    val roadWidth = 8.dp.toPx()

                    // Vertical roads
                    for (x in listOf(0.2f, 0.5f, 0.8f)) {
                        drawLine(
                            color = gridColor,
                            start = Offset(w * x, 0f),
                            end = Offset(w * x, h),
                            strokeWidth = roadWidth
                        )
                    }

                    // Horizontal roads
                    for (y in listOf(0.3f, 0.7f)) {
                        drawLine(
                            color = gridColor,
                            start = Offset(0f, h * y),
                            end = Offset(w, h * y),
                            strokeWidth = roadWidth
                        )
                    }

                    // Draw route path between responder and user
                    // Responder is at (w * 0.2f, h * 0.7f), user is at (w * 0.8f, h * 0.3f)
                    val routePath = androidx.compose.ui.graphics.Path().apply {
                        moveTo(w * 0.2f, h * 0.7f)
                        lineTo(w * 0.5f, h * 0.7f)
                        lineTo(w * 0.5f, h * 0.3f)
                        lineTo(w * 0.8f, h * 0.3f)
                    }

                    drawPath(
                        path = routePath,
                        color = InteractiveTeal,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(
                            width = 4.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 12f), 0f)
                        )
                    )

                    // Draw Responder Pin (Red circle)
                    drawCircle(
                        color = EmergencyRed,
                        center = Offset(w * 0.2f, h * 0.7f),
                        radius = 8.dp.toPx()
                    )
                    drawCircle(
                        color = Color.White,
                        center = Offset(w * 0.2f, h * 0.7f),
                        radius = 3.dp.toPx()
                    )

                    // Draw User Pin (Teal circle with glowing pulse)
                    drawCircle(
                        color = InteractiveTeal.copy(alpha = 0.3f),
                        center = Offset(w * 0.8f, h * 0.3f),
                        radius = 16.dp.toPx()
                    )
                    drawCircle(
                        color = InteractiveTeal,
                        center = Offset(w * 0.8f, h * 0.3f),
                        radius = 8.dp.toPx()
                    )
                    drawCircle(
                        color = Color.White,
                        center = Offset(w * 0.8f, h * 0.3f),
                        radius = 3.dp.toPx()
                    )
                }

                // Overlay Text label
                Box(
                    modifier = Modifier
                        .padding(12.dp)
                        .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .align(Alignment.BottomStart)
                ) {
                    Text(
                        text = "LIVE LOCATION SHARED",
                        color = InteractiveTeal,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Summary Row: Photo, Timestamp, Responder Name, Vehicle ETA
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(ElevatedCardBackground)
                    .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Image Thumbnail
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF2A2A2A)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (bitmap != null) {
                            Image(
                                bitmap = bitmap,
                                contentDescription = "Proof Thumbnail",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else if (state.photoPath != null && state.photoPath.contains("simulated")) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.DarkGray),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "MOCK\nPROOF",
                                    color = InteractiveTeal,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    lineHeight = 12.sp
                                )
                            }
                        } else {
                            Text(
                                "NO\nPHOTO",
                                color = TextSecondary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                lineHeight = 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Summary Text info
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "RESPONDER: ${station.responderName.uppercase()}",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = InteractiveTeal,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "ETA: ${station.etaMinutes} mins (${station.distanceKm} km)",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Sent: $currentTimestamp",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            // Buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Call Help Now (EmergencyRed, primary — launches dialer intent)
                Button(
                    onClick = {
                        val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                            data = Uri.parse("tel:911")
                        }
                        context.startActivity(dialIntent)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmergencyRed),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = "Call Dialer Icon",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "CALL HELP NOW (911)",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                }

                // Exit to Home (Outlined / Secondary)
                OutlinedButton(
                    onClick = {
                        viewModel.resetToHome()
                        onExit()
                    },
                    border = BorderStroke(1.dp, CardBorder),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text(
                        text = "EXIT TO HOME",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
