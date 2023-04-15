package ge.transitgeorgia.common.service.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.media.AudioAttributes
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.lifecycle.LiveData
import androidx.work.*
import com.mapbox.mapboxsdk.geometry.LatLng
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import ge.transitgeorgia.common.analytics.Analytics
import ge.transitgeorgia.common.other.Const
import ge.transitgeorgia.data.repository.TransportRepository
import ge.transitgeorgia.domain.model.Bus
import ge.transitgeorgia.presentation.live_bus.LiveBusActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.random.Random

@HiltWorker
class BusDistanceReminderWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted private val params: WorkerParameters
) : CoroutineWorker(context, params) {

    @Inject
    lateinit var repository: TransportRepository

    override suspend fun doWork(): Result {

        val busNumber = params.inputData.getInt("route_number", -1)
        val userLocation = params.inputData.getDoubleArray("user_lat_lng")?.let {
            LatLng(it.first(), it.last())
        }
        val distance = params.inputData.getInt("distance", 0)
        val isForward = params.inputData.getBoolean("is_forward", true)

        Analytics.logScheduleBusDistanceNotifier()

        if (userLocation != null) {
            fetch(busNumber, isForward, userLocation, distance)
        } else {
            Log.e("BusDistanceWorker", "User Location is NULL")
            return Result.failure()
        }

        return Result.success()
    }

    private suspend fun fetch(
        busNumber: Int,
        isForward: Boolean,
        location: LatLng,
        distance: Int
    ) = withContext(Dispatchers.IO) {
        var isDone = false
        var loopCounter = 0
        while (!isDone) {
            try {
                repository.getBusPositions(busNumber, isForward).let {
                    isDone = process(it, location, distance)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            loopCounter++
            delay(
                when (loopCounter) {
                    in 0..45 -> 15000
                    in 46..105 -> 8500
                    else -> 7500
                }
            )

            if (loopCounter == 155) isDone = true
        }
    }

    private suspend fun process(buses: List<Bus>, location: LatLng, distance: Int): Boolean {
        val thatOneBus = buses.find {
            val latLng = LatLng(it.lat, it.lng)
            val realDistance = latLng.distanceTo(location)
            realDistance <= distance
        }

        if (thatOneBus != null) {
            val latLng = LatLng(thatOneBus.lat, thatOneBus.lng)
            notify(thatOneBus.number, location.distanceTo(latLng))
        }

        return thatOneBus != null
    }

    private suspend fun notify(busNumber: Int, distance: Double) = withContext(Dispatchers.Main) {
        val manager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        val intent = Intent(context, LiveBusActivity::class.java)
        intent.putExtra("route_number", busNumber)
        val pendingIntent = PendingIntent.getActivity(
            context,
            901,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT xor PendingIntent.FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Bus Distance Reminder"
            val descriptionText = "-"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val mChannel = NotificationChannel("distance-reminder", name, importance)
            mChannel.setSound(
                Const.notificationSound, AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION).build()
            )
            mChannel.enableVibration(true)
            mChannel.vibrationPattern = longArrayOf(1000, 500, 1000, 500, 2500)
            mChannel.description = descriptionText
            manager.deleteNotificationChannel("distance-reminder")
            manager.createNotificationChannel(mChannel)
        }

        val builder = NotificationCompat.Builder(context, "distance-reminder")
            .setSmallIcon(ge.transitgeorgia.R.mipmap.ic_launcher_round)
            .setAutoCancel(true)
            .setContentTitle("$busNumber")
            .setContentText("ავტობუსი [$busNumber] შენგან უკვე $distance მეტრშია!")
            .setContentIntent(pendingIntent)
            .setVibrate(longArrayOf(1000, 500, 1000, 500, 2500))
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        manager.notify(Random.nextInt(), builder.build())
    }

    companion object {
        fun start(
            context: Context,
            busNumber: Int,
            userLocation: LatLng,
            distance: Int,
            isForward: Boolean
        ) {
            WorkManager.getInstance(context).cancelUniqueWork(busNumber.toString())
            val data = Data.Builder()
                .putInt("route_number", busNumber)
                .putDoubleArray(
                    "user_lat_lng",
                    doubleArrayOf(userLocation.latitude, userLocation.longitude)
                )
                .putInt("distance", distance)
                .putBoolean("is_forward", isForward)
                .build()
            val workRequest = OneTimeWorkRequest.Builder(BusDistanceReminderWorker::class.java)
                .setInputData(data)
                .addTag(busNumber.toString())
                .build()
            WorkManager.getInstance(context).enqueueUniqueWork(
                busNumber.toString(),
                ExistingWorkPolicy.REPLACE,
                workRequest
            )
        }

        fun stop(context: Context, routeNumber: Int?) {
            WorkManager.getInstance(context).cancelUniqueWork(routeNumber!!.toString())
        }

        fun getWorkInfo(context: Context, routeNumber: Int): LiveData<List<WorkInfo>> {
            return WorkManager.getInstance(context)
                .getWorkInfosForUniqueWorkLiveData(routeNumber.toString())
        }
    }
}