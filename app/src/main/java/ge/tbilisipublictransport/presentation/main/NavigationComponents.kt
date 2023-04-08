package ge.tbilisipublictransport.presentation.main

import androidx.compose.foundation.layout.*
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
import ge.tbilisipublictransport.presentation.bus_routes.BusRoutesScreen
import ge.tbilisipublictransport.presentation.bus_stops.BusStopsScreen
import ge.tbilisipublictransport.presentation.home.HomeScreen
import ge.tbilisipublictransport.presentation.scan_bus_stop.ScanStopScreen

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

        }

        composable(MainNavigationScreen.Settings.screenName) {

        }

        // Not Bottom Menu
        composable(MainNavigationScreen.Stops.screenName) {
            BusStopsScreen(navController)
        }

        composable(MainNavigationScreen.Scanner.screenName) {
            ScanStopScreen()
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