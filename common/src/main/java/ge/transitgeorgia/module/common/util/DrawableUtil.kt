package ge.transitgeorgia.module.common.util

import android.R.attr.bitmap
import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.drawable.toDrawable
import ge.transitgeorgia.common.util.dpToPx
import ge.transitgeorgia.module.common.util.DrawableUtil.resize


object DrawableUtil {

    fun Drawable.resize(context: Context, width: Int, height: Int): Drawable {
        return this.toBitmap(width.dpToPx(), height.dpToPx()).toDrawable(context.resources)
    }
}