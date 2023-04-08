package ge.tbilisipublictransport.presentation.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ge.tbilisipublictransport.common.other.mapper.toDomain
import ge.tbilisipublictransport.data.local.db.AppDatabase
import ge.tbilisipublictransport.domain.model.BusStop
import ge.tbilisipublictransport.domain.model.Route
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val db: AppDatabase
) : ViewModel() {

    val stops: MutableStateFlow<List<BusStop>> = MutableStateFlow(emptyList())
    val routes: MutableStateFlow<List<Route>> = MutableStateFlow(emptyList())

    init {
        loadAll()
    }

    private fun loadAll() {
        viewModelScope.launch {
            db.busStopDao().getFavoritesFlow().onEach {
                stops.value = it.map { i -> i.toDomain() }
            }.stateIn(viewModelScope)

            db.routeDao().getTopRoutesFlow().onEach {
                routes.value = it.map { i -> i.toDomain() }
            }.stateIn(viewModelScope)
        }
    }
}