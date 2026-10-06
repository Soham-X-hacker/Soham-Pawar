package com.example.ui.admin

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
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
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
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
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
import com.example.data.model.AuditLogEntity
import com.example.data.model.ConnectionRequestEntity
import com.example.data.model.RouteEntity
import com.example.data.model.SafetyAlertEntity
import com.example.data.model.StudentEntity
import com.example.data.model.SystemSettingsEntity
import com.example.data.model.TripEntity
import com.example.data.model.UserEntity
import com.example.data.model.VehicleEntity
import com.example.gps.LiveVehicleLocation
import com.example.localization.AppLanguage
import com.example.localization.StringsDefinition
import com.example.ui.components.LanguageSelectionDialog
import com.example.ui.components.RouteMapCanvas
import com.example.ui.components.SafeRideTopBar
import com.example.ui.components.StatsCard
import com.example.ui.components.StatusBadge
import com.example.ui.theme.AdminDarkBg
import com.example.ui.theme.AdminDarkBorder
import com.example.ui.theme.AdminDarkSurface
import com.example.ui.theme.AdminDarkSurfaceVariant
import com.example.ui.theme.SafeRideBlue
import com.example.ui.theme.SafeRideGold
import com.example.ui.theme.SafeRideNavy
import com.example.ui.theme.StatusDanger
import com.example.ui.theme.StatusSuccess
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AdminMainScreen(
    user: UserEntity,
    vehicles: List<VehicleEntity>,
    routes: List<RouteEntity>,
    students: List<StudentEntity>,
    users: List<UserEntity>,
    trips: List<TripEntity>,
    safetyAlerts: List<SafetyAlertEntity>,
    auditLogs: List<AuditLogEntity>,
    settings: List<SystemSettingsEntity>,
    liveLocation: LiveVehicleLocation?,
    isSimulationMode: Boolean,
    strings: StringsDefinition,
    currentLanguage: AppLanguage,
    onLanguageSelected: (AppLanguage) -> Unit,
    onToggleUserSuspension: (userId: String, isSuspended: Boolean) -> Unit,
    onToggleVehicleStatus: (vehicleId: String, currentStatus: String) -> Unit,
    onResolveAlert: (alertId: String) -> Unit,
    onAddVehicle: ((number: String, type: String, driver: String, phone: String, capacity: Int, school: String) -> Unit)? = null,
    onUpdateAdminProfile: ((fullName: String, phone: String) -> Unit)? = null,
    onUpdateSetting: ((key: String, value: String) -> Unit)? = null,
    onLogout: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    var showLanguageDialog by remember { mutableStateOf(false) }

    val tabs = listOf(
        "Overview",
        "Live Network",
        "Vehicles",
        "Users",
        "Students",
        "Safety Alerts",
        "Audit Logs",
        "Settings"
    )

    BackHandler(enabled = selectedTab != 0) {
        selectedTab = 0
    }

    if (showLanguageDialog) {
        LanguageSelectionDialog(
            currentLanguage = currentLanguage,
            onLanguageSelected = onLanguageSelected,
            onDismiss = { showLanguageDialog = false }
        )
    }

    Scaffold(
        topBar = {
            SafeRideTopBar(
                title = "Security Operations Center",
                subtitle = "Super Admin: ${user.fullName} • Platform Protected",
                currentLanguage = currentLanguage,
                onLanguageClick = { showLanguageDialog = true },
                onLogoutClick = onLogout,
                isAdmin = true
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(AdminDarkBg)
        ) {
            // Admin Sub-Navigation Tabs
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = AdminDarkSurface,
                contentColor = SafeRideGold,
                edgePadding = 12.dp
            ) {
                tabs.forEachIndexed { index, tabName ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = tabName,
                                fontSize = 13.sp,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium,
                                color = if (selectedTab == index) SafeRideGold else Color(0xFF94A3B8)
                            )
                        }
                    )
                }
            }

            // Global Search Bar across authorized records
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search vehicles, users, routes...", fontSize = 12.sp, color = Color(0xFF64748B)) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF94A3B8)) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("admin_search_bar")
                )
            }

            // Tab Content
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                when (selectedTab) {
                    0 -> AdminOverviewTab(
                        vehicles = vehicles,
                        users = users,
                        students = students,
                        trips = trips,
                        safetyAlerts = safetyAlerts,
                        onNavigateToLive = { selectedTab = 1 },
                        onNavigateToAlerts = { selectedTab = 5 }
                    )
                    1 -> AdminLiveNetworkTab(
                        vehicles = vehicles,
                        liveLocation = liveLocation,
                        isSimulationMode = isSimulationMode,
                        safetyAlerts = safetyAlerts
                    )
                    2 -> AdminVehiclesTab(
                        vehicles = vehicles.filter {
                            searchQuery.isBlank() || it.vehicleNumber.contains(searchQuery, ignoreCase = true) || it.busCode.contains(searchQuery, ignoreCase = true)
                        },
                        onToggleVehicleStatus = onToggleVehicleStatus,
                        onAddVehicle = onAddVehicle
                    )
                    3 -> AdminUsersTab(
                        users = users.filter {
                            searchQuery.isBlank() || it.fullName.contains(searchQuery, ignoreCase = true) || it.email.contains(searchQuery, ignoreCase = true)
                        },
                        onToggleUserSuspension = onToggleUserSuspension
                    )
                    4 -> AdminStudentsTab(
                        students = students.filter {
                            searchQuery.isBlank() || it.fullName.contains(searchQuery, ignoreCase = true)
                        },
                        routes = routes
                    )
                    5 -> AdminSafetyAlertsTab(
                        alerts = safetyAlerts,
                        onResolveAlert = onResolveAlert
                    )
                    6 -> AdminAuditLogsTab(auditLogs = auditLogs)
                    7 -> AdminSettingsTab(
                        user = user,
                        settings = settings,
                        onUpdateAdminProfile = onUpdateAdminProfile,
                        onUpdateSetting = onUpdateSetting
                    )
                }
            }
        }
    }
}

