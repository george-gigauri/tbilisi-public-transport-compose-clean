package ge.transitgeorgia.domain.model

data class ScheduleStop(
    val id: String,
    val name: String,
    val lat: Double,
    val lng: Double,
    val arrivalTimes: List<String>
)
