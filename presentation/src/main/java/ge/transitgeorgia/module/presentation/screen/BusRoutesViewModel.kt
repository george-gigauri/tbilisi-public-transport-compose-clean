package ge.transitgeorgia.module.presentation.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ge.transitgeorgia.module.data.local.db.AppDatabase
import ge.transitgeorgia.module.data.mapper.rustavi.toDomain
import ge.transitgeorgia.module.data.mapper.tbilisi.toDomain
import ge.transitgeorgia.module.domain.model.Route
import ge.transitgeorgia.module.domain.model.RouteTransportType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class BusRoutesViewModel @Inject constructor(
    private val db: AppDatabase
) : ViewModel() {

    val searchKeyword: MutableStateFlow<String?> = MutableStateFlow(null)
    val isLoading: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val transportType: MutableStateFlow<RouteTransportType> = MutableStateFlow(RouteTransportType.ALL)
    val data: MutableStateFlow<List<Route>> = MutableStateFlow(emptyList())
    val routes: MutableStateFlow<List<Route>> = MutableStateFlow(emptyList())
    val error: MutableStateFlow<String?> = MutableStateFlow(null)

    private var searchJob: Job? = null

    init {
        fetchRoutes()
    }

    private fun fetchRoutes() = viewModelScope.launch {
        withContext(Dispatchers.IO) {
            isLoading.value = true
            error.value = null

            try {
                db.routeDao().getAllFlow().collectLatest {
                    val result = it.map { it.toDomain() }
                    data.value = result
                    routes.value = result
                    error.value = null
                }
            } catch (e: java.lang.Exception) {
                error.value = e.message
            } finally {
                isLoading.value = false
            }
        }
    }

    fun searchRoute(keyword: String) {
        searchJob?.cancelChildren()
        searchJob = viewModelScope.launch {
            withContext(Dispatchers.IO) {
                searchKeyword.value = if (keyword.isEmpty() || keyword.isBlank()) null else keyword
                routes.value = if (keyword.isNotEmpty()) {
                    data.value.filter {
                        it.number.startsWith(keyword) ||
                                it.longName.lowercase().contains(keyword.lowercase()) ||
                                it.firstStation.lowercase().contains(keyword.lowercase()) ||
                                it.lastStation.lowercase().contains(keyword.lowercase())
                    }
                } else data.value
            }
        }
    }

    fun setTransportType(type: RouteTransportType) {
        this.transportType.value = type
        if (type == RouteTransportType.ALL) {
            fetchRoutes()
        } else {
            routes.value = data.value.filter { it.type == type }.filter { routes.value.contains(it) }
        }
    }
}