package ge.transitgeorgia.presentation.bus_stops

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.mapbox.mapboxsdk.annotations.IconFactory
import com.mapbox.mapboxsdk.annotations.MarkerOptions
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.utils.BitmapUtils
import ge.transitgeorgia.R
import ge.transitgeorgia.common.analytics.Analytics
import ge.transitgeorgia.common.util.ComposableLifecycle
import ge.transitgeorgia.common.util.LocationUtil
import ge.transitgeorgia.common.util.dpToPx
import ge.transitgeorgia.presentation.main.MainActivity
import ge.transitgeorgia.presentation.timetable.TimeTableActivity
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

@SuppressLint("MissingPermission")
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun BusStopsMapScreen(
    viewModel: BusStopsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val currentActivity = (context as? MainActivity)
    val lifecycleCoroutine = rememberCoroutineScope()
    val lifecycleEvent = remember { MutableStateFlow(Lifecycle.Event.ON_ANY) }
    var map: MapboxMap? = remember { null }
    val mapZoomScope = rememberCoroutineScope()

    val stops by viewModel.stops.collectAsStateWithLifecycle()
    val city by viewModel.city.collectAsStateWithLifecycle()

    val isDarkMode = isSystemInDarkTheme()
    var mapStyle by rememberSaveable { mutableStateOf(if (isDarkMode) Style.DARK else Style.LIGHT) }

    var userLocation by remember { mutableStateOf(LatLng(0.0, 0.0)) }

    val locationPermissionState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        )
    )

    ComposableLifecycle(onEvent = { s, event ->
        lifecycleEvent.value = event
    })

    LaunchedEffect(key1 = Unit) {
        if (locationPermissionState.allPermissionsGranted) {
            LocationUtil.getMyLocation(context, onSuccess = {
                userLocation = LatLng(it.latitude, it.longitude)
            }, onError = {

            })
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(factory = {
            MapView(it).apply {
                getMapAsync { m ->
                    m.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            city.latLng,
                            city.mapDefaultZoom
                        )
                    )
                    m.setStyle(mapStyle) {
                        if (locationPermissionState.allPermissionsGranted) {
                            currentActivity?.let { c ->
                                if (LocationUtil.isLocationTurnedOn(c)) {
                                    m.locationComponent.activateLocationComponent(
                                        LocationComponentActivationOptions
                                            .builder(context, it)
                                            .build()
                                    )
                                    m.locationComponent.isLocationComponentEnabled =
                                        true
                                } else {
                                    LocationUtil.requestLocation(c) {
                                        m.locationComponent.activateLocationComponent(
                                            LocationComponentActivationOptions
                                                .builder(context, it)
                                                .build()
                                        )
                                        m.locationComponent.isLocationComponentEnabled =
                                            true
                                    }
                                }
                            }

                            LocationUtil.getMyLocation(context, onSuccess = { l ->
                                userLocation = LatLng(l.latitude, l.longitude)
                            })
                        } else {
                            locationPermissionState.launchMultiplePermissionRequest()
                        }
                    }

                    m.setOnMarkerClickListener {
                        val intent = Intent(context, TimeTableActivity::class.java)
                        intent.putExtra("stop_id", it.snippet)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        context.startActivity(intent)

                        Analytics.logOpenTimetableFromRouteMap()
                        return@setOnMarkerClickListener true
                    }

                    m.addOnCameraMoveListener {
                        mapZoomScope.coroutineContext.cancelChildren()
                        mapZoomScope.launch {
                            delay(350)
                            if (m.cameraPosition.zoom >= 12.5) {
                                m.markers.forEach { it.remove() }

                                val visibleBounds =
                                    m.projection.visibleRegion.latLngBounds
                                val visibleStops = stops.filter {
                                    val latLng = LatLng(it.lat, it.lng)
                                    visibleBounds.contains(latLng)
                                }

                                visibleStops.forEach {
                                    MarkerOptions().apply {
                                        position(LatLng(it.lat, it.lng))
                                        snippet(it.code)
                                        BitmapUtils.getBitmapFromDrawable(
                                            ContextCompat.getDrawable(
                                                context,
                                                R.drawable.ic_marker_route_stop_backward
                                            )
                                        )?.let { bit ->
                                            val smallMarker =
                                                Bitmap.createScaledBitmap(
                                                    bit,
                                                    24.dpToPx(),
                                                    24.dpToPx(),
                                                    false
                                                )
                                            val smallMarkerIcon =
                                                IconFactory.getInstance(context)
                                                    .fromBitmap(smallMarker)

                                            icon(smallMarkerIcon)
                                        }

                                        m.addMarker(`this`)
                                    }
                                }

                            } else {
                                m.markers.forEach { it.remove() }
                            }
                        }.start()
                    }

                    map = m
                }

                lifecycleCoroutine.launch {
                    lifecycleEvent.collect {
                        when (it) {
                            Lifecycle.Event.ON_CREATE -> onCreate(null)
                            Lifecycle.Event.ON_START -> onStart()
                            Lifecycle.Event.ON_RESUME -> onResume()
                            Lifecycle.Event.ON_DESTROY -> onDestroy()
                            else -> Unit
                        }
                    }
                }
            }
        })

        // Button Move Zoom to user location
        FilledTonalIconButton(
            onClick = {
                if (!LocationUtil.isLocationTurnedOn(context)) {
                    LocationUtil.requestLocation(currentActivity!!) {
                        LocationUtil.requestLocation(context) {}
                    }
                }

                LocationUtil.getLastKnownLocation(context)?.let {
                    val latLng = LatLng(it.latitude, it.longitude)
                    map?.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16.0), 1500)
                }
            },
            modifier = Modifier
                .size(85.dp)
                .align(Alignment.BottomEnd)
                .padding(12.dp),
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_gps),
                contentDescription = null,
                tint = colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}