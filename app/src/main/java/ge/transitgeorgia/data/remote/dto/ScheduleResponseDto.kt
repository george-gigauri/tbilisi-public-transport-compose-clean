package ge.transitgeorgia.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ScheduleResponseDto(
    @SerializedName("RouteNumber") val routeNumber: String,
    @SerializedName("Forward") val forward: Boolean,
    @SerializedName("WeekdaySchedules") val schedules: List<ScheduleWeekDayDto>
)

data class ScheduleWeekDayDto(
    @SerializedName("FromDay") val fromDay: String,
    @SerializedName("ToDay") val toDay: String,
    @SerializedName("Stops") val stops: List<ScheduleStopDto>
)

data class ScheduleStopDto(
    @SerializedName("StopId") val stopId: String,
    @SerializedName("Name") val name: String,
    @SerializedName("Forward") val forward: Boolean,
    @SerializedName("Lat") val lat: Double,
    @SerializedName("Lon") val lng: Double,
    @SerializedName("Sequence") val sequence: Int,
    @SerializedName("Type") val type: String,
    @SerializedName("ArriveTimes") val arrivalTimes: String,
    @SerializedName("HasBoard") val hasBoard: Boolean,
    @SerializedName("Virtual") val virtual: Boolean,
    @SerializedName("Routes") val routes: List<Any>
)