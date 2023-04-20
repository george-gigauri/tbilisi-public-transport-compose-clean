package ge.transitgeorgia.common.util

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage


class QrCodeAnalyzer(
    private val onScan: (Barcode) -> Unit,
    private val onError: (Throwable) -> Unit
) : ImageAnalysis.Analyzer {

    override fun analyze(image: ImageProxy) {
        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build()

        image.image?.let {
            process(
                InputImage.fromMediaImage(it, image.imageInfo.rotationDegrees),
                options,
                onSuccess = onScan,
                onError = onError
            )
        }

        image.close()
    }

    private fun process(
        img: InputImage,
        options: BarcodeScannerOptions,
        onSuccess: (Barcode) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        val scanner = BarcodeScanning.getClient(options)
        scanner.process(img).addOnSuccessListener {
            it.firstOrNull()?.let(onSuccess)
        }.addOnFailureListener(onError)
    }
}