package ge.transitgeorgia.module.common.util

import android.graphics.Bitmap
import android.graphics.Canvas

object BitmapUtil {

    fun combineBitmaps(background: Bitmap, overlay: Bitmap): Bitmap {
        // Create a new bitmap with the dimensions of the background bitmap
        val result = Bitmap.createBitmap(background.width, background.height, background.config)

        // Create a Canvas object with the result bitmap
        val canvas = Canvas(result)

        // Draw the background bitmap onto the canvas
        canvas.drawBitmap(background, 0f, 0f, null)

        // Calculate the position for overlaying the second bitmap (centered)
        val left = (background.width - overlay.width) / 2f
        val top = (background.height - overlay.height) / 2f

        // Draw the overlay bitmap onto the canvas
        canvas.drawBitmap(overlay, left, top, null)

        return result
    }
}