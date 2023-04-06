package ge.tbilisipublictransport.presentation.bus_routes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ge.tbilisipublictransport.common.other.mapper.toDomain
import ge.tbilisipublictransport.data.local.db.AppDatabase
import ge.tbilisipublictransport.data.repository.TransportRepository
import ge.tbilisipublictransport.domain.model.Route
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

@HiltViewModel
class BusRoutesViewModel @Inject constructor(
    private val repository: TransportRepository,
    private val db: AppDatabase
) : ViewModel() {

    val isLoading: MutableStateFlow<Boolean> = MutableStateFlow(false)
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
                db.routeDao().getAllFlow().collect {
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
                routes.value = if (keyword.isNotEmpty()) {
                    data.value.filter {
                        it.number.startsWith(keyword) || it.longName.contains(keyword)
                    }
                } else data.value
            }
        }
    }
}