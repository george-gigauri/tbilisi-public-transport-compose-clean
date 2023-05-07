package ge.transitgeorgia.domain.model

import com.google.maps.android.PolyUtil
import com.mapbox.mapboxsdk.geometry.LatLng

data class RouteInfo(
    val color: String,
    val number: Int,
    val longName: String,
    val polyline: List<LatLng>,
    val stops: List<RouteStop>
) {

    fun polylineContains(latLng: LatLng): Boolean {
        return PolyUtil.isLocationOnEdge(
            com.google.android.gms.maps.model.LatLng(latLng.latitude, latLng.longitude),
            polyline.map { com.google.android.gms.maps.model.LatLng(it.latitude, it.longitude) },
            true
        )
    }

    fun polylineContains(lat: Double, lng: Double): Boolean {
        return polylineContains(LatLng(lat, lng))
    }

    companion object {
        fun empty(): RouteInfo {
            return RouteInfo("#ffffff", -1, "", emptyList(), emptyList())
        }
    }
}