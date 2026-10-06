package com.example.ui.viewmodels

import android.app.Activity
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.SafeRideDatabase
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
import com.example.data.repository.SafeRideRepository
import com.example.gps.GpsTracker
import com.example.gps.LiveVehicleLocation
import com.example.localization.AppLanguage
import com.example.localization.LocalizationManager
import com.example.localization.StringsDefinition
import com.example.security.AuthManager
import com.example.security.AuthState
import com.example.security.FirebaseAuthService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val database = SafeRideDatabase.getDatabase(application)
    val repository = SafeRideRepository(database.safeRideDao())
    val firebaseAuthService = FirebaseAuthService(application)
    val authManager = AuthManager(repository, firebaseAuthService)
    val gpsTracker = GpsTracker(application, repository, viewModelScope)

    // Language State
    private val _currentLanguage = MutableStateFlow(AppLanguage.ENGLISH)
    val currentLanguage: StateFlow<AppLanguage> = _currentLanguage.asStateFlow()

    val strings: StateFlow<StringsDefinition> = combine(_currentLanguage) { langArray ->
        LocalizationManager.getStrings(langArray[0])
    }.stateIn(viewModelScope, SharingStarted.Eagerly, LocalizationManager.getStrings(AppLanguage.ENGLISH))

    fun setLanguage(lang: AppLanguage) {
        _currentLanguage.value = lang
    }

    // Auth State
    val authState: StateFlow<AuthState> = authManager.authState

    // Live GPS Telemetry
    val liveLocation: StateFlow<LiveVehicleLocation?> = gpsTracker.liveLocation
    val isSimulationMode: StateFlow<Boolean> = gpsTracker.isSimulationMode

    fun toggleSimulationMode(enabled: Boolean, vehicleId: String = "veh_01") {
        gpsTracker.setSimulationMode(enabled, vehicleId)
    }

    // Database Reactive Flows
    val allVehicles: StateFlow<List<VehicleEntity>> = repository.getAllVehicles()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allRoutes: StateFlow<List<RouteEntity>> = repository.getAllRoutes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allStudents: StateFlow<List<StudentEntity>> = repository.getAllStudents()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTrips: StateFlow<List<TripEntity>> = repository.getAllTrips()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allConnectionRequests: StateFlow<List<ConnectionRequestEntity>> = repository.getAllConnectionRequests()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allUsers: StateFlow<List<UserEntity>> = repository.getAllUsers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allSafetyAlerts: StateFlow<List<SafetyAlertEntity>> = repository.getAllSafetyAlerts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAuditLogs: StateFlow<List<AuditLogEntity>> = repository.getAllAuditLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allSettings: StateFlow<List<SystemSettingsEntity>> = repository.getAllSettings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val isAdminPasswordConfigured: StateFlow<Boolean> = repository.isAdminPasswordConfiguredFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val recentTripEvents: StateFlow<List<TripEventEntity>> = repository.getRecentTripEvents()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI Feedback Message
    private val _uiMessage = MutableStateFlow<String?>(null)
    val uiMessage: StateFlow<String?> = _uiMessage.asStateFlow()

    fun clearUiMessage() {
        _uiMessage.value = null
    }

    // --- Authentication Actions ---
    fun login(email: String, pass: String) {
        viewModelScope.launch {
            authManager.login(email, pass).onFailure { err ->
                _uiMessage.value = err.message ?: "Login failed"
            }
        }
    }

    fun loginWithGoogle(activity: Activity, selectedRole: String = "PARENT") {
        viewModelScope.launch {
            authManager.loginWithGoogle(activity, selectedRole).onFailure { err ->
                _uiMessage.value = err.message ?: "Google Sign-In failed"
            }
        }
    }

    fun verifyMfa(code: String) {
        viewModelScope.launch {
            val success = authManager.verifyMfa(code)
            if (!success) {
                _uiMessage.value = "Invalid MFA verification code"
            }
        }
    }

    fun register(fullName: String, email: String, pass: String, role: String, phone: String) {
        viewModelScope.launch {
            authManager.registerUser(fullName, email, pass, role, phone).onFailure { err ->
                _uiMessage.value = err.message ?: "Registration failed"
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authManager.logout()
        }
    }

    // --- Parent Actions ---
    fun submitBusConnectionRequest(
        busCode: String,
        studentName: String,
        pickupStop: String,
        dropOffStop: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val codeClean = busCode.trim().uppercase()
            val vehicle = repository.getVehicleByBusCode(codeClean)
            if (vehicle == null) {
                _uiMessage.value = "Invalid Bus Code. Please check with your driver or school."
                return@launch
            }

            val currentAuth = authState.value
            val parentUser = if (currentAuth is AuthState.Authenticated) currentAuth.user else null
            val parentId = parentUser?.id ?: "user_parent_01"
            val parentName = parentUser?.fullName ?: "Parent"

            val req = ConnectionRequestEntity(
                id = "req_${UUID.randomUUID().toString().take(8)}",
                busCode = codeClean,
                parentId = parentId,
                parentName = parentName,
                studentName = studentName,
                vehicleId = vehicle.id,
                pickupStop = pickupStop,
                dropOffStop = dropOffStop,
                status = "PENDING"
            )
            repository.insertConnectionRequest(req)
            _uiMessage.value = "Request submitted to driver of ${vehicle.vehicleNumber}. Awaiting approval."
            onSuccess()
        }
    }

    // --- Driver Actions ---
    fun approveConnectionRequest(requestId: String, driverName: String) {
        viewModelScope.launch {
            repository.approveConnectionRequest(requestId, driverName)
            _uiMessage.value = "Parent request approved. Student added to vehicle roster."
        }
    }

    fun rejectConnectionRequest(requestId: String, driverName: String) {
        viewModelScope.launch {
            repository.rejectConnectionRequest(requestId, driverName)
            _uiMessage.value = "Connection request declined."
        }
    }

    fun startTrip(vehicleId: String, routeId: String, driverId: String, driverName: String) {
        viewModelScope.launch {
            repository.startTrip(vehicleId, routeId, driverId, driverName)
            _uiMessage.value = "Trip started! Live GPS notifications broadcasted."
        }
    }

    fun markStudentPickup(tripId: String, studentId: String, studentName: String, parentId: String) {
        viewModelScope.launch {
            repository.markStudentPickedUp(tripId, studentId, studentName, parentId)
            _uiMessage.value = "$studentName marked as picked up. Parent notified."
        }
    }

    fun markSchoolArrival(tripId: String, vehicleId: String) {
        viewModelScope.launch {
            repository.markSchoolArrival(tripId, vehicleId)
            _uiMessage.value = "School arrival recorded. Parents notified."
        }
    }

    fun markStudentDropOff(tripId: String, studentId: String, studentName: String, parentId: String) {
        viewModelScope.launch {
            repository.markStudentDroppedOff(tripId, studentId, studentName, parentId)
            _uiMessage.value = "$studentName dropped off safely."
        }
    }

    fun endTrip(tripId: String, driverName: String) {
        viewModelScope.launch {
            repository.endTrip(tripId, driverName)
            _uiMessage.value = "Trip ended successfully."
        }
    }

    fun addVehicle(
        vehicleNumber: String,
        vehicleType: String,
        driverName: String,
        driverPhone: String,
        capacity: Int,
        schoolName: String,
        operatorId: String
    ) {
        viewModelScope.launch {
            val newCode = repository.generateUniqueBusCode()
            val vehicle = VehicleEntity(
                id = "veh_${UUID.randomUUID().toString().take(6)}",
                vehicleNumber = vehicleNumber.trim().uppercase(),
                vehicleType = vehicleType,
                operatorId = operatorId,
                driverName = driverName,
                driverPhone = driverPhone,
                capacity = capacity,
                busCode = newCode,
                schoolDestination = schoolName,
                status = "ACTIVE"
            )
            repository.insertVehicle(vehicle)
            _uiMessage.value = "Vehicle added! Assigned Bus Code: $newCode"
        }
    }

    fun regenerateBusCode(vehicleId: String) {
        viewModelScope.launch {
            val vehicle = repository.getVehicleById(vehicleId) ?: return@launch
            val newCode = repository.generateUniqueBusCode()
            repository.updateVehicle(vehicle.copy(busCode = newCode))
            _uiMessage.value = "Bus Code updated to $newCode"
        }
    }

    // --- Safety & Emergency Reporting ---
    fun reportSafetyAlert(
        vehicleId: String,
        severity: String,
        category: String,
        description: String,
        reporterId: String,
        reporterName: String,
        reporterRole: String
    ) {
        viewModelScope.launch {
            val alert = SafetyAlertEntity(
                id = "alert_${UUID.randomUUID().toString().take(8)}",
                reporterId = reporterId,
                reporterName = reporterName,
                reporterRole = reporterRole,
                vehicleId = vehicleId,
                severity = severity,
                category = category,
                description = description,
                status = "OPEN"
            )
            repository.reportSafetyAlert(alert)
            _uiMessage.value = if (severity == "EMERGENCY") "EMERGENCY BROADCAST TRANSMITTED" else "Safety alert logged."
        }
    }

    fun resolveSafetyAlert(alertId: String) {
        viewModelScope.launch {
            repository.updateSafetyAlertStatus(alertId, "RESOLVED")
            _uiMessage.value = "Alert marked as RESOLVED."
        }
    }

    // --- Admin Operations ---
    fun toggleUserSuspension(userId: String, currentSuspended: Boolean) {
        viewModelScope.launch {
            repository.setUserSuspended(userId, !currentSuspended)
            _uiMessage.value = if (!currentSuspended) "Account suspended" else "Account reactivated"
        }
    }

    fun toggleVehicleStatus(vehicleId: String, currentStatus: String) {
        viewModelScope.launch {
            val newStatus = if (currentStatus == "ACTIVE") "SUSPENDED" else "ACTIVE"
            repository.updateVehicleStatus(vehicleId, newStatus)
            _uiMessage.value = "Vehicle status updated to $newStatus"
        }
    }

    fun updateAdminProfile(fullName: String, phone: String) {
        viewModelScope.launch {
            repository.updateAdminProfile(fullName, phone)
            _uiMessage.value = "Admin profile saved successfully."
        }
    }

    fun updateSystemSetting(key: String, value: String) {
        viewModelScope.launch {
            repository.insertSetting(key, value)
            _uiMessage.value = "System setting updated."
        }
    }
}
