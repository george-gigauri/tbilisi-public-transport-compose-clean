package ge.tbilisipublictransport.domain.model

data class RouteInfo(
    val color: String,
    val number: Int,
    val longName: String,
    val polyline: List<Double>,
    val stops: List<RouteStop>
)
