package ge.transitgeorgia.module.presentation.util

import android.graphics.Paint
import ge.transitgeorgia.common.util.dpToPx
import org.osmdroid.api.IGeoPoint
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
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
    val midpointLat = (point1.latitude + point2.latitude) / 2
    val midpointLon = (point1.longitude + point2.longitude) / 2
    val midpoint = GeoPoint(midpointLat, midpointLon)

    val distance = point1.distanceToAsDouble(point2)

    val zoomLevel = calculateZoomLevel(this, distance, padding)

    // Set the center point and zoom level of the map
    controller.setCenter(midpoint)
    controller.setZoom(zoomLevel)
}

fun calculateZoomLevel(mapView: MapView, distance: Double, padding: Int): Double {
    val screenWidth = mapView.width - padding
    val screenHeight = mapView.height - padding

    val equatorLength = 40075004.0
    val metersPerPixel = equatorLength / 256

    return ln(1.0 * screenWidth * screenHeight / (distance * metersPerPixel)) / Math.log(2.0)
}