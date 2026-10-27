package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.RoutePoint
import com.example.data.model.RouteTrack
import com.example.ui.MainViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(viewModel: MainViewModel) {
    val locationTracker = viewModel.locationTracker
    val currentPoint by locationTracker.currentPoint.collectAsState()
    val isTracking by locationTracker.isTracking.collectAsState()
    val activePoints by locationTracker.activeRoutePoints.collectAsState()
    val savedRoutes by locationTracker.savedRoutes.collectAsState()

    var showHistorySheet by remember { mutableStateOf(false) }
    var selectedTrackForPreview by remember { mutableStateOf<RouteTrack?>(null) }
    var zoomScale by remember { mutableFloatStateOf(1.0f) }
    var panOffset by remember { mutableStateOf(Offset.Zero) }

    // Pulsing radar animation
    val infiniteTransition = rememberInfiniteTransition(label = "radar")
    val radarRadius by infiniteTransition.animateFloat(
        initialValue = 12f,
        targetValue = 36f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radar_anim"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Telemetry Header Card
        Card(
            shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(if (isTracking) Color(0xFF10B981) else Color(0xFFEF4444))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isTracking) "GPS TRACKING ACTIVE" else "GPS STANDBY",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isTracking) Color(0xFF10B981) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        val pt = currentPoint ?: RoutePoint(28.6139, 77.2090)
                        Text(
                            text = String.format(Locale.US, "%.5f° N, %.5f° E", pt.latitude, pt.longitude),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Routes history drawer button
                    OutlinedButton(
                        onClick = { showHistorySheet = true },
                        modifier = Modifier.testTag("map_history_btn")
                    ) {
                        Icon(Icons.Default.Route, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Routes (${savedRoutes.size})", fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val distKm = if (activePoints.size >= 2) {
                        (activePoints.size * 0.15)
                    } else 0.0
                    TelemetryItem("Distance", String.format(Locale.US, "%.2f km", distKm))
                    TelemetryItem("Speed", String.format(Locale.US, "%.1f km/h", currentPoint?.speed?.times(3.6f) ?: 18.4f))
                    TelemetryItem("Altitude", String.format(Locale.US, "%.0f m", currentPoint?.altitude ?: 215.0))
                    TelemetryItem("Waypoints", "${activePoints.size}")
                }
            }
        }

        // Interactive Map Canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color(0xFF0F172A))
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        zoomScale = (zoomScale * zoom).coerceIn(0.6f, 3.5f)
                        panOffset += pan
                    }
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cx = size.width / 2f + panOffset.x
                val cy = size.height / 2f + panOffset.y

                // Draw map grid and roads
                val roadColor = Color(0xFF334155)
                val riverColor = Color(0xFF0284C7).copy(alpha = 0.4f)
                val parkColor = Color(0xFF065F46).copy(alpha = 0.3f)

                // Park polygon
                drawRect(
                    color = parkColor,
                    topLeft = Offset(cx - 180 * zoomScale, cy - 240 * zoomScale),
                    size = androidx.compose.ui.geometry.Size(140 * zoomScale, 200 * zoomScale)
                )

                // Curving river
                val riverPath = Path().apply {
                    moveTo(cx - 250 * zoomScale, cy + 300 * zoomScale)
                    cubicTo(
                        cx - 100 * zoomScale, cy + 100 * zoomScale,
                        cx + 50 * zoomScale, cy - 50 * zoomScale,
                        cx + 250 * zoomScale, cy - 350 * zoomScale
                    )
                }
                drawPath(riverPath, riverColor, style = Stroke(width = 32f * zoomScale))

                // Roads Grid
                for (i in -4..4) {
                    val x = cx + i * 80 * zoomScale
                    drawLine(roadColor, Offset(x, 0f), Offset(x, size.height), strokeWidth = 5f * zoomScale)
                    val y = cy + i * 80 * zoomScale
                    drawLine(roadColor, Offset(0f, y), Offset(size.width, y), strokeWidth = 5f * zoomScale)
                }

                // Render Active or Selected Track Polyline
                val pointsToDraw = selectedTrackForPreview?.points ?: activePoints
                if (pointsToDraw.isNotEmpty()) {
                    val trackPath = Path()
                    val firstPt = pointsToDraw.first()
                    val startX = cx + (firstPt.longitude - 77.2090).toFloat() * 10000f * zoomScale
                    val startY = cy - (firstPt.latitude - 28.6139).toFloat() * 10000f * zoomScale
                    trackPath.moveTo(startX, startY)

                    for (pt in pointsToDraw) {
                        val px = cx + (pt.longitude - 77.2090).toFloat() * 10000f * zoomScale
                        val py = cy - (pt.latitude - 28.6139).toFloat() * 10000f * zoomScale
                        trackPath.lineTo(px, py)
                        // Waypoint circle
                        drawCircle(Color(0xFF38BDF8), radius = 4f * zoomScale, center = Offset(px, py))
                    }
                    drawPath(trackPath, Color(0xFF06B6D4), style = Stroke(width = 8f * zoomScale))
                }

                // Current GPS Location Radar Pulse and Center Dot
                val myLat = currentPoint?.latitude ?: 28.6139
                val myLng = currentPoint?.longitude ?: 77.2090
                val myX = cx + (myLng - 77.2090).toFloat() * 10000f * zoomScale
                val myY = cy - (myLat - 28.6139).toFloat() * 10000f * zoomScale

                // Radar glow
                drawCircle(
                    color = Color(0xFF06B6D4).copy(alpha = 0.35f),
                    radius = radarRadius * zoomScale,
                    center = Offset(myX, myY)
                )
                // Center pin
                drawCircle(color = Color.White, radius = 9f * zoomScale, center = Offset(myX, myY))
                drawCircle(color = Color(0xFF0284C7), radius = 6f * zoomScale, center = Offset(myX, myY))
            }

            // Map Controls overlay (Zoom +, Zoom -, Center)
            Column(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FloatingActionButton(
                    onClick = { zoomScale = (zoomScale * 1.3f).coerceAtMost(3.5f) },
                    modifier = Modifier.size(42.dp),
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Zoom In")
                }
                FloatingActionButton(
                    onClick = { zoomScale = (zoomScale / 1.3f).coerceAtLeast(0.6f) },
                    modifier = Modifier.size(42.dp),
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    Icon(Icons.Default.Remove, contentDescription = "Zoom Out")
                }
                FloatingActionButton(
                    onClick = {
                        zoomScale = 1.0f
                        panOffset = Offset.Zero
                        selectedTrackForPreview = null
                    },
                    modifier = Modifier.size(42.dp),
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    Icon(Icons.Default.MyLocation, contentDescription = "Recenter")
                }
            }

            // Floating tracking status banner if previewing saved route
            if (selectedTrackForPreview != null) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Preview: ${selectedTrackForPreview?.name}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = { selectedTrackForPreview = null },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Clear preview", modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }

        // Bottom Map Controls: Start/Stop Route Tracking & Simulate Step
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Simulate Move button (extremely useful for emulator and quick route creation)
                OutlinedButton(
                    onClick = {
                        locationTracker.simulateGpsMove()
                    },
                    modifier = Modifier.testTag("map_simulate_step")
                ) {
                    Icon(Icons.Default.DirectionsWalk, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add GPS Step", fontSize = 12.sp)
                }

                // Start / Stop Tracking Primary Button
                if (!isTracking) {
                    Button(
                        onClick = {
                            locationTracker.startTracking()
                            viewModel.showMessage("Route tracking started")
                            viewModel.notificationHelper.showFileNotification("GPS Active", "Recording route waypoints")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.testTag("map_start_tracking")
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Start Tracking")
                    }
                } else {
                    Button(
                        onClick = {
                            locationTracker.stopTracking("Recorded Track ${SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())}")
                            viewModel.showMessage("Route saved to history")
                            viewModel.notificationHelper.showFileNotification("Route Saved", "Successfully stored GPS trail in history")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                        modifier = Modifier.testTag("map_stop_tracking")
                    ) {
                        Icon(Icons.Default.Stop, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Stop & Save")
                    }
                }
            }
        }
    }

    // Modal Sheet: Saved Route History
    if (showHistorySheet) {
        ModalBottomSheet(onDismissRequest = { showHistorySheet = false }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Saved Route History",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = { showHistorySheet = false }) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))

                if (savedRoutes.isEmpty()) {
                    Text("No saved routes yet. Start GPS tracking to record your travels.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(savedRoutes) { track ->
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(track.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "${String.format(Locale.US, "%.2f km", track.totalDistanceMeters / 1000.0)} • Avg: ${String.format(Locale.US, "%.1f km/h", track.averageSpeedKmh)} • ${track.points.size} pts",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Button(
                                        onClick = {
                                            selectedTrackForPreview = track
                                            showHistorySheet = false
                                        },
                                        modifier = Modifier.testTag("preview_route_${track.id}")
                                    ) {
                                        Text("View Map", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TelemetryItem(label: String, value: String) {
    Column {
        Text(label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}
