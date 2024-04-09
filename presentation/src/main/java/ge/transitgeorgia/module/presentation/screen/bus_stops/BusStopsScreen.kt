package ge.transitgeorgia.module.presentation.screen.bus_stops

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.mlkit.vision.barcode.common.Barcode
import ge.transitgeorgia.common.analytics.Analytics
import ge.transitgeorgia.common.util.QRScanner
import ge.transitgeorgia.module.domain.model.BusStop
import ge.transitgeorgia.module.presentation.R
import ge.transitgeorgia.module.presentation.theme.DynamicPrimary
import ge.transitgeorgia.module.presentation.theme.DynamicWhite
import ge.transitgeorgia.presentation.scanner.QRScannerActivity
import ge.transitgeorgia.module.presentation.screen.timetable.TimeTableActivity
import java.text.DecimalFormat

@Composable
fun BusStopsScreen(
    viewModel: BusStopsViewModel = hiltViewModel()
) {
    Analytics.logOpenAllStops()
    rememberSystemUiController().setStatusBarColor(DynamicPrimary)
    val stops by viewModel.result.collectAsState()

    val context = LocalContext.current

    Scaffold(
        topBar = {
            BusStopTopBar(
                onSearchKeywordChange = viewModel::search,
                onScanClick = {
                    Analytics.logClickQrScanner()
                    QRScanner.start(onSuccess = {
                        processBarcode(context, it)
                        Analytics.logOpenGoogleQrScanner()
                    }, onError = {
                        val intent = Intent(context, QRScannerActivity::class.java)
                        context.startActivity(intent)
                    })
                }
            )
        }
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Spacer(modifier = Modifier.height(it.calculateTopPadding()))
            LazyColumn {
                items(stops) {
                    ItemBusStop(context, it)
                }
            }
        }
    }
}

@Composable
fun ItemBusStop(
    context: Context,
    stop: BusStop,
    isDistance: Boolean = false,
    distance: Double = 1.0
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                val intent = Intent(context, TimeTableActivity::class.java)
                intent.putExtra("stop_id", stop.code)
                intent.putExtra("stop", stop)
                context.startActivity(intent)
            }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = if (isDistance) {
                val isMoreThanKm = (distance / 1000).toInt() != 0
                val distanceInKms = (distance / 1000)
                if (isMoreThanKm) {
                    "${DecimalFormat("#.#").format(distanceInKms)}${stringResource(id = R.string.km)}"
                } else "${DecimalFormat("#.#").format(distance)}${stringResource(id = R.string.m)}"
            } else "ID:${stop.code}",
            modifier = Modifier
                .defaultMinSize(85.dp)
                .border(2.dp, DynamicPrimary, RoundedCornerShape(8.dp))
                .padding(horizontal = 16.dp, vertical = 12.dp),
            color = DynamicWhite,
            fontWeight = FontWeight.SemiBold,
            fontSize = 15.sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = "${stop.name}",
            modifier = Modifier,
            color = DynamicWhite,
            fontWeight = FontWeight.Normal,
            fontSize = 15.sp
        )
    }
}

private fun processBarcode(c: Context, b: Barcode) {
    when (b.valueType) {
        Barcode.TYPE_SMS -> processSMSBarcode(c, b)
    }
}

private fun processSMSBarcode(context: Context, b: Barcode) {
    b.sms?.message?.also {
        val intent = Intent(context, TimeTableActivity::class.java)
        intent.putExtra("stop_id", it)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }
}