package ge.transitgeorgia.module.domain.model

import ge.transitgeorgia.domain.model.RouteStop
import ge.transitgeorgia.module.common.model.LatLngPoint

data class RouteInfo(
    val id: String,
    val color: String,
    val number: String,
    val longName: String,
    val forwardHeadSign: String?,
    val backwardHeadSign: String?,
    val polyline: List<LatLngPoint>,
    val polylineHash: String?,
    val stops: List<RouteStop>,
    val isBus: Boolean,
    val isMetro: Boolean,
    val isMicroBus: Boolean,
    val isCircular: Boolean
) {

    companion object {
        fun empty(): RouteInfo {
            return RouteInfo(
                "",
                "#ffffff",
                "", "",
                null, null,
                emptyList(),
                null,
                emptyList(),
                true,
                false,
                false,
                false
            )
        }
    }
}