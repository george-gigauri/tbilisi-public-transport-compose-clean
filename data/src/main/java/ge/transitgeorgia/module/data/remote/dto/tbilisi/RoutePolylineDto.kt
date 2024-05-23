package ge.transitgeorgia.module.data.remote.dto.tbilisi

import com.google.gson.annotations.SerializedName

data class RoutePolylineDto(
    @SerializedName("encodedValue") val encodedValue: String,
    @SerializedName("color") val color: String,
)