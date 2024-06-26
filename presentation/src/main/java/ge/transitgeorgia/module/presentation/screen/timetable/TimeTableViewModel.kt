package ge.transitgeorgia.module.presentation.screen.timetable

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ge.transitgeorgia.common.analytics.Analytics
import ge.transitgeorgia.data.local.entity.FavoriteStopEntity
import ge.transitgeorgia.module.domain.model.ArrivalTime
import ge.transitgeorgia.module.domain.repository.ITransportRepository
import ge.transitgeorgia.module.data.local.datastore.AppDataStore
import ge.transitgeorgia.module.data.local.db.AppDatabase
import ge.transitgeorgia.module.data.mapper.rustavi.toDomain
import ge.transitgeorgia.module.domain.model.BusStop
import ge.transitgeorgia.module.domain.util.ErrorType
import ge.transitgeorgia.module.domain.util.ResultWrapper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
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
    val stop: MutableStateFlow<BusStop?> = MutableStateFlow(null)
    val isFavorite: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val error: MutableSharedFlow<ErrorType?> = MutableSharedFlow()

    private val scope = CoroutineScope(Dispatchers.IO)

    init {
        getStopInfo()
        listenIsFavorite()
        requestTimetableUpdates()
    }

    private fun getStopInfo() = viewModelScope.launch {
        stop.value = db.busStopDao().getStopByCode(stopId)?.toDomain()
    }

    private fun listenIsFavorite() = viewModelScope.launch {
        db.busStopDao().isFavorite(stopId).collectLatest {
            isFavorite.value = it
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
            isLoading.value = true
            repository.getTimeTable(stopId).let {
                when (it) {
                    is ResultWrapper.Success -> {
                        data.value = it.data.map {
                            val r = db.routeDao().getRoute(it.routeNumber)
                            it.copy(
                                routeId = r?.id
                            )
                        }
                    }

                    is ResultWrapper.Error -> {
                        error.emit(it.type)
                    }

                    else -> Unit
                }
            }

            shouldShowLoading.value = false
            isLoading.value = false
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