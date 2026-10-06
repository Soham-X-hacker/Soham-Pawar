package com.example.security

import com.example.data.dao.SafeRideDao
import com.example.data.model.AuditLogEntity
import com.example.data.model.StudentEntity
import com.example.data.model.UserEntity
import com.example.data.model.VehicleEntity
import kotlinx.coroutines.flow.first
import java.util.UUID

enum class SafeRideRole {
    SUPER_ADMIN,
    OPERATOR,
    DRIVER,
    PARENT
}

enum class SafeRidePermission {
    READ_LINKED_BUS,
    READ_OWN_STUDENT,
    WRITE_OWN_VEHICLE,
    MANAGE_OWN_ROUTES,
    MANAGE_OWN_STUDENTS,
    START_OWN_TRIP,
    APPROVE_CONNECTION_REQUEST,
    REPORT_SAFETY_ALERT,
    ADMIN_MANAGE_USERS,
    ADMIN_MANAGE_VEHICLES,
    ADMIN_VIEW_AUDIT_LOGS,
    ADMIN_SYSTEM_SETTINGS
}

class AccessControlGateway(private val dao: SafeRideDao) {

    // Evaluate base permissions
    fun hasPermission(role: String, permission: SafeRidePermission): Boolean {
        return when (role.uppercase()) {
            "SUPER_ADMIN" -> true // Super Admin has all permissions
            "OPERATOR", "DRIVER" -> when (permission) {
                SafeRidePermission.WRITE_OWN_VEHICLE,
                SafeRidePermission.MANAGE_OWN_ROUTES,
                SafeRidePermission.MANAGE_OWN_STUDENTS,
                SafeRidePermission.START_OWN_TRIP,
                SafeRidePermission.APPROVE_CONNECTION_REQUEST,
                SafeRidePermission.REPORT_SAFETY_ALERT,
                SafeRidePermission.READ_LINKED_BUS -> true
                else -> false
            }
            "PARENT" -> when (permission) {
                SafeRidePermission.READ_LINKED_BUS,
                SafeRidePermission.READ_OWN_STUDENT,
                SafeRidePermission.REPORT_SAFETY_ALERT -> true
                else -> false
            }
            else -> false
        }
    }

    // Verify server-side parent access to vehicle data
    suspend fun validateParentVehicleAccess(parentUserId: String, vehicleId: String): Result<Unit> {
        val user = dao.getUserById(parentUserId) ?: return Result.failure(SecurityException("Unauthenticated user"))
        if (user.role == "SUPER_ADMIN") return Result.success(Unit)

        val myStudents = dao.getStudentsByParent(parentUserId).first()
        val isLinked = myStudents.any { it.vehicleId == vehicleId && it.isActive }
        return if (isLinked) {
            Result.success(Unit)
        } else {
            logViolation(parentUserId, user.fullName, user.role, "UNAUTHORIZED_VEHICLE_ACCESS", "Vehicle: $vehicleId")
            Result.failure(SecurityException("Access Denied: You are not authorized to view this vehicle's telemetry."))
        }
    }

    // Verify parent access to student record (prevents cross-student data leakage)
    suspend fun validateParentStudentAccess(parentUserId: String, studentId: String): Result<Unit> {
        val user = dao.getUserById(parentUserId) ?: return Result.failure(SecurityException("Unauthenticated user"))
        if (user.role == "SUPER_ADMIN") return Result.success(Unit)

        val myStudents = dao.getStudentsByParent(parentUserId).first()
        val hasAccess = myStudents.any { it.id == studentId }
        return if (hasAccess) {
            Result.success(Unit)
        } else {
            logViolation(parentUserId, user.fullName, user.role, "UNAUTHORIZED_STUDENT_ACCESS", "Student: $studentId")
            Result.failure(SecurityException("Access Denied: You are not authorized to view other students."))
        }
    }

    // Verify driver access to manage a vehicle
    suspend fun validateDriverVehicleOwnership(driverUserId: String, vehicleId: String): Result<Unit> {
        val user = dao.getUserById(driverUserId) ?: return Result.failure(SecurityException("Unauthenticated user"))
        if (user.role == "SUPER_ADMIN") return Result.success(Unit)

        val vehicle = dao.getVehicleById(vehicleId)
        return if (vehicle != null && (vehicle.operatorId == driverUserId || user.role == "DRIVER")) {
            Result.success(Unit)
        } else {
            logViolation(driverUserId, user.fullName, user.role, "UNAUTHORIZED_VEHICLE_MUTATION", "Vehicle: $vehicleId")
            Result.failure(SecurityException("Access Denied: You cannot modify a vehicle not registered to your organization."))
        }
    }

    // Verify admin operation
    suspend fun validateAdminAccess(userId: String): Result<Unit> {
        val user = dao.getUserById(userId) ?: return Result.failure(SecurityException("Unauthenticated user"))
        return if (user.role == "SUPER_ADMIN" && !user.isSuspended) {
            Result.success(Unit)
        } else {
            logViolation(userId, user.fullName, user.role, "ADMIN_ELEVATION_VIOLATION", "Platform Control Center")
            Result.failure(SecurityException("Access Denied: Administrative privileges required."))
        }
    }

    // Log security violation attempts into persistent audit trail
    private suspend fun logViolation(actorId: String, actorName: String, role: String, action: String, resource: String) {
        dao.insertAuditLog(
            AuditLogEntity(
                id = UUID.randomUUID().toString(),
                actorId = actorId,
                actorName = actorName,
                actorRole = role,
                action = "SECURITY_VIOLATION_$action",
                targetResource = resource,
                details = "Blocked unauthorized attempt to bypass server-side role boundaries."
            )
        )
    }
}
