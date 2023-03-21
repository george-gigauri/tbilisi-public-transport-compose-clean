package ge.tbilisipublictransport.presentation.home

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ge.tbilisipublictransport.data.repository.TransportRepository
import ge.tbilisipublictransport.domain.model.BusStop
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val busRepository: TransportRepository
) : ViewModel() {

    var busStops by mutableStateOf(emptyList<BusStop>())
        private set

    init {
        fetchBusStops()
    }

    private fun fetchBusStops() = viewModelScope.launch {
        withContext(Dispatchers.IO) {
            busRepository.getStops().let {
                Log.d("HomeScreen", "Fetch Successfully")
                busStops = it
                Log.d("HomeScreen", "State Value Changed")
            }
        }
    }
}