package ge.transitgeorgia.data.remote.dto

import com.google.gson.annotations.SerializedName

data class TimeTableResponse(
    @SerializedName("ArrivalTime") val arrivalTimes: List<ArrivalTimeDto>
)