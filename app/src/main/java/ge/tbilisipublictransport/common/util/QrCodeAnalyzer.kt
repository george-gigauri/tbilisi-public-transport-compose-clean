package ge.tbilisipublictransport.common.util

//
//import androidx.camera.core.ImageAnalysis.Analyzer
//import androidx.camera.core.ImageProxy
//import com.google.mlkit.vision.barcode.BarcodeScannerOptions
//import com.google.mlkit.vision.barcode.BarcodeScanning
//import com.google.mlkit.vision.barcode.common.Barcode
//import com.google.mlkit.vision.common.InputImage
//import java.nio.ByteBuffer
//
class QrCodeAnalyzer(
    private val onScan: (String) -> Unit,
    private val onError: (Exception) -> Unit = {}
)  {
//
//    override fun analyze(image: ImageProxy) {
//        val options = BarcodeScannerOptions.Builder()
//            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
//            .build()
//
//        image.image?.let {
//            process(InputImage.fromMediaImage(it, image.imageInfo.rotationDegrees), options)
//        }
//    }
//
//    private fun process(img: InputImage, options: BarcodeScannerOptions) {
//        val scanner = BarcodeScanning.getClient(options)
//        scanner.process(img).addOnSuccessListener {
//            it.firstOrNull()?.let { b ->
//                onScan.invoke(b.rawValue.orEmpty())
//            } ?: onError.invoke(Exception("No Barcode"))
//        }.addOnFailureListener { onError.invoke(it) }
//    }
//
//    private fun ByteBuffer.toByteArray(): ByteArray {
//        rewind()
//        return ByteArray(remaining()).also {
//            get(it)
//        }
//    }
}