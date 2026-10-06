package com.example.gps

data class GpsWaypoint(
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val isStop: Boolean = false,
    val stopOrder: Int = -1
)

data class LiveVehicleLocation(
    val vehicleId: String,
    val latitude: Double,
    val longitude: Double,
    val speedKmH: Float,
    val headingDegrees: Float,
    val timestamp: Long,
    val isSimulated: Boolean,
    val isOnline: Boolean,
    val accuracyMeters: Float = 5.0f,
    val nextStopName: String = "",
    val etaMinutes: Int = 0,
    val distanceRemainingKm: Float = 0f
)

object RouteCoordinates {
    // Realistic Route 04 coordinates (Kautha Road to Model Public School)
    val ROUTE_04_WAYPOINTS = listOf(
        GpsWaypoint("Depot", 19.1680, 77.3050, false),
        GpsWaypoint("Kautha Road Stop", 19.1628, 77.3175, true, 1),
        GpsWaypoint("Midway Sector 2", 19.1600, 77.3220, false),
        GpsWaypoint("Shivaji Chowk Stop", 19.1555, 77.3265, true, 2),
        GpsWaypoint("Commercial Avenue", 19.1520, 77.3290, false),
        GpsWaypoint("Anand Nagar Stop", 19.1480, 77.3325, true, 3),
        GpsWaypoint("CIDCO Link Road", 19.1440, 77.3360, false),
        GpsWaypoint("CIDCO Corner Stop", 19.1400, 77.3400, true, 4),
        GpsWaypoint("Campus Gate Approach", 19.1360, 77.3435, false),
        GpsWaypoint("Model Public School", 19.1320, 77.3465, true, 5)
    )

    // Route 02 coordinates (Railway Station to St. Xavier Academy)
    val ROUTE_02_WAYPOINTS = listOf(
        GpsWaypoint("Railway Station", 19.1550, 77.3110, true, 1),
        GpsWaypoint("Mahaveer Chowk", 19.1500, 77.3150, true, 2),
        GpsWaypoint("Taroda Naka", 19.1450, 77.3190, true, 3),
        GpsWaypoint("St. Xavier Academy", 19.1400, 77.3230, true, 4)
    )
}
