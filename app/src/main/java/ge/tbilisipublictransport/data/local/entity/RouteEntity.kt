package ge.tbilisipublictransport.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "route")
data class RouteEntity(
    @PrimaryKey val id: String,
    val color: String,
    val number: String,
    val longName: String,
    val firstStation: String,
    val lastStation: String
)