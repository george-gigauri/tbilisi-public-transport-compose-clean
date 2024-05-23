package ge.transitgeorgia.module.domain.model

import ge.transitgeorgia.module.common.other.enums.TransportType

enum class RouteTransportType {
    ALL,
    METRO,
    BUS,
    MICRO_BUS;

    companion object {
        fun fromTransportType(t: TransportType): RouteTransportType {
            return when (t) {
                TransportType.BUS -> BUS
                TransportType.METRO -> METRO
            }
        }

        fun fromV2(t: String): RouteTransportType {
            return when (t) {
                "SUBWAY" -> METRO
                "BUS" -> BUS
                "MINIBUS" -> MICRO_BUS
                else -> ALL
            }
        }
    }
}