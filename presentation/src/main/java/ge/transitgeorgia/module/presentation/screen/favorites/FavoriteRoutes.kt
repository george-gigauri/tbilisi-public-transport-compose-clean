package ge.transitgeorgia.presentation.favorites

import android.content.Context
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ge.transitgeorgia.module.domain.model.Route
import ge.transitgeorgia.module.presentation.screen.RouteItem

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
        items(routes) { item ->
            RouteItem(context = context, item = item)
        }
    }
}