package ge.transitgeorgia.module.presentation.screen.live_bus

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ge.transitgeorgia.module.data.local.entity.RouteClicksEntity
import ge.transitgeorgia.domain.model.Bus
import ge.transitgeorgia.domain.model.RouteStop
import ge.transitgeorgia.module.domain.repository.ITransportRepository
import ge.transitgeorgia.module.common.other.enums.SupportedCity
import ge.transitgeorgia.module.common.util.LatLngUtil
import ge.transitgeorgia.module.data.local.datastore.AppDataStore
import ge.transitgeorgia.module.data.local.db.AppDatabase
import ge.transitgeorgia.module.data.mapper.rustavi.toDomain
import ge.transitgeorgia.module.data.mapper.rustavi.toEntity
import ge.transitgeorgia.module.domain.model.RouteInfo
import ge.transitgeorgia.module.domain.util.ErrorType
import ge.transitgeorgia.module.domain.util.ResultWrapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class LiveBusViewModel @Inject constructor(
    private val repository: ITransportRepository,
    private val savedStateHandle: SavedStateHandle,
    private val db: AppDatabase,
    private val dataStore: AppDataStore
) : ViewModel() {

    val isLoading: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val error: MutableSharedFlow<ErrorType?> = MutableSharedFlow()
    val routeInfo: MutableStateFlow<RouteInfo?> = MutableStateFlow(null)
    val route1: MutableStateFlow<RouteInfo> = MutableStateFlow(RouteInfo.empty())
    val route2: MutableStateFlow<RouteInfo> = MutableStateFlow(RouteInfo.empty())
    val stops: MutableStateFlow<List<RouteStop>> = MutableStateFlow(emptyList())
    val isFavoriteRoute: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val shouldShowAddToFavoriteRoutesDialog: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val previousBuses: MutableStateFlow<List<Bus>> = MutableStateFlow(emptyList())
    val availableBuses: MutableStateFlow<List<Bus>> = MutableStateFlow(emptyList())

    val routeId: String? get() = savedStateHandle["route_id"]
    val routeNumber: String? get() = savedStateHandle["route_number"]
    val routeColor: String get() = savedStateHandle["route_color"] ?: "#000000"

    init {
        viewModelScope.launch {
            isLoading.value = true
            runBlocking { getRoute() }
            runBlocking { fetchRoutes() }
            increaseRouteVisitNumber()
            fetchAvailableBuses()
        }
    }

    fun addToFavoriteRoutes() = viewModelScope.launch {
        routeNumber?.toIntOrNull()?.let {
            val city = dataStore.city.first()
            db.routeDao().setClickCount(it, city.id, 10)
        }
    }

    fun rejectAddToFavorites() = viewModelScope.launch {
        routeNumber?.toIntOrNull()?.let {
            val city = dataStore.city.first()
            db.routeDao().setClickCount(it, city.id, 1)
        }
    }

    private suspend fun getRoute() {
        val city = runBlocking { dataStore.city.firstOrNull() } ?: SupportedCity.TBILISI
        routeInfo.value = routeId?.let {
            val info = db.routeInfoDao().get(it)
            if (info != null && !info.isOutdated()) {
                info.toDomain()
            } else {
                if (info?.uid != null) db.routeInfoDao().deleteByUid(info.uid!!)
                repository.getRouteByBus(routeId ?: "", true).let { r ->
                    if (r is ResultWrapper.Success) {
                        if (!r.data.isCircular) {
                            repository.getRouteByBus(routeId ?: "", false).let { rb ->
                                if (rb is ResultWrapper.Success) {
                                    r.data.also { d ->
                                        rb.data.also { d2 ->
                                            db.routeInfoDao().insert(
                                                d.toEntity(
                                                    city,
                                                    d2.polyline,
                                                    d2.polylineHash
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        } else {
                            r.data.also { d ->
                                db.routeInfoDao().insert(d.toEntity(city))
                            }
                        }
                    }
                    db.routeInfoDao().get(it)?.toDomain()
                }
            }
        }
    }

    private fun increaseRouteVisitNumber() = viewModelScope.launch {
        val city = dataStore.city.first()
        routeNumber?.toIntOrNull()?.let {
            if (!db.routeDao().isTrackingClicks(it, dataStore.city.first().id)) {
                db.routeDao().insertClickEntity(
                    RouteClicksEntity(
                        null,
                        it,
                        1,
                        city.id
                    )
                )
            }
            db.routeDao().getClickCount(it, city.id).let { clicks ->
                isFavoriteRoute.value = clicks >= 10
                delay(1500)
                shouldShowAddToFavoriteRoutesDialog.value =
                    ((clicks % 5L) == 0L) && !isFavoriteRoute.value
            }
            if (!isFavoriteRoute.value) {
                db.routeDao().increaseClickCount(it, city.id)
            }
        }
    }

    private fun fetchRoutes() = viewModelScope.launch {
        if (routeNumber.isNullOrEmpty()) {
            throw NullPointerException("Route number is invalid!")
        } else {
            routeId?.let {
                db.routeInfoDao().get(it)?.toDomain(true)?.let { r ->
                    route1.value = r
                }

                db.routeInfoDao().get(it)?.toDomain(false)?.let { r ->
                    route2.value = r
                }

                repository.getBusStopsByBusNumber(routeId ?: "-").let { bs ->
                    if (bs is ResultWrapper.Success) {
                        route1.value = route1.value.copy(
                            stops = bs.data
                        )
                    }
                }
            }
            isLoading.value = false
        }
    }

    private fun fetchAvailableBuses() = viewModelScope.launch {
        if (routeId.isNullOrEmpty()) throw NullPointerException("Route number is MUST!")

        while (true) {
            val busesAsync = awaitAll(
                async {
                    repository.getBusPositions(routeId ?: "", true)
                },
                async {
                    routeInfo.value?.let {
                        if (!it.isCircular) {
                            repository.getBusPositions(routeId ?: "", false)
                        } else ResultWrapper.Success(emptyList())
                    } ?: ResultWrapper.Success(emptyList())
                }
            )

            var busesForward: List<Bus> = emptyList()
            var busesBackward: List<Bus> = emptyList()

            when (val b = busesAsync[0]) {
                is ResultWrapper.Success -> busesForward = b.data
                is ResultWrapper.Error -> error.emit(b.type)
                else -> Unit
            }

            when (val b = busesAsync[1]) {
                is ResultWrapper.Success -> busesBackward = b.data
                is ResultWrapper.Error -> error.emit(b.type)
                else -> Unit
            }

            val bothBuses = listOf(
                busesForward.map { it.copy(isForward = true) },
                busesBackward.map { it.copy(isForward = false) },
            ).flatten()

            if (previousBuses.value.isEmpty()) {
                previousBuses.value = bothBuses
            }

            previousBuses.value = availableBuses.value

            availableBuses.value = bothBuses

            delay(if (route1.value.isMicroBus) 25000 else Random.nextLong(3000, 10000))
        }
    }
}