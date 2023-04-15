package ge.transitgeorgia.presentation.favorites

import android.content.Context
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ge.transitgeorgia.domain.model.BusStop
import ge.transitgeorgia.presentation.bus_stops.ItemBusStop

@Composable
fun FavoriteStops(
    context: Context,
    stops: List<BusStop>,
    modifier: Modifier
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .then(modifier)
    ) {
        items(stops) { item ->
            ItemBusStop(context = context, stop = item)
        }
    }
}