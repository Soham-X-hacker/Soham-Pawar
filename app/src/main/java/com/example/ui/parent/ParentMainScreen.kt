package com.example.ui.parent

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ConnectionRequestEntity
import com.example.data.model.NotificationEntity
import com.example.data.model.StudentEntity
import com.example.data.model.TripEntity
import com.example.data.model.TripEventEntity
import com.example.data.model.UserEntity
import com.example.data.model.VehicleEntity
import com.example.gps.LiveVehicleLocation
import com.example.localization.AppLanguage
import com.example.localization.StringsDefinition
import com.example.ui.components.LanguageSelectionDialog
import com.example.ui.components.ParentTimelineStepper
import com.example.ui.components.RouteMapCanvas
import com.example.ui.components.SafeRideTopBar
import com.example.ui.components.SafetyReportDialog
import com.example.ui.components.StatusBadge
import com.example.ui.theme.SafeRideBlue
import com.example.ui.theme.SafeRideGold
import com.example.ui.theme.SafeRideNavy
import com.example.ui.theme.StatusSuccess

@Composable
fun ParentMainScreen(
    user: UserEntity,
    students: List<StudentEntity>,
    vehicles: List<VehicleEntity>,
    connectionRequests: List<ConnectionRequestEntity>,
    activeTrips: List<TripEntity>,
    tripEvents: List<TripEventEntity>,
    notifications: List<NotificationEntity>,
    liveLocation: LiveVehicleLocation?,
    isSimulationMode: Boolean,
    strings: StringsDefinition,
    currentLanguage: AppLanguage,
    onLanguageSelected: (AppLanguage) -> Unit,
    onToggleSimulation: (Boolean) -> Unit,
    onSubmitConnectionRequest: (busCode: String, studentName: String, pickup: String, dropOff: String, onSuccess: () -> Unit) -> Unit,
    onReportSafetyIssue: (vehicleId: String, severity: String, category: String, desc: String) -> Unit,
    onLogout: () -> Unit
) {
    var selectedNavIndex by remember { mutableIntStateOf(0) }
    var showConnectDialog by remember { mutableStateOf(false) }
    var showReportDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }

    BackHandler(enabled = selectedNavIndex != 0) {
        selectedNavIndex = 0
    }

    // Filter data strictly for this parent (Zero cross-user exposure)
    val myStudents = students.filter { it.parentId == user.id }
    val myLinkedVehicle = vehicles.firstOrNull { veh -> myStudents.any { it.vehicleId == veh.id } }
    val myPendingRequests = connectionRequests.filter { it.parentId == user.id }
    val myActiveTrip = activeTrips.firstOrNull { it.vehicleId == myLinkedVehicle?.id && it.status == "ACTIVE" }

    if (showLanguageDialog) {
        LanguageSelectionDialog(
            currentLanguage = currentLanguage,
            onLanguageSelected = onLanguageSelected,
            onDismiss = { showLanguageDialog = false }
        )
    }

    if (showReportDialog && myLinkedVehicle != null) {
        SafetyReportDialog(
            vehicleId = myLinkedVehicle.id,
            reporterRole = "PARENT",
            onSubmit = { severity, category, description ->
                onReportSafetyIssue(myLinkedVehicle.id, severity, category, description)
            },
            onDismiss = { showReportDialog = false }
        )
    }

    if (showConnectDialog) {
        ConnectBusDialog(
            onSubmit = { code, studentName, pickup, dropOff ->
                onSubmitConnectionRequest(code, studentName, pickup, dropOff) {
                    showConnectDialog = false
                }
            },
            onDismiss = { showConnectDialog = false }
        )
    }

    Scaffold(
        topBar = {
            SafeRideTopBar(
                title = strings.appName,
                subtitle = myStudents.firstOrNull()?.let { "${it.fullName} • ${it.schoolName}" } ?: "Parent Portal",
                currentLanguage = currentLanguage,
                onLanguageClick = { showLanguageDialog = true },
                onLogoutClick = onLogout
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 4.dp,
                modifier = Modifier.navigationBarsPadding()
            ) {
                NavigationBarItem(
                    selected = selectedNavIndex == 0,
                    onClick = { selectedNavIndex = 0 },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Live Bus", fontSize = 11.sp) }
                )
                NavigationBarItem(
                    selected = selectedNavIndex == 1,
                    onClick = { selectedNavIndex = 1 },
                    icon = { Icon(Icons.Default.History, contentDescription = "Trips") },
                    label = { Text("Trips", fontSize = 11.sp) }
                )
                NavigationBarItem(
                    selected = selectedNavIndex == 2,
                    onClick = { selectedNavIndex = 2 },
                    icon = { Icon(Icons.Default.Notifications, contentDescription = "Notifications") },
                    label = { Text("Alerts", fontSize = 11.sp) }
                )
                NavigationBarItem(
                    selected = selectedNavIndex == 3,
                    onClick = { selectedNavIndex = 3 },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                    label = { Text("Profile", fontSize = 11.sp) }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF8FAFC))
        ) {
            when (selectedNavIndex) {
                0 -> {
                    // Home: Live Bus & Map
                    if (myLinkedVehicle == null) {
                        // Empty State: No bus connected yet
                        ParentEmptyBusView(
                            pendingRequests = myPendingRequests,
                            onConnectClick = { showConnectDialog = true }
                        )
                    } else {
                        ParentLiveDashboardView(
                            vehicle = myLinkedVehicle,
                            activeTrip = myActiveTrip,
                            student = myStudents.firstOrNull(),
                            liveLocation = liveLocation,
                            isSimulationMode = isSimulationMode,
                            strings = strings,
                            onToggleSimulation = onToggleSimulation,
                            onReportClick = { showReportDialog = true }
                        )
                    }
                }
                1 -> {
                    // Trip History
                    ParentTripsHistoryView(
                        tripEvents = tripEvents,
                        student = myStudents.firstOrNull()
                    )
                }
                2 -> {
                    // Notifications
                    ParentNotificationsView(notifications = notifications)
                }
                3 -> {
                    // Profile & Security
                    ParentProfileView(
                        user = user,
                        students = myStudents,
                        vehicle = myLinkedVehicle,
                        onConnectAnotherBus = { showConnectDialog = true }
                    )
                }
            }
        }
    }
}

