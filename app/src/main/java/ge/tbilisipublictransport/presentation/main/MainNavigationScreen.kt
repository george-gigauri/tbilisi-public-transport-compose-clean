package ge.tbilisipublictransport.presentation.main

import ge.tbilisipublictransport.R

sealed class MainNavigationScreen(val title: String, val iconRes: Int, val screenName: String) {
    object Home : MainNavigationScreen("მთავარი", R.drawable.ic_launcher_foreground, "home")
    object Stops : MainNavigationScreen(
        "გაჩერებები",
        com.chuckerteam.chucker.R.drawable.btn_checkbox_checked_mtrl,
        "stops"
    )
}
