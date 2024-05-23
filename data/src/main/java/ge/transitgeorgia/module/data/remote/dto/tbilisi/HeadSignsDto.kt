package ge.transitgeorgia.module.data.remote.dto.tbilisi

import com.google.gson.annotations.SerializedName

data class HeadSignsDto(
    @SerializedName("forwardHeadSign") val forwardSign: String,
    @SerializedName("backwardHeadSign") val backwardSign: String,
)
