package ge.transitgeorgia.presentation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ge.transitgeorgia.common.other.Const
import ge.transitgeorgia.common.other.di.AppModule
import ge.transitgeorgia.common.util.AppLanguage
import ge.transitgeorgia.data.local.datastore.AppDataStore
import ge.transitgeorgia.domain.repository.ITransportRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: ITransportRepository,
    private val dataStore: AppDataStore,
    @Named(AppModule.Name.DISPATCHER_IO) private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val _shouldPromptLanguageSelector = MutableStateFlow(false)
    val shouldPromptLanguageSelector = _shouldPromptLanguageSelector.asStateFlow()

    init {
        checkIsLanguageSelected()
    }

    private fun checkIsLanguageSelected() = viewModelScope.launch {
        _shouldPromptLanguageSelector.value = !dataStore.isLanguageSet.first()
    }

    fun setLanguage(lang: AppLanguage.Language) = viewModelScope.launch {
        dataStore.setLanguage(lang)
    }

    fun load() = viewModelScope.launch {
        withContext(ioDispatcher) {
            val lastUpdated = dataStore.dataLastUpdatedAt.firstOrNull() ?: 0
            val interval = System.currentTimeMillis() - lastUpdated
            val updatedCityId = dataStore.lastUpdatedCityId.first()
            val city = dataStore.city.first()

            if (
                interval >= Const.DATA_UPDATE_INTERVAL_MILLIS ||
                updatedCityId != city.id
            ) {
                repository.getStops()
                repository.getRoutes()
                dataStore.setDataLastUpdatedAt(System.currentTimeMillis())
                dataStore.setLastUpdatedCityId(city)
            }
        }
    }
}