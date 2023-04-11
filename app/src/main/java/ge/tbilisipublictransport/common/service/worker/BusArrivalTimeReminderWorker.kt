package ge.tbilisipublictransport.common.service.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.lifecycle.LiveData
import androidx.work.*
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import ge.tbilisipublictransport.R
import ge.tbilisipublictransport.data.repository.TransportRepository
import ge.tbilisipublictransport.presentation.timetable.TimeTableActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.random.Random
import kotlin.random.nextInt

@HiltWorker
class BusArrivalTimeReminderWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted private val params: WorkerParameters
) : CoroutineWorker(context, params) {

    @Inject
    lateinit var repository: TransportRepository

    private val notifiedRoutes = arrayListOf<Int>()

    override suspend fun doWork(): Result {
        val stopId = params.inputData.getString("stop_id")
        val routes = params.inputData.getIntArray("routes")
        val arrivalTime = params.inputData.getInt("arrival_time", 5)


        return if (stopId != null && routes?.isNotEmpty() == true) {
            fetch(stopId, routes, arrivalTime)
            Result.success()
        } else Result.failure()
    }

    private suspend fun fetch(
        stopId: String,
        routes: IntArray,
        arrivalTime: Int
    ) = withContext(Dispatchers.IO) {
        while (true) {
            delay(Random.nextInt(IntRange(7, 20)) * 1000L)
            try {
                val result = repository.getTimeTable(stopId)
                result.forEach {
                    if (it.time <= arrivalTime && routes.contains(it.routeNumber) &&
                        !notifiedRoutes.contains(it.routeNumber)
                    ) {
                        notifiedRoutes.add(it.routeNumber)
                        notify(stopId, it.routeNumber, it.time)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            if (notifiedRoutes.containsAll(routes.toList())) break
        }
    }

    private suspend fun notify(
        stopId: String,
        busNumber: Int,
        arrivalTime: Int
    ) = withContext(Dispatchers.Main) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val intent = Intent(context, TimeTableActivity::class.java)
        intent.putExtra("stop_id", stopId)
        val pendingIntent = PendingIntent.getActivity(
            context,
            901,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT xor PendingIntent.FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Bus Arrival Time Reminder"
            val descriptionText = "-"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val mChannel = NotificationChannel("arrival-time-reminder", name, importance)
            mChannel.description = descriptionText
            manager.createNotificationChannel(mChannel)
        }

        val builder = NotificationCompat.Builder(context, "arrival-time-reminder")
            .setSmallIcon(R.drawable.ic_marker_bus_forward)
            .setAutoCancel(true)
            .setContentTitle("$busNumber")
            .setContentText("ავტობუსი [#$busNumber] მოვა $arrivalTime წუთში")
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        manager.notify(Random.nextInt(), builder.build())
    }

    companion object {
        fun start(
            context: Context,
            stopId: String,
            arrivalTime: Int,
            routeNumbers: List<Int>
        ) {
            WorkManager.getInstance(context).cancelUniqueWork(stopId)
            val data = Data.Builder()
                .putString("stop_id", stopId)
                .putIntArray("routes", routeNumbers.toIntArray())
                .putInt("arrival_time", arrivalTime)
                .build()
            val workRequest = OneTimeWorkRequest.Builder(BusArrivalTimeReminderWorker::class.java)
                .setInputData(data)
                .addTag(stopId)
                .build()
            WorkManager.getInstance(context).enqueueUniqueWork(
                stopId,
                ExistingWorkPolicy.REPLACE,
                workRequest
            )
        }

        fun stop(context: Context, stopId: String) {
            WorkManager.getInstance(context).cancelUniqueWork(stopId)
        }

        fun getWorkInfo(context: Context, stopId: String): LiveData<List<WorkInfo>> {
            return WorkManager.getInstance(context).getWorkInfosForUniqueWorkLiveData(stopId)
        }
    }
}