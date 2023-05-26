package ge.transitgeorgia.presentation.schedule

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import ge.transitgeorgia.R
import ge.transitgeorgia.ui.theme.DynamicPrimary

@Composable
@Preview
fun ScheduleTopBar() {
    val systemUi = rememberSystemUiController()
    val statusBarColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
    val iconColor = MaterialTheme.colorScheme.secondary
    systemUi.setStatusBarColor(statusBarColor)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(DynamicPrimary)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(id = R.drawable.ic_arrow_square_left),
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier
                    .size(54.dp)
                    .clickable { }
                    .padding(8.dp)
            )

            Text(
                text = "განრიგები - 351",
                color = Color.White,
                fontSize = with(LocalDensity.current) { 17.dp.toSp() },
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .background(
                        Color(android.graphics.Color.parseColor("#000000")),
                        RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            )

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}