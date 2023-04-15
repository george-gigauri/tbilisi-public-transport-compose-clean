package ge.transitgeorgia.presentation.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.mapbox.mapboxsdk.Mapbox
import dagger.hilt.android.AndroidEntryPoint
import ge.transitgeorgia.common.analytics.Analytics
import ge.transitgeorgia.common.util.AppLanguage
import ge.transitgeorgia.common.util.LocationUtil
import ge.transitgeorgia.common.util.QRScanner
import ge.transitgeorgia.data.local.datastore.AppDataStore
import ge.transitgeorgia.ui.theme.TbilisiPublicTransportTheme
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
            Analytics.logLocationPrompt()
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

    override fun onStart() {
        super.onStart()
        Analytics.logAppLoaded()
    }
}