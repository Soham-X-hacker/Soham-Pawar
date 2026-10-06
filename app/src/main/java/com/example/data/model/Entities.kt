package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val email: String,
    val passwordHash: String,
    val role: String, // SUPER_ADMIN, DRIVER, PARENT
    val fullName: String,
    val phone: String,
    val isMfaEnabled: Boolean = false,
    val isSuspended: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "vehicles")
data class VehicleEntity(
    @PrimaryKey val id: String,
    val vehicleNumber: String,
    val vehicleType: String, // School Bus, Mini Bus, Van, Auto, Car
    val operatorId: String,
    val driverName: String,
    val driverPhone: String,
    val capacity: Int,
    val busCode: String,
    val status: String = "ACTIVE", // ACTIVE, MAINTENANCE, SUSPENDED
    val schoolDestination: String = "Model Public School",
    val currentLatitude: Double = 19.1628,
    val currentLongitude: Double = 77.3175,
    val speedKmH: Float = 28.5f,
    val isGpsActive: Boolean = true,
    val isSimulationMode: Boolean = true,
    val lastUpdated: Long = System.currentTimeMillis()
)

@Entity(tableName = "routes")
data class RouteEntity(
    @PrimaryKey val id: String,
    val routeName: String,
    val vehicleId: String,
    val operatorId: String,
    val startPoint: String,
    val destination: String,
    val approximateSchedule: String,
    val stopsJson: String, // comma-separated or serialized stops
    val isActive: Boolean = true
)

@Entity(tableName = "students")
data class StudentEntity(
    @PrimaryKey val id: String,
    val fullName: String,
    val parentId: String,
    val vehicleId: String,
    val routeId: String,
    val pickupStop: String,
    val dropOffStop: String,
    val pickupStatus: String = "WAITING", // WAITING, PICKED_UP, DROPPED_OFF
    val rollNumber: String = "",
    val schoolName: String = "Model Public School",
    val isActive: Boolean = true
)

@Entity(tableName = "connection_requests")
data class ConnectionRequestEntity(
    @PrimaryKey val id: String,
    val busCode: String,
    val parentId: String,
    val parentName: String,
    val studentName: String,
    val vehicleId: String,
    val pickupStop: String,
    val dropOffStop: String,
    val status: String = "PENDING", // PENDING, APPROVED, REJECTED
    val requestedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "trips")
data class TripEntity(
    @PrimaryKey val id: String,
    val vehicleId: String,
    val routeId: String,
    val driverId: String,
    val tripName: String,
    val type: String, // MORNING_PICKUP, AFTERNOON_DROP
    val status: String, // SCHEDULED, ACTIVE, PAUSED, COMPLETED
    val startTime: Long,
    val endTime: Long? = null,
    val currentStopIndex: Int = 0,
    val etaMinutes: Int = 8,
    val nextStopName: String = "Kautha Road",
    val delayMinutes: Int = 0
)

@Entity(tableName = "trip_events")
data class TripEventEntity(
    @PrimaryKey val id: String,
    val tripId: String,
    val eventType: String, // TRIP_STARTED, STOP_APPROACHING, STOP_REACHED, STUDENT_PICKED_UP, SCHOOL_ARRIVAL, STUDENT_DROPPED_OFF, DELAY, TRIP_COMPLETED
    val title: String,
    val description: String,
    val timestamp: Long = System.currentTimeMillis(),
    val studentId: String? = null
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey val id: String,
    val targetUserId: String, // User ID or "ALL" or "PARENT" or "DRIVER"
    val title: String,
    val message: String,
    val type: String = "INFO", // INFO, WARNING, ALERT, SUCCESS
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)

@Entity(tableName = "safety_alerts")
data class SafetyAlertEntity(
    @PrimaryKey val id: String,
    val reporterId: String,
    val reporterName: String,
    val reporterRole: String,
    val vehicleId: String,
    val tripId: String? = null,
    val severity: String, // NORMAL, DELAYED, WARNING, EMERGENCY
    val category: String,
    val description: String,
    val status: String = "OPEN", // OPEN, INVESTIGATING, RESOLVED
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "audit_logs")
data class AuditLogEntity(
    @PrimaryKey val id: String,
    val actorId: String,
    val actorName: String,
    val actorRole: String,
    val action: String,
    val targetResource: String,
    val details: String,
    val timestamp: Long = System.currentTimeMillis(),
    val ipAddress: String = "127.0.0.1"
)

@Entity(tableName = "system_settings")
data class SystemSettingsEntity(
    @PrimaryKey val key: String,
    val value: String
)
