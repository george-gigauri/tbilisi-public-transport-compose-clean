package ge.transitgeorgia.module.common.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint

object BitmapUtil {

    fun rotateBitmap(bitmap: Bitmap, degrees: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degrees)
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    fun combineBitmaps(background: Bitmap, overlay: Bitmap): Bitmap {
        // Create a new bitmap with the dimensions of the background bitmap
        val result = Bitmap.createBitmap(background.width, background.height, Bitmap.Config.ARGB_8888)

        // Create a Canvas object with the result bitmap
        val canvas = Canvas(result)

        // Draw the background bitmap onto the canvas
        canvas.drawBitmap(background, 0f, 0f, null)

        // Draw the overlay bitmap onto the canvas at a specific position
        val left = (background.width - overlay.width) / 2f
        val top = (background.height - overlay.height) / 2f
        canvas.drawBitmap(overlay, left, top, null)

        return result
    }
}