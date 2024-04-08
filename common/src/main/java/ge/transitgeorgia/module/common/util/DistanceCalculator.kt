package ge.transitgeorgia.module.common.util

import org.osmdroid.util.GeoPoint

object DistanceCalculator {

    fun getNearestLatLng(myLocation: GeoPoint, list: List<GeoPoint>): DistanceLatLng? {
        return list.minByOrNull { it.distanceToAsDouble(myLocation) }?.let {
            DistanceLatLng(it.distanceToAsDouble(myLocation), it.latitude, it.longitude)
        }
    }

    fun sortByNearest(myLocation: GeoPoint, list: List<GeoPoint>): List<DistanceLatLng> {
        return list.sortedBy { it.distanceToAsDouble(myLocation) }.map {
            DistanceLatLng(it.distanceToAsDouble(myLocation), it.latitude, it.longitude)
        }
    }


    data class DistanceLatLng(
        val distance: Double,
        val latitude: Double,
        val longitude: Double
    )
}