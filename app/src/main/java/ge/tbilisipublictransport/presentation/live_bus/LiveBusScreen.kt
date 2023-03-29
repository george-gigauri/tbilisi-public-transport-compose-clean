package ge.tbilisipublictransport.presentation.live_bus

import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import com.mapbox.mapboxsdk.annotations.MarkerOptions
import com.mapbox.mapboxsdk.annotations.PolylineOptions
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.Style
import ge.tbilisipublictransport.R
import ge.tbilisipublictransport.common.util.ComposableLifecycle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@Composable
fun LiveBusScreen(
    viewModel: LiveBusViewModel = hiltViewModel()
) {
    val currentActivity = (LocalContext.current as? LiveBusActivity)
    val lifecycleCoroutine = rememberCoroutineScope()
    val route1Scope = rememberCoroutineScope()
    val route2Scope = rememberCoroutineScope()
    val lifecycleEvent = remember { MutableStateFlow(Lifecycle.Event.ON_CREATE) }
    val isDarkMode = isSystemInDarkTheme()
    val context = LocalContext.current

    ComposableLifecycle(onEvent = { s, event ->
        lifecycleEvent.value = event
    })

    Box() {
        AndroidView(factory = {
            MapView(it).apply {
                getMapAsync { map ->
                    map.setStyle(if (isDarkMode) Style.DARK else Style.LIGHT)

                    route1Scope.launch {
                        viewModel.route1.collectLatest { ri ->
                            map.addPolyline(
                                PolylineOptions().apply {
                                    this.color(Color.Green.toArgb())
                                    this.width(5f)
                                    addAll(ri.polyline)
                                }
                            )

                            map.addMarkers(
                                ri.stops.map {
                                    MarkerOptions().apply {
                                        this.position(LatLng(it.lat, it.lng))
                                    }
                                }
                            )
                        }
                    }

                    route2Scope.launch {
                        viewModel.route2.collectLatest { ri ->
                            map.addPolyline(
                                PolylineOptions().apply {
                                    this.color(Color.Red.toArgb())
                                    this.width(5f)
                                    addAll(ri.polyline)
                                }
                            )

                            map.addMarkers(
                                ri.stops.map {
                                    MarkerOptions().apply {
                                        this.position(LatLng(it.lat, it.lng))
                                    }
                                }
                            )
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
        })

        Icon(
            painter = painterResource(id = R.drawable.ic_arrow_square_left),
            contentDescription = null,
            modifier = Modifier
                .padding(12.dp)
                .size(54.dp)
                .clickable { currentActivity?.finish() }
                .align(Alignment.TopStart)
        )
    }
}