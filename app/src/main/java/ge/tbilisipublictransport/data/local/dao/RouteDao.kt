package ge.tbilisipublictransport.data.local.dao

import androidx.room.*
import ge.tbilisipublictransport.data.local.entity.RouteClicksEntity
import ge.tbilisipublictransport.data.local.entity.RouteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RouteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: RouteEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(items: List<RouteEntity>)

    @Query("SELECT * FROM route")
    suspend fun getAll(): List<RouteEntity>

    @Query("SELECT * FROM route")
    fun getAllFlow(): Flow<List<RouteEntity>>

    @Query("SELECT * FROM route INNER JOIN route_click_count WHERE clicks >= 3 ORDER BY clicks DESC")
    suspend fun getTopRoutes(): List<RouteEntity>

    @Query("SELECT * FROM route INNER JOIN route_click_count ON number=routeNumber WHERE clicks >= 3 ORDER BY clicks DESC")
    fun getTopRoutesFlow(): Flow<List<RouteEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertClickEntity(clicksEntity: RouteClicksEntity)

    @Query("UPDATE route_click_count SET clicks=(clicks + 1) WHERE routeNumber=:routeNumber")
    suspend fun increaseClickCount(routeNumber: Int)

    @Delete
    suspend fun delete(item: RouteEntity)

    @Query("DELETE FROM route")
    suspend fun deleteAll()
}