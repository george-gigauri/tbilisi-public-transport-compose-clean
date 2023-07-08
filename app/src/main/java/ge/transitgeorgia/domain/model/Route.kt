package ge.transitgeorgia.domain.model

import ge.transitgeorgia.common.other.enums.TransportType

data class Route(
    val id: String,
    val color: String,
    val number: String,
    val type: TransportType,
    var longName: String,
    var firstStation: String,
    var lastStation: String
) {

    val isMetro: Boolean get() = type == TransportType.METRO

    companion object {
        fun empty(): Route {
            return Route(
                "-",
                "#000000",
                "351",
                TransportType.BUS,
                "ვაკე-ბაგები - გლდანულა-გლდანი",
                "---",
                "---"
            )
        }
    }
}