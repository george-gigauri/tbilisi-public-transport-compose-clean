package ge.transitgeorgia.presentation.timetable

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ge.transitgeorgia.common.analytics.Analytics
import ge.transitgeorgia.data.local.datastore.AppDataStore
import ge.transitgeorgia.data.local.db.AppDatabase
import ge.transitgeorgia.data.local.entity.FavoriteStopEntity
import ge.transitgeorgia.domain.model.ArrivalTime
import ge.transitgeorgia.domain.repository.ITransportRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import retrofit2.HttpException
import javax.inject.Inject

@HiltViewModel
class TimeTableViewModel @Inject constructor(
    private val repository: ITransportRepository,
    private val db: AppDatabase,
    private val savedStateHandle: SavedStateHandle,
    private val dataStore: AppDataStore
) : ViewModel() {

    val stopId: String get() = savedStateHandle["stop_id"] ?: "-1"
    val isLoading: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val shouldShowLoading: MutableStateFlow<Boolean> = MutableStateFlow(true)
    val data: MutableStateFlow<List<ArrivalTime>> = MutableStateFlow(emptyList())
    val isFavorite: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val error: MutableStateFlow<String?> = MutableStateFlow(null)

    private val scope = CoroutineScope(Dispatchers.IO)

    init {
        listenIsFavorite()
        requestTimetableUpdates()
    }

    private fun listenIsFavorite() = viewModelScope.launch {
        db.busStopDao().isFavorite(stopId).collectLatest {
            isFavorite.value = it
        }
    }

    private fun requestTimetableUpdates() = viewModelScope.launch {
        while (scope.isActive) {
            load()
            delay(12 * 1000L)
        }
    }

    private fun load() = viewModelScope.launch {
        scope.coroutineContext.cancelChildren()
        scope.launch {
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
                shouldShowLoading.value = false
                isLoading.value = false
                error.value = null
            }
        }
    }

    fun refresh() {
        load()
    }

    fun onSwipeToRefresh() = viewModelScope.launch {
        shouldShowLoading.value = true
        load()
    }

    fun addOrRemoveToFavorites() = viewModelScope.launch {
        if (!isFavorite.value) {
            db.busStopDao().addToFavorites(
                FavoriteStopEntity(
                    stopId,
                    dataStore.city.first().id,
                    System.currentTimeMillis()
                )
            )
            Analytics.logAddStopToFavorites()
        } else {
            db.busStopDao().removeFromFavorites(stopId)
            Analytics.logRemoveStopFromFavorites()
        }
    }
}