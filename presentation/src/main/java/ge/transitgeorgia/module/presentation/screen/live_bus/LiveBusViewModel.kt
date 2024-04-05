package ge.transitgeorgia.module.presentation.screen.live_bus

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ge.transitgeorgia.module.data.local.entity.RouteClicksEntity
import ge.transitgeorgia.domain.model.Bus
import ge.transitgeorgia.domain.repository.ITransportRepository
import ge.transitgeorgia.module.common.util.LatLngUtil
import ge.transitgeorgia.module.data.local.datastore.AppDataStore
import ge.transitgeorgia.module.data.local.db.AppDatabase
import ge.transitgeorgia.module.data.mapper.toDomain
import ge.transitgeorgia.module.domain.model.Route
import ge.transitgeorgia.module.domain.model.RouteInfo
import ge.transitgeorgia.module.domain.util.ErrorType
import ge.transitgeorgia.module.domain.util.ResultWrapper
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
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
    val route: MutableStateFlow<Route?> = MutableStateFlow(null)
    val route1: MutableStateFlow<RouteInfo> = MutableStateFlow(RouteInfo.empty())
    val route2: MutableStateFlow<RouteInfo> = MutableStateFlow(RouteInfo.empty())
    val isCircular: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val previousBuses: MutableStateFlow<List<Bus>> = MutableStateFlow(emptyList())
    val availableBuses: MutableStateFlow<List<Bus>> = MutableStateFlow(emptyList())

    var autoRefresh: Boolean = true
        set(value) {
            field = value
        }

    val routeNumber: String? get() = savedStateHandle["route_number"]
    val routeColor: String get() = savedStateHandle["route_color"] ?: "#000000"

    init {
        viewModelScope.launch {
            getRoute()
            listOf(
                async { fetchRoutes() },
                async { fetchAvailableBuses() },
                async { increaseRouteVisitNumber() }
            ).joinAll()
        }
    }

    private suspend fun getRoute() {
        route.value = routeNumber?.toIntOrNull()?.let { db.routeDao().getRoute(it)?.toDomain() }
    }

    private fun increaseRouteVisitNumber() = viewModelScope.launch {
        val city = dataStore.city.first()
        routeNumber?.toIntOrNull()?.let {
            if (!db.routeDao().isTrackingClicks(it, dataStore.city.first().id)) {
                db.routeDao().insertClickEntity(
                    RouteClicksEntity(
                        null,
                        it,
                        0,
                        city.id
                    )
                )
            }
            db.routeDao().increaseClickCount(it, city.id)
        }
    }

    private fun fetchRoutes() = viewModelScope.launch {
        isLoading.value = true
        if (routeNumber.isNullOrEmpty()) {
            throw NullPointerException("Route number is invalid!")
        } else {
            val routes = awaitAll(
                async { repository.getRouteByBus(routeNumber?.toIntOrNull()!!, true) },
                async { repository.getRouteByBus(routeNumber?.toIntOrNull()!!, false) }
            )

            when (val r = routes[0]) {
                is ResultWrapper.Success -> route1.value = r.data
                is ResultWrapper.Error -> error.emit(r.type)
                else -> Unit
            }

            when (val r = routes[1]) {
                is ResultWrapper.Success -> route2.value = r.data
                is ResultWrapper.Error -> error.emit(r.type)
                else -> Unit
            }
        }
        isCircular.value = (route1.value.stops.isNotEmpty() && route2.value.stops.isEmpty()) ||
                (route1.value.stops.isEmpty() && route2.value.stops.isNotEmpty())
        isLoading.value = false
    }

    private fun fetchAvailableBuses() = viewModelScope.launch {
        if (routeNumber.isNullOrEmpty()) throw NullPointerException("Route number is MUST!")

        while (true) {
            val busesAsync = awaitAll(
                async {
                    repository.getBusPositions(routeNumber?.toIntOrNull()!!, true)
                },
                async {
                    if (!isCircular.value) {
                        repository.getBusPositions(routeNumber?.toIntOrNull()!!, false)
                    } else ResultWrapper.Success(emptyList())
                }
            )

            var busesForward: List<Bus> = emptyList()
            var busesBackward: List<Bus> = emptyList()

            when (val b = busesAsync[0]) {
                is ResultWrapper.Success -> busesForward = b.data.sortedByDescending { it.lng }
                is ResultWrapper.Error -> error.emit(b.type)
                else -> Unit
            }

            when (val b = busesAsync[1]) {
                is ResultWrapper.Success -> busesBackward = b.data.sortedByDescending { it.lng }
                is ResultWrapper.Error -> error.emit(b.type)
                else -> Unit
            }

            val bothBuses = listOf(busesForward, busesBackward).flatten().map { b ->
                b.apply {
                    previousBuses.value.find { pb -> pb.nextStopId == b.nextStopId }?.let { pb ->
                        if (this.lat != pb.lat && this.lng != pb.lng) {
                            this.bearing = LatLngUtil.calculateBearing(
                                pb.lat,
                                pb.lng,
                                this.lat,
                                this.lng
                            ).toDouble()
                        }
                    }
                }
            }

            if (previousBuses.value.isEmpty()) {
                previousBuses.value = bothBuses
            }

            if (!previousBuses.value.containsAll(bothBuses)) {
                previousBuses.value = availableBuses.value
            }
            availableBuses.value = bothBuses.map {
                it.apply {
                    this.bearing = it.bearing ?: previousBuses.value.find { ib ->
                        ib.lat == this.lat && ib.lng == this.lng
                    }.let { i -> i?.bearing }
                }
            }

            delay(if (route1.value.isMicroBus) 30000 else Random.nextLong(3000, 10000))
        }
    }
}