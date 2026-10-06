package com.example.data.repository

import com.example.data.dao.SafeRideDao
import com.example.data.model.AuditLogEntity
import com.example.data.model.ConnectionRequestEntity
import com.example.data.model.NotificationEntity
import com.example.data.model.RouteEntity
import com.example.data.model.SafetyAlertEntity
import com.example.data.model.StudentEntity
import com.example.data.model.SystemSettingsEntity
import com.example.data.model.TripEntity
import com.example.data.model.TripEventEntity
import com.example.data.model.UserEntity
import com.example.data.model.VehicleEntity
import com.example.security.AccessControlGateway
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.security.SecureRandom
import java.util.UUID

class SafeRideRepository(private val dao: SafeRideDao) {

    val accessControl = AccessControlGateway(dao)

    init {
        CoroutineScope(Dispatchers.IO).launch {
            seedInitialDataIfNeeded()
        }
    }

    // --- Users & Auth ---
    suspend fun getUserByEmail(email: String): UserEntity? = dao.getUserByEmail(email)
    suspend fun getUserById(id: String): UserEntity? = dao.getUserById(id)
    fun getAllUsers(): Flow<List<UserEntity>> = dao.getAllUsers()
    suspend fun insertUser(user: UserEntity) = dao.insertUser(user)
    suspend fun setUserSuspended(userId: String, suspended: Boolean) = dao.setUserSuspended(userId, suspended)

    suspend fun getAdminUser(): UserEntity? {
        val all = dao.getAllUsers().first()
        return all.firstOrNull { it.role == "SUPER_ADMIN" }
    }

    fun isAdminPasswordConfiguredFlow(): Flow<Boolean> = dao.getAllUsers().map { users ->
        val admin = users.firstOrNull { it.role == "SUPER_ADMIN" }
        !admin?.passwordHash.isNullOrBlank()
    }

    suspend fun updateAdminProfile(fullName: String, phone: String) {
        val admin = getAdminUser() ?: return
        val updated = admin.copy(fullName = fullName, phone = phone)
        dao.insertUser(updated)
        dao.insertAuditLog(
            AuditLogEntity(
                id = UUID.randomUUID().toString(),
                actorId = updated.id,
                actorName = updated.fullName,
                actorRole = "SUPER_ADMIN",
                action = "UPDATE_ADMIN_PROFILE",
                targetResource = "Super Admin Profile",
                details = "Updated root admin details for ${updated.fullName}"
            )
        )
    }

    suspend fun setAdminPassword(password: String): UserEntity {
        val admin = getAdminUser() ?: UserEntity(
            id = "user_admin_soham",
            email = "soham.pawar.8765@gmail.com",
            passwordHash = password,
            role = "SUPER_ADMIN",
            fullName = "Soham Pawar",
            phone = "+91 98000 00000"
        )
        val updated = admin.copy(passwordHash = password)
        dao.insertUser(updated)
        dao.insertAuditLog(
            AuditLogEntity(
                id = UUID.randomUUID().toString(),
                actorId = updated.id,
                actorName = "Soham Pawar",
                actorRole = "SUPER_ADMIN",
                action = "ADMIN_PASSWORD_CONFIGURED",
                targetResource = "Master Admin Security",
                details = "Master administrative password set by Soham Pawar"
            )
        )
        return updated
    }

    // --- Vehicles ---
    fun getAllVehicles(): Flow<List<VehicleEntity>> = dao.getAllVehicles()
    fun getVehicleByIdFlow(vehicleId: String): Flow<VehicleEntity?> = dao.getVehicleByIdFlow(vehicleId)
    suspend fun getVehicleById(vehicleId: String): VehicleEntity? = dao.getVehicleById(vehicleId)
    suspend fun getVehicleByBusCode(busCode: String): VehicleEntity? = dao.getVehicleByBusCode(busCode.trim().uppercase())
    fun getVehiclesByOperator(operatorId: String): Flow<List<VehicleEntity>> = dao.getVehiclesByOperator(operatorId)
    suspend fun insertVehicle(vehicle: VehicleEntity) = dao.insertVehicle(vehicle)
    suspend fun updateVehicle(vehicle: VehicleEntity) = dao.updateVehicle(vehicle)
    suspend fun updateVehicleStatus(vehicleId: String, status: String) = dao.updateVehicleStatus(vehicleId, status)
    suspend fun updateVehicleLocation(vehicleId: String, lat: Double, lng: Double, speed: Float) {
        dao.updateVehicleLocation(vehicleId, lat, lng, speed, System.currentTimeMillis())
    }

