package ge.tbilisipublictransport.presentation.main

import androidx.annotation.DrawableRes
import ge.tbilisipublictransport.R

sealed class MainNavigationScreen(
    val title: String,
    @DrawableRes val iconResOutline: Int,
    @DrawableRes val iconResFilled: Int,
    val screenName: String
) {

    object Home : MainNavigationScreen(
        "მთავარი",
        R.drawable.ic_home_outline,
        R.drawable.ic_home_filled,
        "home"
    )

    object Routes : MainNavigationScreen(
        "მარშრუტები",
        R.drawable.ic_task_square_outline,
        R.drawable.ic_task_square_filled,
        "routes"
    )

    object Favorites : MainNavigationScreen(
        "ფავორიტები",
        R.drawable.ic_heart_outline,
        R.drawable.ic_heart_filled,
        "favorites"
    )

    object Settings : MainNavigationScreen(
        "პარამეტრები",
        R.drawable.ic_category_outline,
        R.drawable.ic_category_filled,
        "settings"
    )

    // Non-Bottom
    object Stops : MainNavigationScreen("", -1, -1, "stops")

    companion object {
        fun all(): List<MainNavigationScreen> = listOf(Home, Routes, Favorites, Settings)
    }
}
