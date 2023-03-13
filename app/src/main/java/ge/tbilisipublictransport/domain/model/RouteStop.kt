package ge.tbilisipublictransport.domain.model

data class RouteStop(
    val id: String,
    val name: String,
    val isForward: Boolean,
    val lat: Double,
    val lng: Double
)
