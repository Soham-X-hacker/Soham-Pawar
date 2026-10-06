package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
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

@Database(
    entities = [
        UserEntity::class,
        VehicleEntity::class,
        RouteEntity::class,
        StudentEntity::class,
        ConnectionRequestEntity::class,
        TripEntity::class,
        TripEventEntity::class,
        NotificationEntity::class,
        SafetyAlertEntity::class,
        AuditLogEntity::class,
        SystemSettingsEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class SafeRideDatabase : RoomDatabase() {

    abstract fun safeRideDao(): SafeRideDao

    companion object {
        @Volatile
        private var INSTANCE: SafeRideDatabase? = null

        fun getDatabase(context: Context): SafeRideDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SafeRideDatabase::class.java,
                    "saferide_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
