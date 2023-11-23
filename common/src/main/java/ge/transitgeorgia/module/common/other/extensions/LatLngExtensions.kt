package ge.transitgeorgia.common.other.extensions

import com.mapbox.mapboxsdk.geometry.LatLng

fun List<LatLng>.calculateTotalLengthInMeters(): Double {
    var result = 0.0

    forEachIndexed { index, latLng ->
        if (index < lastIndex - 1) {
            result += latLng.distanceTo(get(index + 1))
        }

        if (index == lastIndex) {
            result += get(lastIndex - 1).distanceTo(latLng)
        }
    }

    return result
}