package ge.transitgeorgia.presentation.bus_stops

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ge.transitgeorgia.common.analytics.Analytics
import ge.transitgeorgia.common.other.di.AppModule
import ge.transitgeorgia.common.other.enums.SupportedCity
import ge.transitgeorgia.common.other.mapper.toDomain
import ge.transitgeorgia.data.local.datastore.AppDataStore
import ge.transitgeorgia.data.local.db.AppDatabase
import ge.transitgeorgia.domain.model.BusStop
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class BusStopsViewModel @Inject constructor(
    private val db: AppDatabase,
    private val appDataStore: AppDataStore,
    @Named(AppModule.Name.DISPATCHER_IO) private val dispatcher: CoroutineDispatcher
) : ViewModel() {

    init {
        load()
        observeCity()
    }

    val isLoading: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val city: MutableStateFlow<SupportedCity> = MutableStateFlow(SupportedCity.TBILISI)
    val stops: MutableStateFlow<List<BusStop>> = MutableStateFlow(emptyList())
    val result: MutableStateFlow<List<BusStop>> = MutableStateFlow(emptyList())
    val error: MutableSharedFlow<String?> = MutableSharedFlow()

    private fun load() = viewModelScope.launch {
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

    private fun observeCity() = viewModelScope.launch {
        withContext(dispatcher) {
            appDataStore.city.collectLatest {
                city.value = it
            }
        }
    }

    fun search(keyword: String) = viewModelScope.launch {
        Analytics.logSearchStops()
        withContext(dispatcher) {
            result.value = stops.value.filter {
                it.id.contains(keyword) || it.code.contains(keyword) ||
                        it.name.lowercase().contains(keyword.lowercase())
            }
        }
    }
}