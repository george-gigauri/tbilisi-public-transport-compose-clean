package ge.tbilisipublictransport.presentation.live_bus

import android.content.Context
import android.location.Address
import android.location.Geocoder
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.mapbox.mapboxsdk.geometry.LatLng
import ge.tbilisipublictransport.common.util.DistanceCalculator
import ge.tbilisipublictransport.domain.model.Bus
import ge.tbilisipublictransport.domain.model.RouteInfo
import ge.tbilisipublictransport.domain.model.RouteStop
import java.text.DecimalFormat
import java.util.*


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveBusInfoBottomSheet(
    state: SheetState = SheetState(true, SheetValue.Hidden),
    userLocation: LatLng = LatLng(),
    route1: RouteInfo = RouteInfo.empty(),
    route2: RouteInfo = RouteInfo.empty(),
    route1Buses: List<Bus> = emptyList(),
    route2Buses: List<Bus> = emptyList(),
    onCancel: () -> Unit = { }
) {
    ModalBottomSheet(
        sheetState = state,
        onDismissRequest = { onCancel.invoke() }
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            if (route1.stops.isNotEmpty()) RouteDirectionInfo(Color.Green, route1)
            if (route2.stops.isNotEmpty()) RouteDirectionInfo(Color.Red, route2)
            Spacer(modifier = Modifier.height(24.dp))
            TotalAvailableBusCount(route1Buses, route2Buses)
            Spacer(modifier = Modifier.height(24.dp))
            NearestBusStopInfo(userLocation, route1.stops, route2.stops)
            Spacer(modifier = Modifier.height(36.dp))
        }
    }
}

@Composable
fun RouteDirectionInfo(color: Color, route: RouteInfo) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(bottom = 12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .background(color, CircleShape)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = "${route.stops.firstOrNull()?.name}  ➡️ ${route.stops.lastOrNull()?.name}"
        )
    }
}

@Composable
fun TotalAvailableBusCount(route1Buses: List<Bus>, route2Buses: List<Bus>) {
    Column {
        Text(text = buildAnnotatedString {
            append("ჯამში, ამ მომენტში, მოძრაობს ")
            withStyle(
                style = SpanStyle(
                    fontWeight = FontWeight.ExtraBold
                )
            ) {
                append("${route1Buses.size + route2Buses.size}")
            }
            append(" ერთეული ავტობუსი.")
        })

        if (route1Buses.isNotEmpty() && route2Buses.isNotEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .background(Color.Green, CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = route1Buses.size.toString(), fontWeight = FontWeight.ExtraBold)
                Spacer(modifier = Modifier.width(24.dp))
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .background(Color.Red, CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = route2Buses.size.toString(), fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

@Composable
fun NearestBusStopInfo(
    location: LatLng,
    route1Stops: List<RouteStop>,
    route2Stops: List<RouteStop>
) {
    if (location.latitude != 0.0 && location.longitude != 0.0) {
        val nearestStop = DistanceCalculator.getNearestLatLng(
            location,
            route1Stops.plus(route2Stops).map { LatLng(it.lat, it.lng) }
        )

        Text(text = buildAnnotatedString {
            append("თქვენს ადგილმდებარეობასთან უახლოესი გაჩერება ")
            withStyle(
                SpanStyle(fontWeight = FontWeight.ExtraBold)
            ) {
                append("${DecimalFormat("#.#").format(nearestStop?.distance ?: 0.0)} მეტრში")
            }
            append(
                " მდებარეობს. "
            )
            withStyle(
                SpanStyle(fontWeight = FontWeight.ExtraBold)
            ) {
                append(
                    "მის: ${
                        getNearestStopAddress(
                            LocalContext.current,
                            nearestStop?.latLng
                        )
                    }."
                )
            }
        })
    }
}

private fun getNearestStopAddress(context: Context, point: LatLng?): String {
    val addresses: List<Address>?
    val geocoder: Geocoder = Geocoder(context, Locale.getDefault())

    if (point == null) return "---"

    addresses = geocoder.getFromLocation(point.latitude, point.longitude, 1)
    return addresses?.let {
        if (addresses.isNotEmpty()) {
            val address: String = addresses[0].getAddressLine(0)

            val city: String = addresses[0].locality
            val state: String = addresses[0].adminArea
            val knownName: String = addresses[0].featureName

            "$knownName"
        } else "---"
    } ?: "---"

}