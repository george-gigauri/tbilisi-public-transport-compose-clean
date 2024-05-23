package ge.transitgeorgia.module.data.remote.dto.tbilisi

import com.google.gson.annotations.SerializedName

data class TimeTableResponse(
    @SerializedName("ArrivalTime") val arrivalTimes: List<ArrivalTimeDto>
)