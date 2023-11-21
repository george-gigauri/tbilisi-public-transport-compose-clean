package ge.transitgeorgia.domain.model

data class ArrivalTime(
    val routeNumber: Int,
    val destination: String,
    val time: Int
)
