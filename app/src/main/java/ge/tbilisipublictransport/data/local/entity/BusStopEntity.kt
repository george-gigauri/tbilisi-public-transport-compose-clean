package ge.tbilisipublictransport.data.local.entity

import androidx.room.PrimaryKey

data class BusStopEntity(
    @PrimaryKey val id: String,
    val code: String,
    val name: String,
    val lat: Double,
    val lng: Double
)
