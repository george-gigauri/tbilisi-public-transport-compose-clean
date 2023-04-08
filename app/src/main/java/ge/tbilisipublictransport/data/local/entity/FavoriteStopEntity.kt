package ge.tbilisipublictransport.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_stop")
data class FavoriteStopEntity(
    @PrimaryKey val stopId: String,
    val savedAt: Long
)