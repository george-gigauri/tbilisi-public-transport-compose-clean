package ge.tbilisipublictransport.data.local.dao

import androidx.room.*
import ge.tbilisipublictransport.common.other.enums.SupportedCity
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

    @Query("SELECT * FROM route INNER JOIN route_click_count ON number=routeNumber WHERE clicks >= 3 AND city=:cityId ORDER BY clicks DESC")
    suspend fun getTopRoutes(cityId: String = SupportedCity.TBILISI.id): List<RouteEntity>

    @Query("SELECT * FROM route INNER JOIN route_click_count ON number=routeNumber WHERE clicks >= 3 AND city=:cityId ORDER BY clicks DESC")
    fun getTopRoutesFlow(cityId: String = SupportedCity.TBILISI.id): Flow<List<RouteEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertClickEntity(clicksEntity: RouteClicksEntity)

    @Query("SELECT (COUNT(*) > 0) FROM route_click_count WHERE routeNumber=:routeNumber AND city=:cityId")
    suspend fun isTop(routeNumber: Int, cityId: String): Boolean

    @Query("UPDATE route_click_count SET clicks=(clicks + 1) WHERE routeNumber=:routeNumber AND city=:cityId")
    suspend fun increaseClickCount(routeNumber: Int, cityId: String = SupportedCity.TBILISI.id)

    @Delete
    suspend fun delete(item: RouteEntity)

    @Query("DELETE FROM route")
    suspend fun deleteAll()
}