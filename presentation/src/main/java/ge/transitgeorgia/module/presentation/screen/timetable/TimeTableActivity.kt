package ge.transitgeorgia.module.presentation.screen.timetable

import android.os.Bundle
import android.preference.PreferenceManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import dagger.hilt.android.AndroidEntryPoint
import ge.transitgeorgia.common.analytics.Analytics
import ge.transitgeorgia.module.common.util.AppLanguage
import ge.transitgeorgia.module.data.local.datastore.AppDataStore
import ge.transitgeorgia.ui.theme.TbilisiPublicTransportTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.osmdroid.config.Configuration

@AndroidEntryPoint
class TimeTableActivity : ComponentActivity() {

    private lateinit var dataStore: AppDataStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dataStore = AppDataStore(this)
        val language = runBlocking { dataStore.language.first() }
        AppLanguage.updateLanguage(this, language.value)

        Configuration.getInstance()
            .load(this, PreferenceManager.getDefaultSharedPreferences(this))
        setContent {
            TbilisiPublicTransportTheme {
                TimeTableScreen()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        Analytics.logOpenStopTimetable()
    }
}