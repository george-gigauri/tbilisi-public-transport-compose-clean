package ge.tbilisipublictransport.presentation.timetable

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Observer
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.work.WorkInfo
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import ge.tbilisipublictransport.common.service.worker.BusArrivalTimeReminderWorker
import ge.tbilisipublictransport.domain.model.ArrivalTime
import ge.tbilisipublictransport.presentation.live_bus.LiveBusActivity
import ge.tbilisipublictransport.ui.theme.DynamicPrimary

@OptIn(ExperimentalFoundationApi::class, ExperimentalPermissionsApi::class)
@Composable
fun TimeTableScreen(
    viewModel: TimeTableViewModel = hiltViewModel()
) {

    val arrivalTimes by viewModel.data.collectAsStateWithLifecycle()
    val currentActivity = (LocalContext.current as? TimeTableActivity)
    val context = LocalContext.current

    val isFavorite by viewModel.isFavorite.collectAsStateWithLifecycle()
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
                },
                onRefresh = { viewModel.refresh() },
                onFavorites = { viewModel.addOrRemoveToFavorites() }
            )
        }
    ) {
        Column {
            Spacer(modifier = Modifier.height(54.dp))
            Spacer(modifier = Modifier.height(12.dp))
            LazyColumn(
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
                item { HeaderInformation() }
                itemsIndexed(arrivalTimes, key = { _, it -> it.routeNumber }) { index, it ->
                    RouteTimeItem(
                        context,
                        it,
                        index == arrivalTimes.size - 1,
                        modifier = Modifier.animateItemPlacement()
                    )
                }
            }
        }

        it.calculateBottomPadding()
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
            text = stringResource(id = ge.tbilisipublictransport.R.string.direction),
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.SemiBold,
            color = Color.White,
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .weight(1f)
        )

        Text(
            text = stringResource(id = ge.tbilisipublictransport.R.string.min),
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
                intent.putExtra("route_number", item.routeNumber)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
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