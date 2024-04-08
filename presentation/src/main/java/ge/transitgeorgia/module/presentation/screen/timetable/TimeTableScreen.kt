package ge.transitgeorgia.module.presentation.screen.timetable

import android.Manifest
import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Observer
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.work.WorkInfo
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import ge.transitgeorgia.common.analytics.Analytics
import ge.transitgeorgia.common.service.worker.BusArrivalTimeReminderWorker
import ge.transitgeorgia.domain.model.ArrivalTime
import ge.transitgeorgia.module.domain.model.BusStop
import ge.transitgeorgia.module.presentation.R
import ge.transitgeorgia.module.presentation.screen.live_bus.LiveBusActivity
import ge.transitgeorgia.module.presentation.theme.DynamicPrimary
import ge.transitgeorgia.module.presentation.theme.DynamicWhite
import ge.transitgeorgia.presentation.timetable.ScheduleTimeTableDialog
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun TimeTableScreen(
    viewModel: TimeTableViewModel = hiltViewModel()
) {

    val arrivalTimes by viewModel.data.collectAsStateWithLifecycle()
    val stopInfo by viewModel.stop.collectAsStateWithLifecycle()
    val currentActivity = (LocalContext.current as? TimeTableActivity)
    val context = LocalContext.current

    val isFavorite by viewModel.isFavorite.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val shouldShowLoading by viewModel.shouldShowLoading.collectAsStateWithLifecycle()

    var isReminderRunning by rememberSaveable { mutableStateOf(false) }
    var isNotifyMeDialogVisible by rememberSaveable { mutableStateOf(false) }

    val notificationsPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        rememberPermissionState(permission = Manifest.permission.POST_NOTIFICATIONS)
    } else null

    // Check if reminder worker is running for current route
    DisposableEffect(key1 = Unit) {
        val workInfo = BusArrivalTimeReminderWorker.getWorkInfo(context, viewModel.stopId)
        val observer = Observer<List<WorkInfo>> {
            isReminderRunning = it.isNotEmpty() && (it.lastOrNull()?.state in listOf(
                WorkInfo.State.ENQUEUED,
                WorkInfo.State.RUNNING,
            ))
        }
        workInfo.observeForever(observer)
        onDispose { workInfo.removeObserver(observer) }
    }

    BackHandler {
        currentActivity?.finish()
    }

    if (isNotifyMeDialogVisible) {
        ScheduleTimeTableDialog(
            arrivalTimes,
            onCancel = { isNotifyMeDialogVisible = false }
        ) { routes, arrivalTime ->
            BusArrivalTimeReminderWorker.start(context, viewModel.stopId, arrivalTime, routes)
        }
    }

    Scaffold(
        topBar = {
            TopBar(
                isReminderRunning = isReminderRunning,
                isFavorite = isFavorite,
                onBack = { currentActivity?.finish() },
                onNotify = {
                    if (isReminderRunning) {
                        BusArrivalTimeReminderWorker.stop(context, viewModel.stopId)
                    } else {
                        if (notificationsPermission?.status == PermissionStatus.Granted) {
                            isNotifyMeDialogVisible = true
                        } else {
                            notificationsPermission?.launchPermissionRequest()
                        }
                    }
                    Analytics.logClickArrivalTimeAlert()
                },
                onRefresh = { viewModel.refresh() },
                onFavorites = { viewModel.addOrRemoveToFavorites() }
            )
        }
    ) {
        SwipeRefresh(
            state = rememberSwipeRefreshState(isRefreshing = isLoading && shouldShowLoading),
            onRefresh = { viewModel.onSwipeToRefresh() },
            modifier = Modifier.padding(top = 54.dp)
        ) {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Spacer(modifier = Modifier.height(12.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .background(DynamicPrimary, RoundedCornerShape(16.dp))
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.secondaryContainer,
                            RoundedCornerShape(16.dp)
                        )
                        .padding(1.dp)
                ) {
                    HeaderInformation()

                    if (arrivalTimes.isEmpty()) {
                        Text(
                            text = stringResource(id = R.string.error_nothing_found),
                            textAlign = TextAlign.Center,
                            color = DynamicWhite,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp)
                        )
                    } else {
                        arrivalTimes.forEachIndexed { index, arrivalTime ->
                            RouteTimeItem(
                                context,
                                arrivalTime,
                                index == arrivalTimes.size - 1,
                                modifier = Modifier
                            )
                        }
                    }
                }
                stopInfo?.let { StopMapLocation(it) }
            }

            it.calculateBottomPadding()
        }
    }
}

@Composable
fun HeaderInformation() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .background(
                MaterialTheme.colorScheme.primary,
                RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
            )
            .border(
                2.dp,
                MaterialTheme.colorScheme.primary,
                RoundedCornerShape(topEnd = 16.dp, topStart = 16.dp)
            )
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = "#",
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White,
            modifier = Modifier
                .fillMaxHeight()
                .defaultMinSize(minWidth = 54.dp)
                .padding(horizontal = 12.dp, vertical = 8.dp)
        )

        Text(
            text = stringResource(id = R.string.direction),
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.SemiBold,
            color = Color.White,
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .weight(1f)
        )

        Text(
            text = stringResource(id = R.string.min),
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.SemiBold,
            color = Color.White,
            modifier = Modifier
                .fillMaxHeight()
                .defaultMinSize(minWidth = 54.dp)
                .padding(horizontal = 12.dp, vertical = 8.dp)
        )
    }
}

@Composable
fun RouteTimeItem(
    context: Context,
    item: ArrivalTime,
    isLast: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .clickable {
                val intent = Intent(context, LiveBusActivity::class.java)
                intent.putExtra("route_number", item.routeNumber.toString())
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
                Analytics.logOpenRouteFromTimetable()
            }
            .then(modifier)
    ) {
        Text(
            text = item.routeNumber.toString(),
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .fillMaxHeight()
                .defaultMinSize(minWidth = 54.dp)
                .border(
                    1.dp, MaterialTheme.colorScheme.secondaryContainer,
                    if (isLast) RoundedCornerShape(bottomStart = 15.dp) else RectangleShape
                )
                .padding(horizontal = 12.dp, vertical = 12.dp)
        )

        Text(
            text = item.destination,
            modifier = Modifier
                .border(
                    1.dp, MaterialTheme.colorScheme.secondaryContainer,
                    RectangleShape
                )
                .padding(horizontal = 12.dp, vertical = 12.dp)
                .weight(1f)
        )

        Text(
            text = item.time.toString(),
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .defaultMinSize(minWidth = 54.dp)
                .fillMaxHeight()
                .border(
                    1.dp, MaterialTheme.colorScheme.secondaryContainer,
                    if (isLast) RoundedCornerShape(bottomEnd = 15.dp) else RectangleShape
                )
                .padding(horizontal = 12.dp, vertical = 12.dp)
        )
    }
}

@Composable
private fun StopMapLocation(stop: BusStop) {
    Column(
        modifier = Modifier
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "[ID:${stop.code}] ${stop.name}",
            color = DynamicWhite,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Start,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))

        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            AndroidView(factory = {
                MapView(it).apply {
                    val point = GeoPoint(stop.lat, stop.lng)
                    this.setTileSource(TileSourceFactory.MAPNIK)
                    this.getLocalVisibleRect(Rect())
                    this.setMultiTouchControls(true)
                    this.controller.animateTo(point, 18.5, 200)

                    this.overlays.add(
                        Marker(this).apply {
                            this.setDefaultIcon()
                            this.position = point
                            this.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                        }
                    )
                }
            }, modifier = Modifier.height(350.dp))
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}