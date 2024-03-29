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
        val latitude1 = Math.toRadians(lat1)
        val latitude2 = Math.toRadians(lat2)
        val longDiff = Math.toRadians(lon2 - lon1)
        val y = sin(longDiff) * cos(latitude2)
        val x = cos(latitude1) * sin(latitude2) - sin(latitude1) * cos(latitude2) * cos(longDiff)

        return (Math.toDegrees(atan2(y, x)) + 360) % 360
    }
}