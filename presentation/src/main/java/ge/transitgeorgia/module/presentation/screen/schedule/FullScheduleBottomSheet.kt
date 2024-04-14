package ge.transitgeorgia.module.presentation.screen.schedule

import android.icu.util.Calendar.WeekData
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ge.transitgeorgia.domain.model.Schedule
import ge.transitgeorgia.module.presentation.R
import ge.transitgeorgia.module.presentation.theme.DynamicBlack
import ge.transitgeorgia.module.presentation.theme.DynamicGray
import ge.transitgeorgia.module.presentation.theme.DynamicPrimary
import ge.transitgeorgia.module.presentation.theme.DynamicWhite
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullScheduleBottomSheet(
    stopName: String? = "M/S Akhmeteli Theatre",
    schedule: List<Schedule> = emptyList(),
    state: SheetState = SheetState(false, SheetValue.PartiallyExpanded),
    onCancel: () -> Unit = { }
) {

    ModalBottomSheet(
        containerColor = DynamicPrimary,
        sheetState = state,
        onDismissRequest = { onCancel.invoke() },
        windowInsets = WindowInsets.safeContent
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {

            Column {
                Text(
                    text = stringResource(id = R.string.label_stop_name),
                    fontSize = 12.sp,
                    color = DynamicGray
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = stopName ?: "---",
                    fontSize = 15.sp,
                    color = DynamicWhite,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            schedule.map { s ->
                val dayFrom = DayOfWeek.valueOf(s.fromDay).getDisplayName(TextStyle.FULL, Locale.getDefault())
                val dayTo = DayOfWeek.valueOf(s.toDay).getDisplayName(TextStyle.FULL, Locale.getDefault())
                Text(
                    text = if (dayFrom == dayTo) {
                        dayFrom
                    } else "$dayFrom - $dayTo",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = DynamicWhite
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = s.stops.find { it.name == stopName }?.arrivalTimes?.joinToString(", ")
                        ?: "---",
                    fontSize = 15.sp,
                    color = if (isSystemInDarkTheme()) Color.LightGray else Color.DarkGray
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            Spacer(modifier = Modifier.height(36.dp))
        }
    }
}