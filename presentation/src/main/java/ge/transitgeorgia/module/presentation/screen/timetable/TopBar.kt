package ge.transitgeorgia.module.presentation.screen.timetable

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import ge.transitgeorgia.module.presentation.R
import ge.transitgeorgia.module.presentation.theme.DynamicPrimary

@Composable
fun TopBar(
    isReminderRunning: Boolean = false,
    isFavorite: Boolean = false,
    onBack: () -> Unit,
    onNotify: () -> Unit,
    onRefresh: () -> Unit,
    onFavorites: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(DynamicPrimary)
            .statusBarsPadding(),
        verticalAlignment = Alignment.Bottom
    ) {
        Spacer(modifier = Modifier.width(4.dp))
        Icon(
            painter = painterResource(id = R.drawable.ic_arrow_square_left),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.secondary,
            modifier = Modifier
                .size(56.dp)
                .clickable { onBack.invoke() }
                .padding(8.dp)
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
                .clickable { onNotify.invoke() }
                .padding(8.dp)
                .background(MaterialTheme.colorScheme.secondary, RoundedCornerShape(8.dp))
                .padding(6.dp)
        )

        Icon(
            painter = painterResource(id = R.drawable.ic_star_filled),
            contentDescription = null,
            tint = if (isFavorite) {
                Color(0xFFffdd00)
            } else {
                MaterialTheme.colorScheme.primaryContainer
            },
            modifier = Modifier
                .height(50.dp)
                .width(52.dp)
                .clickable { onFavorites.invoke() }
                .padding(8.dp)
                .background(MaterialTheme.colorScheme.secondary, RoundedCornerShape(8.dp))
                .padding(6.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))

//        Icon(
//            painter = painterResource(id = R.drawable.ic_refresh_square),
//            contentDescription = null,
//            tint = MaterialTheme.colorScheme.secondary,
//            modifier = Modifier
//                .size(54.dp)
//                .clickable { onRefresh.invoke() }
//                .padding(8.dp)
//        )
    }
}