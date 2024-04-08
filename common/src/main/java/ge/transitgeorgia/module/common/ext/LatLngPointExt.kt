package ge.transitgeorgia.module.common.ext

import ge.transitgeorgia.module.common.model.LatLngPoint
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Polyline

fun List<LatLngPoint>.calculateTotalLengthInMeters(): Double {
    var result = 0.0
    return map { GeoPoint(it.latitude, it.longitude) }.let {
        for (i in 0..it.size - 2) {
            result += it[i].distanceToAsDouble(it[i + 1])
        }
        result
    }
}