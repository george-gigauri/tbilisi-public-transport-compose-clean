package ge.tbilisipublictransport.presentation.bus_stops

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import ge.tbilisipublictransport.domain.model.BusStop
import ge.tbilisipublictransport.presentation.main.MainNavigationScreen
import ge.tbilisipublictransport.presentation.timetable.TimeTableActivity
import ge.tbilisipublictransport.ui.theme.DynamicPrimary
import ge.tbilisipublictransport.ui.theme.DynamicWhite
import java.text.DecimalFormat

@Composable
fun BusStopsScreen(
    navController: NavController,
    viewModel: BusStopsViewModel = hiltViewModel()
) {
    rememberSystemUiController().setStatusBarColor(DynamicPrimary)
    val stops by viewModel.result.collectAsState()

    val context = LocalContext.current

    Scaffold(
        topBar = {
            BusStopTopBar(
                onSearchKeywordChange = viewModel::search,
                onScanClick = { navController.navigate(MainNavigationScreen.Scanner.screenName) }
            )
        }
    ) {
        it.calculateBottomPadding()
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Spacer(modifier = Modifier.height(54.dp))
            LazyColumn {
                items(stops) {
                    ItemBusStop(context, it)
                }
            }
        }
    }
}

@Composable
fun ItemBusStop(
    context: Context,
    stop: BusStop,
    isDistance: Boolean = false,
    distance: Double = 1.0
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable {
                val intent = Intent(context, TimeTableActivity::class.java)
                intent.putExtra("stop_id", stop.code)
                context.startActivity(intent)
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = if (isDistance) {
                val isMoreThanKm = (distance / 1000).toInt() != 0
                val distanceInKms = (distance / 1000)
                if (isMoreThanKm) {
                    "${DecimalFormat("#.#").format(distanceInKms)}კმ"
                } else "${DecimalFormat("#.#").format(distance)}მ"
            } else "ID:${stop.code}",
            modifier = Modifier
                .defaultMinSize(85.dp)
                .border(2.dp, DynamicPrimary, RoundedCornerShape(8.dp))
                .padding(horizontal = 16.dp, vertical = 12.dp),
            color = DynamicWhite,
            fontWeight = FontWeight.SemiBold,
            fontSize = 15.sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = "${stop.name}",
            modifier = Modifier,
            color = DynamicWhite,
            fontWeight = FontWeight.Normal,
            fontSize = 15.sp
        )
    }
}