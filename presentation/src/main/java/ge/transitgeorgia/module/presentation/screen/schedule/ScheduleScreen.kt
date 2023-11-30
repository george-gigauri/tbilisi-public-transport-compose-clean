package ge.transitgeorgia.presentation.schedule

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ge.transitgeorgia.domain.model.CurrentTimeStationSchedule
import ge.transitgeorgia.module.domain.model.Route
import ge.transitgeorgia.module.domain.model.RouteTransportType
import ge.transitgeorgia.module.presentation.R
import ge.transitgeorgia.module.presentation.screen.schedule.ScheduleViewModel
import ge.transitgeorgia.module.presentation.theme.DynamicPrimary
import ge.transitgeorgia.module.presentation.theme.DynamicWhite
import ge.transitgeorgia.module.presentation.util.asMessage

@Composable
fun ScheduleScreen(
    viewModel: ScheduleViewModel,
    onFinishActivity: () -> Unit
) {
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle(initialValue = null)
    val schedules by viewModel.data.collectAsStateWithLifecycle()
    val route by viewModel.route.collectAsStateWithLifecycle()
    val isForward by viewModel.isForward.collectAsStateWithLifecycle()
    val isMetro = route.type == RouteTransportType.METRO

    Scaffold(
        topBar = {
            Column(modifier = Modifier.fillMaxWidth()) {
                ScheduleTopBar(if (isMetro) route.id else route.number, viewModel.routeColor) {
                    onFinishActivity()
                }

                DirectionHeader(
                    route = route,
                    isForward = isForward,
                    onDirectionChange = {
                        viewModel.changeDirection()
                    }
                )
            }
        }
    ) {
        if (!isLoading && error == null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(it)
                    .verticalScroll(rememberScrollState())
            ) {
                schedules.forEachIndexed { index, item ->
                    StopScheduleItem(
                        schedule = item,
                        isFirst = index == 0,
                        isLast = index == schedules.lastIndex
                    )
                }
                Spacer(modifier = Modifier.height(85.dp))
            }
        } else if (!isLoading && error != null) {
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(text = error.asMessage() ?: "Unknown Error Occurred.")
                    Spacer(modifier = Modifier.height(8.dp))
                    FilledTonalButton(onClick = { viewModel.refresh() }) {
                        Text(text = "თავიდან ცდა")
                    }
                }
            }
        } else if (isLoading) {
            Box(modifier = Modifier.fillMaxSize()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}

@Composable
private fun DirectionHeader(route: Route, isForward: Boolean, onDirectionChange: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .background(DynamicPrimary)
            .padding(vertical = 8.dp)
            .padding(end = 8.dp, start = 16.dp)
    ) {
        Text(
            text = buildAnnotatedString {
                append("${stringResource(id = R.string.direction)}: ")
                if (route.isMetro) append(if (isForward) route.lastStation else route.firstStation)
                else append(if (isForward) route.firstStation else route.lastStation)
            },
            modifier = Modifier.weight(1f)
        )
        Icon(
            painter = painterResource(id = R.drawable.ic_arrange_square),
            contentDescription = null,
            modifier = Modifier
                .size(44.dp)
                .clickable { onDirectionChange() }
                .padding(8.dp)
        )
    }
}

@Composable
private fun StopScheduleItem(
    schedule: CurrentTimeStationSchedule,
    isFirst: Boolean,
    isLast: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .padding(horizontal = 12.dp)
    ) {

        val highlightedArrivalTime = when {
            schedule.currentScheduledArrivalTime.length in 1..2 -> "${schedule.currentScheduledArrivalTime} ${
                stringResource(
                    id = R.string.min
                )
            }"

            else -> schedule.currentScheduledArrivalTime
        }

        Text(
            text = highlightedArrivalTime,
            color = DynamicWhite,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .width(36.dp)
                .align(Alignment.Top)
                .padding(top = 24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(
                modifier = Modifier
                    .width(1.dp)
                    .height(24.dp)
                    .background(if (!isFirst) Color.Gray else Color.Transparent)
            )
            Spacer(
                modifier = Modifier
                    .size(20.dp)
                    .background(DynamicPrimary, CircleShape)
            )
            if (!isLast) {
                Spacer(
                    modifier = Modifier
                        .width(1.dp)
                        .fillMaxHeight()
                        .background(Color.Gray)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))
        Column(
            modifier = Modifier
                .weight(1f)
                .align(Alignment.Top)
                .padding(top = 16.dp)
        ) {
            Text(text = schedule.stopName, fontWeight = FontWeight.Bold, color = DynamicWhite)
            Text(
                text = schedule.futureScheduledArrivalTimes.joinToString(",  "),
                fontWeight = FontWeight.Normal,
                color = Color.Gray,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}