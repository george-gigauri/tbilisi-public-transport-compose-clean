package ge.transitgeorgia.module.data.remote.dto.rustavi

import com.google.gson.annotations.SerializedName

data class RouteInfoDto(
    @SerializedName("Id") val id: String,
    @SerializedName("Color") val color: String,
    @SerializedName("RouteNumber") val routeNumber: String,
    @SerializedName("LongName") val title: String,
    @SerializedName("Shape") val shape: String,
    @SerializedName("Type") val type: String,
    @SerializedName("RouteStops") val stops: List<RouteStopDto>
)
