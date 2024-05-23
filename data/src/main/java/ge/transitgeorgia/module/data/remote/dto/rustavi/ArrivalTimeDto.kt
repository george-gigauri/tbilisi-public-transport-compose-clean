package ge.transitgeorgia.module.data.remote.dto.rustavi

import com.google.gson.annotations.SerializedName

data class ArrivalTimeDto(
    @SerializedName("RouteNumber") val routeNumber: String,
    @SerializedName("DestinationStopName") val destination: String,
    @SerializedName("ArrivalTime") val time: Int
)