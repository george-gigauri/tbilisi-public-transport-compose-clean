package ge.tbilisipublictransport.presentation.live_bus

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ge.tbilisipublictransport.data.repository.TransportRepository
import ge.tbilisipublictransport.domain.model.RouteInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class LiveBusViewModel @Inject constructor(
    private val repository: TransportRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    val isLoading: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val error: MutableStateFlow<String?> = MutableStateFlow(null)
    val route1: MutableStateFlow<RouteInfo> = MutableStateFlow(RouteInfo.empty())
    val route2: MutableStateFlow<RouteInfo> = MutableStateFlow(RouteInfo.empty())

    private val routeNumber: Int? get() = savedStateHandle["route_number"]

    init {
        fetchRoutes()
    }

    private fun fetchRoutes() = viewModelScope.launch {
        withContext(Dispatchers.IO) {
            try {
                isLoading.value = true
                error.value = null
                if (routeNumber == null) throw NullPointerException("Route number is invalid!")
                else {
                    val _route1 = repository.getRouteByBus(routeNumber!!, true)
                    val _route2 = repository.getRouteByBus(routeNumber!!, false)
                    route1.value = _route1
                    route2.value = _route2
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
}