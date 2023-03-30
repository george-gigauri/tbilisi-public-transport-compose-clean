package ge.tbilisipublictransport.presentation.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.mapbox.mapboxsdk.Mapbox
import dagger.hilt.android.AndroidEntryPoint
import ge.tbilisipublictransport.ui.theme.TbilisiPublicTransportTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Mapbox.getInstance(
            this,
            "pk.eyJ1IjoiZ2VvcmdlZ2lnYXVyaSIsImEiOiJjbGZhdTBqMGIydHRqM3ByMG00c2wyaGo2In0.rISsWHRrxsQRKfrrdkntRw"
        )

        setContent {
            TbilisiPublicTransportTheme {
                MainScreenContent()
            }
        }
    }
}