package ge.tbilisipublictransport.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import ge.tbilisipublictransport.common.other.enums.SupportedCity

@Entity(tableName = "route_click_count")
data class RouteClicksEntity(
    @PrimaryKey val id: Long?,
    val routeNumber: Int,
    val clicks: Long,
    var city: String = SupportedCity.TBILISI.id
)