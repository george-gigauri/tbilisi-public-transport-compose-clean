package ge.transitgeorgia.module.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import ge.transitgeorgia.module.common.other.enums.SupportedCity

@Entity(tableName = "route_click_count")
data class RouteClicksEntity(
    @PrimaryKey val id: Long?,
    val routeNumber: Int,
    val clicks: Long,
    @ColumnInfo(defaultValue = "0")
    var secondaryClicks: Long,
    var city: String = SupportedCity.TBILISI.id
)