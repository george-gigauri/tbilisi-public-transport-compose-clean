package ge.transitgeorgia.data.remote.dto

import com.google.gson.annotations.SerializedName

data class BusesResponseDto(
    @SerializedName("bus") val buses: List<BusDto>
)
