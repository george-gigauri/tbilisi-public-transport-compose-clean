package ge.transitgeorgia.module.presentation.screen.live_bus

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.location.Location
import android.os.Build
import android.util.Log
import android.view.MotionEvent
import android.widget.Toast
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import androidx.lifecycle.Observer
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.work.WorkInfo
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.rememberPermissionState
import com.google.maps.android.PolyUtil
import ge.transitgeorgia.common.analytics.Analytics
import ge.transitgeorgia.common.service.worker.BusDistanceReminderWorker
import ge.transitgeorgia.common.util.dpToPx
import ge.transitgeorgia.domain.model.Bus
import ge.transitgeorgia.module.common.util.DrawableUtil.resize
import ge.transitgeorgia.module.common.util.LatLngUtil
import ge.transitgeorgia.module.common.util.LocationUtil
import ge.transitgeorgia.module.domain.model.RouteInfo
import ge.transitgeorgia.module.presentation.R
import ge.transitgeorgia.module.presentation.theme.AppColor
import ge.transitgeorgia.module.presentation.util.asMessage
import ge.transitgeorgia.presentation.live_bus.LiveBusScheduleNotificationDialog
import ge.transitgeorgia.presentation.live_bus.LiveBusTopBar
import ge.transitgeorgia.presentation.schedule.ScheduleActivity
import ge.transitgeorgia.module.presentation.screen.timetable.TimeTableActivity
import ge.transitgeorgia.module.presentation.util.ComposableLifecycle
import ge.transitgeorgia.module.presentation.util.centerAndZoomPolyline
import ge.transitgeorgia.module.presentation.util.centerMapBetweenPoints
import kotlinx.coroutines.launch
import org.osmdroid.api.IMapController
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Overlay
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.gestures.OneFingerZoomOverlay
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

