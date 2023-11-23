package ge.transitgeorgia.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import ge.transitgeorgia.common.other.enums.SupportedCity

@Entity(tableName = "favorite_stop")
data class FavoriteStopEntity(
    @PrimaryKey val stopId: String,
    var city: String = SupportedCity.TBILISI.id,
    val savedAt: Long
)