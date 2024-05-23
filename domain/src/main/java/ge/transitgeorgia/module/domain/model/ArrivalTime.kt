package ge.transitgeorgia.module.domain.model

data class ArrivalTime(
    val routeNumber: Int,
    val destination: String,
    val time: Int,
    var routeId: String? = null
)
