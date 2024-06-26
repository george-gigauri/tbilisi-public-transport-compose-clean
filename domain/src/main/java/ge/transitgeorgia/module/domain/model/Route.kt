package ge.transitgeorgia.module.domain.model

data class Route(
    val id: String,
    val color: String,
    val number: String,
    val type: RouteTransportType,
    var longName: String,
    var firstStation: String,
    var lastStation: String
) {

    val isMetro: Boolean get() = type == RouteTransportType.METRO

    companion object {
        fun empty(): Route {
            return Route(
                "-",
                "#000000",
                "---",
                RouteTransportType.BUS,
                "---",
                "---",
                "---",
            )
        }
    }
}