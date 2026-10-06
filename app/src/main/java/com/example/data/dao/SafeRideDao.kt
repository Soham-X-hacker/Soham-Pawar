package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
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
import kotlinx.coroutines.flow.Flow

@Dao
interface SafeRideDao {

    // --- Users ---
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: String): UserEntity?

    @Query("SELECT * FROM users ORDER BY createdAt DESC")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("UPDATE users SET isSuspended = :suspended WHERE id = :userId")
    suspend fun setUserSuspended(userId: String, suspended: Boolean)

    // --- Vehicles ---
    @Query("SELECT * FROM vehicles ORDER BY vehicleNumber ASC")
    fun getAllVehicles(): Flow<List<VehicleEntity>>

    @Query("SELECT * FROM vehicles WHERE id = :vehicleId LIMIT 1")
    fun getVehicleByIdFlow(vehicleId: String): Flow<VehicleEntity?>

    @Query("SELECT * FROM vehicles WHERE id = :vehicleId LIMIT 1")
    suspend fun getVehicleById(vehicleId: String): VehicleEntity?

    @Query("SELECT * FROM vehicles WHERE busCode = :busCode LIMIT 1")
    suspend fun getVehicleByBusCode(busCode: String): VehicleEntity?

    @Query("SELECT * FROM vehicles WHERE operatorId = :operatorId")
    fun getVehiclesByOperator(operatorId: String): Flow<List<VehicleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVehicle(vehicle: VehicleEntity)

    @Update
    suspend fun updateVehicle(vehicle: VehicleEntity)

    @Query("UPDATE vehicles SET currentLatitude = :lat, currentLongitude = :lng, speedKmH = :speed, lastUpdated = :timestamp WHERE id = :vehicleId")
    suspend fun updateVehicleLocation(vehicleId: String, lat: Double, lng: Double, speed: Float, timestamp: Long)

    @Query("UPDATE vehicles SET status = :status WHERE id = :vehicleId")
    suspend fun updateVehicleStatus(vehicleId: String, status: String)

    // --- Routes ---
    @Query("SELECT * FROM routes ORDER BY routeName ASC")
    fun getAllRoutes(): Flow<List<RouteEntity>>

    @Query("SELECT * FROM routes WHERE vehicleId = :vehicleId LIMIT 1")
    fun getRouteForVehicle(vehicleId: String): Flow<RouteEntity?>

    @Query("SELECT * FROM routes WHERE operatorId = :operatorId")
    fun getRoutesByOperator(operatorId: String): Flow<List<RouteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoute(route: RouteEntity)

    // --- Students ---
    @Query("SELECT * FROM students WHERE parentId = :parentId")
    fun getStudentsByParent(parentId: String): Flow<List<StudentEntity>>

    @Query("SELECT * FROM students WHERE vehicleId = :vehicleId")
    fun getStudentsByVehicle(vehicleId: String): Flow<List<StudentEntity>>

    @Query("SELECT * FROM students ORDER BY fullName ASC")
    fun getAllStudents(): Flow<List<StudentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: StudentEntity)

    @Query("UPDATE students SET pickupStatus = :status WHERE id = :studentId")
    suspend fun updateStudentPickupStatus(studentId: String, status: String)

    @Query("DELETE FROM students WHERE id = :studentId")
    suspend fun deleteStudent(studentId: String)

    // --- Connection Requests ---
    @Query("SELECT * FROM connection_requests WHERE vehicleId = :vehicleId ORDER BY requestedAt DESC")
    fun getConnectionRequestsForVehicle(vehicleId: String): Flow<List<ConnectionRequestEntity>>

    @Query("SELECT * FROM connection_requests WHERE parentId = :parentId ORDER BY requestedAt DESC")
    fun getConnectionRequestsForParent(parentId: String): Flow<List<ConnectionRequestEntity>>

    @Query("SELECT * FROM connection_requests ORDER BY requestedAt DESC")
    fun getAllConnectionRequests(): Flow<List<ConnectionRequestEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConnectionRequest(request: ConnectionRequestEntity)

    @Query("UPDATE connection_requests SET status = :status WHERE id = :requestId")
    suspend fun updateConnectionRequestStatus(requestId: String, status: String)

    @Query("SELECT * FROM connection_requests WHERE id = :requestId LIMIT 1")
    suspend fun getConnectionRequestById(requestId: String): ConnectionRequestEntity?

    // --- Trips ---
    @Query("SELECT * FROM trips WHERE vehicleId = :vehicleId AND status = 'ACTIVE' LIMIT 1")
    fun getActiveTripForVehicle(vehicleId: String): Flow<TripEntity?>

    @Query("SELECT * FROM trips WHERE driverId = :driverId ORDER BY startTime DESC")
    fun getTripsForDriver(driverId: String): Flow<List<TripEntity>>

    @Query("SELECT * FROM trips ORDER BY startTime DESC")
    fun getAllTrips(): Flow<List<TripEntity>>

    @Query("SELECT * FROM trips WHERE id = :tripId LIMIT 1")
    suspend fun getTripById(tripId: String): TripEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrip(trip: TripEntity)

    @Update
    suspend fun updateTrip(trip: TripEntity)

    // --- Trip Events ---
    @Query("SELECT * FROM trip_events WHERE tripId = :tripId ORDER BY timestamp DESC")
    fun getEventsForTrip(tripId: String): Flow<List<TripEventEntity>>

    @Query("SELECT * FROM trip_events ORDER BY timestamp DESC LIMIT 50")
    fun getRecentTripEvents(): Flow<List<TripEventEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTripEvent(event: TripEventEntity)

    // --- Notifications ---
    @Query("SELECT * FROM notifications WHERE targetUserId = :userId OR targetUserId = 'ALL' ORDER BY timestamp DESC")
    fun getNotificationsForUser(userId: String): Flow<List<NotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity)

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :notificationId")
    suspend fun markNotificationAsRead(notificationId: String)

    // --- Safety Alerts ---
    @Query("SELECT * FROM safety_alerts ORDER BY timestamp DESC")
    fun getAllSafetyAlerts(): Flow<List<SafetyAlertEntity>>

    @Query("SELECT * FROM safety_alerts WHERE vehicleId = :vehicleId ORDER BY timestamp DESC")
    fun getSafetyAlertsForVehicle(vehicleId: String): Flow<List<SafetyAlertEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSafetyAlert(alert: SafetyAlertEntity)

    @Query("UPDATE safety_alerts SET status = :status WHERE id = :alertId")
    suspend fun updateSafetyAlertStatus(alertId: String, status: String)

    // --- Audit Logs ---
    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC")
    fun getAllAuditLogs(): Flow<List<AuditLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditLog(log: AuditLogEntity)

    // --- System Settings ---
    @Query("SELECT * FROM system_settings")
    fun getAllSettings(): Flow<List<SystemSettingsEntity>>

    @Query("SELECT value FROM system_settings WHERE key = :key LIMIT 1")
    suspend fun getSetting(key: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSetting(setting: SystemSettingsEntity)
}
