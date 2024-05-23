package ge.transitgeorgia.module.data.remote.dto.tbilisi

import com.google.gson.annotations.SerializedName

data class ScheduleResponseDto(
    @SerializedName("routeShortName") val routeNumber: String?,
    @SerializedName("routeColor") val routeColor: String?,
    @SerializedName("weekdaySchedules") val schedules: List<ScheduleWeekDayDto>?
)

data class ScheduleWeekDayDto(
    @SerializedName("fromDay") val fromDay: String,
    @SerializedName("toDay") val toDay: String,
    @SerializedName("stops") val stops: List<ScheduleStopDto>?
)

data class ScheduleStopDto(
    @SerializedName("id") val stopId: String?,
    @SerializedName("name") val name: String,
    @SerializedName("position") val sequence: Int,
    @SerializedName("arrivalTimes") val arrivalTimes: String,
)