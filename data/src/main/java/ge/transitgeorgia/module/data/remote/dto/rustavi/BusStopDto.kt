package ge.transitgeorgia.module.data.remote.dto.rustavi

import com.google.gson.annotations.SerializedName

data class BusStopDto(
    @SerializedName("id") val id: String?,
    @SerializedName("code") val code: String?,
    @SerializedName("name") val name: String?,
    @SerializedName("lat") val lat: Double?,
    @SerializedName("lon") val lng: Double?
)
