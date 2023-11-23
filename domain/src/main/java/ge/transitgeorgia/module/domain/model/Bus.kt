package ge.transitgeorgia.domain.model

data class Bus(
    val number: Int,
    val nextStopId: String,
    val isForward: Boolean,
    val lat: Double,
    val lng: Double
)