package ge.transitgeorgia.module.domain.model

import ge.transitgeorgia.domain.model.RouteStop
import ge.transitgeorgia.module.common.model.LatLngPoint

data class RouteInfo(
    val color: String,
    val number: Int,
    val longName: String,
    val polyline: List<LatLngPoint>,
    val stops: List<RouteStop>,
    val isBus: Boolean,
    val isMetro: Boolean,
    val isMicroBus: Boolean
) {

    companion object {
        fun empty(): RouteInfo {
            return RouteInfo(
                "#ffffff",
                -1, "",
                emptyList(),
                emptyList(),
                true,
                false,
                false
            )
        }
    }
}