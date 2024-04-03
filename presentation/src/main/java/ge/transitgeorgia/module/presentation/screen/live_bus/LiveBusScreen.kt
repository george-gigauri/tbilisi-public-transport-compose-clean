package ge.transitgeorgia.module.presentation.screen.live_bus

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.location.Location
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
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
import androidx.lifecycle.Observer
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.work.WorkInfo
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.rememberPermissionState
import com.mapbox.mapboxsdk.annotations.Icon
import com.mapbox.mapboxsdk.annotations.IconFactory
import com.mapbox.mapboxsdk.annotations.MarkerOptions
import com.mapbox.mapboxsdk.annotations.PolylineOptions
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.geometry.LatLngBounds
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.utils.BitmapUtils
import ge.transitgeorgia.common.analytics.Analytics
import ge.transitgeorgia.common.service.worker.BusDistanceReminderWorker
import ge.transitgeorgia.common.util.dpToPx
import ge.transitgeorgia.domain.model.RouteStop
import ge.transitgeorgia.module.common.util.BitmapUtil
import ge.transitgeorgia.module.common.util.LocationUtil
import ge.transitgeorgia.module.common.util.MapStyle
import ge.transitgeorgia.module.common.util.style
import ge.transitgeorgia.module.presentation.R
import ge.transitgeorgia.module.presentation.util.ComposableLifecycle
import ge.transitgeorgia.module.presentation.util.asMessage
import ge.transitgeorgia.presentation.bus_stops.createMyLocationMarker
import ge.transitgeorgia.presentation.live_bus.LiveBusScheduleNotificationDialog
import ge.transitgeorgia.presentation.live_bus.LiveBusTopBar
import ge.transitgeorgia.presentation.schedule.ScheduleActivity
import ge.transitgeorgia.presentation.timetable.TimeTableActivity
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@SuppressLint("MissingPermission")
@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun LiveBusScreen(
    viewModel: LiveBusViewModel = hiltViewModel()
) {
    val currentActivity = (LocalContext.current as? LiveBusActivity)
    val lifecycleCoroutine = rememberCoroutineScope()
    val locationScope = rememberCoroutineScope()
    val mapZoomScope = rememberCoroutineScope()
    val route1Scope = rememberCoroutineScope()
    val route2Scope = rememberCoroutineScope()
    val availableBusesScope = rememberCoroutineScope()
    val lifecycleEvent = remember { MutableStateFlow(Lifecycle.Event.ON_ANY) }
    val isDarkMode = isSystemInDarkTheme()
    val context = LocalContext.current
    var mbMap: MapboxMap? = remember { null }

    val route by viewModel.route.collectAsStateWithLifecycle()
    val route1 by viewModel.route1.collectAsStateWithLifecycle()
    val route2 by viewModel.route2.collectAsStateWithLifecycle()
    val availableBuses by viewModel.availableBuses.collectAsStateWithLifecycle()

    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val errorMessage by viewModel.error.collectAsStateWithLifecycle(null)

    var isReminderRunning by rememberSaveable { mutableStateOf(false) }

    val notificationsPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        rememberPermissionState(permission = Manifest.permission.POST_NOTIFICATIONS)
    } else null

    val locationPermissionState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        )
    )

    val infoBottomSheetState = rememberModalBottomSheetState()
    val infoBottomSheetScope = rememberCoroutineScope()

    var isNotifyMeDialogVisible by rememberSaveable { mutableStateOf(false) }

    var mapStyle by rememberSaveable { mutableStateOf(if (isDarkMode) MapStyle.BUSPLORE_DARK else MapStyle.BUSPLORE_LIGHT) }

    ComposableLifecycle(onEvent = { s, event ->
        lifecycleEvent.value = event
    })

    var userLocation by remember { mutableStateOf(Location(null)) }

    LaunchedEffect(key1 = Unit) {
        if (locationPermissionState.allPermissionsGranted) {
            LocationUtil.getMyLocation(context, onSuccess = {
                userLocation = it
            }, onError = {

            })
        }
    }

    // Check if reminder worker is running for current route
    DisposableEffect(key1 = Unit) {
        val workInfo =
            BusDistanceReminderWorker.getWorkInfo(context, route?.number?.toIntOrNull() ?: 0)
        val observer = Observer<List<WorkInfo>> {
            isReminderRunning = it.isNotEmpty() && (it.lastOrNull()?.state in listOf(
                WorkInfo.State.ENQUEUED,
                WorkInfo.State.RUNNING,
            ))
        }
        workInfo.observeForever(observer)
        onDispose { workInfo.removeObserver(observer) }
    }

    // Bottom Sheet of Information
    if (infoBottomSheetState.isVisible) {
        LiveBusInfoBottomSheet(
            infoBottomSheetState,
            LatLng(userLocation.latitude, userLocation.longitude),
            route,
            route1,
            route2,
            availableBuses.filter { it.isForward },
            availableBuses.filter { !it.isForward },
        ) {
            infoBottomSheetScope.launch { infoBottomSheetState.hide() }
        }
    }

    // Notification Bottom Sheet
    if (isNotifyMeDialogVisible) {
        LiveBusScheduleNotificationDialog(
            route1,
            route2,
            onSchedule = { distance, isForward ->
                BusDistanceReminderWorker.start(
                    context,
                    route1.number,
                    LatLng(userLocation.latitude, userLocation.longitude),
                    distance,
                    isForward
                )
            }, onCancel = {
                isNotifyMeDialogVisible = false
            })
    }

    Scaffold(
        topBar = {
            LiveBusTopBar(
                isReminderRunning = isReminderRunning,
                routeNumber = route?.number.orEmpty(),
                routeColor = viewModel.routeColor,
                onBackButtonClick = { currentActivity?.finish() },
                onScheduleClick = {
                    val intent = Intent(context, ScheduleActivity::class.java)
                    intent.putExtra("route_number", route?.number)
                    intent.putExtra("route_color", viewModel.routeColor)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    context.startActivity(intent)
                },
                onNotifyClick = {
                    if (isReminderRunning) {
                        BusDistanceReminderWorker.stop(context, route?.number?.toIntOrNull() ?: -1)
                    } else {
                        if (notificationsPermission?.status == PermissionStatus.Granted) {
                            isNotifyMeDialogVisible = true
                        } else {
                            notificationsPermission?.launchPermissionRequest()
                        }
                    }
                    Analytics.logClickBusDistanceNotifier()
                },
                onInfoClick = {
                    infoBottomSheetScope.launch { infoBottomSheetState.show() }
                    Analytics.logClickRouteAdditionalInfo()
                }
            )
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = it.calculateTopPadding())
        ) {
            if (isLoading && errorMessage == null) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (!isLoading && errorMessage != null) {
                Toast.makeText(context, errorMessage.asMessage(), Toast.LENGTH_SHORT).show()
                currentActivity?.finish()
            } else {

                FilledTonalButton(onClick = { }, modifier = Modifier.align(Alignment.Center)) {
                    Text(text = "სცადე თავიდან / Try Again")
                }

                AndroidView(
                    update = {
                        mbMap?.markers?.filter { i -> i.snippet == "MY" }
                            ?.forEach { i -> mbMap?.removeMarker(i) }
                        mbMap?.addMarker(createMyLocationMarker(it.context, userLocation))
                    },
                    factory = {
                        MapView(it).apply {
                            getMapAsync { map ->
                                mbMap = map
                                map.setStyle(mapStyle.style(it)) {
                                    if (locationPermissionState.allPermissionsGranted) {
                                        currentActivity?.let { c ->
                                            if (LocationUtil.isLocationTurnedOn(c)) {
                                                map.locationComponent.activateLocationComponent(
                                                    LocationComponentActivationOptions
                                                        .builder(context, it)
                                                        .build()
                                                )
//                                            map.locationComponent.isLocationComponentEnabled =
//                                                    true
                                            } else {
                                                LocationUtil.requestLocation(c) {
                                                    map.locationComponent.activateLocationComponent(
                                                        LocationComponentActivationOptions
                                                            .builder(context, it)
                                                            .build()
                                                    )
//                                                map.locationComponent.isLocationComponentEnabled =
//                                                        true
                                                }
                                            }
                                        }
                                    } else {
                                        locationPermissionState.launchMultiplePermissionRequest()
                                    }
                                }

                                map.setOnMarkerClickListener {
                                    return@setOnMarkerClickListener if (
                                        it.snippet == "stop"
                                    ) {
                                        val stopId = arrayListOf<RouteStop>().apply {
                                            addAll(route1.stops)
                                            addAll(route2.stops)
                                        }.find { s -> LatLng(s.lat, s.lng) == it.position }?.id

                                        val intent = Intent(context, TimeTableActivity::class.java)
                                        intent.putExtra("stop_id", stopId)
                                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                        context.startActivity(intent)

                                        Analytics.logOpenTimetableFromRouteMap()
                                        true
                                    } else false
                                }

                                map.addOnCameraMoveListener {
                                    mapZoomScope.coroutineContext.cancelChildren()
                                    mapZoomScope.launch {
                                        delay(350)
                                        if (map.cameraPosition.zoom >= 13.5) {
                                            map.markers.filter { it.snippet == "stop" }
                                                .forEach { it.remove() }

                                            val visibleBounds =
                                                map.projection.visibleRegion.latLngBounds
                                            val visibleStops = arrayListOf<RouteStop>().apply {
                                                addAll(route1.stops.filter { rs ->
                                                    val latLng = LatLng(rs.lat, rs.lng)
                                                    visibleBounds.contains(latLng)
                                                })

                                                addAll(route2.stops.filter { rs ->
                                                    val latLng = LatLng(rs.lat, rs.lng)
                                                    visibleBounds.contains(latLng)
                                                })
                                            }

                                            visibleStops.forEach { rs ->
                                                MarkerOptions().apply {
                                                    position(LatLng(rs.lat, rs.lng))
                                                    snippet("stop")
                                                    BitmapUtils.getBitmapFromDrawable(
                                                        ContextCompat.getDrawable(
                                                            context,
                                                            if (route1.polylineContains(
                                                                    rs.lat,
                                                                    rs.lng
                                                                )
                                                            )
                                                                R.drawable.ic_marker_route_stop_forward
                                                            else R.drawable.ic_marker_route_stop_backward
                                                        )
                                                    )?.let { bit ->
                                                        val smallMarker =
                                                            Bitmap.createScaledBitmap(
                                                                bit,
                                                                18.dpToPx(),
                                                                18.dpToPx(),
                                                                false
                                                            )
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
                                    val firstStation = route1.stops.firstOrNull()
                                    val lastStation = route1.stops.lastOrNull()

                                    viewModel.route1.collectLatest { ri ->
                                        map.addPolyline(PolylineOptions().apply {
                                            this.color(
                                                if (viewModel.route2.value.stops.isEmpty()) {
                                                    Color(0xFF0094FF).toArgb()
                                                } else Color.Green.toArgb()
                                            )
                                            this.width(5f)
                                            addAll(ri.polyline)
                                        })

                                        ri.stops.firstOrNull()?.let { rs ->
                                            map.addMarker(
                                                MarkerOptions().position(
                                                    LatLng(
                                                        rs.lat,
                                                        rs.lng
                                                    )
                                                )
                                            )
                                        }

                                        ri.stops.lastOrNull()?.let { rs ->
                                            map.addMarker(
                                                MarkerOptions().position(
                                                    LatLng(
                                                        rs.lat,
                                                        rs.lng
                                                    )
                                                )
                                            )
                                        }

                                        val firstLatLng =
                                            LatLng(
                                                firstStation?.lat ?: 0.0,
                                                firstStation?.lng ?: 0.0
                                            )
                                        val lastLatLng =
                                            LatLng(
                                                lastStation?.lat ?: 0.0,
                                                lastStation?.lng ?: 0.0
                                            )
                                        val latLngBounds =
                                            LatLngBounds.Builder().include(firstLatLng)
                                                .include(lastLatLng)
                                                .build()

                                        map.moveCamera(
                                            CameraUpdateFactory.newLatLngZoom(
                                                latLngBounds.center,
                                                11.1
                                            )
                                        )
                                    }
                                }

                                route2Scope.launch {
                                    viewModel.route2.collectLatest { ri ->
                                        map.addPolyline(PolylineOptions().apply {
                                            this.color(Color.Red.toArgb())
                                            this.width(5f)
                                            addAll(ri.polyline)
                                        })

                                        ri.stops.firstOrNull()?.let { rs ->
                                            map.addMarker(
                                                MarkerOptions().position(
                                                    LatLng(
                                                        rs.lat,
                                                        rs.lng
                                                    )
                                                )
                                            )
                                        }

                                        ri.stops.lastOrNull()?.let { rs ->
                                            map.addMarker(
                                                MarkerOptions().position(LatLng(rs.lat, rs.lng))
                                            )
                                        }
                                    }
                                }

                                availableBusesScope.launch {
                                    viewModel.availableBuses.collectLatest { buses ->
                                        map.markers.filter { m -> m.snippet == "bus" || m.snippet == "bus_bg" }
                                            .map { m -> m.remove() }

                                        map.addMarkers(buses.map { b ->
                                            MarkerOptions().apply {
                                                BitmapUtils.getBitmapFromDrawable(
                                                    ContextCompat.getDrawable(
                                                        context,
                                                        if (b.isForward) {
                                                            if (viewModel.route2.value.stops.isEmpty()) {
                                                                R.drawable.marker_microbus
                                                            } else {
                                                                R.drawable.ic_marker_bus_forward
                                                            }
                                                        } else {
                                                            R.drawable.ic_marker_bus_backwards
                                                        }
                                                    )
                                                )?.let { bit ->

                                                    val iconSize = if (viewModel.route2.value.stops.isEmpty()) {
                                                        32.dpToPx()
                                                    } else 24.dpToPx()

                                                    val markerIconBitmap =
                                                        Bitmap.createScaledBitmap(
                                                            bit,
                                                            iconSize,
                                                            iconSize,
                                                            false
                                                        )

                                                    val markerIconBgBitmap =
                                                        BitmapUtils.getBitmapFromDrawable(
                                                            ContextCompat.getDrawable(
                                                                context,
                                                                R.drawable.marker_microbus_bg
                                                            )
                                                        )?.let { bitmap ->
                                                            val matrix = Matrix()
                                                            matrix.setRotate(
                                                                b.bearing?.toFloat() ?: 0f
                                                            )
                                                            Bitmap.createScaledBitmap(
                                                                bitmap,
                                                                48.dpToPx(),
                                                                48.dpToPx(),
                                                                false
                                                            ).let { nmbtmp ->
                                                                BitmapUtil.rotateBitmap(nmbtmp, b.bearing?.toFloat() ?: 0f)
                                                            }
                                                        } ?: Bitmap.createBitmap(
                                                            0,
                                                            0,
                                                            Bitmap.Config.ARGB_8888
                                                        )

                                                    val markerIconFull =
                                                        if (viewModel.route2.value.stops.isEmpty() && b.bearing != null) {
                                                            BitmapUtil.combineBitmaps(
                                                                markerIconBgBitmap,
                                                                markerIconBitmap
                                                            )
                                                        } else {
                                                            markerIconBitmap
                                                        }

                                                    IconFactory.getInstance(context)
                                                        .fromBitmap(markerIconFull).let {
                                                            icon(it)
                                                        }
                                                    snippet("bus")
                                                }
                                                position(LatLng(b.lat, b.lng))
                                            }
                                        })
                                    }
                                }
                            }

                            lifecycleCoroutine.launch {
                                lifecycleEvent.collectLatest { event ->
                                    when (event) {
                                        Lifecycle.Event.ON_CREATE -> {
                                            onCreate(null)
                                        }

                                        Lifecycle.Event.ON_START -> {
                                            onStart()
                                        }

                                        Lifecycle.Event.ON_RESUME -> {
                                            viewModel.autoRefresh = true
                                            onResume()
                                        }

                                        Lifecycle.Event.ON_PAUSE -> {
                                            viewModel.autoRefresh = false
                                            onPause()
                                        }

                                        Lifecycle.Event.ON_DESTROY -> {
                                            onDestroy()
                                        }

                                        else -> onStart()
                                    }
                                }
                            }
                        }
                    })
            }

            // Button Move Zoom to user location
            Column(modifier = Modifier.align(Alignment.BottomEnd)) {
                FilledTonalIconButton(
                    onClick = {
                        if (!LocationUtil.isLocationTurnedOn(context)) {
                            LocationUtil.requestLocation(currentActivity!!) {
                                LocationUtil.requestLocation(currentActivity) {}
                            }
                        }

                        LocationUtil.getLastKnownLocation(context)?.let {
                            val latLng = LatLng(it.latitude, it.longitude)
                            mbMap?.animateCamera(
                                CameraUpdateFactory.newLatLngZoom(latLng, 16.0),
                                1500
                            )
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
                        mbMap?.setStyle(mapStyle.style(context))
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
}