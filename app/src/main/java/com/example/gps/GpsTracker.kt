package com.example.gps

import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import com.example.data.repository.SafeRideRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

class GpsTracker(
    private val context: Context,
    private val repository: SafeRideRepository,
    private val coroutineScope: CoroutineScope
) {

    private val _isSimulationMode = MutableStateFlow(true)
    val isSimulationMode: StateFlow<Boolean> = _isSimulationMode.asStateFlow()

    private val _liveLocation = MutableStateFlow<LiveVehicleLocation?>(null)
    val liveLocation: StateFlow<LiveVehicleLocation?> = _liveLocation.asStateFlow()

    private var simulationJob: Job? = null
    private var locationManager: LocationManager? = null
    private var realGpsListener: LocationListener? = null

    init {
        // Start simulation by default as per Development Mode requirement
        startSimulation("veh_01")
    }

    fun setSimulationMode(enabled: Boolean, vehicleId: String = "veh_01") {
        _isSimulationMode.value = enabled
        if (enabled) {
            stopRealGps()
            startSimulation(vehicleId)
        } else {
            stopSimulation()
            startRealGps(vehicleId)
        }
    }

    fun startSimulation(vehicleId: String = "veh_01") {
        simulationJob?.cancel()
        val waypoints = RouteCoordinates.ROUTE_04_WAYPOINTS
        simulationJob = coroutineScope.launch(Dispatchers.Default) {
            var stepIndex = 1
            var progress = 0.0f // 0.0 to 1.0 between waypoints

            while (isActive) {
                val from = waypoints[stepIndex]
                val to = waypoints[(stepIndex + 1) % waypoints.size]

                // Interpolate
                val currentLat = from.latitude + (to.latitude - from.latitude) * progress
                val currentLng = from.longitude + (to.longitude - from.longitude) * progress

                // Calculate heading
                val heading = calculateBearing(from.latitude, from.longitude, to.latitude, to.longitude)
                val speed = if (from.isStop && progress < 0.2f) 0f else (22f + (stepIndex * 3) % 15)

                // Find next stop & calculate ETA
                val nextStop = waypoints.drop(stepIndex + 1).firstOrNull { it.isStop } ?: waypoints.first { it.isStop }
                val distanceKm = calculateDistanceKm(currentLat, currentLng, nextStop.latitude, nextStop.longitude)
                val etaMin = if (speed > 5f) ((distanceKm / (speed / 60f))).roundToInt().coerceAtLeast(1) else 8

                val loc = LiveVehicleLocation(
                    vehicleId = vehicleId,
                    latitude = currentLat,
                    longitude = currentLng,
                    speedKmH = speed,
                    headingDegrees = heading,
                    timestamp = System.currentTimeMillis(),
                    isSimulated = true,
                    isOnline = true,
                    accuracyMeters = 4.2f,
                    nextStopName = nextStop.name,
                    etaMinutes = etaMin,
                    distanceRemainingKm = distanceKm.toFloat()
                )
                _liveLocation.value = loc

                // Persist update into repository
                repository.updateVehicleLocation(vehicleId, currentLat, currentLng, speed)

                progress += 0.08f
                if (progress >= 1.0f) {
                    progress = 0.0f
                    stepIndex = (stepIndex + 1) % (waypoints.size - 1)
                }

                delay(2000) // Update every 2 seconds
            }
        }
    }

    fun stopSimulation() {
        simulationJob?.cancel()
        simulationJob = null
    }

    private fun startRealGps(vehicleId: String) {
        try {
            locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            realGpsListener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    val loc = LiveVehicleLocation(
                        vehicleId = vehicleId,
                        latitude = location.latitude,
                        longitude = location.longitude,
                        speedKmH = (location.speed * 3.6f),
                        headingDegrees = location.bearing,
                        timestamp = location.time,
                        isSimulated = false,
                        isOnline = true,
                        accuracyMeters = location.accuracy,
                        nextStopName = "In Transit",
                        etaMinutes = 6,
                        distanceRemainingKm = 1.2f
                    )
                    _liveLocation.value = loc
                    coroutineScope.launch(Dispatchers.IO) {
                        repository.updateVehicleLocation(vehicleId, location.latitude, location.longitude, loc.speedKmH)
                    }
                }

                @Deprecated("Deprecated in Java")
                override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                override fun onProviderEnabled(provider: String) {}
                override fun onProviderDisabled(provider: String) {}
            }

            // Register GPS / Network updates if allowed
            val hasGps = locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) == true
            val provider = if (hasGps) LocationManager.GPS_PROVIDER else LocationManager.NETWORK_PROVIDER
            locationManager?.requestLocationUpdates(provider, 2000L, 5f, realGpsListener!!)
        } catch (e: SecurityException) {
            // Permission missing; fallback to simulation
            _isSimulationMode.value = true
            startSimulation(vehicleId)
        } catch (e: Exception) {
            _isSimulationMode.value = true
            startSimulation(vehicleId)
        }
    }

    private fun stopRealGps() {
        realGpsListener?.let {
            try {
                locationManager?.removeUpdates(it)
            } catch (_: Exception) {}
        }
        realGpsListener = null
    }

    companion object {
        fun calculateDistanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
            val r = 6371.0 // Radius of the earth in km
            val dLat = Math.toRadians(lat2 - lat1)
            val dLon = Math.toRadians(lon2 - lon1)
            val a = sin(dLat / 2) * sin(dLat / 2) +
                    cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                    sin(dLon / 2) * sin(dLon / 2)
            val c = 2 * atan2(sqrt(a), sqrt(1 - a))
            return r * c
        }

        fun calculateBearing(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Float {
            val dLon = Math.toRadians(lon2 - lon1)
            val y = sin(dLon) * cos(Math.toRadians(lat2))
            val x = cos(Math.toRadians(lat1)) * sin(Math.toRadians(lat2)) -
                    sin(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * cos(dLon)
            val brng = Math.toDegrees(atan2(y, x))
            return ((brng + 360) % 360).toFloat()
        }

        fun isVehicleOnline(lastUpdatedTimestamp: Long): Boolean {
            return (System.currentTimeMillis() - lastUpdatedTimestamp) < 60_000 // Online within 60s
        }
    }
}
