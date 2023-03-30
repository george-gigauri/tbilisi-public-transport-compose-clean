package ge.tbilisipublictransport.presentation.live_bus

import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mapbox.mapboxsdk.annotations.IconFactory
import com.mapbox.mapboxsdk.annotations.MarkerOptions
import com.mapbox.mapboxsdk.annotations.PolylineOptions
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.geometry.LatLngBounds
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.utils.BitmapUtils
import ge.tbilisipublictransport.R
import ge.tbilisipublictransport.common.util.ComposableLifecycle
import ge.tbilisipublictransport.domain.model.RouteStop
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


@Composable
fun LiveBusScreen(
    viewModel: LiveBusViewModel = hiltViewModel()
) {
    val currentActivity = (LocalContext.current as? LiveBusActivity)
    val lifecycleCoroutine = rememberCoroutineScope()
    val mapZoomScope = rememberCoroutineScope()
    val route1Scope = rememberCoroutineScope()
    val route2Scope = rememberCoroutineScope()
    val availableBusesScope = rememberCoroutineScope()
    val lifecycleEvent = remember { MutableStateFlow(Lifecycle.Event.ON_CREATE) }
    val isDarkMode = isSystemInDarkTheme()
    val context = LocalContext.current

    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val errorMessage by viewModel.error.collectAsStateWithLifecycle()

    ComposableLifecycle(onEvent = { s, event ->
        lifecycleEvent.value = event
    })

    Box(modifier = Modifier.fillMaxSize()) {
        if (isLoading && errorMessage.isNullOrEmpty()) {
            LinearProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else if (!isLoading && !errorMessage.isNullOrEmpty()) {
            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
            currentActivity?.finish()
        } else {
            AndroidView(factory = {
                MapView(it).apply {
                    getMapAsync { map ->
                        map.setStyle(if (isDarkMode) Style.DARK else Style.LIGHT)

                        map.addOnCameraMoveListener {
                            mapZoomScope.coroutineContext.cancelChildren()
                            mapZoomScope.launch {
                                delay(350)
                                if (map.cameraPosition.zoom >= 13.5) {
                                    map.markers.filter { it.snippet == "stop" }
                                        .forEach { it.remove() }
                                    val route1Stops = viewModel.route1.value.stops
                                    val route2Stops = viewModel.route2.value.stops
                                    val visibleBounds = map.projection.visibleRegion.latLngBounds
                                    val visibleStops = arrayListOf<RouteStop>().apply {
                                        addAll(
                                            route1Stops.filter {
                                                val latLng = LatLng(it.lat, it.lng)
                                                visibleBounds.contains(latLng)
                                            }
                                        )

                                        addAll(
                                            route2Stops.filter {
                                                val latLng = LatLng(it.lat, it.lng)
                                                visibleBounds.contains(latLng)
                                            }
                                        )
                                    }

                                    visibleStops.forEach {
                                        MarkerOptions().apply {
                                            position(LatLng(it.lat, it.lng))
                                            snippet("stop")
                                            BitmapUtils.getBitmapFromDrawable(
                                                ContextCompat.getDrawable(
                                                    context,
                                                    if (it.isForward) R.drawable.ic_marker_route_stop_forward else R.drawable.ic_marker_route_stop_backward
                                                )
                                            )?.let { bit ->
                                                val smallMarker =
                                                    Bitmap.createScaledBitmap(bit, 50, 50, false)
                                                val smallMarkerIcon =
                                                    IconFactory.getInstance(context)
                                                        .fromBitmap(smallMarker)

                                                icon(smallMarkerIcon)
                                            }

                                            map.addMarker(`this`)
                                        }
                                    }

                                } else {
                                    map.markers.filter { it.snippet == "stop" }
                                        .forEach { it.remove() }
                                }
                            }.start()
                        }

                        route1Scope.launch {
                            viewModel.route1.collectLatest { ri ->
                                map.addPolyline(
                                    PolylineOptions().apply {
                                        this.color(Color.Green.toArgb())
                                        this.width(5f)
                                        addAll(ri.polyline)
                                    }
                                )

                                ri.stops.firstOrNull()?.let {
                                    map.addMarker(MarkerOptions().position(LatLng(it.lat, it.lng)))
                                }

                                ri.stops.lastOrNull()?.let {
                                    map.addMarker(MarkerOptions().position(LatLng(it.lat, it.lng)))
                                }

                                val firstStop = ri.stops.firstOrNull()
                                val lastStop = ri.stops.lastOrNull()
                                val firstLatLng =
                                    LatLng(firstStop?.lat ?: 0.0, firstStop?.lng ?: 0.0)
                                val lastLatLng = LatLng(lastStop?.lat ?: 0.0, lastStop?.lng ?: 0.0)
                                val latLngBounds = LatLngBounds.Builder()
                                    .include(firstLatLng)
                                    .include(lastLatLng)
                                    .build()

                                map.animateCamera(
                                    CameraUpdateFactory.newLatLngZoom(latLngBounds.center, 11.15)
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

                                ri.stops.firstOrNull()?.let {
                                    map.addMarker(MarkerOptions().position(LatLng(it.lat, it.lng)))
                                }

                                ri.stops.lastOrNull()?.let {
                                    map.addMarker(MarkerOptions().position(LatLng(it.lat, it.lng)))
                                }
                            }
                        }

                        availableBusesScope.launch {
                            viewModel.availableBuses.collectLatest { buses ->

                                map.markers.filter { it.snippet == "bus" }.forEach {
                                    it.remove()
                                }

                                map.addMarkers(
                                    buses.map { b ->
                                        MarkerOptions().apply {
                                            BitmapUtils.getBitmapFromDrawable(
                                                ContextCompat.getDrawable(
                                                    context,
                                                    if (b.isForward) R.drawable.ic_marker_bus_forward else R.drawable.ic_marker_bus_backwards
                                                )
                                            )?.let { bit ->
                                                val smallMarker =
                                                    Bitmap.createScaledBitmap(bit, 85, 85, false)
                                                val smallMarkerIcon =
                                                    IconFactory.getInstance(context)
                                                        .fromBitmap(smallMarker)

                                                icon(smallMarkerIcon)
                                                snippet("bus")
                                            }

                                            position(LatLng(b.lat, b.lng))
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
        }

        Icon(
            painter = painterResource(id = R.drawable.ic_arrow_square_left),
            contentDescription = null,
            modifier = Modifier
                .padding(8.dp)
                .size(54.dp)
                .padding(8.dp)
                .clickable { currentActivity?.finish() }
                .align(Alignment.TopStart)
        )
    }
}