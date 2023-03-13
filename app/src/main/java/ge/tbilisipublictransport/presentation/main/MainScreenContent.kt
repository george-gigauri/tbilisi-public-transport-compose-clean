package ge.tbilisipublictransport.presentation.main

import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
@Preview(showBackground = true, device = Devices.PIXEL_4_XL, showSystemUi = true)
fun MainScreenContent() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {

        }
    ) {
        it.calculateLeftPadding(LayoutDirection.Rtl)
    }

    NavHost(
        navController = navController,
        startDestination = MainNavigationScreen.Home.screenName
    ) {
        composable(MainNavigationScreen.Home.screenName) {

        }

        composable(MainNavigationScreen.Stops.screenName) {

        }
    }
}