package ge.transitgeorgia.presentation.timetable

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import ge.transitgeorgia.R
import ge.transitgeorgia.ui.theme.DynamicPrimary

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
            .height(54.dp)
            .background(DynamicPrimary),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.width(4.dp))
        Icon(
            painter = painterResource(id = R.drawable.ic_arrow_square_left),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.secondary,
            modifier = Modifier
                .size(54.dp)
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
                .height(48.dp)
                .width(50.dp)
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
                .height(48.dp)
                .width(50.dp)
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