package ge.transitgeorgia.presentation.main

import android.annotation.SuppressLint
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import ge.transitgeorgia.module.presentation.screen.live_bus.AskAddToFavoritesBottomSheet

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview(showBackground = true, device = Devices.PIXEL_4_XL, showSystemUi = true)
fun MainScreenContent() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = { BottomNavigation(navController = navController) },
    ) {
        BottomNavGraph(navController = navController)
    }
}