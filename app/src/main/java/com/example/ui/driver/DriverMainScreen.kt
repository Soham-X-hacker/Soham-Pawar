package com.example.ui.driver

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.AltRoute
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ConnectionRequestEntity
import com.example.data.model.NotificationEntity
import com.example.data.model.RouteEntity
import com.example.data.model.StudentEntity
import com.example.data.model.TripEntity
import com.example.data.model.UserEntity
import com.example.data.model.VehicleEntity
import com.example.gps.LiveVehicleLocation
import com.example.localization.AppLanguage
import com.example.localization.StringsDefinition
import com.example.ui.components.LanguageSelectionDialog
import com.example.ui.components.RouteMapCanvas
import com.example.ui.components.SafeRideTopBar
import com.example.ui.components.SafetyReportDialog
import com.example.ui.components.StatsCard
import com.example.ui.components.StatusBadge
import com.example.ui.theme.SafeRideBlue
import com.example.ui.theme.SafeRideGold
import com.example.ui.theme.SafeRideNavy
import com.example.ui.theme.StatusDanger
import com.example.ui.theme.StatusSuccess

@Composable
fun DriverMainScreen(
    user: UserEntity,
    vehicles: List<VehicleEntity>,
    routes: List<RouteEntity>,
    students: List<StudentEntity>,
    trips: List<TripEntity>,
    connectionRequests: List<ConnectionRequestEntity>,
    notifications: List<NotificationEntity>,
    liveLocation: LiveVehicleLocation?,
    isSimulationMode: Boolean,
    strings: StringsDefinition,
    currentLanguage: AppLanguage,
    onLanguageSelected: (AppLanguage) -> Unit,
    onToggleSimulation: (Boolean) -> Unit,
    onStartTrip: (vehicleId: String, routeId: String) -> Unit,
    onMarkStudentPickup: (tripId: String, studentId: String, studentName: String, parentId: String) -> Unit,
    onMarkSchoolArrival: (tripId: String, vehicleId: String) -> Unit,
    onMarkStudentDropOff: (tripId: String, studentId: String, studentName: String, parentId: String) -> Unit,
    onEndTrip: (tripId: String) -> Unit,
    onApproveRequest: (requestId: String) -> Unit,
    onRejectRequest: (requestId: String) -> Unit,
    onAddVehicle: (number: String, type: String, driverName: String, phone: String, capacity: Int, school: String) -> Unit,
    onRegenerateCode: (vehicleId: String) -> Unit,
    onReportSafetyIssue: (vehicleId: String, severity: String, category: String, desc: String) -> Unit,
    onLogout: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Dashboard, 1: Live Trip, 2: Students, 3: Vehicles
    var showAddVehicleDialog by remember { mutableStateOf(false) }
    var showReportDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }

    BackHandler(enabled = selectedTab != 0) {
        selectedTab = 0
    }

    // Isolate data to this operator's vehicles
    val myVehicles = vehicles.filter { it.operatorId == user.id }
    val primaryVehicle = myVehicles.firstOrNull() ?: vehicles.firstOrNull()
    val myStudents = students.filter { st -> myVehicles.any { it.id == st.vehicleId } }
    val myRequests = connectionRequests.filter { req -> myVehicles.any { it.id == req.vehicleId } }
    val myRoutes = routes.filter { r -> myVehicles.any { it.id == r.vehicleId } }
    val activeTrip = trips.firstOrNull { it.vehicleId == primaryVehicle?.id && it.status == "ACTIVE" }

    if (showLanguageDialog) {
        LanguageSelectionDialog(
            currentLanguage = currentLanguage,
            onLanguageSelected = onLanguageSelected,
            onDismiss = { showLanguageDialog = false }
        )
    }

    if (showReportDialog && primaryVehicle != null) {
        SafetyReportDialog(
            vehicleId = primaryVehicle.id,
            reporterRole = "DRIVER",
            onSubmit = { severity, category, description ->
                onReportSafetyIssue(primaryVehicle.id, severity, category, description)
            },
            onDismiss = { showReportDialog = false }
        )
    }

    if (showAddVehicleDialog) {
        AddVehicleDialog(
            operatorName = user.fullName,
            operatorPhone = user.phone,
            onAdd = { num, type, driverName, phone, capacity, school ->
                onAddVehicle(num, type, driverName, phone, capacity, school)
                showAddVehicleDialog = false
            },
            onDismiss = { showAddVehicleDialog = false }
        )
    }

    Scaffold(
        topBar = {
            SafeRideTopBar(
                title = "Driver Control",
                subtitle = "${user.fullName} • ${primaryVehicle?.vehicleNumber ?: "Transport Operator"}",
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
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                    label = { Text("Dashboard", fontSize = 11.sp) }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Navigation, contentDescription = "Live Trip") },
                    label = { Text("Live Trip", fontSize = 11.sp) }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.Groups, contentDescription = "Students") },
                    label = { Text("Students", fontSize = 11.sp) }
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Default.DirectionsBus, contentDescription = "Vehicles") },
                    label = { Text("Vehicles", fontSize = 11.sp) }
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
            when (selectedTab) {
                0 -> {
                    DriverDashboardView(
                        user = user,
                        vehicle = primaryVehicle,
                        activeTrip = activeTrip,
                        studentsCount = myStudents.size,
                        pendingRequestsCount = myRequests.count { it.status == "PENDING" },
                        isSimulationMode = isSimulationMode,
                        onGoToLiveTrip = { selectedTab = 1 },
                        onGoToStudents = { selectedTab = 2 },
                        onAddVehicleClick = { showAddVehicleDialog = true },
                        onReportIssueClick = { showReportDialog = true }
                    )
                }
                1 -> {
                    DriverLiveTripView(
                        vehicle = primaryVehicle,
                        route = myRoutes.firstOrNull(),
                        activeTrip = activeTrip,
                        students = myStudents,
                        liveLocation = liveLocation,
                        isSimulationMode = isSimulationMode,
                        onToggleSimulation = onToggleSimulation,
                        onStartTrip = { vId, rId -> onStartTrip(vId, rId) },
                        onMarkPickup = { tId, sId, name, pId -> onMarkStudentPickup(tId, sId, name, pId) },
                        onMarkSchoolArrival = { tId, vId -> onMarkSchoolArrival(tId, vId) },
                        onMarkDropOff = { tId, sId, name, pId -> onMarkStudentDropOff(tId, sId, name, pId) },
                        onEndTrip = onEndTrip,
                        onReportClick = { showReportDialog = true }
                    )
                }
                2 -> {
                    DriverStudentsView(
                        students = myStudents,
                        requests = myRequests,
                        onApproveRequest = onApproveRequest,
                        onRejectRequest = onRejectRequest
                    )
                }
                3 -> {
                    DriverVehiclesView(
                        vehicles = myVehicles,
                        onAddVehicleClick = { showAddVehicleDialog = true },
                        onRegenerateCode = onRegenerateCode
                    )
                }
            }
        }
    }
}

