package ge.transitgeorgia.data.remote.dto

import com.google.gson.annotations.SerializedName

data class RouteResponse(
    @SerializedName("Route") val routes: List<RouteDto>
)
