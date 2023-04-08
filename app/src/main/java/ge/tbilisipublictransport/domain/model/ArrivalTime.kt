package ge.tbilisipublictransport.domain.model

data class ArrivalTime(
    val routeNumber: Int,
    val destination: String,
    val time: Int
)
