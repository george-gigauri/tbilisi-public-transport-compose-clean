package ge.transitgeorgia.common.util

import android.content.res.Resources


// Dp to Px
fun Float.dpToPx(): Int {
    return (toFloat() * Resources.getSystem().displayMetrics.density + 0.5f).toInt()
}

fun Double.dpToPx(): Int {
    return (toFloat() * Resources.getSystem().displayMetrics.density + 0.5f).toInt()
}

fun Int.dpToPx(): Int {
    return (toFloat() * Resources.getSystem().displayMetrics.density + 0.5f).toInt()
}