    // --- Routes ---
    fun getAllRoutes(): Flow<List<RouteEntity>> = dao.getAllRoutes()
    fun getRouteForVehicle(vehicleId: String): Flow<RouteEntity?> = dao.getRouteForVehicle(vehicleId)
    fun getRoutesByOperator(operatorId: String): Flow<List<RouteEntity>> = dao.getRoutesByOperator(operatorId)
    suspend fun insertRoute(route: RouteEntity) = dao.insertRoute(route)

    // --- Students ---
    fun getStudentsByParent(parentId: String): Flow<List<StudentEntity>> = dao.getStudentsByParent(parentId)
    fun getStudentsByVehicle(vehicleId: String): Flow<List<StudentEntity>> = dao.getStudentsByVehicle(vehicleId)
    fun getAllStudents(): Flow<List<StudentEntity>> = dao.getAllStudents()
    suspend fun insertStudent(student: StudentEntity) = dao.insertStudent(student)
    suspend fun updateStudentPickupStatus(studentId: String, status: String) = dao.updateStudentPickupStatus(studentId, status)
    suspend fun deleteStudent(studentId: String) = dao.deleteStudent(studentId)

    // --- Connection Requests ---
    fun getConnectionRequestsForVehicle(vehicleId: String): Flow<List<ConnectionRequestEntity>> = dao.getConnectionRequestsForVehicle(vehicleId)
    fun getConnectionRequestsForParent(parentId: String): Flow<List<ConnectionRequestEntity>> = dao.getConnectionRequestsForParent(parentId)
    fun getAllConnectionRequests(): Flow<List<ConnectionRequestEntity>> = dao.getAllConnectionRequests()
    suspend fun insertConnectionRequest(request: ConnectionRequestEntity) = dao.insertConnectionRequest(request)

    suspend fun approveConnectionRequest(requestId: String, approverName: String) {
        val req = dao.getConnectionRequestById(requestId) ?: return
        dao.updateConnectionRequestStatus(requestId, "APPROVED")

        // Add the student officially to the database
        val student = StudentEntity(
            id = "std_${UUID.randomUUID().toString().take(8)}",
            fullName = req.studentName,
            parentId = req.parentId,
            vehicleId = req.vehicleId,
            routeId = "route_04",
            pickupStop = req.pickupStop,
            dropOffStop = req.dropOffStop,
            pickupStatus = "WAITING",
            rollNumber = "SR-${(10..99).random()}",
            schoolName = "Model Public School"
        )
        dao.insertStudent(student)

        // Notify parent
        dao.insertNotification(
            NotificationEntity(
                id = UUID.randomUUID().toString(),
                targetUserId = req.parentId,
                title = "Connection Request Approved",
                message = "Your request to connect ${req.studentName} to the bus has been approved by the operator.",
                type = "SUCCESS"
            )
        )

        // Audit Log
        dao.insertAuditLog(
            AuditLogEntity(
                id = UUID.randomUUID().toString(),
                actorId = req.vehicleId,
                actorName = approverName,
                actorRole = "DRIVER",
                action = "APPROVE_CONNECTION",
                targetResource = "Student: ${req.studentName}",
                details = "Approved connection for parent ${req.parentName} to vehicle ${req.vehicleId}"
            )
        )
    }

    suspend fun rejectConnectionRequest(requestId: String, rejecterName: String) {
        val req = dao.getConnectionRequestById(requestId) ?: return
        dao.updateConnectionRequestStatus(requestId, "REJECTED")

        dao.insertNotification(
            NotificationEntity(
                id = UUID.randomUUID().toString(),
                targetUserId = req.parentId,
                title = "Connection Request Declined",
                message = "Your request to connect ${req.studentName} could not be approved at this time.",
                type = "WARNING"
            )
        )

        dao.insertAuditLog(
            AuditLogEntity(
                id = UUID.randomUUID().toString(),
                actorId = req.vehicleId,
                actorName = rejecterName,
                actorRole = "DRIVER",
                action = "REJECT_CONNECTION",
                targetResource = "Request: $requestId",
                details = "Rejected connection request for student ${req.studentName}"
            )
        )
    }

