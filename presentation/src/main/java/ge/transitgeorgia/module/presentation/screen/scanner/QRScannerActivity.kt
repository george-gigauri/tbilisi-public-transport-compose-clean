package ge.transitgeorgia.presentation.scanner

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import com.google.mlkit.vision.barcode.common.Barcode
import dagger.hilt.android.AndroidEntryPoint
import ge.transitgeorgia.common.analytics.Analytics
import ge.transitgeorgia.presentation.timetable.TimeTableActivity

@AndroidEntryPoint
class QRScannerActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
        setContent {
            QRScannerScreen(onScanSuccess = this::processBarcode, onScanFailure = {

            })
        }
        Analytics.logViewQrScannerPage()
    }

    private fun processBarcode(b: Barcode) {
        when (b.valueType) {
            Barcode.TYPE_SMS -> processSMSBarcode(b)
        }
    }

    private fun processSMSBarcode(b: Barcode) {
        b.sms?.message?.also {
            val intent = Intent(this, TimeTableActivity::class.java)
            intent.putExtra("stop_id", it)
            startActivity(intent)
            finish()
        }
    }
}