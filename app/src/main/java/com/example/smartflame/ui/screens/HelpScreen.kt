package com.example.smartflame.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartflame.theme.*

@Composable
fun HelpScreen(
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 24.dp, vertical = 16.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            text = "HElP & SAFETY RESOURCES",
            style = MaterialTheme.typography.titleMedium.copy(
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                letterSpacing = 1.0.sp
            ),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Guide 1: How it works CARD
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(ElevatedCardBackground, RoundedCornerShape(16.dp))
                .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = InteractiveTeal,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "How SmartFlame Works",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                }
                Text(
                    text = "Press and hold the EMERGENCY button to resolve your current GPS coordinates. Once verified, details are sent directly to the nearest fire station dispatch hub for immediate action.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = TextSecondary,
                        lineHeight = 20.sp
                    )
                )
            }
        }

        // Guide 2: Fire Safety Tips CARD
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(ElevatedCardBackground, RoundedCornerShape(16.dp))
                .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Critical Fire Safety Rules",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )

                BulletItem("Stop, Drop, and Roll immediately if your clothes catch fire.")
                BulletItem("Stay low to the floor to avoid inhaling heavy smoke and gas.")
                BulletItem("Never use an elevator during a fire emergency. Use stairs.")
                BulletItem("Before opening any door, feel the metal handle. If hot, do not open.")
            }
        }

        // Guide 3: Emergency Contacts CARD
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(ElevatedCardBackground, RoundedCornerShape(16.dp))
                .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Emergency Telephone Hotlines",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )

                Divider(color = CardBorder, thickness = 1.dp)

                HotlineItem("National Fire Alert Service", "101")
                HotlineItem("Emergency Services Dispatch", "112")
                HotlineItem("SmartFlame Technical Support", "+1 (800) 555-FIRE")
            }
        }
    }
}

@Composable
fun BulletItem(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = "•",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Bold,
                color = InteractiveTeal
            )
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = TextPrimary,
                lineHeight = 18.sp
            ),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun HotlineItem(title: String, phone: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
            )
            Text(
                text = phone,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = InteractiveTeal,
                    fontWeight = FontWeight.Bold
                )
            )
        }
        IconButton(
            onClick = { /* Dial Phone */ },
            colors = IconButtonDefaults.iconButtonColors(
                contentColor = InteractiveTeal
            )
        ) {
            Icon(
                imageVector = Icons.Default.Phone,
                contentDescription = "Call Hotline",
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
