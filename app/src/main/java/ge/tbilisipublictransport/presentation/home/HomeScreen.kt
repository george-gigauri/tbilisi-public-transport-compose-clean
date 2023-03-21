package ge.tbilisipublictransport.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.mapbox.mapboxsdk.annotations.IconFactory
import com.mapbox.mapboxsdk.annotations.MarkerOptions
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.Style
import ge.tbilisipublictransport.domain.model.BusStop

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview(showBackground = true, showSystemUi = true)
fun HomeScreen(viewModel: HomeViewModel = hiltViewModel()) {

    Scaffold(topBar = {
        TopBar()
    }) {
        Map(viewModel)
        it.calculateBottomPadding()
    }
}

@Composable
fun Map(viewModel: HomeViewModel) {
    val isDarkMode = isSystemInDarkTheme()
    val context = LocalContext.current
    val stops: List<BusStop> = viewModel.busStops

    AndroidView(factory = {
        MapView(it).apply {
            getMapAsync { map ->
                map.setStyle(if (isDarkMode) Style.DARK else Style.LIGHT)
                map.addMarkers(
                    stops.map { s ->
                        MarkerOptions().apply {
                            position(LatLng(s.lat, s.lng))
                            icon(IconFactory.getInstance(context).defaultMarker())
                        }
                    }
                )
            }
        }
    }, modifier = Modifier.fillMaxSize())
}

@Composable
fun TopBar() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .background(colorScheme.surfaceColorAtElevation(3.dp))
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "მთავარი",
            color = if (isSystemInDarkTheme()) Color.White else Color.DarkGray,
            //     fontFamily = FontFamily(Font(R.font.bpg_nino_mtavruli_bold)),
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
            modifier = Modifier.align(Alignment.CenterStart)
        )
    }
}