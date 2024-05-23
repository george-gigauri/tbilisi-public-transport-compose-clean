package ge.transitgeorgia.module.data.remote.dto.tbilisi

import com.google.gson.annotations.SerializedName

data class ArrivalTimeDto(
    @SerializedName("shortName") val shortName: String,
    @SerializedName("headsign") val headSign: String?,
    @SerializedName("realtime") val realtime: Boolean,
    @SerializedName("realtimeArrivalMinutes") val realtimeArrivalMinutes: Int,
    @SerializedName("scheduledArrivalMinutes") val scheduledArrivalMinutes: Int,
)