@Composable
private fun ParentEmptyBusView(
    pendingRequests: List<ConnectionRequestEntity>,
    onConnectClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(Color(0xFFEFF6FF)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.DirectionsBus,
                contentDescription = null,
                tint = SafeRideBlue,
                modifier = Modifier.size(44.dp)
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = "No Bus Connected Yet",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = SafeRideNavy
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Connect your child to their school bus using the unique Bus Code provided by your driver or school operator.",
            fontSize = 13.sp,
            color = Color(0xFF64748B),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onConnectClick,
            colors = ButtonDefaults.buttonColors(containerColor = SafeRideBlue),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(48.dp)
                .testTag("parent_connect_bus_btn")
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Connect to a Bus", fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }

        if (pendingRequests.isNotEmpty()) {
            Spacer(modifier = Modifier.height(28.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFDE68A))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Pending Connection Request",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color(0xFF92400E)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Bus Code: ${pendingRequests.first().busCode} • Student: ${pendingRequests.first().studentName}",
                        fontSize = 12.sp,
                        color = Color(0xFFB45309)
                    )
                    Text(
                        text = "Waiting for vehicle operator verification. Once approved, live tracking activates automatically.",
                        fontSize = 11.sp,
                        color = Color(0xFF78350F)
                    )
                }
            }
        }
    }
}

