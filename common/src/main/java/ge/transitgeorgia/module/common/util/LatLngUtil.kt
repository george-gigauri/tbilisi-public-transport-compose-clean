package ge.transitgeorgia.module.common.util

import android.location.Location
import android.location.LocationManager
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

object LatLngUtil {

    fun calculateBearing(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Float {
        val l1 = Location(LocationManager.NETWORK_PROVIDER)
        val l2 = Location(LocationManager.NETWORK_PROVIDER)

        l1.latitude = lat1
        l1.longitude = lon1

        l2.latitude = lat2
        l2.longitude = lon2

        return l1.bearingTo(l2)
    }
}