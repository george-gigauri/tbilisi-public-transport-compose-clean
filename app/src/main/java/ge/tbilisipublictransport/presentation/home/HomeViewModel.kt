package ge.tbilisipublictransport.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mapbox.mapboxsdk.geometry.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import ge.tbilisipublictransport.common.other.mapper.toDomain
import ge.tbilisipublictransport.data.local.db.AppDatabase
import ge.tbilisipublictransport.domain.model.BusStop
import ge.tbilisipublictransport.domain.model.Route
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val db: AppDatabase
) : ViewModel() {

    val topRoutes: MutableStateFlow<List<Route>> = MutableStateFlow(emptyList())
    val favoriteStops: MutableStateFlow<List<BusStop>> = MutableStateFlow(emptyList())
    val nearbyStops: MutableStateFlow<List<BusStop>> = MutableStateFlow(emptyList())

    val error: MutableStateFlow<String?> = MutableStateFlow(null)

    init {
        viewModelScope.launch {
            listOf(
                launch { fetchTopRoutes() },
                launch { fetchFavoriteStops() }
            ).joinAll()
        }
    }

    private fun fetchTopRoutes() = viewModelScope.launch {
        withContext(Dispatchers.IO) {
            db.routeDao().getTopRoutesFlow().collectLatest {
                topRoutes.value = it.take(3).map { it.toDomain() }
            }
        }
    }

    private fun fetchFavoriteStops() = viewModelScope.launch {
        withContext(Dispatchers.IO) {
            db.busStopDao().getFavoritesFlow().collectLatest {
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
}