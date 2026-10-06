package com.example.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gps.GpsWaypoint
import com.example.gps.LiveVehicleLocation
import com.example.gps.RouteCoordinates
import com.example.ui.theme.SafeRideBlue
import com.example.ui.theme.SafeRideBlueLight
import com.example.ui.theme.SafeRideGold
import com.example.ui.theme.SafeRideNavy
import com.example.ui.theme.StatusDanger
import com.example.ui.theme.StatusSuccess

@Composable
fun RouteMapCanvas(
    vehicleLocation: LiveVehicleLocation?,
    vehicleNumber: String,
    modifier: Modifier = Modifier,
    waypoints: List<GpsWaypoint> = RouteCoordinates.ROUTE_04_WAYPOINTS,
    isSimulationMode: Boolean = true,
    isOnline: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 18f,
        targetValue = 38f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseRadius"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseAlpha"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // High-detail Vector Canvas Map
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("route_map_canvas")
            ) {
                drawRoadGrid()
                drawRoutePath(waypoints)
                drawStopMarkers(waypoints)

                // Draw Live Vehicle Location
                if (vehicleLocation != null) {
                    val pos = mapGeoToCanvas(vehicleLocation.latitude, vehicleLocation.longitude, size.width, size.height)

                    // Pulse effect for live vehicle
                    drawCircle(
                        color = (if (isSimulationMode) SafeRideGold else SafeRideBlue).copy(alpha = pulseAlpha),
                        radius = pulseRadius,
                        center = pos
                    )

                    // Outer halo
                    drawCircle(
                        color = Color.White,
                        radius = 16f,
                        center = pos
                    )

                    // Main Vehicle dot
                    drawCircle(
                        color = if (isSimulationMode) Color(0xFFD97706) else SafeRideBlue,
                        radius = 11f,
                        center = pos
                    )

                    // Direction arrow head
                    val angleRad = Math.toRadians(vehicleLocation.headingDegrees.toDouble())
                    val arrowLength = 22f
                    val arrowEnd = Offset(
                        pos.x + (arrowLength * kotlin.math.sin(angleRad)).toFloat(),
                        pos.y - (arrowLength * kotlin.math.cos(angleRad)).toFloat()
                    )
                    drawLine(
                        color = SafeRideNavy,
                        start = pos,
                        end = arrowEnd,
                        strokeWidth = 4f,
                        cap = StrokeCap.Round
                    )
                }
            }

            // Top Status Bar Banner - Real-Time Fleet Telemetry & GPS Radar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Mode Badge
                Surface(
                    color = if (isSimulationMode) Color(0xFFFEF3C7) else Color(0xFFDCFCE7),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isSimulationMode) Color(0xFFF59E0B) else Color(0xFF10B981)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (isSimulationMode) Color(0xFFD97706) else Color(0xFF10B981))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isSimulationMode) "ROUTE RADAR ACTIVE" else "LIVE GPS ACTIVE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSimulationMode) Color(0xFF92400E) else Color(0xFF065F46)
                        )
                    }
                }

                // Online/Offline status
                Surface(
                    color = if (isOnline) Color(0xFFECFDF5) else Color(0xFFFEF2F2),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isOnline) Color(0xFF34D399) else Color(0xFFF87171)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sensors,
                            contentDescription = "Online Status",
                            tint = if (isOnline) StatusSuccess else StatusDanger,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isOnline) "ONLINE" else "OFFLINE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isOnline) StatusSuccess else StatusDanger
                        )
                    }
                }
            }

            // Bottom Floating Telemetry Card
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(12.dp),
                shape = RoundedCornerShape(14.dp),
                color = SafeRideNavy.copy(alpha = 0.94f),
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(SafeRideBlueLight.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.DirectionsBus,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = vehicleNumber,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = vehicleLocation?.nextStopName?.let { "Next: $it" } ?: "En route",
                                color = Color(0xFF94A3B8),
                                fontSize = 11.sp
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.End) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Speed,
                                    contentDescription = null,
                                    tint = SafeRideGold,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "${vehicleLocation?.speedKmH?.toInt() ?: 0} km/h",
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp
                                )
                            }
                            Text(
                                text = "Speed",
                                color = Color(0xFF94A3B8),
                                fontSize = 10.sp
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${vehicleLocation?.etaMinutes ?: 8} min",
                                color = Color(0xFF38BDF8),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "ETA",
                                color = Color(0xFF94A3B8),
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

// Canvas Drawing Helpers
private fun DrawScope.drawRoadGrid() {
    val roadColor = Color(0xFFE2E8F0)
    // Horizontal secondary roads
    for (i in 1..7) {
        val y = size.height * (i / 8f)
        drawLine(
            color = roadColor,
            start = Offset(0f, y),
            end = Offset(size.width, y),
            strokeWidth = 6f
        )
    }
    // Vertical secondary roads
    for (i in 1..7) {
        val x = size.width * (i / 8f)
        drawLine(
            color = roadColor,
            start = Offset(x, 0f),
            end = Offset(x, size.height),
            strokeWidth = 6f
        )
    }
}

private fun DrawScope.drawRoutePath(waypoints: List<GpsWaypoint>) {
    if (waypoints.isEmpty()) return
    val path = Path()
    val first = mapGeoToCanvas(waypoints[0].latitude, waypoints[0].longitude, size.width, size.height)
    path.moveTo(first.x, first.y)

    for (i in 1 until waypoints.size) {
        val p = mapGeoToCanvas(waypoints[i].latitude, waypoints[i].longitude, size.width, size.height)
        path.lineTo(p.x, p.y)
    }

    // Outer path glow
    drawPath(
        path = path,
        color = Color(0xFFBFDBFE),
        style = Stroke(width = 12f, cap = StrokeCap.Round, join = StrokeJoin.Round)
    )

    // Inner path track
    drawPath(
        path = path,
        color = SafeRideBlue,
        style = Stroke(
            width = 5f,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 10f), 0f)
        )
    )
}

private fun DrawScope.drawStopMarkers(waypoints: List<GpsWaypoint>) {
    val stops = waypoints.filter { it.isStop }
    stops.forEachIndexed { index, stop ->
        val pos = mapGeoToCanvas(stop.latitude, stop.longitude, size.width, size.height)
        val isSchool = index == stops.size - 1

        // Pin base halo
        drawCircle(
            color = Color.White,
            radius = if (isSchool) 12f else 9f,
            center = pos
        )

        // Pin core
        drawCircle(
            color = if (isSchool) StatusSuccess else Color(0xFF3B82F6),
            radius = if (isSchool) 8f else 6f,
            center = pos
        )
    }
}

// Convert real GPS Lat/Lng bbox to normalized canvas space
private fun mapGeoToCanvas(lat: Double, lng: Double, width: Float, height: Float): Offset {
    val minLat = 19.1250
    val maxLat = 19.1720
    val minLng = 77.3000
    val maxLng = 77.3520

    val normX = ((lng - minLng) / (maxLng - minLng)).coerceIn(0.05, 0.95)
    // Latitude decreases as we go southward (down in canvas)
    val normY = (1.0 - ((lat - minLat) / (maxLat - minLat))).coerceIn(0.05, 0.95)

    return Offset((normX * width).toFloat(), (normY * height).toFloat())
}
