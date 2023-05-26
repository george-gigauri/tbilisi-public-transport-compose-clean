package ge.transitgeorgia.presentation.schedule

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ge.transitgeorgia.domain.model.CurrentTimeStationSchedule
import ge.transitgeorgia.ui.theme.DynamicPrimary

@Composable
fun ScheduleScreen(
    viewModel: ScheduleViewModel
) {

    val schedules by viewModel.data.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { ScheduleTopBar() }
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
        ) {
            itemsIndexed(schedules) { index, item ->
                StopScheduleItem(
                    schedule = item,
                    isFirst = index == 0,
                    isLast = index == schedules.lastIndex
                )
            }
            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
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

        Text(
            text = schedule.currentScheduledArrivalTime,
            color = Color.DarkGray,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
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
            Text(text = schedule.stopName, fontWeight = FontWeight.Bold, color = Color.DarkGray)
            Text(
                text = schedule.futureScheduledArrivalTimes.joinToString(",  "),
                fontWeight = FontWeight.Normal,
                color = Color.LightGray,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}