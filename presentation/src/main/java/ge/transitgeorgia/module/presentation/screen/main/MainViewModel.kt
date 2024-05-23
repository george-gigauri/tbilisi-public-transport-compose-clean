package ge.transitgeorgia.module.presentation.screen.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ge.transitgeorgia.module.domain.repository.ITransportRepository
import ge.transitgeorgia.module.common.other.Const
import ge.transitgeorgia.module.common.util.AppLanguage
import ge.transitgeorgia.module.data.di.qualifier.dispatcher.IODispatcher
import ge.transitgeorgia.module.data.local.datastore.AppDataStore
import ge.transitgeorgia.module.data.local.db.AppDatabase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: ITransportRepository,
    private val dataStore: AppDataStore,
    private val db: AppDatabase,
    @IODispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val _shouldPromptLanguageSelector = MutableStateFlow(false)
    val shouldPromptLanguageSelector = _shouldPromptLanguageSelector.asStateFlow()

    private val _isDataDeletionRequired = MutableStateFlow(false)
    val isDataDeletionRequired = _isDataDeletionRequired.asStateFlow()

    init {
        viewModelScope.launch {
            runBlocking { checkRequiresDataDeletion() }
            checkIsLanguageSelected()
            load()
        }
    }

    fun deleteData() = viewModelScope.launch {
        withContext(Dispatchers.IO) {
            dataStore.deleteDataLastUpdatedAt()
            db.clearAllTables()
        }
    }

    private fun checkRequiresDataDeletion() = viewModelScope.launch {
        _isDataDeletionRequired.value = dataStore.requiresDataDeletion.firstOrNull() ?: true
    }

    private fun checkIsLanguageSelected() = viewModelScope.launch {
        dataStore.isLanguageSet.collectLatest {
            _shouldPromptLanguageSelector.value = !it
        }
    }

    fun setLanguage(lang: AppLanguage.Language) = viewModelScope.launch {
        dataStore.setLanguage(lang)
    }

    fun load() = viewModelScope.launch {
        withContext(ioDispatcher) {
            if (!isDataDeletionRequired.value) {
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
}