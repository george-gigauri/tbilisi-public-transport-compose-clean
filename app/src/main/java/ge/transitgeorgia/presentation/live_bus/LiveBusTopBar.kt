package ge.transitgeorgia.presentation.live_bus

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import ge.transitgeorgia.R

@Composable
fun LiveBusTopBar(
    isReminderRunning: Boolean = false,
    routeNumber: String = "---",
    routeColor: String = "#ff0584",
    onBackButtonClick: () -> Unit = { },
    onScheduleClick: () -> Unit = { },
    onNotifyClick: () -> Unit = { },
    onInfoClick: () -> Unit = { }
) {
    val systemUi = rememberSystemUiController()
    val statusBarColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
    val iconColor = MaterialTheme.colorScheme.secondary
    systemUi.setStatusBarColor(statusBarColor)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .background(statusBarColor)
    ) {
        Spacer(modifier = Modifier.width(2.dp))
        Icon(
            painter = painterResource(id = R.drawable.ic_arrow_square_left),
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier
                .size(54.dp)
                .clickable { onBackButtonClick() }
                .padding(8.dp)
        )

        Text(
            text = routeNumber,
            color = Color.White,
            fontSize = with(LocalDensity.current) { 17.dp.toSp() },
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .background(
                    Color(android.graphics.Color.parseColor(routeColor)),
                    RoundedCornerShape(8.dp)
                )
                .padding(horizontal = 12.dp, vertical = 4.dp)
        )

        Spacer(modifier = Modifier.weight(1f))

        // SCHEDULE
        Icon(
            painter = painterResource(id = R.drawable.ic_calendar),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier
                .height(50.dp)
                .width(52.dp)
                .clickable { onScheduleClick.invoke() }
                .padding(8.dp)
                .background(iconColor, RoundedCornerShape(8.dp))
                .padding(6.dp)
        )

        Icon(
            painter = painterResource(id = R.drawable.ic_notification),
            contentDescription = null,
            tint = if (isReminderRunning) {
                Color(0xFFffdd00)
            } else {
                MaterialTheme.colorScheme.primaryContainer
            },
            modifier = Modifier
                .height(50.dp)
                .width(52.dp)
                .clickable { onNotifyClick.invoke() }
                .padding(8.dp)
                .background(iconColor, RoundedCornerShape(8.dp))
                .padding(6.dp)
        )

        Icon(
            painter = painterResource(id = R.drawable.ic_info_circle),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier
                .height(50.dp)
                .width(52.dp)
                .clickable { onInfoClick.invoke() }
                .padding(8.dp)
                .background(iconColor, RoundedCornerShape(8.dp))
                .padding(6.dp)
                .rotate(180f)
        )

        Spacer(modifier = Modifier.width(4.dp))
    }
}