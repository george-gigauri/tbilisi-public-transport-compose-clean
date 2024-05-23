package ge.transitgeorgia.module.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ge.transitgeorgia.module.common.other.enums.SupportedCity
import ge.transitgeorgia.module.data.local.entity.RouteInfoEntity

@Dao
interface RouteInfoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(routeInfo: RouteInfoEntity)

    @Query("SELECT (COUNT(*) > 0) FROM route_info WHERE id = :id")
    suspend fun exists(id: String): Boolean

    @Query("SELECT * FROM route_info WHERE id = :id ORDER BY addedAt DESC")
    suspend fun get(id: String): RouteInfoEntity?

    @Query("SELECT * FROM route_info")
    suspend fun getAll(): List<RouteInfoEntity>

    @Query("DELETE FROM route_info WHERE uid=:uid")
    suspend fun deleteByUid(uid: Int)

    @Query("DELETE FROM route_info")
    suspend fun deleteAll()

}