@SuppressLint("MissingPermission")
@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun LiveBusScreen(
    viewModel: LiveBusViewModel = hiltViewModel()
) {
    val currentActivity = (LocalActivity.current as LiveBusActivity)
    val context = LocalContext.current

    var myLocationOverlay: MyLocationNewOverlay? = remember { null }
    var mapController: IMapController? = remember { null }
    var mapZoom: Double by remember { mutableDoubleStateOf(0.0) }

    val routeInfo by viewModel.routeInfo.collectAsStateWithLifecycle()
    val route1 by viewModel.route1.collectAsStateWithLifecycle()
    val route2 by viewModel.route2.collectAsStateWithLifecycle()
    val availableBuses by viewModel.availableBuses.collectAsStateWithLifecycle()

    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val errorMessage by viewModel.error.collectAsStateWithLifecycle(null)

    var isReminderRunning by rememberSaveable { mutableStateOf(false) }

    var isBackgroundLocationTutorialVisible by rememberSaveable { mutableStateOf(false) }

    val notificationsPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        rememberPermissionState(permission = Manifest.permission.POST_NOTIFICATIONS)
    } else null

    val locationPermissionState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        )
    )

    val backgroundLocationPermissionState =
        rememberPermissionState(permission = Manifest.permission.ACCESS_BACKGROUND_LOCATION)

    val infoBottomSheetState = rememberModalBottomSheetState()
    val askFavoriteBottomSheetState = rememberModalBottomSheetState()
    val infoBottomSheetScope = rememberCoroutineScope()
    val askFavoriteBottomSheetScope = rememberCoroutineScope()

    var isNotifyMeDialogVisible by rememberSaveable { mutableStateOf(false) }
    val shouldShowAskToAddToFavorites by viewModel.shouldShowAddToFavoriteRoutesDialog.collectAsStateWithLifecycle()
    var isAddToFavoritesConsentVisible by rememberSaveable { mutableStateOf(false) }

    var userLocation by remember { mutableStateOf(Location(null)) }

    LaunchedEffect(key1 = shouldShowAskToAddToFavorites) {
        isAddToFavoritesConsentVisible = shouldShowAskToAddToFavorites
    }

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
            BusDistanceReminderWorker.getWorkInfo(context, routeInfo?.number?.toIntOrNull() ?: 0)
        val observer = Observer<List<WorkInfo>> {
            isReminderRunning = it.isNotEmpty() && (it.lastOrNull()?.state in listOf(
                WorkInfo.State.ENQUEUED,
                WorkInfo.State.RUNNING,
            ))
        }
        workInfo.observeForever(observer)
        onDispose { workInfo.removeObserver(observer) }
    }

    if (isBackgroundLocationTutorialVisible) {
        BackgroundLocationDisclosureSheet(
            state = rememberModalBottomSheetState(),
            onAccept = {
                backgroundLocationPermissionState.launchPermissionRequest()
                isBackgroundLocationTutorialVisible = false
            },
            onDeny = {
                isBackgroundLocationTutorialVisible = false
            })
    }

    // Bottom Sheet Ask To Add To Favorites
    if (isAddToFavoritesConsentVisible) {
        AskAddToFavoritesBottomSheet(
            state = askFavoriteBottomSheetState,
            onAccept = {
                viewModel.addToFavoriteRoutes()
                isAddToFavoritesConsentVisible = false
            },
            onCancel = {
                viewModel.rejectAddToFavorites()
                isAddToFavoritesConsentVisible = false
                askFavoriteBottomSheetScope.launch {
                    askFavoriteBottomSheetState.hide()
                }
            }
        )
    }

    // Bottom Sheet of Information
    if (infoBottomSheetState.isVisible) {
        LiveBusInfoBottomSheet(
            infoBottomSheetState,
            GeoPoint(userLocation.latitude, userLocation.longitude),
            routeInfo,
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
                    GeoPoint(userLocation.latitude, userLocation.longitude),
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
                routeNumber = routeInfo?.number.orEmpty(),
                routeColor = viewModel.routeColor,
                onBackButtonClick = { currentActivity.finish() },
                onScheduleClick = {
                    val intent = Intent(context, ScheduleActivity::class.java)
                    intent.putExtra("route_id", routeInfo?.id)
                    intent.putExtra("route_number", routeInfo?.number)
                    intent.putExtra("route_color", viewModel.routeColor)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    context.startActivity(intent)
                },
                onNotifyClick = {
                    if (isReminderRunning) {
                        BusDistanceReminderWorker.stop(
                            context,
                            routeInfo?.number?.toIntOrNull() ?: -1
                        )
                    } else {
                        if (notificationsPermission?.status == PermissionStatus.Granted) {
                            if (backgroundLocationPermissionState.status != PermissionStatus.Granted) {
                                isBackgroundLocationTutorialVisible = true
                            } else {
                                isNotifyMeDialogVisible = true
                            }
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
        it.calculateTopPadding()
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            if (isLoading && errorMessage == null) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (!isLoading && errorMessage != null) {
                FilledTonalButton(onClick = {
                    viewModel.retry()
                }, modifier = Modifier.align(Alignment.Center)) {
                    Text(text = "სცადე თავიდან / Try Again")
                }
                Toast.makeText(context, errorMessage.asMessage(), Toast.LENGTH_SHORT).show()
            } else {

                AndroidView(
                    update = { map ->

                        val points1 = if (route1.polyline.isEmpty()) {
                            PolyUtil.decode(route1.polylineHash).map { latLng ->
                                GeoPoint(latLng.latitude, latLng.longitude)
                            }
                        } else {
                            route1.polyline.map { latLng ->
                                GeoPoint(latLng.latitude, latLng.longitude)
                            }
                        }

                        val points2 = if (routeInfo?.isCircular == true) emptyList() else {
                            if (route2.polyline.isEmpty()) {
                                if (!route2.polylineHash.isNullOrEmpty()) {
                                    PolyUtil.decode(route2.polylineHash).map { latLng ->
                                        GeoPoint(latLng.latitude, latLng.longitude)
                                    }
                                } else emptyList()
                            } else {
                                route2.polyline.map { latLng ->
                                    GeoPoint(latLng.latitude, latLng.longitude)
                                }
                            }
                        }

                        // Route 1 Polyline
                        val polyline1 = Polyline(map, true, false)
                        polyline1.color = if (routeInfo?.isCircular == true) {
                            AppColor.POLYLINE_BLUE.toArgb()
                        } else AppColor.POLYLINE_GREEN.toArgb()
                        polyline1.width = 14f
                        polyline1.setOnClickListener { polyline, _, eventPos -> false }
                        polyline1.setPoints(points1);

                        // Route 2 Polyline
                        val polyline2 = Polyline(map, true, false)
                        polyline2.width = 12.5f
                        polyline2.color = AppColor.POLYLINE_RED.toArgb()
                        polyline2.setOnClickListener { polyline, _, eventPos -> false }
                        polyline2.setPoints(points2)

                        val busMarkerBgs = availableBuses.map { bus ->
                            Marker(map).apply {
                                if (bus.bearing == null) {
                                    if (routeInfo?.isCircular == false) {
                                        if (bus.isForward) {
                                            this.rotation = LatLngUtil.calculateBearing(
                                                bus.lat,
                                                bus.lng,
                                                route1.stops.firstOrNull()?.lat ?: 0.0,
                                                route1.stops.firstOrNull()?.lng ?: 0.0,
                                            )
                                        } else {
                                            this.rotation = LatLngUtil.calculateBearing(
                                                bus.lat,
                                                bus.lng,
                                                route2.stops.firstOrNull()?.lat ?: 0.0,
                                                route2.stops.firstOrNull()?.lng ?: 0.0,
                                            )
                                        }
                                        this.setVisible(true)
                                    } else {
                                        this.setVisible(false)
                                    }
                                } else {
                                    this.rotation = 0 - (bus.bearing?.toFloat() ?: 0f)
                                }

                                this.icon =
                                    getBusIconBackground(context, route1, route2, bus)?.resize(
                                        context,
                                        25.dpToPx(),
                                        25.dpToPx()
                                    )
                                this.position = GeoPoint(bus.lat, bus.lng)
                                this.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                                this.setOnMarkerClickListener { _, _ -> false }
                                this.snippet = "BUS_BG"
                            }
                        }

                        val busMarkers = availableBuses.map { bus ->
                            Marker(map).apply {
                                this.setVisible(true)
                                this.icon = getBusIcon(context, route1, route2, bus)?.resize(
                                    context, 10.dpToPx(), 10.dpToPx()
                                )
                                this.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                                this.position = GeoPoint(bus.lat, bus.lng)
                                this.setOnMarkerClickListener { _, _ -> false }
                                this.closeInfoWindow()
                                this.snippet = "BUS"
                            }
                        }

                        val fwdStops = route1.stops.map { stop ->
                            Marker(map).apply {
                                this.setVisible(true)
                                this.icon = ContextCompat.getDrawable(
                                    context,
                                    R.drawable.ic_marker_route_stop_forward
                                )?.resize(context, 6.dpToPx(), 6.dpToPx())
                                this.position = GeoPoint(stop.lat, stop.lng)
                                this.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                                this.setOnMarkerClickListener { marker, _ ->
                                    Intent(context, TimeTableActivity::class.java).apply {
                                        this.putExtra("stop_id", marker.id)
                                        this.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                        context.startActivity(this)
                                    }
                                    true
                                }
                                this.id = stop.id
                            }
                        }

                        val bwdStops = route2.stops.map { stop ->
                            Marker(map).apply {
                                this.setVisible(true)
                                this.icon = ContextCompat.getDrawable(
                                    context,
                                    R.drawable.ic_marker_route_stop_backward
                                )?.resize(context, 6.dpToPx(), 6.dpToPx())
                                this.position = GeoPoint(stop.lat, stop.lng)
                                this.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                                this.setOnMarkerClickListener { marker, _ ->
                                    Intent(context, TimeTableActivity::class.java).apply {
                                        this.putExtra("stop_id", marker.id)
                                        this.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                        context.startActivity(this)
                                    }
                                    true
                                }
                                this.id = stop.id
                            }
                        }

                        map.overlays.forEach { o ->
                            if (!(o is OneFingerZoomOverlay || o is RotationGestureOverlay)) {
                                map.overlays.remove(o)
                            }
                        }

                        map.overlays.addAll(listOf(polyline1, polyline2))
                        if (mapZoom >= 16) map.overlays.addAll(listOf(fwdStops, bwdStops).flatten())
                        if (busMarkerBgs.isNotEmpty()) map.overlays.addAll(busMarkerBgs)
                        map.overlays.addAll(busMarkers)

                        MyLocationNewOverlay(GpsMyLocationProvider(context), map).apply {
                            this.enableMyLocation()
                            this.setDirectionIcon(
                                ContextCompat.getDrawable(
                                    context,
                                    R.drawable.marker_my_location
                                )?.toBitmap(26.dpToPx(), 42.dpToPx())
                            )
                            this.setDirectionAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                            this.isDrawAccuracyEnabled = false
                        }.also { o ->
                            myLocationOverlay = o
                        }
                    },
                    factory = { c ->
                        MapView(c).apply {
                            this.setTileSource(TileSourceFactory.MAPNIK)
                            this.getLocalVisibleRect(android.graphics.Rect())
                            this.setMultiTouchControls(true)

                            mapController = this.controller

                            this.overlays.add(RotationGestureOverlay(this))
                            this.overlays.add(OneFingerZoomOverlay())

                            Polyline(this).apply {
                                this.setPoints(
                                    if (route1.polyline.isEmpty()) {
                                        PolyUtil.decode(route1.polylineHash).map { i ->
                                            GeoPoint(i.latitude, i.longitude)
                                        }
                                    } else {
                                        route1.polyline.map { i ->
                                            GeoPoint(i.latitude, i.longitude)
                                        }
                                    }
                                )
                            }.also { p ->
                                this.centerAndZoomPolyline(p)
                            }

                            MyLocationNewOverlay(GpsMyLocationProvider(context), this).apply {
                                this.enableMyLocation()
                                this.setDirectionIcon(
                                    ContextCompat.getDrawable(
                                        context,
                                        R.drawable.marker_my_location
                                    )?.toBitmap(26.dpToPx(), 42.dpToPx())
                                )
                                this.setDirectionAnchor(.5f, .5f)
                                this.isDrawAccuracyEnabled = true
                            }.also { o ->
                                myLocationOverlay = o
                            }

                            addMapListener(object : MapListener {
                                override fun onScroll(event: ScrollEvent?): Boolean {
                                    return true
                                }

                                override fun onZoom(event: ZoomEvent?): Boolean {
                                    event?.let { mapZoom = event.zoomLevel }
                                    return true
                                }
                            })
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
                        } else {
                            myLocationOverlay?.myLocation?.let { gp ->
                                mapController?.animateTo(
                                    gp,
                                    17.0,
                                    1000
                                )
                            }
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
}

fun getBusIcon(
    context: Context,
    route1: RouteInfo,
    route2: RouteInfo,
    bus: Bus
): Drawable? {
    return (if (bus.isForward) {
        if (route1.isCircular) {
            if (route1.isMicroBus) {
                ContextCompat.getDrawable(
                    context,
                    R.drawable.marker_microbus
                )
            } else {
                ContextCompat.getDrawable(
                    context,
                    R.drawable.ic_marker_bus
                )
            }
        } else {
            ContextCompat.getDrawable(
                context,
                R.drawable.ic_marker_bus_forward
            )
        }
    } else ContextCompat.getDrawable(
        context,
        R.drawable.ic_marker_bus_backward
    ))
}

fun getBusIconBackground(
    context: Context,
    route1: RouteInfo,
    route2: RouteInfo,
    bus: Bus
): Drawable? {
    return (if (bus.isForward) {
        if (route1.isCircular) {
            if (route1.isMicroBus) {
                ContextCompat.getDrawable(
                    context,
                    R.drawable.ic_bus_marker_bg
                )
            } else {
                ContextCompat.getDrawable(
                    context,
                    R.drawable.ic_bus_marker_bg
                )
            }
        } else {
            ContextCompat.getDrawable(
                context,
                R.drawable.ic_marker_bus_forward_bg
            )
        }
    } else ContextCompat.getDrawable(
        context,
        R.drawable.ic_marker_bus_backward_bg
    ))
}