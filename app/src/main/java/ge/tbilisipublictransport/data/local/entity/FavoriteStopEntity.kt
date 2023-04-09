package ge.tbilisipublictransport.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import ge.tbilisipublictransport.common.other.enums.SupportedCity

@Entity(tableName = "favorite_stop")
data class FavoriteStopEntity(
    @PrimaryKey val stopId: String,
    var city: String = SupportedCity.TBILISI.id,
    val savedAt: Long
)