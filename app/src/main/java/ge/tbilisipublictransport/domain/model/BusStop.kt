package ge.tbilisipublictransport.domain.model

data class BusStop(
    val id: String,
    val code: String,
    val name: String,
    val lat: Double,
    val lng: Double
)
