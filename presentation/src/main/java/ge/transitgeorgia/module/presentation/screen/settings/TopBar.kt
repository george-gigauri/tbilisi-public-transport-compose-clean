package ge.transitgeorgia.module.presentation.screen.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ge.transitgeorgia.module.presentation.R

@Composable
fun TopBar() {

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {

        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(42.dp)
                .background(MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp))
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .background(MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp))
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = stringResource(id = R.string.settings),
                color = if (isSystemInDarkTheme()) Color.White else Color.DarkGray,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                modifier = Modifier
                    .wrapContentWidth()
                    .align(Alignment.CenterStart)
            )
        }
    }
}