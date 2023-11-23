package ge.transitgeorgia.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bus_stop")
data class BusStopEntity(
    @PrimaryKey val id: String,
    val code: String,
    var name: String,
    val lat: Double,
    val lng: Double,
)