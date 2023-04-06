package ge.tbilisipublictransport.presentation.bus_stops

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ge.tbilisipublictransport.common.other.mapper.toDomain
import ge.tbilisipublictransport.data.local.db.AppDatabase
import ge.tbilisipublictransport.data.repository.TransportRepository
import ge.tbilisipublictransport.domain.model.BusStop
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import javax.inject.Inject

@HiltViewModel
class BusStopsViewModel @Inject constructor(
    private val repository: TransportRepository,
    private val db: AppDatabase
) : ViewModel() {

    init {
        load()
    }

    val isLoading: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val stops: MutableStateFlow<List<BusStop>> = MutableStateFlow(emptyList())
    val result: MutableStateFlow<List<BusStop>> = MutableStateFlow(emptyList())
    val error: MutableSharedFlow<String?> = MutableSharedFlow()

    private fun load() = viewModelScope.launch {
        withContext(Dispatchers.IO) {
            try {
                db.busStopDao().getStopsFlow().collectLatest {
                    val data = db.busStopDao().getStops().map { it.toDomain() }
                    result.value = data
                    stops.value = data
                }
            } catch (e: HttpException) {
                error.emit(e.message())
            } catch (e: Exception) {
                error.emit("Unknown Error")
            } finally {
                isLoading.value = false
            }
        }
    }

    fun search(keyword: String) = viewModelScope.launch {
        withContext(Dispatchers.IO) {
            result.value = stops.value.filter {
                it.id.contains(keyword) || it.code.contains(keyword) ||
                        it.name.contains(keyword)
            }
        }
    }
}