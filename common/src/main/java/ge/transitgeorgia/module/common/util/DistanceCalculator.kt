package ge.transitgeorgia.common.util

import com.mapbox.mapboxsdk.geometry.LatLng

object DistanceCalculator {

    fun getNearestLatLng(myLocation: LatLng, list: List<LatLng>): DistanceLatLng? {
        return list.minByOrNull { it.distanceTo(myLocation) }?.let {
            DistanceLatLng(it.distanceTo(myLocation), it)
        }
    }

    fun sortByNearest(myLocation: LatLng, list: List<LatLng>): List<DistanceLatLng> {
        return list.sortedBy { it.distanceTo(myLocation) }.map {
            DistanceLatLng(it.distanceTo(myLocation), it)
        }
    }


    data class DistanceLatLng(
        val distance: Double,
        val latLng: LatLng
    )
}