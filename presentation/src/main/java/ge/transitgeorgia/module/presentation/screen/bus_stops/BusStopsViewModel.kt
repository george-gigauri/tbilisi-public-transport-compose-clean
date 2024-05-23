package ge.transitgeorgia.module.presentation.screen.bus_stops

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ge.transitgeorgia.common.analytics.Analytics
import ge.transitgeorgia.module.common.other.enums.SupportedCity
import ge.transitgeorgia.module.data.mapper.tbilisi.toDomain
import ge.transitgeorgia.module.data.di.qualifier.dispatcher.IODispatcher
import ge.transitgeorgia.module.data.local.datastore.AppDataStore
import ge.transitgeorgia.module.data.local.db.AppDatabase
import ge.transitgeorgia.module.data.mapper.rustavi.toDomain
import ge.transitgeorgia.module.domain.model.BusStop
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class BusStopsViewModel @Inject constructor(
    private val db: AppDatabase,
    private val appDataStore: AppDataStore,
    @IODispatcher private val dispatcher: CoroutineDispatcher
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
        db.busStopDao().getStopsFlow().collectLatest {
            val data = db.busStopDao().getStops().map { it.toDomain() }
            result.value = data
            stops.value = data
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