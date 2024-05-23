package ge.transitgeorgia.module.data.remote.dto.tbilisi

import com.google.gson.annotations.SerializedName

data class RouteInfoDto(
    @SerializedName("id") val id: String,
    @SerializedName("color") val color: String,
    @SerializedName("shortName") val shortName: String,
    @SerializedName("longName") val longName: String,
    @SerializedName("circular") val isCircular: Boolean,
    @SerializedName("mode") val mode: String,
    @SerializedName("headSigns") val headSigns: HeadSignsDto?,
)
