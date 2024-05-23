package ge.transitgeorgia.module.data.remote.dto.rustavi

import com.google.gson.annotations.SerializedName

data class RouteStopsResponse(
    @SerializedName("Stops") val stops: List<RouteStopDto>
)
