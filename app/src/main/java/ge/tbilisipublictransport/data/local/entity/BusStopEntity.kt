package ge.tbilisipublictransport.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bus_stop")
data class BusStopEntity(
    @PrimaryKey val id: String,
    val code: String,
    val name: String,
    val lat: Double,
    val lng: Double,
)