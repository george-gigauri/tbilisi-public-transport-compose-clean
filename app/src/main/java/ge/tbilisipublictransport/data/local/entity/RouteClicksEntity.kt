package ge.tbilisipublictransport.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "route_click_count")
data class RouteClicksEntity(
    @PrimaryKey val routeNumber: Int,
    val clicks: Long
)