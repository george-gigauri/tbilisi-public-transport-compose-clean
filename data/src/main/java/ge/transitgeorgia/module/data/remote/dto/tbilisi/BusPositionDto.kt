package ge.transitgeorgia.module.data.remote.dto.tbilisi

import com.google.gson.annotations.SerializedName

data class BusPositionDto(
    @SerializedName("lat") val lat: Double?,
    @SerializedName("lon") val lng: Double?,
    @SerializedName("heading") val bearing: Double?,
    @SerializedName("nextStopId") val nextStopId: String?,
)