@Composable
private fun ParentLiveDashboardView(
    vehicle: VehicleEntity,
    activeTrip: TripEntity?,
    student: StudentEntity?,
    liveLocation: LiveVehicleLocation?,
    isSimulationMode: Boolean,
    strings: StringsDefinition,
    onToggleSimulation: (Boolean) -> Unit,
    onReportClick: () -> Unit
) {
    val context = LocalContext.current
    val isTripActive = activeTrip != null

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // High-level Bus Status Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isTripActive) Color(0xFFEFF6FF) else Color.White
            ),
            border = androidx.compose.foundation.BorderStroke(
                1.5.dp,
                if (isTripActive) SafeRideBlue else Color(0xFFE2E8F0)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isTripActive) strings.busOnTheWay else "BUS AT DEPOT / SCHEDULED",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp,
                            color = if (isTripActive) SafeRideBlue else Color(0xFF475569)
                        )
                        Text(
                            text = "${vehicle.vehicleNumber} • Route 04",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = SafeRideNavy
                        )
                    }

                    Surface(
                        color = if (isTripActive) Color(0xFFDCFCE7) else Color(0xFFF1F5F9),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = if (isTripActive) "● LIVE" else "SCHEDULED",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isTripActive) Color(0xFF15803D) else Color(0xFF64748B),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFDBEAFE)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = null,
                                tint = SafeRideBlue,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "ETA: ${liveLocation?.etaMinutes ?: 8} minutes",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = SafeRideNavy
                            )
                            Text(
                                text = "Next: ${liveLocation?.nextStopName ?: "Kautha Road"}",
                                fontSize = 11.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                    }

                    // Contact Driver Button
                    OutlinedButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${vehicle.driverPhone}"))
                            context.startActivity(intent)
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("call_driver_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Call, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Call Driver", fontSize = 12.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Vector Road Map Canvas with Live Location
        RouteMapCanvas(
            vehicleLocation = liveLocation,
            vehicleNumber = vehicle.vehicleNumber,
            isSimulationMode = isSimulationMode,
            modifier = Modifier.height(260.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        // GPS Simulation Toggle (Clearly labeled as per requirement)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Sensors,
                        contentDescription = null,
                        tint = if (isSimulationMode) SafeRideGold else StatusSuccess,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = if (isSimulationMode) "GPS Mode: Route Waypoint Radar" else "GPS Mode: Real-Time Satellite GPS",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = SafeRideNavy
                        )
                        Text(
                            text = if (isSimulationMode) "Active route coordinate telemetry along Route 04" else "Receiving live device GPS telemetry",
                            fontSize = 10.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }

                Switch(
                    checked = isSimulationMode,
                    onCheckedChange = onToggleSimulation,
                    modifier = Modifier.testTag("parent_gps_toggle")
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Stepper: Trip Timeline
        val stepIndex = when (student?.pickupStatus) {
            "PICKED_UP" -> 2
            "DROPPED_OFF" -> 4
            else -> if (isTripActive) 1 else 0
        }

        ParentTimelineStepper(currentStepIndex = stepIndex)

        Spacer(modifier = Modifier.height(16.dp))

        // Safety Issue Action Button
        Button(
            onClick = onReportClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .testTag("parent_report_issue_btn"),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFEF2F2)),
            shape = RoundedCornerShape(10.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFECACA))
        ) {
            Icon(
                imageVector = Icons.Default.ReportProblem,
                contentDescription = null,
                tint = Color(0xFFDC2626),
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Report Delay or Safety Concern",
                color = Color(0xFFDC2626),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun ParentTripsHistoryView(
    tripEvents: List<TripEventEntity>,
    student: StudentEntity?
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Trip Event History",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = SafeRideNavy
        )
        Text(
            text = "Official timestamped events for ${student?.fullName ?: "your child"}",
            fontSize = 12.sp,
            color = Color(0xFF64748B)
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (tripEvents.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("No recent trip events recorded.", color = Color(0xFF94A3B8))
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(tripEvents) { ev ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFEFF6FF)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = SafeRideBlue,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = ev.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = SafeRideNavy
                                )
                                Text(
                                    text = ev.description,
                                    fontSize = 12.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                            Text(
                                text = "Today",
                                fontSize = 10.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ParentNotificationsView(notifications: List<NotificationEntity>) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Real-time Transport Alerts",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = SafeRideNavy
        )
        Text(
            text = "Pickup, delay, and safety notices",
            fontSize = 12.sp,
            color = Color(0xFF64748B)
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (notifications.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("No notifications at this time.", color = Color(0xFF94A3B8))
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(notifications) { notif ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = notif.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = SafeRideNavy
                                )
                                StatusBadge(status = notif.type)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = notif.message,
                                fontSize = 12.sp,
                                color = Color(0xFF475569)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ParentProfileView(
    user: UserEntity,
    students: List<StudentEntity>,
    vehicle: VehicleEntity?,
    onConnectAnotherBus: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Guardian Account & Privacy",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = SafeRideNavy
        )

        Spacer(modifier = Modifier.height(14.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Guardian Details", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                Spacer(modifier = Modifier.height(6.dp))
                Text(user.fullName, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = SafeRideNavy)
                Text(user.email, fontSize = 13.sp, color = Color(0xFF64748B))
                Text(user.phone, fontSize = 13.sp, color = Color(0xFF64748B))

                Spacer(modifier = Modifier.height(14.dp))
                Text("Connected Children", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                Spacer(modifier = Modifier.height(6.dp))
                students.forEach { st ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFF1F5F9)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(st.fullName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Pickup: ${st.pickupStop} • ${st.schoolName}", fontSize = 11.sp, color = Color(0xFF64748B))
                            }
                            StatusBadge(status = st.pickupStatus)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = onConnectAnotherBus,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SafeRideBlue),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Connect Another Bus / Student")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Student Privacy Guarantee Banner
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBFDBFE))
        ) {
            Row(modifier = Modifier.padding(14.dp)) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = SafeRideBlue,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "SafeRide Student Privacy Guarantee",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = SafeRideNavy
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "You are linked exclusively to your child's assigned vehicle. You will never see unrelated student profiles, contacts, or addresses. Real-time GPS coordinates are protected under end-to-end transport isolation.",
                        fontSize = 11.sp,
                        color = Color(0xFF1E3A8A)
                    )
                }
            }
        }
    }
}

