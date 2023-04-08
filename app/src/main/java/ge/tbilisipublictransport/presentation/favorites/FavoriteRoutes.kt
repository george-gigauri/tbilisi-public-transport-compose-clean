package ge.tbilisipublictransport.presentation.favorites

import android.content.Context
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ge.tbilisipublictransport.domain.model.Route
import ge.tbilisipublictransport.presentation.bus_routes.RouteItem

@Composable
fun FavoriteRoutes(
    context: Context,
    routes: List<Route>,
    modifier: Modifier
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .then(modifier)
    ) {
        itemsIndexed(routes) { index, item ->
            RouteItem(context = context, index = index, item = item)
        }
    }
}