package ge.tbilisipublictransport.data.remote.dto

import com.google.gson.annotations.SerializedName

data class RouteStopDto(
    @SerializedName("StopId") val stopId: String,
    @SerializedName("Name") val title: String,
    @SerializedName("Forward") val isForward: Boolean,
    @SerializedName("Sequence") val sequence: Int,
    @SerializedName("Lat") val lat: Double,
    @SerializedName("Lon") val lng: Double
)