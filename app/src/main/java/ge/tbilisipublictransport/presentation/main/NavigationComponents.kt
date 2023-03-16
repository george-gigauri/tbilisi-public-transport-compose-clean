package ge.tbilisipublictransport.presentation.main

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import ge.tbilisipublictransport.presentation.home.HomeScreen

@Composable
fun BottomNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = MainNavigationScreen.Home.screenName
    ) {
        composable(MainNavigationScreen.Home.screenName) {
            HomeScreen()
        }

        composable(MainNavigationScreen.Stops.screenName) {

        }

        composable(MainNavigationScreen.Favorites.screenName) {

        }

        composable(MainNavigationScreen.Settings.screenName) {

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