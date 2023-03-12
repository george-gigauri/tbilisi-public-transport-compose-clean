package ge.tbilisipublictransport.data.remote.dto

import com.google.gson.annotations.SerializedName

data class RouteDto(
    @SerializedName("Id") val id: String,
    @SerializedName("Color") val color: String,
    @SerializedName("RouteNumber") val number: String,
    @SerializedName("LongName") val longName: String,
    @SerializedName("StopA") val startStop: String,
    @SerializedName("StopB") val lastStop: String,
    @SerializedName("Type") val type: String
)
