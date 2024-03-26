package ge.transitgeorgia.module.common.util

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

object LatLngUtil {

    fun calculateBearing(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Double {
        val deltaLon = lon2 - lon1

        val y = sin(deltaLon) * cos(lat2)
        val x = cos(lat1) * sin(lat2) - sin(lat1) * cos(lat2) * cos(deltaLon)

        var bearing = atan2(y, x)
        bearing = Math.toDegrees(bearing)
        bearing = (bearing + 360) % 360

        return bearing
    }
}