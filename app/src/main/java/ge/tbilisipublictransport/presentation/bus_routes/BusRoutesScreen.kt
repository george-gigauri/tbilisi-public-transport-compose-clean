package ge.tbilisipublictransport.presentation.bus_routes

import android.annotation.SuppressLint
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ge.tbilisipublictransport.R
import ge.tbilisipublictransport.domain.model.Route
import ge.tbilisipublictransport.presentation.live_bus.LiveBusActivity

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun BusRoutesScreen(
    viewModel: BusRoutesViewModel = hiltViewModel()
) {
    val routes by viewModel.data.collectAsStateWithLifecycle()
    val searchKeyword by remember { mutableStateOf("") }

    Scaffold(
        topBar = { TopBar(searchKeyword) }
    ) {
        Box(modifier = Modifier.padding(top = 54.dp)) {
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                itemsIndexed(routes) { index, item ->
                    RouteItem(index, item)
                }
            }
        }
    }
}

@Composable
fun RouteItem(index: Int, item: Route) {
    val context = LocalContext.current

    Row(
        modifier = Modifier
            .clickable {
                val intent = Intent(context, LiveBusActivity::class.java)
                intent.putExtra("route_number", item.number.toIntOrNull() ?: -1)
                context.startActivity(intent)
            }
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = "#${item.number}",
            modifier = Modifier
                .background(
                    Color(android.graphics.Color.parseColor(item.color)),
                    RoundedCornerShape(8.dp)
                )
                .padding(vertical = 8.dp, horizontal = 12.dp),
            color = Color.White,
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = item.longName, color = if (isSystemInDarkTheme()) Color.White else Color.Black,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
fun TopBar(searchKeyword: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .background(MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp))
            .padding(horizontal = 16.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "მარშრუტის ძიება...",
                maxLines = 1,
                color = if (isSystemInDarkTheme()) Color.LightGray.copy(alpha = 0.4f)
                else Color.DarkGray.copy(alpha = 0.5f),
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .background(
                        if (isSystemInDarkTheme()) Color.DarkGray.copy(alpha = 0.5f)
                        else Color.LightGray.copy(alpha = 0.5f),
                        RoundedCornerShape(100)
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .weight(1f)
            )
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