    // --- Trips ---
    fun getActiveTripForVehicle(vehicleId: String): Flow<TripEntity?> = dao.getActiveTripForVehicle(vehicleId)
    fun getTripsForDriver(driverId: String): Flow<List<TripEntity>> = dao.getTripsForDriver(driverId)
    fun getAllTrips(): Flow<List<TripEntity>> = dao.getAllTrips()
    suspend fun getTripById(tripId: String): TripEntity? = dao.getTripById(tripId)
    suspend fun insertTrip(trip: TripEntity) = dao.insertTrip(trip)
    suspend fun updateTrip(trip: TripEntity) = dao.updateTrip(trip)

    suspend fun startTrip(vehicleId: String, routeId: String, driverId: String, driverName: String): TripEntity {
        val tripId = "trip_${System.currentTimeMillis()}"
        val vehicle = dao.getVehicleById(vehicleId)
        val vehicleNumber = vehicle?.vehicleNumber ?: "Vehicle"
        val newTrip = TripEntity(
            id = tripId,
            vehicleId = vehicleId,
            routeId = routeId,
            driverId = driverId,
            tripName = "Morning Route 04",
            type = "MORNING_PICKUP",
            status = "ACTIVE",
            startTime = System.currentTimeMillis(),
            currentStopIndex = 0,
            etaMinutes = 12,
            nextStopName = "Kautha Road",
            delayMinutes = 0
        )
        dao.insertTrip(newTrip)

        // Trip Event
        dao.insertTripEvent(
            TripEventEntity(
                id = UUID.randomUUID().toString(),
                tripId = tripId,
                eventType = "TRIP_STARTED",
                title = "Trip Started",
                description = "Bus $vehicleNumber has started the morning pickup journey."
            )
        )

        // Broadcast notification to all parents
        dao.insertNotification(
            NotificationEntity(
                id = UUID.randomUUID().toString(),
                targetUserId = "ALL",
                title = "Bus Journey Started",
                message = "Your child's bus ($vehicleNumber) has started the morning pickup trip.",
                type = "INFO"
            )
        )

        // Audit Log
        dao.insertAuditLog(
            AuditLogEntity(
                id = UUID.randomUUID().toString(),
                actorId = driverId,
                actorName = driverName,
                actorRole = "DRIVER",
                action = "START_TRIP",
                targetResource = "Trip: $tripId",
                details = "Started morning trip on $vehicleNumber (Route 04)"
            )
        )
        return newTrip
    }

    suspend fun markStudentPickedUp(tripId: String, studentId: String, studentName: String, parentId: String) {
        dao.updateStudentPickupStatus(studentId, "PICKED_UP")
        dao.insertTripEvent(
            TripEventEntity(
                id = UUID.randomUUID().toString(),
                tripId = tripId,
                eventType = "STUDENT_PICKED_UP",
                title = "Student Boarded",
                description = "$studentName safely boarded the bus.",
                studentId = studentId
            )
        )
        dao.insertNotification(
            NotificationEntity(
                id = UUID.randomUUID().toString(),
                targetUserId = parentId,
                title = "Child Picked Up",
                message = "Your child $studentName has been marked as picked up safely.",
                type = "SUCCESS"
            )
        )
    }

    suspend fun markSchoolArrival(tripId: String, vehicleId: String) {
        dao.insertTripEvent(
            TripEventEntity(
                id = UUID.randomUUID().toString(),
                tripId = tripId,
                eventType = "SCHOOL_ARRIVAL",
                title = "School Arrival",
                description = "Bus safely reached Model Public School."
            )
        )
        dao.insertNotification(
            NotificationEntity(
                id = UUID.randomUUID().toString(),
                targetUserId = "ALL",
                title = "School Arrival",
                message = "The bus has safely arrived at the school campus.",
                type = "SUCCESS"
            )
        )
    }

