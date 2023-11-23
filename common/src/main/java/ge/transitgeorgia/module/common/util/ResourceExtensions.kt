package ge.transitgeorgia.common.util

import android.content.res.Resources
import androidx.annotation.RawRes

fun Resources.rawAsString(@RawRes resId: Int): String {
    return openRawResource(resId).bufferedReader().use { it.readText() }
}