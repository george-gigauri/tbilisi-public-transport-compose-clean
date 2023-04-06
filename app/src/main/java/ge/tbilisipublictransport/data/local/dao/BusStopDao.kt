package ge.tbilisipublictransport.data.local.dao

import androidx.room.*
import ge.tbilisipublictransport.data.local.entity.BusStopEntity
import ge.tbilisipublictransport.data.local.entity.FavoriteStopEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BusStopDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: BusStopEntity)

    @Insert
    suspend fun insertAll(items: List<BusStopEntity>)

    @Query("SELECT * FROM bus_stop")
    suspend fun getStops(): List<BusStopEntity>

    @Query("SELECT * FROM bus_stop")
    fun getStopsFlow(): Flow<List<BusStopEntity>>

    @Query("SELECT * FROM bus_stop INNER JOIN favorite_stop WHERE code=stopCode")
    suspend fun getFavorites(): List<BusStopEntity>

    @Query("SELECT * FROM bus_stop INNER JOIN favorite_stop WHERE code=stopCode ORDER BY savedAt DESC LIMIT :limit")
    suspend fun getFavorites(limit: Int): List<BusStopEntity>

    @Query("SELECT * FROM bus_stop INNER JOIN favorite_stop WHERE code=stopCode ORDER BY savedAt DESC")
    suspend fun getFavoritesFlow(): Flow<List<BusStopEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addToFavorites(item: FavoriteStopEntity)

    @Query("DELETE FROM favorite_stop WHERE stopId=:id")
    suspend fun removeFromFavorites(id: String)

    @Query("SELECT (COUNT(*) > 0) FROM favorite_stop WHERE stopId=:id")
    fun isFavorite(id: String): Flow<Boolean>

    @Delete
    suspend fun delete(item: BusStopEntity)

    @Query("DELETE FROM bus_stop")
    suspend fun deleteAll()

    @Query("DELETE FROM bus_stop WHERE id=:id")
    suspend fun deleteById(id: String)
}