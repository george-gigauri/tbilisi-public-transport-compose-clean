package ge.transitgeorgia.module.presentation.screen.live_bus

import android.content.Context
import android.location.Address
import android.location.Geocoder
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ge.transitgeorgia.module.common.other.extensions.calculateTotalLengthInMeters
import ge.transitgeorgia.module.common.util.DistanceCalculator
import ge.transitgeorgia.domain.model.Bus
import ge.transitgeorgia.domain.model.RouteStop
import ge.transitgeorgia.module.common.ext.calculateTotalLengthInMeters
import ge.transitgeorgia.module.domain.model.Route
import ge.transitgeorgia.module.domain.model.RouteInfo
import ge.transitgeorgia.module.presentation.R
import ge.transitgeorgia.module.presentation.theme.DynamicWhite
import org.osmdroid.util.GeoPoint
import java.text.DecimalFormat
import java.util.*


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveBusInfoBottomSheet(
    state: SheetState = SheetState(true, SheetValue.Hidden),
    userLocation: GeoPoint = GeoPoint(0.0, 0.0),
    route: RouteInfo? = null,
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
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(bottom = 56.dp)
        ) {
            if (route1.stops.isNotEmpty())
                RouteDirectionInfo(Color.Green, route?.backwardHeadSign, route?.forwardHeadSign)

            if (route2.stops.isNotEmpty())
                RouteDirectionInfo(Color.Red, route?.forwardHeadSign, route?.backwardHeadSign)

            Spacer(modifier = Modifier.height(24.dp))
            TotalAvailableBusCount(route1Buses, route2Buses)
            Spacer(modifier = Modifier.height(24.dp))
            RouteLength(route1, route2)
            Spacer(modifier = Modifier.height(24.dp))
            NearestBusStopInfo(userLocation, route1.stops, route2.stops)
            Spacer(modifier = Modifier.height(36.dp))
        }
    }
}

@Composable
@Preview
fun RouteLength(route1: RouteInfo = RouteInfo.empty(), route2: RouteInfo = RouteInfo.empty()) {
    val route1Length = convertMetersIntoKm(route1.polyline.calculateTotalLengthInMeters())
    val route2Length = convertMetersIntoKm(route2.polyline.calculateTotalLengthInMeters())

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.route_length),
            color = DynamicWhite,
            fontWeight = FontWeight.ExtraBold
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .background(Color.Green, CircleShape)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = "$route1Length ${stringResource(id = R.string.km)}", color = DynamicWhite)

            Spacer(modifier = Modifier.width(16.dp))
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .background(Color.Red, CircleShape)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = "$route2Length ${stringResource(id = R.string.km)}", color = DynamicWhite)
        }
    }
}

@Composable
fun RouteDirectionInfo(color: Color, startStation: String?, endStation: String?) {
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
            text = "$startStation  ➡️  $endStation"
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
    location: GeoPoint,
    route1Stops: List<RouteStop>,
    route2Stops: List<RouteStop>
) {
    val context = LocalContext.current
    var nearestStop: DistanceCalculator.DistanceLatLng? by remember { mutableStateOf(null) }
    var address: String by remember { mutableStateOf("") }

    LaunchedEffect(key1 = Unit) {
        if (location.latitude != 0.0 && location.longitude != 0.0) {
            nearestStop = DistanceCalculator.getNearestLatLng(
                location,
                route1Stops.plus(route2Stops).map { GeoPoint(it.lat, it.lng) }
            )
            address = getNearestStopAddress(
                context,
                GeoPoint(nearestStop?.latitude ?: 0.0, nearestStop?.longitude ?: 0.0)
            )
        }
    }

    if (location.latitude != 0.0 && location.longitude != 0.0) {
        Text(text = buildAnnotatedString {
            append("თქვენს ადგილმდებარეობასთან უახლოესი გაჩერება ")
            withStyle(SpanStyle(fontWeight = FontWeight.ExtraBold)) {
                append("${DecimalFormat("#.#").format(nearestStop?.distance ?: 0.0)} მეტრში")
            }
            append(" მდებარეობს. ")
            withStyle(SpanStyle(fontWeight = FontWeight.ExtraBold)) {
                append("მის: $address.")
            }
        })
    }
}

private fun getNearestStopAddress(context: Context, point: GeoPoint?): String {
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

private fun convertMetersIntoKm(distance: Double): String {
    val isMoreThanKm = (distance / 1000).toInt() != 0
    val distanceInKms = (distance / 1000)
    return if (isMoreThanKm) {
        DecimalFormat("#.#").format(distanceInKms)
    } else DecimalFormat("#.#").format(distance)
}