package ge.tbilisipublictransport.presentation.bus_routes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ge.tbilisipublictransport.data.repository.TransportRepository
import ge.tbilisipublictransport.domain.model.Route
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class BusRoutesViewModel @Inject constructor(
    private val repository: TransportRepository
) : ViewModel() {

    val isLoading: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val data: MutableStateFlow<List<Route>> = MutableStateFlow(emptyList())
    val error: MutableStateFlow<String?> = MutableStateFlow(null)

    init {
        fetchRoutes()
    }

    private fun fetchRoutes() = viewModelScope.launch {
        withContext(Dispatchers.IO) {
            isLoading.value = true
            error.value = null

            try {
                val result = repository.getRoutes()
                data.value = result
                error.value = null
            } catch (e: java.lang.Exception) {
                error.value = e.message
            } finally {
                isLoading.value = false
            }
        }
    }
}