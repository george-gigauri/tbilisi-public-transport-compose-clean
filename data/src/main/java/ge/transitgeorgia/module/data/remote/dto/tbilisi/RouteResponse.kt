package ge.transitgeorgia.module.data.remote.dto.tbilisi

import com.google.gson.annotations.SerializedName

data class RouteResponse(
    @SerializedName("Route") val routes: List<RouteDto>
)