@Composable
private fun AdminOverviewTab(
    vehicles: List<VehicleEntity>,
    users: List<UserEntity>,
    students: List<StudentEntity>,
    trips: List<TripEntity>,
    safetyAlerts: List<SafetyAlertEntity>,
    onNavigateToLive: () -> Unit,
    onNavigateToAlerts: () -> Unit
) {
    val activeVehiclesCount = vehicles.count { it.status == "ACTIVE" }
    val onTripCount = trips.count { it.status == "ACTIVE" }
    val openAlerts = safetyAlerts.filter { it.status == "OPEN" }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 8.dp)
    ) {
        // High-level Alert Notice if any open
        if (openAlerts.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToAlerts() },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF7F1D1D)),
                border = androidx.compose.foundation.BorderStroke(1.dp, StatusDanger)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.ReportProblem, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${openAlerts.size} ACTIVE SAFETY ALERTS",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "Review emergency reports and operational delays.",
                            color = Color(0xFFFCA5A5),
                            fontSize = 11.sp
                        )
                    }
                    Text("VIEW", color = SafeRideGold, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Metrics Grid
        Text(
            text = "PLATFORM METRICS & STATUS",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF94A3B8)
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatsCard(
                title = "Total Vehicles",
                value = "${vehicles.size}",
                subtitle = "$activeVehiclesCount Active",
                icon = Icons.Default.DirectionsBus,
                iconTint = SafeRideGold,
                containerColor = AdminDarkSurface,
                textColor = Color.White,
                modifier = Modifier.weight(1f)
            )
            StatsCard(
                title = "On Live Trips",
                value = "$onTripCount",
                subtitle = "Broadcasting GPS",
                icon = Icons.Default.Sensors,
                iconTint = StatusSuccess,
                containerColor = AdminDarkSurface,
                textColor = Color.White,
                modifier = Modifier
                    .weight(1f)
                    .clickable { onNavigateToLive() }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatsCard(
                title = "Registered Users",
                value = "${users.size}",
                subtitle = "${users.count { it.role == "DRIVER" }} Operators",
                icon = Icons.Default.Person,
                iconTint = SafeRideBlue,
                containerColor = AdminDarkSurface,
                textColor = Color.White,
                modifier = Modifier.weight(1f)
            )
            StatsCard(
                title = "Linked Students",
                value = "${students.size}",
                subtitle = "Privacy Isolated",
                icon = Icons.Default.Groups,
                iconTint = Color(0xFF38BDF8),
                containerColor = AdminDarkSurface,
                textColor = Color.White,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Security Status Panel
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = AdminDarkSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, AdminDarkBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = StatusSuccess)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Security & Encryption Status",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text("• Role-Based Access Control (RBAC): ENFORCED", fontSize = 12.sp, color = Color(0xFF94A3B8))
                Text("• Student Cross-User Leakage Protection: ACTIVE", fontSize = 12.sp, color = Color(0xFF94A3B8))
                Text("• Live GPS Coordinates: AUTHORIZED STREAM ONLY", fontSize = 12.sp, color = Color(0xFF94A3B8))
                Text("• MFA Challenge & Rate Limiting: OPERATIONAL", fontSize = 12.sp, color = Color(0xFF94A3B8))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun AdminLiveNetworkTab(
    vehicles: List<VehicleEntity>,
    liveLocation: LiveVehicleLocation?,
    isSimulationMode: Boolean,
    safetyAlerts: List<SafetyAlertEntity>
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 8.dp)
    ) {
        Text("Central Fleet Map", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Text("Private live monitoring for platform operations", fontSize = 12.sp, color = Color(0xFF94A3B8))

        Spacer(modifier = Modifier.height(12.dp))

        RouteMapCanvas(
            vehicleLocation = liveLocation,
            vehicleNumber = vehicles.firstOrNull()?.vehicleNumber ?: "MH-26 AB 1234",
            isSimulationMode = isSimulationMode,
            modifier = Modifier.height(280.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text("NETWORK VEHICLES TELEMETRY", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8))
        Spacer(modifier = Modifier.height(8.dp))

        vehicles.forEach { veh ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = AdminDarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, AdminDarkBorder)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(veh.vehicleNumber, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
                        Text("Driver: ${veh.driverName} • Code: ${veh.busCode}", fontSize = 12.sp, color = Color(0xFF94A3B8))
                        Text("Destination: ${veh.schoolDestination}", fontSize = 11.sp, color = Color(0xFF64748B))
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        StatusBadge(status = veh.status)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (veh.isGpsActive) "GPS ONLINE" else "GPS OFFLINE",
                            fontSize = 10.sp,
                            color = if (veh.isGpsActive) StatusSuccess else StatusDanger,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminVehiclesTab(
    vehicles: List<VehicleEntity>,
    onToggleVehicleStatus: (vehicleId: String, currentStatus: String) -> Unit,
    onAddVehicle: ((number: String, type: String, driver: String, phone: String, capacity: Int, school: String) -> Unit)? = null
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var plateNumber by remember { mutableStateOf("") }
    var vehicleType by remember { mutableStateOf("School Bus") }
    var driverName by remember { mutableStateOf("") }
    var driverPhone by remember { mutableStateOf("") }
    var capacityStr by remember { mutableStateOf("32") }
    var schoolName by remember { mutableStateOf("Model Public School") }

    if (showAddDialog && onAddVehicle != null) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Register New Fleet Vehicle", fontWeight = FontWeight.Bold, color = SafeRideGold) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    OutlinedTextField(
                        value = plateNumber,
                        onValueChange = { plateNumber = it },
                        label = { Text("Plate Number (e.g. MH-26 AB 9988)") },
                        modifier = Modifier.fillMaxWidth().testTag("admin_add_plate_input")
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = vehicleType,
                        onValueChange = { vehicleType = it },
                        label = { Text("Vehicle Type (School Bus, Van, Auto)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = driverName,
                        onValueChange = { driverName = it },
                        label = { Text("Assigned Driver Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = driverPhone,
                        onValueChange = { driverPhone = it },
                        label = { Text("Driver Phone Number") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = capacityStr,
                        onValueChange = { capacityStr = it },
                        label = { Text("Seating Capacity") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = schoolName,
                        onValueChange = { schoolName = it },
                        label = { Text("School Destination") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (plateNumber.isNotBlank() && driverName.isNotBlank()) {
                            val cap = capacityStr.toIntOrNull() ?: 30
                            onAddVehicle(plateNumber, vehicleType, driverName, driverPhone, cap, schoolName)
                            showAddDialog = false
                            plateNumber = ""
                            driverName = ""
                            driverPhone = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SafeRideGold)
                ) {
                    Text("Register & Generate Code", color = SafeRideNavy, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showAddDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (onAddVehicle != null) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "FLEET VEHICLES (${vehicles.size})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF94A3B8)
                    )
                    Button(
                        onClick = { showAddDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = SafeRideGold),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("admin_add_vehicle_btn")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = SafeRideNavy, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Vehicle", color = SafeRideNavy, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
        items(vehicles) { veh ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = AdminDarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, AdminDarkBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(veh.vehicleNumber, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                            Text("Type: ${veh.vehicleType} • Cap: ${veh.capacity}", fontSize = 12.sp, color = Color(0xFF94A3B8))
                        }
                        StatusBadge(status = veh.status)
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Driver: ${veh.driverName} (${veh.driverPhone})", fontSize = 12.sp, color = Color(0xFFCBD5E1))
                    Text("Bus Code: ${veh.busCode}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SafeRideGold)

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Button(
                            onClick = { onToggleVehicleStatus(veh.id, veh.status) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (veh.status == "ACTIVE") StatusDanger else StatusSuccess
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(if (veh.status == "ACTIVE") "Suspend Vehicle" else "Reactivate")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminUsersTab(
    users: List<UserEntity>,
    onToggleUserSuspension: (userId: String, isSuspended: Boolean) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(users) { usr ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = AdminDarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, AdminDarkBorder)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(usr.fullName, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
                            Spacer(modifier = Modifier.width(6.dp))
                            StatusBadge(status = usr.role)
                        }
                        Text(usr.email, fontSize = 12.sp, color = Color(0xFF94A3B8))
                        Text(usr.phone, fontSize = 11.sp, color = Color(0xFF64748B))
                    }

                    if (usr.role != "SUPER_ADMIN") {
                        Button(
                            onClick = { onToggleUserSuspension(usr.id, usr.isSuspended) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (usr.isSuspended) StatusSuccess else StatusDanger
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(if (usr.isSuspended) "Reactivate" else "Suspend", fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminStudentsTab(
    students: List<StudentEntity>,
    routes: List<RouteEntity>
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(students) { st ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = AdminDarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, AdminDarkBorder)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(st.fullName, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
                        Text("School: ${st.schoolName}", fontSize = 12.sp, color = Color(0xFF94A3B8))
                        Text("Stops: ${st.pickupStop} ➔ ${st.dropOffStop}", fontSize = 11.sp, color = Color(0xFF64748B))
                    }
                    StatusBadge(status = st.pickupStatus)
                }
            }
        }
    }
}

@Composable
private fun AdminSafetyAlertsTab(
    alerts: List<SafetyAlertEntity>,
    onResolveAlert: (alertId: String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(alerts) { alert ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = AdminDarkSurface),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (alert.severity == "EMERGENCY") StatusDanger else AdminDarkBorder
                )
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "[${alert.severity}] ${alert.category}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = if (alert.severity == "EMERGENCY") StatusDanger else Color.White
                        )
                        StatusBadge(status = alert.status)
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(alert.description, fontSize = 13.sp, color = Color(0xFFCBD5E1))
                    Text("Reported by ${alert.reporterName} (${alert.reporterRole})", fontSize = 11.sp, color = Color(0xFF64748B))

                    if (alert.status == "OPEN") {
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = { onResolveAlert(alert.id) },
                            colors = ButtonDefaults.buttonColors(containerColor = StatusSuccess),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("Mark Resolved")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminAuditLogsTab(auditLogs: List<AuditLogEntity>) {
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(auditLogs) { log ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = AdminDarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, AdminDarkBorder)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = log.action,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = SafeRideGold
                        )
                        Text(
                            text = dateFormat.format(Date(log.timestamp)),
                            fontSize = 10.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(log.details, fontSize = 12.sp, color = Color(0xFFCBD5E1))
                    Text("Actor: ${log.actorName} (${log.actorRole}) • Target: ${log.targetResource}", fontSize = 10.sp, color = Color(0xFF94A3B8))
                }
            }
        }
    }
}

@Composable
private fun AdminSettingsTab(
    user: UserEntity,
    settings: List<SystemSettingsEntity>,
    onUpdateAdminProfile: ((fullName: String, phone: String) -> Unit)? = null,
    onUpdateSetting: ((key: String, value: String) -> Unit)? = null
) {
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var editFullName by remember { mutableStateOf(user.fullName) }
    var editPhone by remember { mutableStateOf(user.phone) }

    var showAddSettingDialog by remember { mutableStateOf(false) }
    var settingKey by remember { mutableStateOf("") }
    var settingValue by remember { mutableStateOf("") }

    if (showEditProfileDialog && onUpdateAdminProfile != null) {
        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            title = { Text("Update Platform Director Profile", fontWeight = FontWeight.Bold, color = SafeRideGold) },
            text = {
                Column {
                    OutlinedTextField(
                        value = editFullName,
                        onValueChange = { editFullName = it },
                        label = { Text("Full Name") },
                        modifier = Modifier.fillMaxWidth().testTag("admin_edit_name_input")
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = editPhone,
                        onValueChange = { editPhone = it },
                        label = { Text("Emergency Phone Number") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth().testTag("admin_edit_phone_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editFullName.isNotBlank()) {
                            onUpdateAdminProfile(editFullName, editPhone)
                            showEditProfileDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SafeRideGold)
                ) {
                    Text("Save Changes", color = SafeRideNavy, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showEditProfileDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showAddSettingDialog && onUpdateSetting != null) {
        AlertDialog(
            onDismissRequest = { showAddSettingDialog = false },
            title = { Text("Add / Update System Setting", fontWeight = FontWeight.Bold, color = SafeRideGold) },
            text = {
                Column {
                    OutlinedTextField(
                        value = settingKey,
                        onValueChange = { settingKey = it },
                        label = { Text("Setting Key (e.g. emergency_helpline)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = settingValue,
                        onValueChange = { settingValue = it },
                        label = { Text("Setting Value") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (settingKey.isNotBlank()) {
                            onUpdateSetting(settingKey.trim().lowercase(), settingValue.trim())
                            showAddSettingDialog = false
                            settingKey = ""
                            settingValue = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SafeRideGold)
                ) {
                    Text("Save Setting", color = SafeRideNavy, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showAddSettingDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 8.dp)
    ) {
        Text("Platform Administration & Ownership", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = AdminDarkSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, SafeRideGold.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("SUPER ADMIN PROFILE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SafeRideGold)
                    if (onUpdateAdminProfile != null) {
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable {
                                    editFullName = user.fullName
                                    editPhone = user.phone
                                    showEditProfileDialog = true
                                },
                            color = Color(0xFF1E293B)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = null, tint = SafeRideGold, modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Edit Profile", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(user.fullName, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text(user.email, fontSize = 13.sp, color = Color(0xFF94A3B8))
                if (user.phone.isNotBlank()) {
                    Text("Phone: ${user.phone}", fontSize = 12.sp, color = Color(0xFFCBD5E1))
                }
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = Color(0xFF0F291E),
                    shape = RoundedCornerShape(6.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF10B981))
                ) {
                    Text(
                        text = "● ROOT PLATFORM DIRECTOR",
                        color = Color(0xFF34D399),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Platform Operational Settings", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            if (onUpdateSetting != null) {
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { showAddSettingDialog = true },
                    color = Color(0xFF1E293B)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = SafeRideGold, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Setting", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = AdminDarkSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, AdminDarkBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                if (settings.isEmpty()) {
                    Text("No operational settings configured yet.", fontSize = 12.sp, color = Color(0xFF94A3B8))
                } else {
                    settings.forEach { set ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(set.key.replace("_", " ").uppercase(), fontSize = 12.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.SemiBold)
                            Text(set.value, fontSize = 13.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
