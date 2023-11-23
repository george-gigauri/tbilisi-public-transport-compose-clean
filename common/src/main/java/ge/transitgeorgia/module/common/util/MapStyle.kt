package ge.transitgeorgia.module.common.util

import android.content.Context
import com.mapbox.mapboxsdk.maps.Style
import ge.transitgeorgia.common.util.rawAsString
import ge.transitgeorgia.module.common.R

enum class MapStyle {
    BUSPLORE_LIGHT,
    BUSPLORE_DARK,
    STANDARD_LIGHT,
    STANDARD_DARK,
    TERRAIN
}

fun MapStyle.style(context: Context): Style.Builder {
    val resources = context.resources
    return when (this) {
        MapStyle.BUSPLORE_LIGHT -> Style.Builder().fromJson(resources.rawAsString(R.raw.map_light))
        MapStyle.BUSPLORE_DARK -> Style.Builder().fromJson(resources.rawAsString(R.raw.map_dark))
        MapStyle.STANDARD_LIGHT -> Style.Builder().fromUri(Style.LIGHT)
        MapStyle.STANDARD_DARK -> Style.Builder().fromUri(Style.DARK)
        MapStyle.TERRAIN -> Style.Builder().fromUri(Style.SATELLITE_STREETS)
    }
}