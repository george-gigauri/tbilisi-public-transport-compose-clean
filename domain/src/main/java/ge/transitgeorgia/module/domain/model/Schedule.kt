package ge.transitgeorgia.domain.model

data class Schedule(
    val fromDay: String,
    val toDay: String,
    val isForward: Boolean,
    val stops: List<ScheduleStop>
) {

    companion object {
        fun empty(): Schedule {
            return Schedule(
                "",
                "",
                false,
                emptyList()
            )
        }
    }
}
