package ge.transitgeorgia.module.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class BusStop(
    val id: String,
    val code: String,
    val name: String,
    val lat: Double,
    val lng: Double
) : Parcelable