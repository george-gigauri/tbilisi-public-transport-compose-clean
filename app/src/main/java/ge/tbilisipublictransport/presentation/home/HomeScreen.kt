package ge.tbilisipublictransport.presentation.home

import android.content.Context
import androidx.annotation.DrawableRes
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
import androidx.lifecycle.Lifecycle
import com.mapbox.mapboxsdk.annotations.MarkerOptions
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.expressions.Expression.*
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.*
import ge.tbilisipublictransport.R
import ge.tbilisipublictransport.common.util.ComposableLifecycle
import ge.tbilisipublictransport.domain.model.BusStop
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview(showBackground = true, showSystemUi = true)
fun HomeScreen(viewModel: HomeViewModel = hiltViewModel()) {

    Scaffold(topBar = {
        TopBar()
    }) { _ ->
        Map(viewModel)
    }
}

@Composable
fun Map(viewModel: HomeViewModel) {
    val isDarkMode = isSystemInDarkTheme()
    val context = LocalContext.current
    val lifecycleEvent = remember { MutableStateFlow(Lifecycle.Event.ON_CREATE) }
    val stops: StateFlow<List<BusStop>> = viewModel.busStops
    val coroutine = rememberCoroutineScope()
    val lifecycleCoroutine = rememberCoroutineScope()
    var lastZoom = remember { 0.0 }

    ComposableLifecycle(onEvent = { s, event ->
        lifecycleEvent.value = event
    })

    AndroidView(factory = {
        MapView(it).apply {
            getMapAsync { map ->
                map.setStyle(if (isDarkMode) Style.DARK else Style.LIGHT) {
                    map.uiSettings.isLogoEnabled = true
                    addClusteredGeoJsonSource(context, it)
                }

                map.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(41.716667, 44.783333),
                        10.5
                    )
                )

                map.setOnMarkerClickListener {
                    return@setOnMarkerClickListener true
                }

                coroutine.launch {
                    stops.collectLatest { s ->
                        s.forEach { i ->
                            val marker = marker(context, 85, 85, i.lat, i.lng)
                            map.addMarker(marker)
                        }
                    }
                }
            }

            lifecycleCoroutine.launch {
                lifecycleEvent.collectLatest {
                    when (it) {
                        Lifecycle.Event.ON_CREATE -> onCreate(null)
                        Lifecycle.Event.ON_START -> onStart()
                        Lifecycle.Event.ON_RESUME -> onResume()
                        Lifecycle.Event.ON_PAUSE -> onPause()
                        Lifecycle.Event.ON_STOP -> onStop()
                        Lifecycle.Event.ON_DESTROY -> onDestroy()
                        Lifecycle.Event.ON_ANY -> onLowMemory()
                    }
                }
            }
        }
    }, modifier = Modifier.fillMaxSize())
}

fun marker(
    context: Context,
    width: Int,
    height: Int,
    lat: Double,
    lng: Double,
    @DrawableRes iconRes: Int = R.drawable.marker_bus_stop
): MarkerOptions {
    return MarkerOptions().apply {
        //    val b = BitmapFactory.decodeResource(context.resources, iconRes)
        //      val smallMarker = Bitmap.createScaledBitmap(b, width, height, false)
        //       val smallMarkerIcon = IconFactory.getInstance(context).fromBitmap(smallMarker)

        this.position(LatLng(lat, lng))
        //  this.icon(smallMarkerIcon)
    }
}


private fun addClusteredGeoJsonSource(context: Context, loadedMapStyle: Style) {

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