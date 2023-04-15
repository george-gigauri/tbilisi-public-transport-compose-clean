package ge.transitgeorgia.presentation.timetable

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import dagger.hilt.android.AndroidEntryPoint
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
}