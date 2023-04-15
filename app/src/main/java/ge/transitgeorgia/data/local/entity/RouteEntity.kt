package ge.transitgeorgia.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "route")
data class RouteEntity(
    @PrimaryKey val id: String,
    val color: String,
    val number: String,
    var longName: String,
    var firstStation: String,
    var lastStation: String
)