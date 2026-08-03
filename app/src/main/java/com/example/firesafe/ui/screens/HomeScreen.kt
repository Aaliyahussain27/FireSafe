package com.example.firesafe.ui.screens

import android.Manifest
import androidx.compose.animation.core.*
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.firesafe.data.repository.impl.GPSLocationRepository
import com.example.firesafe.theme.*
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    onEmergencyClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = DarkBackground,
        bottomBar = {
            BottomNavigationBar(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (selectedTab) {
                0 -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 24.dp, vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        HeaderSection()
                        EmergencyButtonSection(onEmergencyClick = onEmergencyClick)
                        InfoChipsSection()
                    }
                }
                1 -> HistoryScreen()
                2 -> StationsScreen()
                3 -> HelpScreen()
            }
        }
    }
}

@Composable
fun HeaderSection() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Canvas(modifier = Modifier.size(22.dp)) {
                val path = Path().apply {
                    moveTo(size.width * 0.5f, size.height * 0.1f)
                    cubicTo(
                        size.width * 0.2f, size.height * 0.4f,
                        size.width * 0.1f, size.height * 0.65f,
                        size.width * 0.5f, size.height * 0.95f
                    )
                    cubicTo(
                        size.width * 0.9f, size.height * 0.65f,
                        size.width * 0.8f, size.height * 0.4f,
                        size.width * 0.5f, size.height * 0.1f
                    )
                }
                drawPath(path, color = Color.White)

                val innerPath = Path().apply {
                    moveTo(size.width * 0.5f, size.height * 0.4f)
                    cubicTo(
                        size.width * 0.35f, size.height * 0.6f,
                        size.width * 0.3f, size.height * 0.75f,
                        size.width * 0.5f, size.height * 0.9f
                    )
                    cubicTo(
                        size.width * 0.7f, size.height * 0.75f,
                        size.width * 0.65f, size.height * 0.6f,
                        size.width * 0.5f, size.height * 0.4f
                    )
                }
                drawPath(innerPath, color = EmergencyRed)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "FIRE SAFE",
                style = TextStyle(
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    letterSpacing = 0.5.sp
                )
            )
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(EmergencyRed)
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            Text(
                text = "SOS",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )
        }
    }
}

@Composable
fun EmergencyButtonSection(onEmergencyClick: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Real location state — starts as "Locating..." then resolves or falls back
    var locationLabel by remember { mutableStateOf("Locating...") }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            scope.launch {
                locationLabel = fetchLocalityLabel(context)
            }
        } else {
            locationLabel = "Location unavailable"
        }
    }

    LaunchedEffect(Unit) {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            locationLabel = fetchLocalityLabel(context)
        } else {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "SosGlow")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = androidx.compose.animation.core.tween(1500, easing = androidx.compose.animation.core.EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseScale"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.padding(vertical = 16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(240.dp)
                .drawBehind {
                    val radius = (size.minDimension / 2.2f) * pulseScale
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                EmergencyRed.copy(alpha = 0.35f),
                                EmergencyRed.copy(alpha = 0.08f),
                                Color.Transparent
                            ),
                            center = center,
                            radius = radius
                        ),
                        radius = radius
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(180.dp)
                    .shadow(
                        elevation = 16.dp,
                        shape = CircleShape,
                        ambientColor = EmergencyRed,
                        spotColor = EmergencyRed
                    )
                    .clip(CircleShape)
                    .background(EmergencyRed)
                    .clickable { onEmergencyClick() },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "!",
                            style = TextStyle(
                                color = EmergencyRed,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Black
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "EMERGENCY",
                        style = TextStyle(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            letterSpacing = 1.5.sp
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "Tap to report a fire",
            style = TextStyle(
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 15.sp,
                fontWeight = FontWeight.Normal
            )
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = "Location Pin",
                tint = Color(0xFF666666),
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Active in $locationLabel",
                style = TextStyle(
                    color = Color(0xFF666666),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            )
        }
    }
}

// Fetches real GPS location and extracts a short locality/city name for display
private suspend fun fetchLocalityLabel(context: android.content.Context): String {
    return try {
        val repo = GPSLocationRepository(context)
        val location = repo.getCurrentLocation()
        val geocoder = android.location.Geocoder(context, java.util.Locale.getDefault())
        val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
        val addr = addresses?.firstOrNull()
        // Prefer locality (city), fall back to subAdminArea, then full address
        addr?.locality ?: addr?.subAdminArea ?: location.address
    } catch (e: Exception) {
        "Location unavailable"
    }
}

