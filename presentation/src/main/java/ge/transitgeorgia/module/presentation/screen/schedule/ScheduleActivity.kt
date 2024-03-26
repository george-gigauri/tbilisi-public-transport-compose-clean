package ge.transitgeorgia.presentation.schedule

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import dagger.hilt.android.AndroidEntryPoint
import ge.transitgeorgia.module.common.util.AppLanguage
import ge.transitgeorgia.module.data.local.datastore.AppDataStore
import ge.transitgeorgia.module.presentation.screen.schedule.ScheduleScreen
import ge.transitgeorgia.module.presentation.screen.schedule.ScheduleViewModel
import ge.transitgeorgia.ui.theme.TbilisiPublicTransportTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

@AndroidEntryPoint
class ScheduleActivity : ComponentActivity() {

    private val viewModel: ScheduleViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val language = runBlocking { AppDataStore(this@ScheduleActivity).language.first() }
        AppLanguage.updateLanguage(this, language.value)
        super.onCreate(savedInstanceState)
        setContent {
            TbilisiPublicTransportTheme {
                ScheduleScreen(
                    viewModel = viewModel,
                    onFinishActivity = {
                        finish()
                    }
                )
            }
        }
    }
}