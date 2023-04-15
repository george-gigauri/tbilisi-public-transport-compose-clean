package ge.transitgeorgia.domain.model

import com.mapbox.mapboxsdk.geometry.LatLng

data class RouteInfo(
    val color: String,
    val number: Int,
    val longName: String,
    val polyline: List<LatLng>,
    val stops: List<RouteStop>
) {

    companion object {
        fun empty(): RouteInfo {
            return RouteInfo("#ffffff", -1, "", emptyList(), emptyList())
        }
    }
}