    suspend fun markStudentDroppedOff(tripId: String, studentId: String, studentName: String, parentId: String) {
        dao.updateStudentPickupStatus(studentId, "DROPPED_OFF")
        dao.insertTripEvent(
            TripEventEntity(
                id = UUID.randomUUID().toString(),
                tripId = tripId,
                eventType = "STUDENT_DROPPED_OFF",
                title = "Student Dropped Off",
                description = "$studentName has reached safely.",
                studentId = studentId
            )
        )
        dao.insertNotification(
            NotificationEntity(
                id = UUID.randomUUID().toString(),
                targetUserId = parentId,
                title = "Child Dropped Off",
                message = "Your child $studentName has been dropped off safely.",
                type = "SUCCESS"
            )
        )
    }

    suspend fun endTrip(tripId: String, driverName: String) {
        val trip = dao.getTripById(tripId) ?: return
        val completedTrip = trip.copy(
            status = "COMPLETED",
            endTime = System.currentTimeMillis()
        )
        dao.updateTrip(completedTrip)

        dao.insertTripEvent(
            TripEventEntity(
                id = UUID.randomUUID().toString(),
                tripId = tripId,
                eventType = "TRIP_COMPLETED",
                title = "Trip Completed",
                description = "All stops concluded safely."
            )
        )

        dao.insertNotification(
            NotificationEntity(
                id = UUID.randomUUID().toString(),
                targetUserId = "ALL",
                title = "Trip Completed",
                message = "The scheduled route has concluded.",
                type = "INFO"
            )
        )

        dao.insertAuditLog(
            AuditLogEntity(
                id = UUID.randomUUID().toString(),
                actorId = trip.driverId,
                actorName = driverName,
                actorRole = "DRIVER",
                action = "END_TRIP",
                targetResource = "Trip: $tripId",
                details = "Completed trip $tripId"
            )
        )
    }

    // --- Trip Events ---
    fun getEventsForTrip(tripId: String): Flow<List<TripEventEntity>> = dao.getEventsForTrip(tripId)
    fun getRecentTripEvents(): Flow<List<TripEventEntity>> = dao.getRecentTripEvents()

    // --- Notifications ---
    fun getNotificationsForUser(userId: String): Flow<List<NotificationEntity>> = dao.getNotificationsForUser(userId)
    suspend fun markNotificationAsRead(id: String) = dao.markNotificationAsRead(id)

    // --- Safety Alerts ---
    fun getAllSafetyAlerts(): Flow<List<SafetyAlertEntity>> = dao.getAllSafetyAlerts()
    fun getSafetyAlertsForVehicle(vehicleId: String): Flow<List<SafetyAlertEntity>> = dao.getSafetyAlertsForVehicle(vehicleId)
    suspend fun reportSafetyAlert(alert: SafetyAlertEntity) {
        dao.insertSafetyAlert(alert)
        dao.insertNotification(
            NotificationEntity(
                id = UUID.randomUUID().toString(),
                targetUserId = "ALL",
                title = "SAFETY ALERT: ${alert.category}",
                message = "${alert.severity}: ${alert.description}",
                type = if (alert.severity == "EMERGENCY") "ALERT" else "WARNING"
            )
        )
        dao.insertAuditLog(
            AuditLogEntity(
                id = UUID.randomUUID().toString(),
                actorId = alert.reporterId,
                actorName = alert.reporterName,
                actorRole = alert.reporterRole,
                action = "REPORT_SAFETY_ALERT",
                targetResource = "Vehicle: ${alert.vehicleId}",
                details = "Severity: ${alert.severity}, Category: ${alert.category}, Description: ${alert.description}"
            )
        )
    }

    suspend fun updateSafetyAlertStatus(alertId: String, status: String) {
        dao.updateSafetyAlertStatus(alertId, status)
    }

    // --- Audit Logs ---
    fun getAllAuditLogs(): Flow<List<AuditLogEntity>> = dao.getAllAuditLogs()
    suspend fun insertAuditLog(log: AuditLogEntity) = dao.insertAuditLog(log)

    // --- System Settings ---
    fun getAllSettings(): Flow<List<SystemSettingsEntity>> = dao.getAllSettings()
    suspend fun insertSetting(key: String, value: String) {
        dao.insertSetting(SystemSettingsEntity(key, value))
    }

    // --- Helper: Secure Bus Code Generator ---
    fun generateUniqueBusCode(): String {
        val alphabet = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ"
        val random = SecureRandom()
        val codeChars = CharArray(5) {
            alphabet[random.nextInt(alphabet.length)]
        }
        return "SR-${String(codeChars)}"
    }

