package ge.transitgeorgia.module.presentation.screen

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ge.transitgeorgia.common.analytics.Analytics
import ge.transitgeorgia.module.domain.model.Route
import ge.transitgeorgia.module.domain.model.RouteTransportType
import ge.transitgeorgia.module.presentation.R
import ge.transitgeorgia.module.presentation.screen.live_bus.LiveBusActivity
import ge.transitgeorgia.module.presentation.theme.DynamicPrimary
import ge.transitgeorgia.module.presentation.theme.DynamicWhite
import ge.transitgeorgia.module.presentation.util.string
import ge.transitgeorgia.presentation.schedule.ScheduleActivity

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun BusRoutesScreen(
    viewModel: BusRoutesViewModel = hiltViewModel()
) {
    val routes by viewModel.routes.collectAsStateWithLifecycle()
    val transportType by viewModel.transportType.collectAsStateWithLifecycle()
    val context: Context = LocalContext.current

    LaunchedEffect(key1 = Unit) {
        Analytics.logOpenRoutesPage()
    }

    Scaffold(
        topBar = { TopBar(viewModel::searchRoute) }
    ) {
        Box(modifier = Modifier.padding(top = 54.dp)) {
            LazyColumn(
                modifier = Modifier.fillMaxWidth()
            ) {

//                item {
//                    LazyRow(
//                        horizontalArrangement = Arrangement.spacedBy(12.dp),
//                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
//                    ) {
//                        items(RouteTransportType.values()) {
//                            TransportCategory(
//                                category = it,
//                                isSelected = transportType == it,
//                                onSelect = { rtt ->
//                                    viewModel.setTransportType(rtt)
//                                })
//                        }
//                    }
//                }

                items(routes) { item ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        RouteItem(context, item)
                    }
                }
            }
        }
    }
}

@Composable
fun TransportCategory(
    category: RouteTransportType,
    isSelected: Boolean,
    onSelect: (c: RouteTransportType) -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(100))
            .clickable { onSelect(category) }
            .background(
                if (isSelected) MaterialTheme.colorScheme.primaryContainer else DynamicPrimary,
                RoundedCornerShape(100)
            )
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = category.string(),
            color = if (isSelected) DynamicWhite else DynamicWhite.copy(alpha = 0.8f),
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

@Composable
fun RouteItem(context: Context, item: Route) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                if (item.isMetro) {
                    Analytics.logOpenMetroSchedule()
                    val intent = Intent(context, ScheduleActivity::class.java)
                    intent.putExtra("route_number", item.number)
                    intent.putExtra("route_color", item.color)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    context.startActivity(intent)
                } else {
                    Analytics.logOpenRouteDetails(item.number.toIntOrNull() ?: -1)
                    val intent = Intent(context, LiveBusActivity::class.java)
                    intent.putExtra("route_number", item.number)
                    intent.putExtra("route_color", item.color)
                    context.startActivity(intent)
                }
            }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = if (item.isMetro) "M" else "#${item.number}",
            modifier = Modifier
                .widthIn(min = 65.dp)
                .background(
                    Color(android.graphics.Color.parseColor(item.color)),
                    RoundedCornerShape(8.dp)
                )
                .padding(vertical = 8.dp, horizontal = 12.dp),
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = item.longName,
            color = if (isSystemInDarkTheme()) Color.White else Color.Black,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            fontSize = 14.sp
        )
    }
}

@Composable
fun TopBar(onSearchKeywordChange: (String) -> Unit) {
    var searchKeywordValue by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .background(MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp))
            .padding(horizontal = 16.dp, vertical = 2.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
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

                BasicTextField(
                    value = searchKeywordValue,
                    onValueChange = {
                        searchKeywordValue = it
                        onSearchKeywordChange.invoke(it)
                        Analytics.logSearchRoutes()
                    },
                    singleLine = true,
                    textStyle = TextStyle(
                        fontSize = 15.sp,
                        color = if (isSystemInDarkTheme()) Color.White else Color.Black,
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 12.dp)
                        .align(Alignment.CenterStart)
                )

                if (searchKeywordValue.isEmpty()) {
                    Text(
                        text = stringResource(id = R.string.search_route),
                        color = if (isSystemInDarkTheme()) Color.LightGray.copy(alpha = 0.4f)
                        else Color.DarkGray.copy(alpha = 0.5f),
                        maxLines = 1,
                        modifier = Modifier
                            .padding(start = 12.dp)
                            .align(Alignment.CenterStart)
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                painter = painterResource(id = R.drawable.search_normal_1),
                contentDescription = null,
                tint = if (isSystemInDarkTheme()) Color.LightGray.copy(alpha = 0.45f) else Color.Gray,
                modifier = Modifier
                    .size(48.dp)
                    .padding(8.dp)
            )
        }
    }
}