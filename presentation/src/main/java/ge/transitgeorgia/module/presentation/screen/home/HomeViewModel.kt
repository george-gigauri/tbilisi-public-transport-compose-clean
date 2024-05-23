package ge.transitgeorgia.module.presentation.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ge.transitgeorgia.module.common.other.enums.SupportedCity
import ge.transitgeorgia.module.data.mapper.tbilisi.toDomain
import ge.transitgeorgia.module.data.local.datastore.AppDataStore
import ge.transitgeorgia.module.data.local.db.AppDatabase
import ge.transitgeorgia.module.data.mapper.rustavi.toDomain
import ge.transitgeorgia.module.domain.model.BusStop
import ge.transitgeorgia.module.domain.model.Route
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.osmdroid.util.GeoPoint
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val db: AppDatabase,
    private val dataStore: AppDataStore
) : ViewModel() {

    val topRoutes: MutableStateFlow<List<Route>> = MutableStateFlow(emptyList())
    val favoriteStops: MutableStateFlow<List<BusStop>> = MutableStateFlow(emptyList())
    val nearbyStops: MutableStateFlow<List<BusStop>> = MutableStateFlow(emptyList())
    val city: MutableStateFlow<SupportedCity> = MutableStateFlow(SupportedCity.TBILISI)
    val isLocationDisclosureAnswered: MutableStateFlow<Boolean> = MutableStateFlow(false)

    val error: MutableStateFlow<String?> = MutableStateFlow(null)

    init {
        viewModelScope.launch {
            listOf(
                launch { listenCityChanges() },
                launch { listenLocationDisclosureChanges() },
                launch { fetchTopRoutes() },
                launch { fetchFavoriteStops() }
            ).joinAll()
        }
    }

    private fun listenLocationDisclosureChanges() = viewModelScope.launch {
        dataStore.shouldShowLocationDisclosure.collectLatest {
            isLocationDisclosureAnswered.value = it
        }
    }

    private fun listenCityChanges() = viewModelScope.launch {
        dataStore.city.collectLatest {
            city.value = it
        }
    }

    private fun fetchTopRoutes() = viewModelScope.launch {
        withContext(Dispatchers.IO) {
            db.routeDao().getTopRoutesFlow(dataStore.city.first().id).collectLatest {
                topRoutes.value = it.take(5).map { it.toDomain() }
            }
        }
    }

    private fun fetchFavoriteStops() = viewModelScope.launch {
        withContext(Dispatchers.IO) {
            db.busStopDao().getFavoritesFlow(dataStore.city.first().id).collectLatest {
                favoriteStops.value = it.take(5).map { it.toDomain() }
            }
        }
    }

    fun fetchNearbyStops(position: GeoPoint) = viewModelScope.launch {
        withContext(Dispatchers.IO) {
            db.busStopDao().getStopsFlow().collectLatest {
                nearbyStops.value = it.map { i -> i.toDomain() }.sortedBy { b ->
                    val latLng = GeoPoint(b.lat, b.lng)
                    position.distanceToAsDouble(latLng)
                }.take(5)
            }
        }
    }

    fun setDefaultCity(city: SupportedCity) = viewModelScope.launch {
        withContext(Dispatchers.IO) {
            dataStore.setCity(city)
        }
    }

    fun answerLocationDisclosure() = viewModelScope.launch {
        dataStore.setLocationDisclosureAnswered()
    }
}