@Composable
fun InfoChipsSection() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        InfoCard(
            title = "Air Quality",
            value = "Poor (210)",
            iconType = InfoCardIcon.Air,
            modifier = Modifier.weight(1f)
        )
        InfoCard(
            title = "Nearby Risk",
            value = "Low Risk",
            iconType = InfoCardIcon.Risk,
            modifier = Modifier.weight(1f)
        )
    }
}

enum class InfoCardIcon { Air, Risk }

@Composable
fun InfoCard(
    title: String,
    value: String,
    iconType: InfoCardIcon,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(ElevatedCardBackground)
            .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Canvas(modifier = Modifier.size(24.dp)) {
                val w = size.width
                val h = size.height
                when (iconType) {
                    InfoCardIcon.Air -> {
                        val path = Path().apply {
                            moveTo(0.1f * w, 0.3f * h)
                            lineTo(0.7f * w, 0.3f * h)
                            quadraticTo(0.85f * w, 0.3f * h, 0.85f * w, 0.2f * h)
                            quadraticTo(0.85f * w, 0.1f * h, 0.75f * w, 0.1f * h)
                            quadraticTo(0.65f * w, 0.1f * h, 0.65f * w, 0.2f * h)
                            moveTo(0.05f * w, 0.5f * h)
                            lineTo(0.85f * w, 0.5f * h)
                            quadraticTo(0.95f * w, 0.5f * h, 0.95f * w, 0.4f * h)
                            quadraticTo(0.95f * w, 0.3f * h, 0.85f * w, 0.3f * h)
                            moveTo(0.15f * w, 0.7f * h)
                            lineTo(0.6f * w, 0.7f * h)
                            quadraticTo(0.75f * w, 0.7f * h, 0.75f * w, 0.8f * h)
                            quadraticTo(0.75f * w, 0.9f * h, 0.65f * w, 0.9f * h)
                            quadraticTo(0.55f * w, 0.9f * h, 0.55f * w, 0.8f * h)
                        }
                        drawPath(
                            path = path,
                            color = Color(0xFF9E9E9E),
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
                        )
                    }
                    InfoCardIcon.Risk -> {
                        val path = Path().apply {
                            moveTo(size.width * 0.5f, size.height * 0.1f)
                            cubicTo(
                                size.width * 0.2f, size.height * 0.45f,
                                size.width * 0.15f, size.height * 0.7f,
                                size.width * 0.5f, size.height * 0.95f
                            )
                            cubicTo(
                                size.width * 0.85f, size.height * 0.7f,
                                size.width * 0.8f, size.height * 0.45f,
                                size.width * 0.5f, size.height * 0.1f
                            )
                        }
                        drawPath(path, color = Color(0xFF9E9E9E))

                        val sparkPath = Path().apply {
                            moveTo(size.width * 0.5f, size.height * 0.5f)
                            cubicTo(
                                size.width * 0.4f, size.height * 0.65f,
                                size.width * 0.38f, size.height * 0.75f,
                                size.width * 0.5f, size.height * 0.88f
                            )
                            cubicTo(
                                size.width * 0.62f, size.height * 0.75f,
                                size.width * 0.6f, size.height * 0.65f,
                                size.width * 0.5f, size.height * 0.5f
                            )
                        }
                        drawPath(sparkPath, color = ElevatedCardBackground)
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = title.uppercase(),
                    style = TextStyle(
                        color = Color(0xFF666666),
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        letterSpacing = 0.5.sp
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = value,
                    style = TextStyle(
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                )
            }
        }
    }
}

@Composable
fun BottomNavigationBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    val items = listOf(
        TabItem("Report", Icons.Default.Warning),
        TabItem("History", Icons.Default.List),
        TabItem("Stations", Icons.Default.LocationOn),
        TabItem("Help", Icons.Default.Info)
    )

    NavigationBar(
        containerColor = Color(0xFF161616),
        tonalElevation = 8.dp,
        modifier = Modifier.drawBehind {
            drawLine(
                color = CardBorder,
                start = Offset(0f, 0f),
                end = Offset(size.width, 0f),
                strokeWidth = 1.dp.toPx()
            )
        }
    ) {
        items.forEachIndexed { index, item ->
            val isSelected = selectedTab == index
            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(index) },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = InteractiveTeal,
                    unselectedIconColor = Color(0xFF666666),
                    selectedTextColor = InteractiveTeal,
                    unselectedTextColor = Color(0xFF666666),
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}

data class TabItem(val label: String, val icon: ImageVector)