package ge.transitgeorgia.presentation.main

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import ge.transitgeorgia.presentation.bus_routes.BusRoutesScreen
import ge.transitgeorgia.presentation.bus_stops.BusStopsMapScreen
import ge.transitgeorgia.presentation.bus_stops.BusStopsScreen
import ge.transitgeorgia.presentation.favorites.FavoritesScreen
import ge.transitgeorgia.presentation.home.HomeScreen
import ge.transitgeorgia.presentation.settings.SettingsScreen

@Composable
fun BottomNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = MainNavigationScreen.Home.screenName,
        modifier = Modifier.windowInsetsPadding(WindowInsets(bottom = 54.dp))
    ) {
        composable(MainNavigationScreen.Home.screenName) {
            HomeScreen(navController)
        }

        composable(MainNavigationScreen.Routes.screenName) {
            BusRoutesScreen()
        }

        composable(MainNavigationScreen.Favorites.screenName) {
            FavoritesScreen()
        }

        composable(MainNavigationScreen.Settings.screenName) {
            SettingsScreen()
        }

        // Not Bottom Menu
        composable(MainNavigationScreen.Stops.screenName) {
            BusStopsScreen()
        }

        composable(MainNavigationScreen.StopsMap.screenName) {
            BusStopsMapScreen()
        }
    }
}

@Composable
fun BottomNavigation(navController: NavHostController) {
    val backstackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backstackEntry?.destination

    NavigationBar(
        modifier = Modifier.height(54.dp)
    ) {
        MainNavigationScreen.all().forEach {
            val isSelected = it.screenName == currentDestination?.route
            val icon = painterResource(id = if (isSelected) it.iconResFilled else it.iconResOutline)

            NavigationBarItem(
                alwaysShowLabel = false,
                selected = isSelected,
                onClick = { navController.navigate(it.screenName) },
                icon = {
                    Icon(
                        painter = icon,
                        contentDescription = null,
                        modifier = Modifier
                            .size(32.dp)
                            .padding(vertical = 4.dp)
                    )
                },
                label = null,
            )
        }
    }
}