package ge.tbilisipublictransport.presentation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ge.tbilisipublictransport.common.other.Const
import ge.tbilisipublictransport.data.local.datastore.AppDataStore
import ge.tbilisipublictransport.data.repository.TransportRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: TransportRepository,
    private val dataStore: AppDataStore
) : ViewModel() {

    fun load() = viewModelScope.launch {
        withContext(Dispatchers.IO) {
            val lastUpdated = dataStore.dataLastUpdatedAt.firstOrNull() ?: 0
            val interval = System.currentTimeMillis() - lastUpdated
            if (interval >= Const.DATA_UPDATE_INTERVAL_MILLIS) {
                repository.getStops()
                repository.getRoutes()
                dataStore.setDataLastUpdatedAt(System.currentTimeMillis())
            }
        }
    }
}