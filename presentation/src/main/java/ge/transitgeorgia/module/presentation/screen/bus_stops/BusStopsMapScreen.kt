package ge.transitgeorgia.presentation.bus_stops

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Matrix
import android.location.Location
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
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
import com.mapbox.mapboxsdk.utils.BitmapUtils
import ge.transitgeorgia.common.analytics.Analytics
import ge.transitgeorgia.common.util.dpToPx
import ge.transitgeorgia.module.common.util.LocationUtil
import ge.transitgeorgia.module.common.util.MapStyle
import ge.transitgeorgia.module.common.util.style
import ge.transitgeorgia.module.presentation.R
import ge.transitgeorgia.module.presentation.screen.bus_stops.BusStopsViewModel
import ge.transitgeorgia.module.presentation.screen.main.MainActivity
import ge.transitgeorgia.module.presentation.util.ComposableLifecycle
import ge.transitgeorgia.presentation.timetable.TimeTableActivity
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
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
    val scope = rememberCoroutineScope()
    val lifecycleEvent = remember { MutableStateFlow(Lifecycle.Event.ON_ANY) }
    var map: MapboxMap? = remember { null }
    val mapZoomScope = rememberCoroutineScope()

    val stops by viewModel.stops.collectAsStateWithLifecycle()
    val city by viewModel.city.collectAsStateWithLifecycle()

    val isDarkMode = isSystemInDarkTheme()
    var mapStyle by rememberSaveable { mutableStateOf(if (isDarkMode) MapStyle.BUSPLORE_DARK else MapStyle.BUSPLORE_LIGHT) }

    var userLocation = remember { MutableStateFlow(Location(null)) }

    val locationPermissionState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        )
    )

    ComposableLifecycle(onEvent = { s, event ->
        lifecycleEvent.value = event
    })

    LaunchedEffect(key1 = Unit, key2 = locationPermissionState.allPermissionsGranted) {
        if (locationPermissionState.allPermissionsGranted) {
            LocationUtil.getMyLocation(context, onSuccess = {
                userLocation.value = it
            }, onError = {

            })
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            update = {

            },
            factory = {
                MapView(it).apply {
                    getMapAsync { m ->
                        m.animateCamera(
                            CameraUpdateFactory.newLatLngZoom(
                                city.latLng,
                                city.mapDefaultZoom
                            )
                        )

                        m.setStyle(mapStyle.style(it)) {
                            if (locationPermissionState.allPermissionsGranted) {
                                currentActivity?.let { c ->
                                    if (LocationUtil.isLocationTurnedOn(c)) {
                                        m.locationComponent.activateLocationComponent(
                                            LocationComponentActivationOptions
                                                .builder(context, it)
                                                .build()
                                        )
                                    } else {
                                        LocationUtil.requestLocation(c) {
                                            m.locationComponent.activateLocationComponent(
                                                LocationComponentActivationOptions
                                                    .builder(context, it)
                                                    .build()
                                            )
                                        }
                                    }
                                }

                                scope.launch {
                                    userLocation.collectLatest {
                                        m.markers.filter { m -> m.snippet == "MY" }
                                            .forEach { r -> m.removeMarker(r) }
                                        m.addMarker(createMyLocationMarker(context, it))
                                    }
                                }
                            } else {
                                locationPermissionState.launchMultiplePermissionRequest()
                            }
                        }

                        m.setOnMarkerClickListener { m ->
                            val intent = Intent(context, TimeTableActivity::class.java)
                            intent.putExtra("stop_id", m.snippet)
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
                                    m.markers.filter { it.snippet != "MY" }.forEach { it.remove() }

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
                                    m.markers.filter { i -> i.snippet != "MY" }.forEach { it.remove() }
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
        Column(modifier = Modifier.align(Alignment.BottomEnd)) {
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
                    .padding(12.dp),
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_gps),
                    contentDescription = null,
                    tint = colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            }

            // Change Map Style
            FilledTonalIconButton(
                onClick = {
                    mapStyle = when (mapStyle) {
                        MapStyle.BUSPLORE_LIGHT -> MapStyle.STANDARD_LIGHT
                        MapStyle.STANDARD_LIGHT -> MapStyle.TERRAIN
                        MapStyle.TERRAIN -> MapStyle.BUSPLORE_DARK
                        MapStyle.BUSPLORE_DARK -> MapStyle.BUSPLORE_LIGHT
                        else -> MapStyle.TERRAIN
                    }
                    map?.setStyle(mapStyle.style(context))
                },
                colors = IconButtonDefaults.filledIconButtonColors(containerColor = colorScheme.primaryContainer),
                modifier = Modifier
                    .size(85.dp)
                    .padding(12.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_map),
                    contentDescription = null,
                    tint = colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

fun createMyLocationMarker(context: Context, l: Location): MarkerOptions {
    return MarkerOptions().apply {
        position(LatLng(l.latitude, l.longitude))
        snippet("MY")
        BitmapUtils.getBitmapFromDrawable(
            ContextCompat.getDrawable(
                context,
                R.drawable.ic_my_location_marker
            )
        )?.let { bit ->
            val matrix = Matrix()
            matrix.postRotate(l.bearing)
            val smallMarker = Bitmap.createScaledBitmap(
                bit,
                24.dpToPx(),
                40.dpToPx(),
                false
            )
            val rotatedBitmap = Bitmap.createBitmap(
                smallMarker,
                0,
                0,
                smallMarker.width,
                smallMarker.height,
                matrix,
                true
            )
            val smallMarkerIcon = IconFactory.getInstance(context)
                .fromBitmap(rotatedBitmap)

            icon(smallMarkerIcon)
        }
    }
}