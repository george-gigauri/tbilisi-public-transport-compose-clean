package ge.transitgeorgia.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mapbox.mapboxsdk.geometry.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import ge.transitgeorgia.common.other.enums.SupportedCity
import ge.transitgeorgia.common.other.mapper.toDomain
import ge.transitgeorgia.data.local.datastore.AppDataStore
import ge.transitgeorgia.data.local.db.AppDatabase
import ge.transitgeorgia.domain.model.BusStop
import ge.transitgeorgia.domain.model.Route
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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

    val error: MutableStateFlow<String?> = MutableStateFlow(null)

    init {
        viewModelScope.launch {
            listOf(
                launch { listenCityChanges() },
                launch { fetchTopRoutes() },
                launch { fetchFavoriteStops() }
            ).joinAll()
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
                topRoutes.value = it.take(4).map { it.toDomain() }
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

    fun fetchNearbyStops(position: LatLng) = viewModelScope.launch {
        withContext(Dispatchers.IO) {
            db.busStopDao().getStopsFlow().collectLatest {
                nearbyStops.value = it.map { i -> i.toDomain() }.sortedBy { b ->
                    val latLng = LatLng(b.lat, b.lng)
                    position.distanceTo(latLng)
                }.take(10)
            }
        }
    }

    fun setDefaultCity(city: SupportedCity) = viewModelScope.launch {
        withContext(Dispatchers.IO) {
            dataStore.setCity(city)
        }
    }
}