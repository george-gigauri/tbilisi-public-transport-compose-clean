package ge.transitgeorgia.module.presentation.screen.live_bus

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import dagger.hilt.android.AndroidEntryPoint
import ge.transitgeorgia.module.common.util.AppLanguage
import ge.transitgeorgia.module.data.local.datastore.AppDataStore
import ge.transitgeorgia.ui.theme.TbilisiPublicTransportTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

@AndroidEntryPoint
class LiveBusActivity : ComponentActivity() {

    private lateinit var dataStore: AppDataStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dataStore = AppDataStore(this)
        val lang = runBlocking { dataStore.language.first() }
        AppLanguage.setLocale(this, lang.value)

        setContent {
            TbilisiPublicTransportTheme {
                LiveBusScreen()
            }
        }
    }
}