@Composable
fun ConnectBusDialog(
    onSubmit: (busCode: String, studentName: String, pickup: String, dropOff: String) -> Unit,
    onDismiss: () -> Unit
) {
    var code by remember { mutableStateOf("SR-7K42Q") }
    var studentName by remember { mutableStateOf("") }
    var pickupStop by remember { mutableStateOf("Kautha Road") }
    var dropOffStop by remember { mutableStateOf("Kautha Road") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.DirectionsBus, contentDescription = null, tint = SafeRideBlue)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Connect to a Bus", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Enter the unique 8-character Bus Code provided by your vehicle operator or school:",
                    fontSize = 12.sp,
                    color = Color(0xFF64748B)
                )
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it.uppercase() },
                    label = { Text("Bus Code (e.g. SR-7K42Q)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("bus_code_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = studentName,
                    onValueChange = { studentName = it },
                    label = { Text("Child / Student Full Name") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("student_name_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = pickupStop,
                    onValueChange = { pickupStop = it },
                    label = { Text("Pickup Stop") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = dropOffStop,
                    onValueChange = { dropOffStop = it },
                    label = { Text("Drop-off Stop") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (code.isNotBlank() && studentName.isNotBlank()) {
                        onSubmit(code, studentName, pickupStop, dropOffStop)
                    }
                },
                modifier = Modifier.testTag("submit_connect_request_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = SafeRideBlue)
            ) {
                Text("Send Request")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