@Composable
private fun DriverDashboardView(
    user: UserEntity,
    vehicle: VehicleEntity?,
    activeTrip: TripEntity?,
    studentsCount: Int,
    pendingRequestsCount: Int,
    isSimulationMode: Boolean,
    onGoToLiveTrip: () -> Unit,
    onGoToStudents: () -> Unit,
    onAddVehicleClick: () -> Unit,
    onReportIssueClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Operator Status Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SafeRideNavy),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "ASSIGNED VEHICLE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF94A3B8)
                        )
                        Text(
                            text = vehicle?.vehicleNumber ?: "No Vehicle Assigned",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Surface(
                        color = if (activeTrip != null) StatusSuccess else Color(0xFF334155),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = if (activeTrip != null) "TRIP RUNNING" else "STANDBY",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Bus Code badge
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF1E293B))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("UNIQUE BUS CODE", fontSize = 10.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
                        Text(
                            text = vehicle?.busCode ?: "N/A",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = SafeRideGold
                        )
                    }
                    Text(
                        text = "Share with parents to connect",
                        fontSize = 11.sp,
                        color = Color(0xFFCBD5E1)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // High priority action: Big START / RESUME TRIP button
        Button(
            onClick = onGoToLiveTrip,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("driver_start_trip_main_btn"),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (activeTrip != null) Color(0xFF059669) else SafeRideBlue
            ),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(
                imageVector = if (activeTrip != null) Icons.Default.Navigation else Icons.Default.PlayArrow,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = if (activeTrip != null) "VIEW ACTIVE TRIP IN PROGRESS" else "START SCHEDULED TRIP",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Key stats
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatsCard(
                title = "Connected Students",
                value = "$studentsCount",
                subtitle = "Authorized roster",
                icon = Icons.Default.Groups,
                modifier = Modifier.weight(1f)
            )
            StatsCard(
                title = "Connection Requests",
                value = "$pendingRequestsCount",
                subtitle = if (pendingRequestsCount > 0) "Requires approval" else "All processed",
                icon = Icons.Default.Notifications,
                iconTint = if (pendingRequestsCount > 0) SafeRideGold else SafeRideBlue,
                modifier = Modifier
                    .weight(1f)
                    .clickable { onGoToStudents() }
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Safety Issue Quick Button
        Button(
            onClick = onReportIssueClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .testTag("driver_report_issue_btn"),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFEF2F2)),
            shape = RoundedCornerShape(10.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFECACA))
        ) {
            Icon(Icons.Default.ReportProblem, contentDescription = null, tint = StatusDanger, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Report Traffic, Breakdown, or Emergency", color = StatusDanger, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun DriverLiveTripView(
    vehicle: VehicleEntity?,
    route: RouteEntity?,
    activeTrip: TripEntity?,
    students: List<StudentEntity>,
    liveLocation: LiveVehicleLocation?,
    isSimulationMode: Boolean,
    onToggleSimulation: (Boolean) -> Unit,
    onStartTrip: (vehicleId: String, routeId: String) -> Unit,
    onMarkPickup: (tripId: String, studentId: String, name: String, parentId: String) -> Unit,
    onMarkSchoolArrival: (tripId: String, vehicleId: String) -> Unit,
    onMarkDropOff: (tripId: String, studentId: String, name: String, parentId: String) -> Unit,
    onEndTrip: (tripId: String) -> Unit,
    onReportClick: () -> Unit
) {
    if (vehicle == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No vehicle registered. Please add a vehicle first.")
        }
        return
    }

    val isTripActive = activeTrip != null

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Trip Status Header Banner
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isTripActive) Color(0xFFDCFCE7) else Color.White
            ),
            border = androidx.compose.foundation.BorderStroke(
                1.5.dp,
                if (isTripActive) Color(0xFF10B981) else Color(0xFFCBD5E1)
            )
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isTripActive) "TRIP ACTIVE • GPS BROADCASTING" else "READY TO DISPATCH",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isTripActive) Color(0xFF166534) else SafeRideNavy
                    )
                    StatusBadge(status = if (isTripActive) "ACTIVE" else "STANDBY")
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${vehicle.vehicleNumber} • ${route?.routeName ?: "Route 04"}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = SafeRideNavy
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Map Canvas
        RouteMapCanvas(
            vehicleLocation = liveLocation,
            vehicleNumber = vehicle.vehicleNumber,
            isSimulationMode = isSimulationMode,
            modifier = Modifier.height(240.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Simulation Mode Toggle Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
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
                Column {
                    Text("Route Waypoint Tracking", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text("GPS telemetry aligned with assigned route", fontSize = 10.sp, color = Color(0xFF64748B))
                }
                Switch(
                    checked = isSimulationMode,
                    onCheckedChange = onToggleSimulation,
                    modifier = Modifier.testTag("driver_sim_toggle")
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Major Trip Controls
        if (!isTripActive) {
            Button(
                onClick = { onStartTrip(vehicle.id, route?.id ?: "route_04") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("driver_start_trip_action"),
                colors = ButtonDefaults.buttonColors(containerColor = SafeRideBlue),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("START TRIP NOW", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        } else {
            // Workflow Actions during active trip
            Text(
                text = "TRIP WORKFLOW CONTROLS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF64748B)
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Action: School Arrival
            Button(
                onClick = { onMarkSchoolArrival(activeTrip.id, vehicle.id) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .testTag("mark_school_arrival_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Mark School Campus Arrival", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // End Trip
            Button(
                onClick = { onEndTrip(activeTrip.id) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .testTag("driver_end_trip_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = StatusDanger),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Stop, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("END TRIP", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Student Roster & Pickup Actions
        Text(
            text = "STUDENT BOARDING & DROP STATUS",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF64748B)
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (students.isEmpty()) {
            Text("No students connected yet.", fontSize = 12.sp, color = Color(0xFF94A3B8))
        } else {
            students.forEach { st ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(st.fullName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Stop: ${st.pickupStop}", fontSize = 11.sp, color = Color(0xFF64748B))
                            StatusBadge(status = st.pickupStatus)
                        }

                        if (isTripActive) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Button(
                                    onClick = { onMarkPickup(activeTrip.id, st.id, st.fullName, st.parentId) },
                                    enabled = st.pickupStatus != "PICKED_UP",
                                    colors = ButtonDefaults.buttonColors(containerColor = SafeRideBlue),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text("Board", fontSize = 11.sp)
                                }

                                Button(
                                    onClick = { onMarkDropOff(activeTrip.id, st.id, st.fullName, st.parentId) },
                                    enabled = st.pickupStatus == "PICKED_UP",
                                    colors = ButtonDefaults.buttonColors(containerColor = StatusSuccess),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text("Drop", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun DriverStudentsView(
    students: List<StudentEntity>,
    requests: List<ConnectionRequestEntity>,
    onApproveRequest: (requestId: String) -> Unit,
    onRejectRequest: (requestId: String) -> Unit
) {
    val pendingRequests = requests.filter { it.status == "PENDING" }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Student Management", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = SafeRideNavy)
        Text("Authorized roster and parent connection requests", fontSize = 12.sp, color = Color(0xFF64748B))

        Spacer(modifier = Modifier.height(16.dp))

        if (pendingRequests.isNotEmpty()) {
            Text(
                text = "PENDING CONNECTION REQUESTS (${pendingRequests.size})",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = SafeRideGold
            )
            Spacer(modifier = Modifier.height(8.dp))

            pendingRequests.forEach { req ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFDE68A))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(req.studentName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text("Code: ${req.busCode}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SafeRideGold)
                        }
                        Text("Parent: ${req.parentName} • Pickup: ${req.pickupStop}", fontSize = 12.sp, color = Color(0xFF92400E))

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            OutlinedButton(
                                onClick = { onRejectRequest(req.id) },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("driver_reject_req_${req.id}")
                            ) {
                                Text("Decline", fontSize = 12.sp)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = { onApproveRequest(req.id) },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = SafeRideBlue),
                                modifier = Modifier.testTag("driver_approve_req_${req.id}")
                            ) {
                                Text("Approve & Link", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        Text(
            text = "ASSIGNED STUDENT ROSTER (${students.size})",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF64748B)
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (students.isEmpty()) {
            Text("No students currently connected.", color = Color(0xFF94A3B8), fontSize = 12.sp)
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(students) { st ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(st.fullName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Pickup: ${st.pickupStop} • Drop: ${st.dropOffStop}", fontSize = 11.sp, color = Color(0xFF64748B))
                            }
                            StatusBadge(status = st.pickupStatus)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DriverVehiclesView(
    vehicles: List<VehicleEntity>,
    onAddVehicleClick: () -> Unit,
    onRegenerateCode: (vehicleId: String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Vehicles & Bus Codes", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = SafeRideNavy)
                Text("Manage fleet and connection keys", fontSize = 12.sp, color = Color(0xFF64748B))
            }
            Button(
                onClick = onAddVehicleClick,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SafeRideBlue),
                modifier = Modifier.testTag("driver_add_vehicle_btn")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Vehicle", fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(vehicles) { veh ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(veh.vehicleNumber, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = SafeRideNavy)
                            StatusBadge(status = veh.status)
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Type: ${veh.vehicleType} • Capacity: ${veh.capacity} seats", fontSize = 12.sp, color = Color(0xFF64748B))
                        Text("School: ${veh.schoolDestination}", fontSize = 12.sp, color = Color(0xFF64748B))

                        Spacer(modifier = Modifier.height(10.dp))

                        // Bus Code & Refresh
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFF1F5F9)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("SAFE RIDE BUS CODE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                                    Text(veh.busCode, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = SafeRideBlue)
                                }
                                IconButton(
                                    onClick = { onRegenerateCode(veh.id) },
                                    modifier = Modifier.testTag("regen_code_${veh.id}")
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = "Regenerate Bus Code", tint = SafeRideBlue)
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
fun AddVehicleDialog(
    operatorName: String,
    operatorPhone: String,
    onAdd: (num: String, type: String, driverName: String, phone: String, cap: Int, school: String) -> Unit,
    onDismiss: () -> Unit
) {
    var vehicleNumber by remember { mutableStateOf("") }
    var vehicleType by remember { mutableStateOf("School Bus") }
    var driverName by remember { mutableStateOf(operatorName) }
    var driverPhone by remember { mutableStateOf(operatorPhone) }
    var capacity by remember { mutableStateOf("32") }
    var schoolName by remember { mutableStateOf("Model Public School") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Vehicle", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = vehicleNumber,
                    onValueChange = { vehicleNumber = it.uppercase() },
                    label = { Text("Vehicle Number (e.g. MH-26 AB 1234)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = vehicleType,
                    onValueChange = { vehicleType = it },
                    label = { Text("Vehicle Type (Bus, Van, Auto, Car)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = capacity,
                    onValueChange = { capacity = it },
                    label = { Text("Capacity") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = schoolName,
                    onValueChange = { schoolName = it },
                    label = { Text("School / Destination") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (vehicleNumber.isNotBlank()) {
                        onAdd(vehicleNumber, vehicleType, driverName, driverPhone, capacity.toIntOrNull() ?: 24, schoolName)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = SafeRideBlue)
            ) {
                Text("Add Vehicle")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
