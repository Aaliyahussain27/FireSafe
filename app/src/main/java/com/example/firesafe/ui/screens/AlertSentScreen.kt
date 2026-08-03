package com.example.firesafe.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.firesafe.theme.*
import com.example.firesafe.viewmodel.EmergencyFlowState
import com.example.firesafe.viewmodel.EmergencyViewModel

@Composable
fun AlertSentScreen(
    viewModel: EmergencyViewModel,
    onNext: () -> Unit,
    onCancelled: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    // We only expect to be in AlertSent state, retrieve values
    val state = uiState as? EmergencyFlowState.AlertSent ?: return
    val station = state.fireStation
    val confirmation = state.alertConfirmation

    // 3s Timer for Auto-navigation
    var timerProgressTarget by remember { mutableFloatStateOf(0f) }
    val animatedTimerProgress by animateFloatAsState(
        targetValue = timerProgressTarget,
        animationSpec = tween(durationMillis = 3000, easing = LinearEasing),
        label = "AutoNavigateTimer"
    )

    LaunchedEffect(state.isCancelling) {
        if (!state.isCancelling) {
            // Start or resume timer when not showing cancel dialog
            timerProgressTarget = 1f
        } else {
            // Pause timer if dialog is shown
            timerProgressTarget = animatedTimerProgress
        }
    }

    // Trigger auto-navigation on timer end
    LaunchedEffect(animatedTimerProgress) {
        if (animatedTimerProgress >= 1.0f) {
            viewModel.proceedToPhoto()
            onNext()
        }
    }

    if (state.isCancelling) {
        AlertDialog(
            onDismissRequest = { viewModel.showCancelConfirmationDialog(false) },
            containerColor = ElevatedCardBackground,
            titleContentColor = TextPrimary,
            textContentColor = TextSecondary,
            title = {
                Text(
                    text = "Cancel Emergency Alert?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to cancel the dispatch? This will notify emergency services that the response is no longer required.",
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.showCancelConfirmationDialog(false)
                        viewModel.cancelAlert(onCancelled)
                    }
                ) {
                    Text(text = "YES, CANCEL ALERT", color = EmergencyRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { viewModel.showCancelConfirmationDialog(false) }
                ) {
                    Text(text = "KEEP ACTIVE", color = InteractiveTeal)
                }
            },
            modifier = Modifier.border(1.dp, CardBorder, RoundedCornerShape(28.dp))
        )
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
            Spacer(modifier = Modifier.height(16.dp))

            // Checkmark Header Circle
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(InteractiveTeal.copy(alpha = 0.2f))
                        .border(2.dp, InteractiveTeal, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Success Checkmark",
                        tint = InteractiveTeal,
                        modifier = Modifier.size(48.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "ALERT DISPATCHED",
                    style = MaterialTheme.typography.headlineMedium.copy(color = TextPrimary),
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Dispatch confirmed by central hub\nID: ${confirmation.alertId}",
                    style = MaterialTheme.typography.labelMedium.copy(color = SuccessGreen),
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }

            // Nearest Fire Station Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(ElevatedCardBackground)
                    .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
                    .padding(20.dp)
            ) {
                Column {
                    Text(
                        text = "NEAREST RESPONDER STATION",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = TextSecondary,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = station.name,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontSize = 18.sp,
                            color = TextPrimary
                        ),
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "ESTIMATED ETA",
                                style = MaterialTheme.typography.labelMedium.copy(color = TextSecondary)
                            )
                            Text(
                                text = "${station.etaMinutes} MINS",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = InteractiveTeal,
                                    fontSize = 16.sp
                                ),
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "DISTANCE",
                                style = MaterialTheme.typography.labelMedium.copy(color = TextSecondary)
                            )
                            Text(
                                text = "${station.distanceKm} KM",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = TextPrimary,
                                    fontSize = 16.sp
                                ),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Action area and countdown progress
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Time progress bar
                LinearProgressIndicator(
                    progress = { animatedTimerProgress },
                    color = InteractiveTeal,
                    trackColor = Color(0xFF2A2A2A),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Next: Add Photo Evidence...",
                        style = MaterialTheme.typography.labelMedium.copy(color = TextSecondary)
                    )
                    TextButton(
                        onClick = {
                            viewModel.proceedToPhoto()
                            onNext()
                        }
                    ) {
                        Text("SKIP WAIT >", color = InteractiveTeal, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Cancel Button
                OutlinedButton(
                    onClick = { viewModel.showCancelConfirmationDialog(true) },
                    border = BorderStroke(1.dp, EmergencyRed),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = EmergencyRed),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text(
                        text = "CANCEL ALERT",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = EmergencyRed
                        )
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
