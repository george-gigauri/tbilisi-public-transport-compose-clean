package ge.tbilisipublictransport.presentation.live_bus

import android.Manifest
import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
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
import com.mapbox.mapboxsdk.annotations.IconFactory
import com.mapbox.mapboxsdk.annotations.MarkerOptions
import com.mapbox.mapboxsdk.annotations.PolylineOptions
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.geometry.LatLngBounds
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.utils.BitmapUtils
import ge.tbilisipublictransport.R
import ge.tbilisipublictransport.common.service.worker.BusDistanceReminderWorker
import ge.tbilisipublictransport.common.util.ComposableLifecycle
import ge.tbilisipublictransport.common.util.LocationUtil
import ge.tbilisipublictransport.domain.model.RouteStop
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
    val mapZoomScope = rememberCoroutineScope()
    val route1Scope = rememberCoroutineScope()
    val route2Scope = rememberCoroutineScope()
    val availableBusesScope = rememberCoroutineScope()
    val lifecycleEvent = remember { MutableStateFlow(Lifecycle.Event.ON_CREATE) }
    val isDarkMode = isSystemInDarkTheme()
    val context = LocalContext.current

    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val errorMessage by viewModel.error.collectAsStateWithLifecycle()

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

    ComposableLifecycle(onEvent = { s, event ->
        lifecycleEvent.value = event
    })

    var userLocation by remember { mutableStateOf(LatLng(0.0, 0.0)) }

    LaunchedEffect(key1 = Unit) {
        if (locationPermissionState.allPermissionsGranted) {
            LocationUtil.getMyLocation(context, onSuccess = {
                userLocation = LatLng(it.latitude, it.longitude)
            }, onError = {

            })
        }
    }

    // Check if reminder worker is running for current route
    DisposableEffect(key1 = Unit) {
        val workInfo = BusDistanceReminderWorker.getWorkInfo(context, viewModel.routeNumber ?: 0)
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
            userLocation,
            viewModel.route1.collectAsState().value,
            viewModel.route2.collectAsState().value,
            viewModel.availableBuses.collectAsState().value.filter { it.isForward },
            viewModel.availableBuses.collectAsState().value.filter { !it.isForward },
        ) {
            infoBottomSheetScope.launch { infoBottomSheetState.hide() }
        }
    }

    // Notification Bottom Sheet
    if (isNotifyMeDialogVisible) {
        LiveBusScheduleNotificationDialog(
            viewModel.route1.collectAsState().value,
            viewModel.route2.collectAsState().value,
            onSchedule = { distance, isForward ->
                BusDistanceReminderWorker.start(
                    context,
                    viewModel.route1.value.number,
                    userLocation,
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
                route = viewModel.route1.collectAsState().value,
                onBackButtonClick = { currentActivity?.finish() },
                onNotifyClick = {
                    if (isReminderRunning) {
                        BusDistanceReminderWorker.stop(context, viewModel.routeNumber)
                    } else {
                        if (notificationsPermission?.status == PermissionStatus.Granted) {
                            isNotifyMeDialogVisible = true
                        } else {
                            notificationsPermission?.launchPermissionRequest()
                        }
                    }
                },
                onInfoClick = { infoBottomSheetScope.launch { infoBottomSheetState.show() } }
            )
        }
    ) {
        it.calculateBottomPadding()
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 54.dp)
        ) {
            if (isLoading && errorMessage.isNullOrEmpty()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (!isLoading && !errorMessage.isNullOrEmpty()) {
                Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                currentActivity?.finish()
            } else {
                AndroidView(factory = {
                    MapView(it).apply {
                        getMapAsync { map ->
                            map.setStyle(if (isDarkMode) Style.DARK else Style.LIGHT) {
                                if (locationPermissionState.allPermissionsGranted) {
                                    currentActivity?.let { c ->
                                        if (LocationUtil.isLocationTurnedOn(c)) {
                                            map.locationComponent.activateLocationComponent(
                                                LocationComponentActivationOptions
                                                    .builder(context, it)
                                                    .build()
                                            )
                                            map.locationComponent.isLocationComponentEnabled =
                                                true
                                        } else {
                                            LocationUtil.requestLocation(c) {
                                                map.locationComponent.activateLocationComponent(
                                                    LocationComponentActivationOptions
                                                        .builder(context, it)
                                                        .build()
                                                )
                                                map.locationComponent.isLocationComponentEnabled =
                                                    true
                                            }
                                        }
                                    }
                                } else {
                                    locationPermissionState.launchMultiplePermissionRequest()
                                }
                            }

                            map.addOnCameraMoveListener {
                                mapZoomScope.coroutineContext.cancelChildren()
                                mapZoomScope.launch {
                                    delay(300)
                                    if (map.cameraPosition.zoom >= 13.2) {
                                        map.markers.filter { it.snippet == "stop" }
                                            .forEach { it.remove() }
                                        val route1Stops = viewModel.route1.value.stops
                                        val route2Stops = viewModel.route2.value.stops
                                        val visibleBounds =
                                            map.projection.visibleRegion.latLngBounds
                                        val visibleStops = arrayListOf<RouteStop>().apply {
                                            addAll(route1Stops.filter {
                                                val latLng = LatLng(it.lat, it.lng)
                                                visibleBounds.contains(latLng)
                                            })

                                            addAll(route2Stops.filter {
                                                val latLng = LatLng(it.lat, it.lng)
                                                visibleBounds.contains(latLng)
                                            })
                                        }

                                        visibleStops.forEach {
                                            MarkerOptions().apply {
                                                position(LatLng(it.lat, it.lng))
                                                snippet("stop")
                                                BitmapUtils.getBitmapFromDrawable(
                                                    ContextCompat.getDrawable(
                                                        context,
                                                        R.drawable.ic_marker_route_stop_backward
                                                    )
                                                )?.let { bit ->
                                                    val smallMarker =
                                                        Bitmap.createScaledBitmap(
                                                            bit,
                                                            55,
                                                            55,
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
                                viewModel.route1.collectLatest { ri ->
                                    map.addPolyline(PolylineOptions().apply {
                                        this.color(Color.Green.toArgb())
                                        this.width(4f)
                                        addAll(ri.polyline)
                                    })

                                    ri.stops.firstOrNull()?.let {
                                        map.addMarker(
                                            MarkerOptions().position(
                                                LatLng(
                                                    it.lat,
                                                    it.lng
                                                )
                                            )
                                        )
                                    }

                                    ri.stops.lastOrNull()?.let {
                                        map.addMarker(
                                            MarkerOptions().position(
                                                LatLng(
                                                    it.lat,
                                                    it.lng
                                                )
                                            )
                                        )
                                    }

                                    val firstStop = ri.stops.firstOrNull()
                                    val lastStop = ri.stops.lastOrNull()
                                    val firstLatLng =
                                        LatLng(firstStop?.lat ?: 0.0, firstStop?.lng ?: 0.0)
                                    val lastLatLng =
                                        LatLng(lastStop?.lat ?: 0.0, lastStop?.lng ?: 0.0)
                                    val latLngBounds =
                                        LatLngBounds.Builder().include(firstLatLng)
                                            .include(lastLatLng)
                                            .build()

                                    map.animateCamera(
                                        CameraUpdateFactory.newLatLngZoom(
                                            latLngBounds.center,
                                            11.15
                                        )
                                    )
                                }
                            }

                            route2Scope.launch {
                                viewModel.route2.collectLatest { ri ->
                                    map.addPolyline(PolylineOptions().apply {
                                        this.color(Color.Red.toArgb())
                                        this.width(4f)
                                        addAll(ri.polyline)
                                    })

                                    ri.stops.firstOrNull()?.let {
                                        map.addMarker(
                                            MarkerOptions().position(
                                                LatLng(
                                                    it.lat,
                                                    it.lng
                                                )
                                            )
                                        )
                                    }

                                    ri.stops.lastOrNull()?.let {
                                        map.addMarker(
                                            MarkerOptions().position(LatLng(it.lat, it.lng))
                                        )
                                    }
                                }
                            }

                            availableBusesScope.launch {
                                viewModel.availableBuses.collectLatest { buses ->
                                    map.markers.filter { it.snippet == "bus" }
                                        .forEach { it.remove() }

                                    map.addMarkers(buses.map { b ->
                                        MarkerOptions().apply {
                                            BitmapUtils.getBitmapFromDrawable(
                                                ContextCompat.getDrawable(
                                                    context,
                                                    if (b.isForward) R.drawable.ic_marker_bus_forward else R.drawable.ic_marker_bus_backwards
                                                )
                                            )?.let { bit ->
                                                val smallMarker =
                                                    Bitmap.createScaledBitmap(
                                                        bit,
                                                        100,
                                                        100,
                                                        false
                                                    )
                                                val smallMarkerIcon =
                                                    IconFactory.getInstance(context)
                                                        .fromBitmap(smallMarker)

                                                icon(smallMarkerIcon)
                                                snippet("bus")
                                            }

                                            position(LatLng(b.lat, b.lng))
                                        }
                                    })
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
                                    Lifecycle.Event.ON_ANY -> onCreate(null)
                                }
                            }
                        }
                    }
                })
            }
        }
    }
}