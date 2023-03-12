package ge.tbilisipublictransport.data.remote.dto

import com.google.gson.annotations.SerializedName

data class RouteStopsResponse(
    @SerializedName("Stops") val stops: List<RouteStopDto>
)
