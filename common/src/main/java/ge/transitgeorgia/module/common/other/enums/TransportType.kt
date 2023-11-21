package ge.transitgeorgia.common.other.enums

import com.google.gson.annotations.SerializedName

enum class TransportType {
    @SerializedName("bus")
    BUS,
    @SerializedName("metro")
    METRO
}