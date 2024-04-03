package ge.transitgeorgia.module.presentation.screen.favorites

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ge.transitgeorgia.common.analytics.Analytics
import ge.transitgeorgia.module.presentation.R
import ge.transitgeorgia.module.presentation.theme.DynamicPrimary
import ge.transitgeorgia.presentation.favorites.FavoriteStops
import ge.transitgeorgia.presentation.favorites.FavoritesViewModel

@Composable
@Preview
fun FavoritesScreen(
    viewModel: FavoritesViewModel = hiltViewModel()
) {
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }
    val context = LocalContext.current

    val stops by viewModel.stops.collectAsStateWithLifecycle()
    val routes by viewModel.routes.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxWidth()) {
        TabRow(selectedTabIndex = selectedTabIndex) {
            TabItem(selectedTabIndex, stringResource(id = R.string.routes), 0) {
                selectedTabIndex = it
                Analytics.logViewTopRoutesPage()
            }
            TabItem(selectedTabIndex, stringResource(id = R.string.stops), 1) {
                selectedTabIndex = it
                Analytics.logViewFavoriteStopsPage()
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        when (selectedTabIndex) {
            0 -> FavoriteRoutes(context, routes, Modifier.padding(horizontal = 8.dp), onDelete = {
                viewModel.deleteRoute(it.number)
            })

            1 -> FavoriteStops(context, stops, Modifier.padding(horizontal = 16.dp))
        }
    }
}

@Composable
private fun TabItem(selectedTabIndex: Int, title: String, index: Int, onClick: (Int) -> Unit) {
    Tab(
        selected = selectedTabIndex == index,
        onClick = { onClick.invoke(index) },
        modifier = Modifier.background(DynamicPrimary)
    ) {
        Text(text = title, modifier = Modifier.padding(vertical = 16.dp))
    }
}