package ge.transitgeorgia.presentation.scanner

import android.annotation.SuppressLint
import android.view.ScaleGestureDetector
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.mlkit.vision.barcode.common.Barcode
import ge.transitgeorgia.module.common.util.QrCodeAnalyzer
import ge.transitgeorgia.module.presentation.R

@SuppressLint("ClickableViewAccessibility")
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun QRScannerScreen(
    onScanSuccess: (Barcode) -> Unit,
    onScanFailure: (Throwable) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var isCameraPermissionGranted by rememberSaveable { mutableStateOf(false) }
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    var camera: Camera? = remember { null }
    var isFlashEnabled by rememberSaveable { mutableStateOf(false) }

    val cameraPermission = rememberPermissionState(
        permission = android.Manifest.permission.CAMERA,
        onPermissionResult = {
            isCameraPermissionGranted = it
        })

    LaunchedEffect(key1 = Unit) {
        isCameraPermissionGranted = cameraPermission.status.isGranted
        if (!cameraPermission.status.isGranted) cameraPermission.launchPermissionRequest()
    }

    val scaleGestureDetector = ScaleGestureDetector(context,
        object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                val scale =
                    (camera?.cameraInfo?.zoomState?.value?.zoomRatio ?: 0f) * detector.scaleFactor
                camera?.cameraControl?.setZoomRatio(scale)
                return true
            }
        })

    Box(modifier = Modifier.fillMaxSize()) {
        // CameraX Preview
        if (isCameraPermissionGranted) {
            AndroidView(
                factory = {
                    val previewView = PreviewView(it)
                    previewView.scaleType = PreviewView.ScaleType.FILL_CENTER
                    previewView.setOnTouchListener { v, event ->
                        scaleGestureDetector.onTouchEvent(event)
                        return@setOnTouchListener true
                    }

                    val preview = androidx.camera.core.Preview.Builder().build()
                    val selector = CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                        .build()
                    preview.setSurfaceProvider(previewView.surfaceProvider)
                    val imageAnalysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()

                    imageAnalysis.setAnalyzer(
                        ContextCompat.getMainExecutor(it),
                        QrCodeAnalyzer(onScan = { barcode ->
                            isCameraPermissionGranted = false
                            onScanSuccess(barcode)
                        }, onError = onScanFailure)
                    )

                    try {
                        camera = cameraProviderFuture.get().bindToLifecycle(
                            lifecycleOwner,
                            selector,
                            preview,
                            imageAnalysis
                        )
                        camera?.cameraControl?.setZoomRatio(100f)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                    return@AndroidView previewView
                }, modifier = Modifier.fillMaxSize()
            )
        }

        // Scanner Overlay
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val rectPath = Path().apply {
                addRoundRect(
                    RoundRect(
                        Rect(center, size.minDimension / 2),
                        CornerRadius(24.dp.toPx(), 24.dp.toPx())
                    )
                )
            }
            clipPath(rectPath, clipOp = ClipOp.Difference) {
                drawRect(SolidColor(Color.Black.copy(alpha = 0.75f)))
            }
        }

        // Flashlight Toggle
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 32.dp)
        ) {
            IconButton(
                onClick = {
                    isFlashEnabled = !isFlashEnabled
                    if (camera?.cameraInfo?.hasFlashUnit() == true) {
                        camera?.cameraControl?.enableTorch(isFlashEnabled)
                    }
                }, modifier = Modifier
                    .size(54.dp)
                    .align(Alignment.TopEnd)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_flash_circle),
                    contentDescription = null,
                    tint = if (isFlashEnabled) Color.Yellow else Color.White,
                    modifier = Modifier.size(42.dp)
                )
            }
        }
    }
}