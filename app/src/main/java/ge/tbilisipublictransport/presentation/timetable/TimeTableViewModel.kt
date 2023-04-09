package ge.tbilisipublictransport.presentation.timetable

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ge.tbilisipublictransport.data.local.datastore.AppDataStore
import ge.tbilisipublictransport.data.local.db.AppDatabase
import ge.tbilisipublictransport.data.local.entity.FavoriteStopEntity
import ge.tbilisipublictransport.data.repository.TransportRepository
import ge.tbilisipublictransport.domain.model.ArrivalTime
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import retrofit2.HttpException
import javax.inject.Inject

@HiltViewModel
class TimeTableViewModel @Inject constructor(
    private val repository: TransportRepository,
    private val db: AppDatabase,
    private val savedStateHandle: SavedStateHandle,
    private val dataStore: AppDataStore
) : ViewModel() {

    val stopId: String get() = savedStateHandle["stop_id"] ?: "-1"
    val isLoading: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val data: MutableStateFlow<List<ArrivalTime>> = MutableStateFlow(emptyList())
    val isFavorite: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val error: MutableStateFlow<String?> = MutableStateFlow(null)

    private val scope = CoroutineScope(Dispatchers.IO)

    init {
        listenIsFavorite()
        requestTimetableUpdates()
    }

    private fun listenIsFavorite() = viewModelScope.launch {
        withContext(Dispatchers.IO) {
            db.busStopDao().isFavorite(stopId).collectLatest {
                isFavorite.value = it
            }
        }
    }

    private fun requestTimetableUpdates() = viewModelScope.launch {
        while (scope.isActive) {
            load()
            delay(15 * 1000L)
        }
    }

    private fun load() = viewModelScope.launch {
        scope.coroutineContext.cancelChildren()
        scope.launch {
            withContext(Dispatchers.IO) {
                isLoading.value = true
                try {
                    repository.getTimeTable(stopId).let {
                        data.value = it
                    }
                } catch (httpError: HttpException) {
                    error.value = httpError.message()
                } catch (e: Exception) {
                    error.value = e.message ?: "Unknown Error"
                } finally {
                    isLoading.value = false
                    error.value = null
                }
            }
        }
    }

    fun refresh() {
        load()
    }

    fun addOrRemoveToFavorites() = viewModelScope.launch {
        withContext(Dispatchers.IO) {
            if (!isFavorite.value) {
                db.busStopDao().addToFavorites(
                    FavoriteStopEntity(
                        stopId,
                        dataStore.city.first().id,
                        System.currentTimeMillis()
                    )
                )
            } else db.busStopDao().removeFromFavorites(stopId)
        }
    }
}