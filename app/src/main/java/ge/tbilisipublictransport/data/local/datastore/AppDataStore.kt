package ge.tbilisipublictransport.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import ge.tbilisipublictransport.common.util.AppLanguage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.appDataStore: DataStore<Preferences> by preferencesDataStore(name = "app_datastore")

class AppDataStore(private val context: Context) {

    private val KEY_LAST_UPDATED = longPreferencesKey("data_last_updated_at")
    private val KEY_APP_LANGUAGE = stringPreferencesKey("app_language")

    // App Language
    val language: Flow<AppLanguage.Language>
        get() = context.appDataStore.data.map {
            AppLanguage.Language.values().find { l -> l.value == it[KEY_APP_LANGUAGE] }
                ?: AppLanguage.Language.GEO
        }

    suspend fun setLanguage(language: AppLanguage.Language) {
        context.appDataStore.edit {
            it[KEY_APP_LANGUAGE] = language.value
        }
    }

    // Data Last Updated At
    val dataLastUpdatedAt: Flow<Long?>
        get() = context.appDataStore.data.map {
            it[KEY_LAST_UPDATED]
        }

    suspend fun setDataLastUpdatedAt(timeMillis: Long) = context.appDataStore.edit {
        it[KEY_LAST_UPDATED] = timeMillis
    }

    suspend fun deleteDataLastUpdatedAt() = context.appDataStore.edit {
        it.remove(KEY_LAST_UPDATED)
    }
}