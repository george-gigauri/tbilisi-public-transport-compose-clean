package ge.transitgeorgia.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ge.transitgeorgia.common.util.AppLanguage
import ge.transitgeorgia.data.local.datastore.AppDataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val dataStore: AppDataStore
) : ViewModel() {

    val isDarkMode: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val appLanguage: MutableStateFlow<AppLanguage.Language> =
        MutableStateFlow(AppLanguage.Language.GEO)

    init {
        getDarkMode()
        getAppLanguage()
    }

    fun setAppLanguage(lang: AppLanguage.Language) = viewModelScope.launch {
        withContext(Dispatchers.IO) {
            dataStore.setLanguage(lang)
            dataStore.deleteDataLastUpdatedAt()
        }
    }

    private fun getDarkMode() = viewModelScope.launch {
        withContext(Dispatchers.IO) {
            dataStore
        }
    }

    private fun getAppLanguage() = viewModelScope.launch {
        withContext(Dispatchers.IO) {
            dataStore.language.collect { appLanguage.value = it }
        }
    }
}