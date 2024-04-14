package ge.transitgeorgia.module.presentation.screen.home

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
import androidx.compose.ui.res.painterResource
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
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.rememberPermissionState
import ge.transitgeorgia.common.analytics.Analytics
import ge.transitgeorgia.module.common.util.LocationUtil
import ge.transitgeorgia.module.domain.model.BusStop
import ge.transitgeorgia.module.domain.model.Route
import ge.transitgeorgia.module.presentation.R
import ge.transitgeorgia.module.presentation.screen.RouteItem
import ge.transitgeorgia.module.presentation.screen.bus_stops.ItemBusStop
import ge.transitgeorgia.module.presentation.screen.main.MainActivity
import ge.transitgeorgia.module.presentation.screen.main.MainNavigationScreen
import ge.transitgeorgia.module.presentation.theme.DynamicWhite
import ge.transitgeorgia.presentation.home.CitySwitchDropDown
import kotlinx.coroutines.delay
import org.osmdroid.util.GeoPoint

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
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
    var userLocation by rememberSaveable { mutableStateOf(GeoPoint(0.0, 0.0)) }

    val isLocationDisclosureVisible by viewModel.isLocationDisclosureAnswered.collectAsState()

    val locationPermissionState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
        ),
        onPermissionsResult = {
            if (it.all { i -> i.value }) {
                LocationUtil.getMyLocation(context, onSuccess = { loc ->
                    userLocation = GeoPoint(loc.latitude, loc.longitude)
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
                    val newLatLng = GeoPoint(it.latitude, it.longitude)
                    userLocation = newLatLng
                    viewModel.fetchNearbyStops(userLocation)
                }
            }
        }

        delay(1500)
        if (notificationsPermission?.status != PermissionStatus.Granted) {
            notificationsPermission?.launchPermissionRequest()
        }
    }

    if (isLocationDisclosureVisible && !locationPermissionState.allPermissionsGranted) {
        LocationDisclosureSheet(
            state = rememberModalBottomSheetState(),
            onAccept = {
                locationPermissionState.launchMultiplePermissionRequest()
            },
            onCancel = {
                viewModel.answerLocationDisclosure()
            })
    }

    Scaffold(topBar = {
        TopBar(context, viewModel)
    }, modifier = Modifier.fillMaxSize()) {

        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(top = it.calculateTopPadding())
                .verticalScroll(rememberScrollState())
        ) {
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
        routes.forEach { route ->
            RouteItem(context = context, item = route)
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
    userLocation: GeoPoint,
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
                ItemBusStop(
                    context,
                    it,
                    true,
                    userLocation.distanceToAsDouble(GeoPoint(it.lat, it.lng))
                )
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

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {

            // Map
            FilledTonalButton(onClick = {
                navController.navigate(MainNavigationScreen.StopsMap.screenName)
            }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_map),
                    contentDescription = null,
                    tint = colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.width(8.dp))

            // All Stops
            FilledTonalButton(onClick = {
                navController.navigate(MainNavigationScreen.Stops.screenName)
            }) {
                Text(
                    text = stringResource(id = R.string.see_all),
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.primary,
                    fontSize = 15.sp,
                )
            }
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

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {

        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(42.dp)
                .background(colorScheme.surfaceColorAtElevation(3.dp))
        )

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
}