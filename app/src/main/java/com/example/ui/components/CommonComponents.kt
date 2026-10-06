package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.localization.AppLanguage
import com.example.ui.theme.SafeRideBlue
import com.example.ui.theme.SafeRideGold
import com.example.ui.theme.SafeRideNavy
import com.example.ui.theme.StatusDanger
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.StatusWarning

@Composable
fun SafeRideTopBar(
    title: String,
    subtitle: String? = null,
    currentLanguage: AppLanguage,
    onLanguageClick: () -> Unit,
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier,
    isAdmin: Boolean = false
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = if (isAdmin) Color(0xFF0F172A) else Color.White,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isAdmin) SafeRideGold else SafeRideBlue),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = "SafeRide Logo",
                        tint = if (isAdmin) SafeRideNavy else Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = if (isAdmin) Color.White else SafeRideNavy
                    )
                    if (subtitle != null) {
                        Text(
                            text = subtitle,
                            fontSize = 11.sp,
                            color = if (isAdmin) Color(0xFF94A3B8) else Color(0xFF64748B)
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Language Switcher Chip
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (isAdmin) Color(0xFF1E293B) else Color(0xFFEFF6FF),
                    modifier = Modifier
                        .clickable { onLanguageClick() }
                        .testTag("language_selector_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = "Language",
                            tint = if (isAdmin) SafeRideGold else SafeRideBlue,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = currentLanguage.nativeName,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isAdmin) Color.White else SafeRideBlue
                        )
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                IconButton(
                    onClick = onLogoutClick,
                    modifier = Modifier.testTag("logout_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Logout,
                        contentDescription = "Sign Out",
                        tint = if (isAdmin) Color(0xFF94A3B8) else Color(0xFF64748B)
                    )
                }
            }
        }
    }
}

@Composable
fun StatusBadge(
    status: String,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor) = when (status.uppercase()) {
        "ACTIVE", "APPROVED", "PICKED_UP", "DROPPED_OFF", "RESOLVED" -> Pair(Color(0xFFDCFCE7), Color(0xFF166534))
        "PENDING", "WAITING", "WARNING", "INVESTIGATING", "DELAYED" -> Pair(Color(0xFFFEF3C7), Color(0xFF92400E))
        "EMERGENCY", "SUSPENDED", "REJECTED" -> Pair(Color(0xFFFEE2E2), Color(0xFF991B1B))
        else -> Pair(Color(0xFFF1F5F9), Color(0xFF475569))
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(6.dp),
        modifier = modifier
    ) {
        Text(
            text = status.replace("_", " "),
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

@Composable
fun ParentTimelineStepper(
    currentStepIndex: Int, // 0: Started, 1: Picked Up, 2: Travelling, 3: School Arrival, 4: Home Drop-off
    modifier: Modifier = Modifier
) {
    val steps = listOf(
        "Trip started",
        "Child picked up",
        "Travelling",
        "School arrival",
        "Home drop-off"
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "TRIP PROGRESS TIMELINE",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF64748B),
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(14.dp))

            steps.forEachIndexed { index, stepName ->
                val isCompleted = index < currentStepIndex
                val isCurrent = index == currentStepIndex
                val isPending = index > currentStepIndex

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Marker Dot / Check
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    isCompleted -> StatusSuccess
                                    isCurrent -> SafeRideBlue
                                    else -> Color(0xFFE2E8F0)
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isCompleted) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                        } else if (isCurrent) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = stepName,
                        fontSize = 14.sp,
                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                        color = when {
                            isCompleted -> Color(0xFF0F172A)
                            isCurrent -> SafeRideBlue
                            else -> Color(0xFF94A3B8)
                        }
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    if (isCurrent) {
                        Surface(
                            color = Color(0xFFDBEAFE),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = "NOW",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = SafeRideBlue,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                if (index < steps.size - 1) {
                    Box(
                        modifier = Modifier
                            .padding(start = 11.dp)
                            .width(2.dp)
                            .height(18.dp)
                            .background(if (index < currentStepIndex) StatusSuccess else Color(0xFFE2E8F0))
                    )
                }
            }
        }
    }
}

@Composable
fun StatsCard(
    title: String,
    value: String,
    subtitle: String? = null,
    icon: ImageVector,
    iconTint: Color = SafeRideBlue,
    containerColor: Color = Color.White,
    textColor: Color = SafeRideNavy,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = title,
                    fontSize = 12.sp,
                    color = textColor.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Medium
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = textColor.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
fun LanguageSelectionDialog(
    currentLanguage: AppLanguage,
    onLanguageSelected: (AppLanguage) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Select Language / भाषा निवडा",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(AppLanguage.values()) { lang ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                onLanguageSelected(lang)
                                onDismiss()
                            }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = lang.nativeName,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp,
                                color = SafeRideNavy
                            )
                            Text(
                                text = lang.displayName,
                                fontSize = 12.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                        RadioButton(
                            selected = lang == currentLanguage,
                            onClick = {
                                onLanguageSelected(lang)
                                onDismiss()
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
fun SafetyReportDialog(
    vehicleId: String,
    reporterRole: String,
    onSubmit: (severity: String, category: String, description: String) -> Unit,
    onDismiss: () -> Unit
) {
    var severity by remember { mutableStateOf("NORMAL") }
    var category by remember { mutableStateOf("Traffic Delay") }
    var description by remember { mutableStateOf("") }

    val categories = if (reporterRole == "DRIVER") {
        listOf("Traffic Delay", "Vehicle Breakdown", "Route Diversion", "Medical Concern", "Emergency")
    } else {
        listOf("Unexpected Delay", "Incorrect Status", "Bus Not Arrived", "Driver Query", "Safety Concern")
    }

    val severities = listOf("NORMAL", "DELAYED", "WARNING", "EMERGENCY")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.ReportProblem,
                    contentDescription = null,
                    tint = StatusDanger,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Report Safety Issue", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Severity Level", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    severities.forEach { sev ->
                        val isSelected = severity == sev
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { severity = sev },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) {
                                if (sev == "EMERGENCY") StatusDanger else SafeRideBlue
                            } else Color(0xFFF1F5F9),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) Color.Transparent else Color(0xFFCBD5E1)
                            )
                        ) {
                            Text(
                                text = sev.take(4),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else Color(0xFF334155),
                                modifier = Modifier.padding(vertical = 8.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Text("Category", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                Spacer(modifier = Modifier.height(4.dp))

                LazyColumn(modifier = Modifier.height(110.dp)) {
                    items(categories) { cat ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { category = cat }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = category == cat, onClick = { category = cat })
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(cat, fontSize = 13.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Details (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalDesc = description.ifBlank { "$category reported on vehicle $vehicleId" }
                    onSubmit(severity, category, finalDesc)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (severity == "EMERGENCY") StatusDanger else SafeRideBlue
                )
            ) {
                Text("Submit Report")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
