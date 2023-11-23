package ge.transitgeorgia.module.presentation.screen.bus_stops

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ge.transitgeorgia.module.presentation.R
import ge.transitgeorgia.module.presentation.theme.DynamicPrimary

@Composable
@Preview
fun BusStopTopBar(
    onSearchKeywordChange: (String) -> Unit = { },
    onScanClick: () -> Unit = { }
) {
    var searchKeywordValue by rememberSaveable { mutableStateOf("") }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .background(DynamicPrimary)
            .padding(start = 16.dp, end = 6.dp, top = 2.dp, bottom = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .padding(vertical = 4.dp)
                .background(
                    if (isSystemInDarkTheme()) Color.DarkGray.copy(alpha = 0.5f)
                    else Color.LightGray.copy(alpha = 0.5f),
                    RoundedCornerShape(100)
                )
                .padding(start = 8.dp)
                .fillMaxHeight()
                .weight(1f)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.search_normal_1),
                contentDescription = null,
                tint = if (isSystemInDarkTheme()) Color.LightGray.copy(alpha = 0.35f) else Color.Gray.copy(
                    alpha = 0.7f
                ),
                modifier = Modifier
                    .size(32.dp)
                    .align(Alignment.CenterStart)
            )

            BasicTextField(
                value = searchKeywordValue,
                onValueChange = {
                    searchKeywordValue = it
                    onSearchKeywordChange.invoke(it)
                },
                singleLine = true,
                textStyle = TextStyle(
                    fontSize = 15.sp,
                    color = if (isSystemInDarkTheme()) Color.White else Color.Black,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 42.dp)
                    .align(Alignment.CenterStart)
            )

            if (searchKeywordValue.isEmpty()) {
                Text(
                    text = stringResource(id = R.string.search_stop),
                    color = if (isSystemInDarkTheme()) Color.LightGray.copy(alpha = 0.4f)
                    else Color.DarkGray.copy(alpha = 0.5f),
                    maxLines = 1,
                    modifier = Modifier
                        .padding(start = 42.dp)
                        .align(Alignment.CenterStart)
                )
            }
        }

        Spacer(modifier = Modifier.width(6.dp))
        IconButton(
            onClick = { onScanClick.invoke() },
            modifier = Modifier.size(54.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_scan_barcode),
                contentDescription = null,
                tint = if (isSystemInDarkTheme()) Color.LightGray.copy(alpha = 0.55f) else Color.DarkGray,
                modifier = Modifier
                    .size(54.dp)
                    .padding(8.dp)
            )
        }
        Spacer(modifier = Modifier.width(2.dp))
    }
}