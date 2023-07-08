package ge.transitgeorgia.presentation.live_bus

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ge.transitgeorgia.BuildConfig
import ge.transitgeorgia.common.other.mapper.toDomain
import ge.transitgeorgia.data.local.datastore.AppDataStore
import ge.transitgeorgia.data.local.db.AppDatabase
import ge.transitgeorgia.data.local.entity.RouteClicksEntity
import ge.transitgeorgia.domain.model.Bus
import ge.transitgeorgia.domain.model.Route
import ge.transitgeorgia.domain.model.RouteInfo
import ge.transitgeorgia.domain.repository.ITransportRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LiveBusViewModel @Inject constructor(
    private val repository: ITransportRepository,
    private val savedStateHandle: SavedStateHandle,
    private val db: AppDatabase,
    private val dataStore: AppDataStore
) : ViewModel() {

    val isLoading: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val error: MutableStateFlow<String?> = MutableStateFlow(null)
    val route: MutableStateFlow<Route?> = MutableStateFlow(null)
    val route1: MutableStateFlow<RouteInfo> = MutableStateFlow(RouteInfo.empty())
    val route2: MutableStateFlow<RouteInfo> = MutableStateFlow(RouteInfo.empty())
    val availableBuses: MutableStateFlow<List<Bus>> = MutableStateFlow(emptyList())
    var autoRefresh: Boolean = true
        set(value) {
            field = value
            fetchAvailableBuses()
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
        try {
            isLoading.value = true
            error.value = null
            if (routeNumber.isNullOrEmpty()) throw NullPointerException("Route number is invalid!")
            else {
                val routes = awaitAll(
                    async { repository.getRouteByBus(routeNumber?.toIntOrNull()!!, true) },
                    async { repository.getRouteByBus(routeNumber?.toIntOrNull()!!, false) }
                )
                route1.value = routes[0]
                route2.value = routes[1]
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.d("LiveBusViewModel", e.message ?: "Error while parsing data")
            error.value = e.message
        } finally {
            error.value = null
            isLoading.value = false
        }
    }

    private fun fetchAvailableBuses() = viewModelScope.launch {
        if (routeNumber.isNullOrEmpty()) throw NullPointerException("Route number is MUST!")

        while (autoRefresh) {
            val busesAsync = awaitAll(
                async {
                    try {
                        repository.getBusPositions(routeNumber?.toIntOrNull()!!, true)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        emptyList()
                    }
                },
                async {
                    try {
                        repository.getBusPositions(routeNumber?.toIntOrNull()!!, false)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        emptyList()
                    }
                }
            )

            val bothBuses = busesAsync.flatten()
            availableBuses.value = bothBuses

            delay(if (BuildConfig.DEBUG) 25000 else 5999)
        }
    }
}