    // --- Seed Initial Data ---
    private suspend fun seedInitialDataIfNeeded() {
        val existingUsers = dao.getAllUsers().first()
        if (existingUsers.isNotEmpty()) return

        // 1. Super Admin — Platform Owner: Soham Pawar
        val adminUser = UserEntity(
            id = "user_admin_soham",
            email = "soham.pawar.8765@gmail.com",
            passwordHash = "", // Unset initially: Soham sets it on first launch, then uses it to login
            role = "SUPER_ADMIN",
            fullName = "Soham Pawar",
            phone = "+91 98000 00000",
            isMfaEnabled = false
        )
        dao.insertUser(adminUser)

        // 2. Driver / Operator 1
        val driverUser1 = UserEntity(
            id = "user_driver_01",
            email = "rajesh@patiltransport.com",
            passwordHash = "driver123",
            role = "DRIVER",
            fullName = "Rajesh Patil",
            phone = "+91 94221 44552"
        )
        dao.insertUser(driverUser1)

        // 3. Driver / Operator 2
        val driverUser2 = UserEntity(
            id = "user_driver_02",
            email = "sunil@vanrides.com",
            passwordHash = "driver123",
            role = "DRIVER",
            fullName = "Sunil Shinde",
            phone = "+91 98900 88776"
        )
        dao.insertUser(driverUser2)

        // 4. Parent 1
        val parentUser1 = UserEntity(
            id = "user_parent_01",
            email = "anita@parent.com",
            passwordHash = "parent123",
            role = "PARENT",
            fullName = "Anita Sharma",
            phone = "+91 98811 22334"
        )
        dao.insertUser(parentUser1)

        // 5. Parent 2
        val parentUser2 = UserEntity(
            id = "user_parent_02",
            email = "rahul@parent.com",
            passwordHash = "parent123",
            role = "PARENT",
            fullName = "Rahul Deshmukh",
            phone = "+91 98505 12345"
        )
        dao.insertUser(parentUser2)

        // Vehicles
        val bus1 = VehicleEntity(
            id = "veh_01",
            vehicleNumber = "MH-26 AB 1234",
            vehicleType = "School Bus",
            operatorId = "user_driver_01",
            driverName = "Rajesh Patil",
            driverPhone = "+91 94221 44552",
            capacity = 32,
            busCode = "SR-7K42Q",
            status = "ACTIVE",
            schoolDestination = "Model Public School",
            currentLatitude = 19.1628,
            currentLongitude = 77.3175,
            speedKmH = 26.0f,
            isGpsActive = true,
            isSimulationMode = true
        )
        dao.insertVehicle(bus1)

        val bus2 = VehicleEntity(
            id = "veh_02",
            vehicleNumber = "MH-26 C 9812",
            vehicleType = "Van",
            operatorId = "user_driver_02",
            driverName = "Sunil Shinde",
            driverPhone = "+91 98900 88776",
            capacity = 14,
            busCode = "SR-91PX2",
            status = "ACTIVE",
            schoolDestination = "St. Xavier Academy",
            currentLatitude = 19.1550,
            currentLongitude = 77.3110,
            speedKmH = 0f,
            isGpsActive = true,
            isSimulationMode = true
        )
        dao.insertVehicle(bus2)

        // Routes
        val route1 = RouteEntity(
            id = "route_04",
            routeName = "Route 04 - East City",
            vehicleId = "veh_01",
            operatorId = "user_driver_01",
            startPoint = "Kautha Road",
            destination = "Model Public School",
            approximateSchedule = "07:30 AM - 08:35 AM",
            stopsJson = "Kautha Road,Shivaji Chowk,Anand Nagar,CIDCO Corner,Model Public School",
            isActive = true
        )
        dao.insertRoute(route1)

        val route2 = RouteEntity(
            id = "route_02",
            routeName = "Route 02 - West Suburbs",
            vehicleId = "veh_02",
            operatorId = "user_driver_02",
            startPoint = "Railway Station",
            destination = "St. Xavier Academy",
            approximateSchedule = "07:45 AM - 08:30 AM",
            stopsJson = "Railway Station,Mahaveer Chowk,Taroda Naka,St. Xavier Academy",
            isActive = true
        )
        dao.insertRoute(route2)

        // Students
        val student1 = StudentEntity(
            id = "std_01",
            fullName = "Aarav Sharma",
            parentId = "user_parent_01",
            vehicleId = "veh_01",
            routeId = "route_04",
            pickupStop = "Kautha Road",
            dropOffStop = "Kautha Road",
            pickupStatus = "WAITING",
            rollNumber = "SR-07",
            schoolName = "Model Public School"
        )
        dao.insertStudent(student1)

        val student2 = StudentEntity(
            id = "std_02",
            fullName = "Rhea Deshmukh",
            parentId = "user_parent_02",
            vehicleId = "veh_01",
            routeId = "route_04",
            pickupStop = "Shivaji Chowk",
            dropOffStop = "Shivaji Chowk",
            pickupStatus = "WAITING",
            rollNumber = "SR-14",
            schoolName = "Model Public School"
        )
        dao.insertStudent(student2)

        // Initialize active morning trip for live route monitoring
        val activeTrip = TripEntity(
            id = "trip_init_01",
            vehicleId = "veh_01",
            routeId = "route_04",
            driverId = "user_driver_01",
            tripName = "Morning Route 04",
            type = "MORNING_PICKUP",
            status = "ACTIVE",
            startTime = System.currentTimeMillis() - 15 * 60 * 1000,
            currentStopIndex = 1,
            etaMinutes = 8,
            nextStopName = "Kautha Road",
            delayMinutes = 0
        )
        dao.insertTrip(activeTrip)

        // Trip Events
        dao.insertTripEvent(
            TripEventEntity(
                id = "ev_01",
                tripId = "trip_init_01",
                eventType = "TRIP_STARTED",
                title = "Trip Started",
                description = "Bus MH-26 AB 1234 departed depot on time.",
                timestamp = System.currentTimeMillis() - 14 * 60 * 1000
            )
        )
        dao.insertTripEvent(
            TripEventEntity(
                id = "ev_02",
                tripId = "trip_init_01",
                eventType = "STOP_APPROACHING",
                title = "Approaching Kautha Road",
                description = "ETA is 8 minutes.",
                timestamp = System.currentTimeMillis() - 4 * 60 * 1000
            )
        )

        // Initial Notifications
        dao.insertNotification(
            NotificationEntity(
                id = "notif_01",
                targetUserId = "user_parent_01",
                title = "Bus Approaching Stop",
                message = "Bus MH-26 AB 1234 is 8 minutes away from Kautha Road.",
                type = "INFO"
            )
        )
        dao.insertNotification(
            NotificationEntity(
                id = "notif_02",
                targetUserId = "ALL",
                title = "Welcome to SafeRide",
                message = "SafeRide privacy-first student transportation platform is active.",
                type = "SUCCESS"
            )
        )

        // Audit Logs
        dao.insertAuditLog(
            AuditLogEntity(
                id = "log_01",
                actorId = "user_admin_01",
                actorName = "Super Admin",
                actorRole = "SUPER_ADMIN",
                action = "SYSTEM_INITIALIZE",
                targetResource = "SafeRide Core",
                details = "Platform initialized with security policies and encryption verified."
            )
        )
        dao.insertAuditLog(
            AuditLogEntity(
                id = "log_02",
                actorId = "user_driver_01",
                actorName = "Rajesh Patil",
                actorRole = "DRIVER",
                action = "START_TRIP",
                targetResource = "MH-26 AB 1234",
                details = "Morning Route 04 initiated with 2 connected students."
            )
        )

        // Safety Alerts
        dao.insertSafetyAlert(
            SafetyAlertEntity(
                id = "alert_01",
                reporterId = "user_driver_01",
                reporterName = "Rajesh Patil",
                reporterRole = "DRIVER",
                vehicleId = "veh_01",
                tripId = "trip_init_01",
                severity = "NORMAL",
                category = "Traffic Advisory",
                description = "Slight morning congestion near Anand Nagar flyover (+3 min delay estimated).",
                status = "RESOLVED"
            )
        )

        // System Settings
        dao.insertSetting(SystemSettingsEntity("max_login_attempts", "5"))
        dao.insertSetting(SystemSettingsEntity("session_timeout_minutes", "60"))
        dao.insertSetting(SystemSettingsEntity("emergency_hotline", "+91 1800 200 9999"))
        dao.insertSetting(SystemSettingsEntity("platform_status", "OPERATIONAL"))
    }
}
