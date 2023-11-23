package ge.transitgeorgia.domain.model

data class CurrentTimeStationSchedule(
    val currentScheduledArrivalTime: String,
    val stopId: String,
    val stopName: String,
    val futureScheduledArrivalTimes: List<String>
)