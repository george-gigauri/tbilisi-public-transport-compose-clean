package ge.transitgeorgia.common.util

import android.content.Context
import android.util.Log
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScanner
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning

object QRScanner {

    private var client: GmsBarcodeScanner? = null

    fun init(context: Context) {
        if (client == null) {
            client = GmsBarcodeScanning.getClient(
                context,
                GmsBarcodeScannerOptions.Builder().setBarcodeFormats(
                    Barcode.FORMAT_QR_CODE
                ).build()
            )
        }
    }

    fun start(onSuccess: (Barcode) -> Unit, onError: (Exception) -> Unit) {
        client?.startScan()?.addOnSuccessListener(onSuccess)
            ?.addOnCanceledListener {
                Log.e("QRScanner", "Cancelled!")
            }?.addOnFailureListener {
                onError(it)
                it.printStackTrace()
                Log.e("QRScanner", "Error while opening scanner: ${it.message}")
            }
    }
}