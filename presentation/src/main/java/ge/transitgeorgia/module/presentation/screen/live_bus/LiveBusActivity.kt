package ge.transitgeorgia.module.presentation.screen.live_bus

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import dagger.hilt.android.AndroidEntryPoint
import ge.transitgeorgia.ui.theme.TbilisiPublicTransportTheme

@AndroidEntryPoint
class LiveBusActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TbilisiPublicTransportTheme {
                LiveBusScreen()
            }
        }
    }
}