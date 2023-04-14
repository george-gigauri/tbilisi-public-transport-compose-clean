package ge.tbilisipublictransport.presentation.live_bus

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ge.tbilisipublictransport.BuildConfig
import ge.tbilisipublictransport.data.local.datastore.AppDataStore
import ge.tbilisipublictransport.data.local.db.AppDatabase
import ge.tbilisipublictransport.data.local.entity.RouteClicksEntity
import ge.tbilisipublictransport.data.repository.TransportRepository
import ge.tbilisipublictransport.domain.model.Bus
import ge.tbilisipublictransport.domain.model.RouteInfo
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import javax.inject.Inject

@HiltViewModel
class LiveBusViewModel @Inject constructor(
    private val repository: TransportRepository,
    private val savedStateHandle: SavedStateHandle,
    private val db: AppDatabase,
    private val dataStore: AppDataStore
) : ViewModel() {

    val isLoading: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val error: MutableStateFlow<String?> = MutableStateFlow(null)
    val route1: MutableStateFlow<RouteInfo> = MutableStateFlow(RouteInfo.empty())
    val route2: MutableStateFlow<RouteInfo> = MutableStateFlow(RouteInfo.empty())
    val availableBuses: MutableStateFlow<List<Bus>> = MutableStateFlow(emptyList())

    val routeNumber: Int? get() = savedStateHandle["route_number"]

    init {
        viewModelScope.launch {
            listOf(
                async { fetchRoutes() },
                async { fetchAvailableBuses() },
                async { increaseRouteVisitNumber() }
            ).joinAll()
        }
    }

    private fun increaseRouteVisitNumber() = viewModelScope.launch {
        val city = dataStore.city.first()
        routeNumber?.let {
            if (!db.routeDao().isTop(it, dataStore.city.first().id)) {
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
        withContext(Dispatchers.IO) {
            try {
                isLoading.value = true
                error.value = null
                if (routeNumber == null) throw NullPointerException("Route number is invalid!")
                else {
                    val routes = awaitAll(
                        async { repository.getRouteByBus(routeNumber!!, true) },
                        async { repository.getRouteByBus(routeNumber!!, false) }
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
    }

    private fun fetchAvailableBuses() = viewModelScope.launch {
        withContext(Dispatchers.IO) {
            if (routeNumber == null) throw NullPointerException("Route number is MUST!")

            while (true) {
                val busesAsync = awaitAll(
                    async {
                        try {
                            repository.getBusPositions(routeNumber!!, true)
                        } catch (e: Exception) {
                            e.printStackTrace()
                            emptyList()
                        }
                    },
                    async {
                        try {
                            repository.getBusPositions(routeNumber!!, false)
                        } catch (e: Exception) {
                            e.printStackTrace()
                            emptyList()
                        }
                    }
                )

                val bothBuses = busesAsync.flatten()
                availableBuses.value = bothBuses

                delay(if (BuildConfig.DEBUG) 15000 else 8000)
            }
        }
    }
}