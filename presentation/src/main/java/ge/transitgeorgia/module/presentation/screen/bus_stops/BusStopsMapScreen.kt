package ge.transitgeorgia.module.presentation.screen.bus_stops

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.Rect
import android.location.Location
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import ge.transitgeorgia.common.analytics.Analytics
import ge.transitgeorgia.common.util.dpToPx
import ge.transitgeorgia.module.common.util.DrawableUtil.resize
import ge.transitgeorgia.module.common.util.LocationUtil
import ge.transitgeorgia.module.presentation.R
import ge.transitgeorgia.module.presentation.screen.main.MainActivity
import ge.transitgeorgia.module.presentation.screen.timetable.TimeTableActivity
import ge.transitgeorgia.module.presentation.util.ComposableLifecycle
import kotlinx.coroutines.flow.MutableStateFlow
import org.osmdroid.api.IMapController
import org.osmdroid.bonuspack.clustering.MarkerClusterer
import org.osmdroid.bonuspack.clustering.RadiusMarkerClusterer
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.TilesOverlay
import org.osmdroid.views.overlay.gestures.OneFingerZoomOverlay
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay
import org.osmdroid.views.overlay.mylocation.DirectedLocationOverlay


@SuppressLint("MissingPermission")
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun BusStopsMapScreen(
    viewModel: BusStopsViewModel = hiltViewModel()
) {

    val context = LocalContext.current
    val currentActivity = (context as? MainActivity)
    val lifecycleEvent = remember { MutableStateFlow(Lifecycle.Event.ON_ANY) }

    var mapController: IMapController? = remember { null }
    val markerCluster: MarkerClusterer = remember {
        RadiusMarkerClusterer(context).apply {
            setRadius(40.dpToPx())
            setAnimation(true)
        }
    }

    val stops by viewModel.stops.collectAsStateWithLifecycle()
    val city by viewModel.city.collectAsStateWithLifecycle()

    val userLocation = remember { MutableStateFlow(Location(null)) }

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
                // Add Markers
                stops.forEach { s ->
                    val marker = Marker(it)
                    marker.setPosition(GeoPoint(s.lat, s.lng))
                    marker.setAnchor(Marker.ANCHOR_BOTTOM, Marker.ANCHOR_BOTTOM)
                    marker.icon = ContextCompat.getDrawable(
                        context,
                        R.drawable.ic_marker_bust_stop
                    )?.resize(context, 10.dpToPx(), 14.dpToPx())
                    marker.setOnMarkerClickListener { _, _ ->
                        val intent = Intent(context, TimeTableActivity::class.java)
                        intent.putExtra("stop_id", s.code)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        context.startActivity(intent)
                        Analytics.logOpenTimetableFromRouteMap()
                        return@setOnMarkerClickListener true
                    }
                    markerCluster.add(marker)
                }
                it.overlays.add(markerCluster)
            },
            factory = {
                org.osmdroid.views.MapView(it).apply {
                    this.setTileSource(TileSourceFactory.MAPNIK)
                    this.getLocalVisibleRect(Rect())
                    this.setMultiTouchControls(true)


                    // Adjust the alpha of the TilesOverlay to dim the map
                    val tilesOverlay: TilesOverlay = overlayManager.tilesOverlay
                    tilesOverlay.setColorFilter(
                        PorterDuffColorFilter(
                            Color(0x1A000000).toArgb(),
                            PorterDuff.Mode.DARKEN
                        )
                    )

                    mapController = this.controller
                    mapController?.animateTo(
                        GeoPoint(
                            city.lat,
                            city.lng
                        )
                    )
                    mapController?.setZoom(city.mapDefaultZoom)

                    this.overlays.add(OneFingerZoomOverlay())
                    this.overlays.add(RotationGestureOverlay(this))
                    this.overlays.add(DirectedLocationOverlay(context))

                    this.addMapListener(object : MapListener {
                        override fun onScroll(event: ScrollEvent?): Boolean {
                            return true
                        }

                        override fun onZoom(event: ZoomEvent?): Boolean {
                            return true
                        }
                    })
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
                        mapController?.animateTo(GeoPoint(it.latitude, it.longitude), 18.5, 1200)
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
        }
    }
}
