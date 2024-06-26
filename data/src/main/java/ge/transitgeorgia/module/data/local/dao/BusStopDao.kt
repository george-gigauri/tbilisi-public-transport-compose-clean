package ge.transitgeorgia.module.data.local.dao

import androidx.room.*
import ge.transitgeorgia.module.common.other.enums.SupportedCity
import ge.transitgeorgia.data.local.entity.BusStopEntity
import ge.transitgeorgia.data.local.entity.FavoriteStopEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BusStopDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: BusStopEntity)

    @Insert
    suspend fun insertAll(items: List<BusStopEntity>)

    @Update
    suspend fun update(item: BusStopEntity)

    @Update
    suspend fun updateAll(items: List<BusStopEntity>)

    @Query("SELECT * FROM bus_stop")
    suspend fun getStops(): List<BusStopEntity>

    @Query("SELECT * FROM bus_stop WHERE id=:id")
    suspend fun getStopById(id: String): BusStopEntity?

    @Query("SELECT * FROM bus_stop WHERE code=:code")
    suspend fun getStopByCode(code: String): BusStopEntity?

    @Query("SELECT * FROM bus_stop")
    fun getStopsFlow(): Flow<List<BusStopEntity>>

    @Query("SELECT b.* FROM bus_stop AS b INNER JOIN favorite_stop ON code=stopId WHERE city=:cityId ORDER BY savedAt DESC")
    suspend fun getFavorites(cityId: String = SupportedCity.TBILISI.id): List<BusStopEntity>

    @Query("SELECT b.* FROM bus_stop AS b INNER JOIN favorite_stop ON code=stopId WHERE city=:cityId ORDER BY savedAt DESC LIMIT :limit")
    suspend fun getFavorites(
        limit: Int,
        cityId: String = SupportedCity.TBILISI.id
    ): List<BusStopEntity>

    @Query("SELECT b.* FROM bus_stop AS b INNER JOIN favorite_stop ON code=stopId WHERE city=:cityId ORDER BY savedAt DESC")
    fun getFavoritesFlow(cityId: String = SupportedCity.TBILISI.id): Flow<List<BusStopEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addToFavorites(item: FavoriteStopEntity)

    @Query("DELETE FROM favorite_stop WHERE stopId=:id AND city=:cityId")
    suspend fun removeFromFavorites(id: String, cityId: String = SupportedCity.TBILISI.id)

    @Query("SELECT (COUNT(*) > 0) FROM favorite_stop WHERE stopId=:id AND city=:cityId")
    fun isFavorite(id: String, cityId: String = SupportedCity.TBILISI.id): Flow<Boolean>

    @Delete
    suspend fun delete(item: BusStopEntity)

    @Query("DELETE FROM bus_stop")
    suspend fun deleteAll()

    @Query("DELETE FROM bus_stop WHERE id=:id")
    suspend fun deleteById(id: String)
}