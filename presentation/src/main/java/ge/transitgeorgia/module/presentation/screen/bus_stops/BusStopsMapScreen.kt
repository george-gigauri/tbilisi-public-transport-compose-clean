package ge.transitgeorgia.module.presentation.screen.bus_stops

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import ge.transitgeorgia.common.analytics.Analytics
import ge.transitgeorgia.common.util.dpToPx
import ge.transitgeorgia.module.common.util.LocationUtil
import ge.transitgeorgia.module.presentation.R
import ge.transitgeorgia.module.presentation.screen.main.MainActivity
import ge.transitgeorgia.module.presentation.util.ComposableLifecycle
import ge.transitgeorgia.module.presentation.screen.timetable.TimeTableActivity
import ge.transitgeorgia.module.presentation.theme.AppColor
import kotlinx.coroutines.flow.MutableStateFlow
import org.osmdroid.api.IMapController
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.TilesOverlay
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import org.osmdroid.views.overlay.simplefastpoint.SimpleFastPointOverlay
import org.osmdroid.views.overlay.simplefastpoint.SimpleFastPointOverlayOptions
import org.osmdroid.views.overlay.simplefastpoint.SimplePointTheme
import org.osmdroid.views.overlay.simplefastpoint.StyledLabelledGeoPoint

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
                stops.map { s ->
                    StyledLabelledGeoPoint(s.lat, s.lng, 0.0, s.name)
                }.let { markers ->
                    SimplePointTheme(markers, true, true)
                }.let { pt ->
                    val textStyle = Paint()
                    textStyle.style = Paint.Style.FILL_AND_STROKE
                    textStyle.strokeWidth = 1.2f
                    textStyle.color = androidx.compose.ui.graphics.Color(0xFF000000).toArgb()
                    textStyle.textSize = 32f

                    val pointStyle = Paint()
                    pointStyle.style = Paint.Style.FILL_AND_STROKE
                    pointStyle.color = AppColor.POLYLINE_RED.toArgb()
                    pointStyle.strokeWidth = 14.dpToPx().toFloat()

                    val opt = SimpleFastPointOverlayOptions()
                        .setSymbol(SimpleFastPointOverlayOptions.Shape.CIRCLE)
                        .setMinZoomShowLabels(7)
                        .setMaxNShownLabels(5)
                        .setPointStyle(pointStyle)
                        .setLabelPolicy(SimpleFastPointOverlayOptions.LabelPolicy.DENSITY_THRESHOLD)
                        .setAlgorithm(SimpleFastPointOverlayOptions.RenderingAlgorithm.MAXIMUM_OPTIMIZATION)
                        .setRadius(30f)
                        .setIsClickable(true)
                        .setCellSize(150)
                        .setTextStyle(textStyle)

                    val sfpo = SimpleFastPointOverlay(pt, opt)

                    sfpo.setOnClickListener { points, point ->
                        val intent = Intent(context, TimeTableActivity::class.java)
                        intent.putExtra("stop_id", stops[point].code)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        context.startActivity(intent)

                        Analytics.logOpenTimetableFromRouteMap()
                    }
                    it.overlays.add(sfpo)
                }
            },
            factory = {
                org.osmdroid.views.MapView(it).apply {
                    this.setTileSource(TileSourceFactory.MAPNIK)
                    this.getLocalVisibleRect(Rect())
                    this.setMultiTouchControls(true)

                    mapController = this.controller
                    mapController?.animateTo(
                        GeoPoint(
                            city.lat,
                            city.lng
                        )
                    )
                    mapController?.setZoom(city.mapDefaultZoom)

                    MyLocationNewOverlay(GpsMyLocationProvider(it), this).apply {
                        this.enableMyLocation()
                        this.setDirectionIcon(
                            ContextCompat.getDrawable(context, R.drawable.marker_my_location)
                                ?.toBitmap(26.dpToPx(), 44.dpToPx())
                        )
                        this.setDirectionAnchor(.5f, .5f)
                        this.isDrawAccuracyEnabled = false
                    }.also { o ->
                        this.overlays.add(o)
                    }

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
