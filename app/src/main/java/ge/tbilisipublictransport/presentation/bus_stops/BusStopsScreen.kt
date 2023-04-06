package ge.tbilisipublictransport.presentation.bus_stops

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import ge.tbilisipublictransport.domain.model.BusStop
import ge.tbilisipublictransport.ui.theme.DynamicPrimary
import ge.tbilisipublictransport.ui.theme.DynamicWhite

@Composable
@Preview
fun BusStopsScreen(
    viewModel: BusStopsViewModel = hiltViewModel()
) {
    rememberSystemUiController().setStatusBarColor(DynamicPrimary)
    var stopSearchKeyword by rememberSaveable { mutableStateOf("") }
    val stops by viewModel.result.collectAsState()

    Scaffold(
        topBar = { BusStopTopBar { viewModel.search(it) } }
    ) {
        it.calculateBottomPadding()
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Spacer(modifier = Modifier.height(54.dp))
            LazyColumn {
                items(stops) {
                    ItemBusStop(it)
                }
            }
        }
    }
}

@Composable
fun ItemBusStop(stop: BusStop) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "ID:${stop.id.replace("1:", "")}",
            modifier = Modifier
                .border(2.dp, DynamicPrimary, RoundedCornerShape(8.dp))
                .padding(horizontal = 16.dp, vertical = 12.dp),
            color = DynamicWhite,
            fontWeight = FontWeight.SemiBold,
            fontSize = 15.sp
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