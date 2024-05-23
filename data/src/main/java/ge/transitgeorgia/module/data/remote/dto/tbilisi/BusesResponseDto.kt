package ge.transitgeorgia.module.data.remote.dto.tbilisi

import com.google.gson.annotations.SerializedName

data class BusesResponseDto(
    @SerializedName("bus") val buses: List<BusPositionDto>
)
