package ge.transitgeorgia.module.presentation.util

import android.graphics.Paint
import ge.transitgeorgia.common.util.dpToPx
import org.osmdroid.api.IGeoPoint
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.simplefastpoint.SimpleFastPointOverlay
import org.osmdroid.views.overlay.simplefastpoint.SimpleFastPointOverlayOptions
import org.osmdroid.views.overlay.simplefastpoint.SimplePointTheme
import kotlin.math.ln

fun createSimpleCircleMarker(
    points: List<GeoPoint>,
    color: Int,
    sizeDp: Int,
    shape: SimpleFastPointOverlayOptions.Shape = SimpleFastPointOverlayOptions.Shape.CIRCLE,
    cellSize: Int = 100,
    onClick: (IGeoPoint, Int) -> Unit = { _, _ -> }
): SimpleFastPointOverlay {

    val pt = SimplePointTheme(points, false, true)

    val pointStyle = Paint()
    pointStyle.style = Paint.Style.FILL_AND_STROKE
    pointStyle.color = color
    pointStyle.strokeWidth = sizeDp.dpToPx().toFloat()

    val opt = SimpleFastPointOverlayOptions()
        .setSymbol(shape)
        .setMaxNShownLabels(5)
        .setPointStyle(pointStyle)
        .setAlgorithm(SimpleFastPointOverlayOptions.RenderingAlgorithm.MAXIMUM_OPTIMIZATION)
        .setRadius(20f)
        .setIsClickable(true)
        .setCellSize(cellSize)

    val sfpo = SimpleFastPointOverlay(pt, opt)

    sfpo.setOnClickListener { _, point ->
        onClick(points[point], point)
    }

    return sfpo
}

fun MapView.centerMapBetweenPoints(point1: GeoPoint, point2: GeoPoint, padding: Int) {
    var minLat = point1.latitude
    var maxLat = point2.latitude
    var minLong = point1.longitude
    var maxLong = point2.longitude

    val boundingBox = BoundingBox(maxLat, maxLong, minLat, minLong)
    zoomToBoundingBox(boundingBox.increaseByScale(2.1f), true)
}

fun MapView.centerAndZoomPolyline(polyline: Polyline) {
    post {
        zoomToBoundingBox(polyline.bounds.increaseByScale(1.5f), false)
    }
}

fun calculateZoomLevel(mapView: MapView, distance: Double, padding: Int): Double {
    val screenWidth = mapView.width - padding
    val screenHeight = mapView.height - padding

    val equatorLength = 40075004.0
    val metersPerPixel = equatorLength / 256

    return ln(1.0 * screenWidth * screenHeight / (distance * metersPerPixel)) / Math.log(2.0)
}