package ge.transitgeorgia.module.data.remote.dto.tbilisi

import com.google.gson.annotations.SerializedName
import ge.transitgeorgia.module.common.other.enums.TransportType

data class RouteDto(
    @SerializedName("id") val id: String,
    @SerializedName("shortName") val shortName: String?,
    @SerializedName("longName") val longName: String?,
    @SerializedName("color") val color: String?,
    @SerializedName("mode") val mode: String?,
)