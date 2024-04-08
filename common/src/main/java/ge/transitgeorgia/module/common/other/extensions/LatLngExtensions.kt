package ge.transitgeorgia.module.common.other.extensions

import org.osmdroid.util.GeoPoint

fun List<GeoPoint>.calculateTotalLengthInMeters(): Double {
    var result = 0.0

    forEachIndexed { index, latLng ->
        if (index < lastIndex - 1) {
            result += latLng.distanceToAsDouble(get(index + 1))
        }

        if (index == lastIndex) {
            result += get(lastIndex - 1).distanceToAsDouble(latLng)
        }
    }

    return result
}