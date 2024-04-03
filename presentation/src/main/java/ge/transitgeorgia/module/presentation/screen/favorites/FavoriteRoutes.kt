package ge.transitgeorgia.module.presentation.screen.favorites

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import ge.transitgeorgia.module.domain.model.Route
import ge.transitgeorgia.module.presentation.R
import ge.transitgeorgia.module.presentation.screen.RouteItem
import ge.transitgeorgia.module.presentation.theme.DynamicBlack
import ge.transitgeorgia.module.presentation.theme.DynamicGray
import ge.transitgeorgia.module.presentation.theme.DynamicPrimary
import ge.transitgeorgia.module.presentation.theme.DynamicRed
import ge.transitgeorgia.module.presentation.theme.DynamicWhite

@Composable
fun FavoriteRoutes(
    context: Context,
    routes: List<Route>,
    modifier: Modifier,
    onDelete: (Route) -> Unit = {}
) {

    var selectedItemIndex by rememberSaveable { mutableIntStateOf(-1) }
    var isDeleteRouteDialogVisible by rememberSaveable { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .then(modifier)
    ) {
        itemsIndexed(routes) { index, item ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RouteItem(context = context, item = item, modifier.weight(1f))
                Spacer(modifier = Modifier.width(12.dp))
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(DynamicRed, CircleShape)
                        .clickable {
                            selectedItemIndex = index
                            isDeleteRouteDialogVisible = true
                        }
                        .padding(12.dp)
                )
            }
        }
    }

    if (isDeleteRouteDialogVisible) {
        Dialog(
            onDismissRequest = {
                isDeleteRouteDialogVisible = false
            }
        ) {
            Column(
                modifier = Modifier
                    .background(DynamicPrimary, RoundedCornerShape(24.dp))
                    .padding(16.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.message_delete_route_from_favorites),
                    color = DynamicBlack,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {

                    Text(
                        text = stringResource(id = R.string.no),
                        color = DynamicGray,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        modifier= Modifier
                            .clip(RoundedCornerShape(100))
                            .clickable {
                                isDeleteRouteDialogVisible = false
                            }
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Button(onClick = {
                        onDelete(routes[selectedItemIndex])
                        isDeleteRouteDialogVisible = false
                    }) {
                        Text(
                            text = stringResource(id = R.string.yes),
                            color= DynamicWhite,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }
    }
}