package ge.tbilisipublictransport.presentation.home

import android.Manifest
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.rememberPermissionState
import com.mapbox.mapboxsdk.geometry.LatLng
import ge.tbilisipublictransport.common.util.LocationUtil
import ge.tbilisipublictransport.domain.model.BusStop
import ge.tbilisipublictransport.domain.model.Route
import ge.tbilisipublictransport.presentation.bus_routes.RouteItem
import ge.tbilisipublictransport.presentation.bus_stops.ItemBusStop
import ge.tbilisipublictransport.presentation.main.MainNavigationScreen


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {

    val context = LocalContext.current

    val locationPermissionState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ),
        onPermissionsResult = {
            if (it.all { it.value }) {
                LocationUtil.getMyLocation(context, onSuccess = {
                    viewModel.fetchNearbyStops(LatLng(it.latitude, it.longitude))
                })
            }
        }
    )

    val notificationsPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        rememberPermissionState(permission = Manifest.permission.POST_NOTIFICATIONS)
    } else null


    LaunchedEffect(key1 = Unit) {
        if (locationPermissionState.allPermissionsGranted) {
            if (LocationUtil.isLocationTurnedOn(context)) {
                LocationUtil.getMyLocation(context, onSuccess = {
                    viewModel.fetchNearbyStops(LatLng(it.latitude, it.longitude))
                })
            }
        } else {
            locationPermissionState.launchMultiplePermissionRequest()
            notificationsPermission?.launchPermissionRequest()
        }
    }

    Scaffold(topBar = {
        TopBar()
    }) {
        it.calculateBottomPadding()

        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(54.dp))
            Spacer(modifier = Modifier.height(12.dp))
            FavoriteRoutes()
            Spacer(modifier = Modifier.height(16.dp))
            FavoriteStops()
            Spacer(modifier = Modifier.height(16.dp))
            NearbyStops(navController, viewModel.nearbyStops.collectAsState().value)
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
fun FavoriteRoutes() {
    val context = LocalContext.current
    Column {
        Header(text = "ტოპ მარშრუტები")
        Spacer(modifier = Modifier.height(8.dp))
        RouteItem(context = context, index = 1, item = Route.empty())
    }
}

@Composable
fun FavoriteStops() {
    Column {
        Header(text = "ფავორიტი გაჩერებები")
        Spacer(modifier = Modifier.height(8.dp))
        ItemBusStop(BusStop("312", "3123", "ხერგიანის ქ. #14", 0.0, 0.0))
        ItemBusStop(BusStop("312", "3123", "ხერგიანის ქ. #26", 0.0, 0.0))
    }
}

@Composable
fun NearbyStops(navController: NavController, stops: List<BusStop>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Header(text = "უახლოესი გაჩერებები")
        Spacer(modifier = Modifier.height(8.dp))

        stops.forEach {
            ItemBusStop(BusStop(it.id, it.code, it.name, it.lat, it.lng))
        }

        Spacer(modifier = Modifier.height(12.dp))

        FilledTonalButton(onClick = {
            navController.navigate(MainNavigationScreen.Stops.screenName)
        }, modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Text(
                text = "ყველას ნახვა",
                fontWeight = FontWeight.SemiBold,
                color = colorScheme.primary,
                fontSize = 15.sp,
            )
        }
    }
}

@Composable
private fun Header(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        fontWeight = FontWeight.ExtraBold,
        modifier = Modifier.then(modifier),
        color = colorScheme.primary,
        fontSize = 16.sp
    )
}

@Composable
fun TopBar() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .background(colorScheme.surfaceColorAtElevation(3.dp))
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "მთავარი",
            color = if (isSystemInDarkTheme()) Color.White else Color.DarkGray,
            //     fontFamily = FontFamily(Font(R.font.bpg_nino_mtavruli_bold)),
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
            modifier = Modifier.align(Alignment.CenterStart)
        )
    }
}