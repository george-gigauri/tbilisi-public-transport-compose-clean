package ge.transitgeorgia.module.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import ge.transitgeorgia.module.common.other.enums.SupportedCity
import ge.transitgeorgia.module.common.util.AppLanguage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.appDataStore: DataStore<Preferences> by preferencesDataStore(name = "app_datastore")

class AppDataStore(private val context: Context) {

    private val KEY_LAST_UPDATED = longPreferencesKey("data_last_updated_at")
    private val KEY_LOCATION_DISCLOSURE = longPreferencesKey("location_disclosure_answered")
    private val KEY_APP_LANGUAGE = stringPreferencesKey("app_language")
    private val KEY_DEFAULT_CITY = stringPreferencesKey("default_city")
    private val KEY_UPDATED_CITY = stringPreferencesKey("updated_city")
    private val KEY_REQUIRES_DATA_DELETION = booleanPreferencesKey("data_deletion_required")

    // Data Deletion
    val requiresDataDeletion: Flow<Boolean>
        get() = context.appDataStore.data.map {
            it[KEY_REQUIRES_DATA_DELETION] ?: true
        }

    suspend fun setRequiresDataDeletion(isRequired: Boolean) {
        context.appDataStore.edit {
            it[KEY_REQUIRES_DATA_DELETION] = isRequired
        }
    }

    // App Language
    val language: Flow<AppLanguage.Language>
        get() = context.appDataStore.data.map {
            AppLanguage.Language.entries.find { l -> l.value == it[KEY_APP_LANGUAGE] }
                ?: AppLanguage.Language.GEO
        }

    suspend fun setLanguage(language: AppLanguage.Language) {
        context.appDataStore.edit {
            it[KEY_APP_LANGUAGE] = language.value
        }
    }

    val isLanguageSet: Flow<Boolean>
        get() = context.appDataStore.data.map {
            it[KEY_APP_LANGUAGE] != null
        }

    // Data Last Updated At
    val dataLastUpdatedAt: Flow<Long?>
        get() = context.appDataStore.data.map {
            it[KEY_LAST_UPDATED]
        }

    val lastUpdatedCityId: Flow<String>
        get() = context.appDataStore.data.map {
            it[KEY_UPDATED_CITY] ?: SupportedCity.TBILISI.id
        }

    suspend fun setLastUpdatedCityId(city: SupportedCity) {
        context.appDataStore.edit {
            it[KEY_UPDATED_CITY] = city.id
        }
    }

    suspend fun setDataLastUpdatedAt(timeMillis: Long) {
        context.appDataStore.edit {
            it[KEY_LAST_UPDATED] = timeMillis
        }
    }

    suspend fun deleteDataLastUpdatedAt() {
        context.appDataStore.edit {
            it.remove(KEY_LAST_UPDATED)
        }
    }

    // City
    val city: Flow<SupportedCity>
        get() = context.appDataStore.data.map {
            SupportedCity.entries.find { c -> c.id == it[KEY_DEFAULT_CITY] }
                ?: SupportedCity.TBILISI
        }

    suspend fun setCity(city: SupportedCity) {
        context.appDataStore.edit {
            it[KEY_DEFAULT_CITY] = city.id
        }
    }

    // Location Disclosure
    val shouldShowLocationDisclosure: Flow<Boolean>
        get() = context.appDataStore.data.map {
            it[KEY_LOCATION_DISCLOSURE]?.let { timeMillis ->
                System.currentTimeMillis() - timeMillis >= 24 * 60 * 60 * 1000
            } ?: true
        }

    val isLocationDisclosureAnswered: Flow<Boolean>
        get() = context.appDataStore.data.map {
            it[KEY_LOCATION_DISCLOSURE] != null
        }

    suspend fun setLocationDisclosureAnswered() {
        context.appDataStore.edit {
            it[KEY_LOCATION_DISCLOSURE] = System.currentTimeMillis()
        }
    }
}