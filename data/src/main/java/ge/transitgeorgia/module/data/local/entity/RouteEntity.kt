package ge.transitgeorgia.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import ge.transitgeorgia.common.other.enums.TransportType

@Entity(tableName = "route")
data class RouteEntity(
    @PrimaryKey val id: String,
    val color: String,
    val number: String,
    @ColumnInfo(defaultValue = "BUS")
    val type: TransportType,
    var longName: String,
    var firstStation: String,
    var lastStation: String
)