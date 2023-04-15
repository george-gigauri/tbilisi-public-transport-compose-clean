package ge.transitgeorgia.domain.model

data class RouteStop(
    val id: String,
    val name: String,
    val isForward: Boolean,
    val lat: Double,
    val lng: Double
)
