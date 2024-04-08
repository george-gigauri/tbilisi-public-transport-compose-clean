package ge.transitgeorgia.module.presentation.screen.timetable

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.mapbox.mapboxsdk.Mapbox
import dagger.hilt.android.AndroidEntryPoint
import ge.transitgeorgia.common.analytics.Analytics
import ge.transitgeorgia.module.presentation.BuildConfig
import ge.transitgeorgia.module.presentation.screen.timetable.TimeTableScreen
import ge.transitgeorgia.ui.theme.TbilisiPublicTransportTheme

@AndroidEntryPoint
class TimeTableActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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