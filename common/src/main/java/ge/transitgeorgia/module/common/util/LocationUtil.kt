package ge.transitgeorgia.module.common.util

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Context.LOCATION_SERVICE
import android.content.IntentSender.SendIntentException
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Looper
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.location.LocationSettingsStatusCodes
import com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.*

object LocationUtil {

    private var locationObserverScope = CoroutineScope(Dispatchers.Main)

    fun isLocationTurnedOn(context: Context): Boolean {
        val manager = (context.getSystemService(LOCATION_SERVICE) as LocationManager)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            manager.isLocationEnabled
        } else {
            manager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        }
    }

    fun requestLocation(context: Activity, onTurnedOn: () -> Unit) {
        val locationRequest: LocationRequest = LocationRequest.create()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 30 * 1000
        locationRequest.fastestInterval = 5 * 1000

        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)

        builder.setAlwaysShow(true)

        val result: Task<LocationSettingsResponse> =
                LocationServices.getSettingsClient(context).checkLocationSettings(builder.build())

        result.addOnCompleteListener { task ->
            onTurnedOn.invoke()

            try {
                val response = task.getResult(ApiException::class.java)
            } catch (exception: ApiException) {
                when (exception.statusCode) {
                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED ->
                        try {
                            val resolvable = exception as ResolvableApiException
                            resolvable.startResolutionForResult(context, 754)
                        } catch (e: SendIntentException) {
                        } catch (e: ClassCastException) {
                        }

                    LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {}
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun getMyLocation(
        context: Context,
        onSuccess: (Location) -> Unit = { },
        onError: () -> Unit = {}
    ) {
        LocationServices.getFusedLocationProviderClient(context)
            .requestLocationUpdates(
                LocationRequest.Builder(
                    PRIORITY_HIGH_ACCURACY,
                    100L,
                ).setWaitForAccurateLocation(true).build(),
                { p0 -> onSuccess(p0) },
                Looper.getMainLooper()
            )
    }

    @SuppressLint("MissingPermission")
    fun getLastKnownLocation(context: Context): Location? {
        return (context.getSystemService(LOCATION_SERVICE) as LocationManager)
            .getLastKnownLocation(LocationManager.PASSIVE_PROVIDER)
    }

    fun observeLocationStatus(context: Context, onEnable: () -> Unit, onDisable: () -> Unit = { }) {
        locationObserverScope.coroutineContext.cancelChildren()
        locationObserverScope.launch {
            while (locationObserverScope.isActive) {
                if (isLocationTurnedOn(context)) onEnable.invoke()
                else onDisable.invoke()
                delay(1500L)
            }
        }
    }

    fun removeLocationObserver() {
        locationObserverScope.coroutineContext.cancelChildren()
    }
}