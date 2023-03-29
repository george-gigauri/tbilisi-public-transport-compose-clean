package ge.tbilisipublictransport.presentation.bus_routes

import android.annotation.SuppressLint
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ge.tbilisipublictransport.R
import ge.tbilisipublictransport.domain.model.Route

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun BusRoutesScreen(
    viewModel: BusRoutesViewModel = hiltViewModel()
) {
    val routes by viewModel.data.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { TopBar() }
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
    Row(modifier = Modifier
        .clickable { }
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
fun TopBar() {
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
                text = "მარშრუტის ნომერი, მიმართულება...",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .background(Color.DarkGray.copy(alpha = 0.5f), RoundedCornerShape(100))
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Icon(
                painter = painterResource(id = R.drawable.search_normal_1),
                contentDescription = null,
                modifier = Modifier.width(54.dp)
            )
        }
    }
}