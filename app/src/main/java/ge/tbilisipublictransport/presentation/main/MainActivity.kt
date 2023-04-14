package ge.tbilisipublictransport.presentation.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.mapbox.mapboxsdk.Mapbox
import dagger.hilt.android.AndroidEntryPoint
import ge.tbilisipublictransport.common.util.AppLanguage
import ge.tbilisipublictransport.common.util.LocationUtil
import ge.tbilisipublictransport.common.util.QRScanner
import ge.tbilisipublictransport.data.local.datastore.AppDataStore
import ge.tbilisipublictransport.ui.theme.TbilisiPublicTransportTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val language = runBlocking { AppDataStore(this@MainActivity).language.first() }
        AppLanguage.updateLanguage(this, language.value)
        super.onCreate(savedInstanceState)

        QRScanner.init(this)

        if (!LocationUtil.isLocationTurnedOn(this)) {
            LocationUtil.requestLocation(this) { }
        }

        viewModel.load()

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