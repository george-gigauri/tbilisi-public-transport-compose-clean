package ge.transitgeorgia.module.common.other.enums

import com.google.gson.annotations.SerializedName

enum class TransportType {
    @SerializedName("metro")
    METRO,
    @SerializedName("bus")
    BUS,
}