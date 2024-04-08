package ge.transitgeorgia.module.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

val DynamicGray: Color
    @Composable get() = if (isSystemInDarkTheme()) Color.LightGray else Color.DarkGray
val DynamicPrimary: Color
    @Composable get() = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)

val DynamicWhite: Color
    @Composable get() = if (isSystemInDarkTheme()) Color.White else Color.Black

val DynamicBlack: Color
    @Composable get() = if (!isSystemInDarkTheme()) Color.White else Color.Black

val DynamicRed: Color
    @Composable get() = if (isSystemInDarkTheme()) Color(0xFFCA0040) else Color(0xFFFF0051)

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

object AppColor {

    val POLYLINE_GREEN = Color(0xFF26FF00)
    val POLYLINE_RED = Color(0xFFFF0059)
    val POLYLINE_BLUE = Color(0xFF0094FF)
}