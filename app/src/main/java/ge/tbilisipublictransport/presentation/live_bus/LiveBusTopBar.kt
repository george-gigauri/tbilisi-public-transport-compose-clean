package ge.tbilisipublictransport.presentation.live_bus

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
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
import ge.tbilisipublictransport.R
import ge.tbilisipublictransport.domain.model.RouteInfo

@Composable
fun LiveBusTopBar(
    isReminderRunning: Boolean = false,
    route: RouteInfo = RouteInfo.empty(),
    onBackButtonClick: () -> Unit = { },
    onNotifyClick: () -> Unit = { },
    onInfoClick: () -> Unit = { }
) {
    val isDarkMode = isSystemInDarkTheme()
    val systemUi = rememberSystemUiController()
    val statusBarColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
    val iconColor = MaterialTheme.colorScheme.secondary
    systemUi.setStatusBarColor(statusBarColor)

    val buttonColorCoroutine = rememberCoroutineScope()

    LaunchedEffect(Unit) {
//        if (isDarkMode) {
//            buttonColorCoroutine.launch {
//                while (true) {
//                    delay(15000)
//                    buttonColor = if (isDarkMode) Color.Black else Color.White
//                    delay(8000)
//                    buttonColor = if (isDarkMode) Color.White else Color.Black
//                }
//            }
//        }
    }

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
            text = route.number.toString(),
            color = Color.White,
            fontSize = with(LocalDensity.current) { 17.dp.toSp() },
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .background(
                    Color(android.graphics.Color.parseColor(route.color)),
                    RoundedCornerShape(8.dp)
                )
                .padding(horizontal = 12.dp, vertical = 4.dp)
        )

        Spacer(modifier = Modifier.weight(1f))

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