package ge.tbilisipublictransport.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ge.tbilisipublictransport.data.repository.TransportRepository
import ge.tbilisipublictransport.domain.model.BusStop
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val busRepository: TransportRepository
) : ViewModel() {

    val busStops: MutableStateFlow<List<BusStop>> = MutableStateFlow(emptyList())

    init {
        fetchBusStops()
    }

    private fun fetchBusStops() = viewModelScope.launch {
        withContext(Dispatchers.IO) {
            busRepository.getStops().let {
                busStops.value = it
            }
        }
    }
}