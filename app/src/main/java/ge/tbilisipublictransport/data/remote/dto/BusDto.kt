package ge.tbilisipublictransport.data.remote.dto

import com.google.gson.annotations.SerializedName

data class BusDto(
    @SerializedName("routeNumber") val routeNumber: String,
    @SerializedName("nextStopId") val nextStopId: String,
    @SerializedName("forward") val isForward: Boolean,
    @SerializedName("lat") val lat: Double,
    @SerializedName("lon") val lng: Double
)
