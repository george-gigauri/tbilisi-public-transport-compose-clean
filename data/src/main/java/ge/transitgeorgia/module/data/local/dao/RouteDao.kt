package ge.transitgeorgia.module.data.local.dao

import androidx.room.*
import ge.transitgeorgia.module.common.other.enums.SupportedCity
import ge.transitgeorgia.module.data.local.entity.RouteClicksEntity
import ge.transitgeorgia.module.data.local.entity.RouteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RouteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: RouteEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(items: List<RouteEntity>)

    @Update
    suspend fun update(item: RouteEntity)

    @Update
    suspend fun update(items: List<RouteEntity>)

    @Query("SELECT * FROM route")
    suspend fun getAll(): List<RouteEntity>

    @Query("SELECT * FROM route WHERE number=:routeNumber")
    suspend fun getRoute(routeNumber: Int): RouteEntity?

    @Query("SELECT * FROM route WHERE id=:id")
    suspend fun getRouteById(id: String): RouteEntity?

    @Query("SELECT * FROM route")
    fun getAllFlow(): Flow<List<RouteEntity>>

    @Query("SELECT r.* FROM route r INNER JOIN route_click_count ON number=routeNumber WHERE clicks >= 10 AND city=:cityId GROUP BY number ORDER BY secondaryClicks DESC")
    suspend fun getTopRoutes(cityId: String = SupportedCity.TBILISI.id): List<RouteEntity>

    @Query("SELECT r.* FROM route r INNER JOIN route_click_count ON number=routeNumber WHERE clicks >= 10 AND city=:cityId GROUP BY number ORDER BY secondaryClicks DESC")
    fun getTopRoutesFlow(cityId: String = SupportedCity.TBILISI.id): Flow<List<RouteEntity>>

    @Query("UPDATE route_click_count SET clicks=1 WHERE routeNumber=:routeNumber")
    suspend fun deleteTopRoute(routeNumber: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClickEntity(clicksEntity: RouteClicksEntity)

    @Query("""UPDATE route_click_count SET clicks=:count WHERE routeNumber=:routeNumber AND city=:cityId""")
    suspend fun setClickCount(routeNumber: Int, cityId: String, count: Long)

    @Query("SELECT (COUNT(*) > 0) FROM route r INNER JOIN route_click_count ON number=:routeNumber WHERE clicks >= 10 AND number=:routeNumber AND city=:cityId ORDER BY clicks DESC")
    suspend fun isTop(routeNumber: Int, cityId: String): Boolean

    @Query("SELECT (COUNT(*) > 0) FROM route_click_count WHERE routeNumber=:routeNumber AND city=:cityId")
    suspend fun isTrackingClicks(routeNumber: Int, cityId: String): Boolean

    @Query("UPDATE route_click_count SET clicks=(clicks + 1) WHERE routeNumber=:routeNumber AND city=:cityId")
    suspend fun increaseClickCount(routeNumber: Int, cityId: String = SupportedCity.TBILISI.id)

    @Query("UPDATE route_click_count SET secondaryClicks=(secondaryClicks + 1) WHERE routeNumber=:routeNumber AND city=:cityId")
    suspend fun increaseSecondaryClickCount(
        routeNumber: Int,
        cityId: String = SupportedCity.TBILISI.id
    )

    @Query("SELECT r.clicks FROM route_click_count r WHERE routeNumber=:routeNumber AND city=:cityId")
    suspend fun getClickCount(routeNumber: Int, cityId: String = SupportedCity.TBILISI.id): Long

    @Delete
    suspend fun delete(item: RouteEntity)

    @Query("DELETE FROM route")
    suspend fun deleteAll()
}