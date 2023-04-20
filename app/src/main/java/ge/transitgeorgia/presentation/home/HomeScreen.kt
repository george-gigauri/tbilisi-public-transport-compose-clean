package ge.transitgeorgia.presentation.home

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.rememberPermissionState
import com.mapbox.mapboxsdk.geometry.LatLng
import ge.transitgeorgia.R
import ge.transitgeorgia.common.analytics.Analytics
import ge.transitgeorgia.common.util.LocationUtil
import ge.transitgeorgia.domain.model.BusStop
import ge.transitgeorgia.domain.model.Route
import ge.transitgeorgia.presentation.bus_routes.RouteItem
import ge.transitgeorgia.presentation.bus_stops.ItemBusStop
import ge.transitgeorgia.presentation.main.MainActivity
import ge.transitgeorgia.presentation.main.MainNavigationScreen
import ge.transitgeorgia.ui.theme.DynamicWhite

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {

    val context = LocalContext.current

    val topRoutes by viewModel.topRoutes.collectAsStateWithLifecycle()
    val favoriteStops by viewModel.favoriteStops.collectAsStateWithLifecycle()
    val nearbyStops by viewModel.nearbyStops.collectAsStateWithLifecycle()

    var isLocationEnabled by rememberSaveable { mutableStateOf(false) }
    var userLocation by rememberSaveable { mutableStateOf(LatLng()) }


    val locationPermissionState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ),
        onPermissionsResult = {
            if (it.all { i -> i.value }) {
                LocationUtil.getMyLocation(context, onSuccess = { loc ->
                    userLocation = LatLng(loc.latitude, loc.longitude)
                    viewModel.fetchNearbyStops(userLocation)
                })
            }
        }
    )

    val notificationsPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        rememberPermissionState(permission = Manifest.permission.POST_NOTIFICATIONS)
    } else null

    DisposableEffect(key1 = Unit) {
        LocationUtil.observeLocationStatus(context, onEnable = {
            isLocationEnabled = true
        }, onDisable = {
            isLocationEnabled = false
        })
        onDispose { LocationUtil.removeLocationObserver() }
    }

    LaunchedEffect(key1 = Unit) {
        if (locationPermissionState.allPermissionsGranted) {
            if (LocationUtil.isLocationTurnedOn(context)) {
                LocationUtil.getLastKnownLocation(context)?.let {
                    val newLatLng = LatLng(it.latitude, it.longitude)
                    userLocation = newLatLng
                    viewModel.fetchNearbyStops(userLocation)
                }
            }
        } else {
            locationPermissionState.launchMultiplePermissionRequest()
            notificationsPermission?.launchPermissionRequest()
        }
    }

    Scaffold(topBar = {
        TopBar(context, viewModel)
    }, modifier = Modifier.fillMaxSize()) {
        it.calculateBottomPadding()

        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(54.dp))
            Spacer(modifier = Modifier.height(12.dp))
            if (topRoutes.isNotEmpty()) {
                FavoriteRoutes(topRoutes)
                Spacer(modifier = Modifier.height(16.dp))
            }
            if (favoriteStops.isNotEmpty()) {
                FavoriteStops(context, favoriteStops)
                Spacer(modifier = Modifier.height(16.dp))
            }
            NearbyStops(context, navController, nearbyStops, isLocationEnabled, userLocation)
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
fun FavoriteRoutes(routes: List<Route>) {
    val context = LocalContext.current
    Column {
        Header(text = stringResource(id = R.string.top_routes))
        Spacer(modifier = Modifier.height(8.dp))
        routes.forEachIndexed { index, route ->
            RouteItem(context = context, index = index, item = route)
        }
    }
}

@Composable
private fun FavoriteStops(context: Context, stops: List<BusStop>) {
    Column {
        Header(text = stringResource(id = R.string.favorite_stops))
        Spacer(modifier = Modifier.height(8.dp))
        stops.forEach {
            ItemBusStop(context, it)
        }
    }
}

@Composable
fun NearbyStops(
    context: Context,
    navController: NavController,
    stops: List<BusStop>,
    isLocationEnabled: Boolean,
    userLocation: LatLng,
) {
    val activity = (LocalContext.current as? MainActivity)

    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Header(text = stringResource(id = R.string.nearby_stops))
        Spacer(modifier = Modifier.height(8.dp))

        if (isLocationEnabled) {
            stops.forEach {
                ItemBusStop(context, it, true, userLocation.distanceTo(LatLng(it.lat, it.lng)))
            }
        } else {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = buildAnnotatedString {
                    withStyle(
                        style = SpanStyle(color = DynamicWhite)
                    ) {
                        append(stringResource(id = R.string.label_home_screen_if_location_is_off_part_one))
                    }
                    withStyle(
                        style = SpanStyle(
                            color = colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    ) {
                        append(" ")
                        append(stringResource(id = R.string.label_home_screen_if_location_is_off_part_two))
                    }
                },
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .clickable {
                        Analytics.logLocationPrompt()
                        activity?.let { LocationUtil.requestLocation(it) {} }
                    },
                textAlign = TextAlign.Center,
                fontSize = 13.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        Spacer(modifier = Modifier.height(12.dp))

        FilledTonalButton(onClick = {
            navController.navigate(MainNavigationScreen.Stops.screenName)
        }, modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Text(
                text = stringResource(id = R.string.see_all),
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
fun TopBar(context: Context, viewModel: HomeViewModel) {
    val currentCity by viewModel.city.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .background(colorScheme.surfaceColorAtElevation(3.dp))
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = stringResource(id = R.string.home),
            color = if (isSystemInDarkTheme()) Color.White else Color.DarkGray,
            //     fontFamily = FontFamily(Font(R.font.bpg_nino_mtavruli_bold)),
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
            modifier = Modifier
                .wrapContentWidth()
                .align(Alignment.CenterStart)
        )

        CitySwitchDropDown(currentCity, {
            Analytics.logChangeCity(it.id)
            viewModel.setDefaultCity(it)
            val intent = Intent(context, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK xor Intent.FLAG_ACTIVITY_CLEAR_TASK
            (context as? MainActivity)?.finish()
            context.startActivity(intent)
            Runtime.getRuntime().exit(0)
        }, Modifier.align(Alignment.CenterEnd))
    }
}