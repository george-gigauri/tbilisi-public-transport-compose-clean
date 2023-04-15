package ge.transitgeorgia.presentation.live_bus

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.mapbox.mapboxsdk.Mapbox
import dagger.hilt.android.AndroidEntryPoint
import ge.transitgeorgia.ui.theme.TbilisiPublicTransportTheme

@AndroidEntryPoint
class LiveBusActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Mapbox.getInstance(
            this,
            "pk.eyJ1IjoiZ2VvcmdlZ2lnYXVyaSIsImEiOiJjbGZhdTBqMGIydHRqM3ByMG00c2wyaGo2In0.rISsWHRrxsQRKfrrdkntRw"
        )

        setContent {
            TbilisiPublicTransportTheme() {
                LiveBusScreen()
            }
        }
    }
}