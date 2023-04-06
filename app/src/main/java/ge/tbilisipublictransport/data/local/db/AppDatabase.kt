package ge.tbilisipublictransport.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import ge.tbilisipublictransport.data.local.dao.BusStopDao
import ge.tbilisipublictransport.data.local.dao.RouteDao
import ge.tbilisipublictransport.data.local.entity.BusStopEntity
import ge.tbilisipublictransport.data.local.entity.FavoriteStopEntity
import ge.tbilisipublictransport.data.local.entity.RouteClicksEntity
import ge.tbilisipublictransport.data.local.entity.RouteEntity

@Database(
    entities = [
        BusStopEntity::class,
        RouteEntity::class,
        RouteClicksEntity::class,
        FavoriteStopEntity::class
    ],
    version = 1,
    exportSchema = true,
    autoMigrations = []
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun busStopDao(): BusStopDao
    abstract fun routeDao(): RouteDao
}