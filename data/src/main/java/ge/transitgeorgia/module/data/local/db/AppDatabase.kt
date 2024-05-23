package ge.transitgeorgia.module.data.local.db

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import ge.transitgeorgia.data.local.entity.BusStopEntity
import ge.transitgeorgia.data.local.entity.FavoriteStopEntity
import ge.transitgeorgia.module.data.local.entity.RouteClicksEntity
import ge.transitgeorgia.data.local.entity.RouteEntity
import ge.transitgeorgia.module.data.local.dao.BusStopDao
import ge.transitgeorgia.module.data.local.dao.RouteDao
import ge.transitgeorgia.module.data.local.dao.RouteInfoDao
import ge.transitgeorgia.module.data.local.entity.RouteInfoEntity

@Database(
    entities = [
        BusStopEntity::class,
        RouteEntity::class,
        RouteClicksEntity::class,
        FavoriteStopEntity::class,
        RouteInfoEntity::class
    ],
    version = 3,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(1, 2),
        AutoMigration(2, 3),
    ]
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun busStopDao(): BusStopDao
    abstract fun routeDao(): RouteDao
    abstract fun routeInfoDao(): RouteInfoDao
}