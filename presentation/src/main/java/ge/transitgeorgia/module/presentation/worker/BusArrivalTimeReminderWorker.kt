package ge.transitgeorgia.common.service.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.lifecycle.LiveData
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import ge.transitgeorgia.common.analytics.Analytics
import ge.transitgeorgia.domain.repository.ITransportRepository
import ge.transitgeorgia.module.common.other.Const
import ge.transitgeorgia.module.domain.util.ResultWrapper
import ge.transitgeorgia.module.presentation.R
import ge.transitgeorgia.module.presentation.screen.timetable.TimeTableActivity
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
    lateinit var repository: ITransportRepository

    private val notifiedRoutes = arrayListOf<Int>()

    override suspend fun doWork(): Result {
        val stopId = params.inputData.getString("stop_id")
        val routes = params.inputData.getIntArray("routes")
        val arrivalTime = params.inputData.getInt("arrival_time", 5)

        Analytics.logScheduleBusArrivalTimeAlert()

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
                when(val result = repository.getTimeTable(stopId)) {
                    is ResultWrapper.Success -> {
                        result.data.forEach {
                            if (it.time <= arrivalTime && routes.contains(it.routeNumber) &&
                                !notifiedRoutes.contains(it.routeNumber)
                            ) {
                                notifiedRoutes.add(it.routeNumber)
                                notify(stopId, it.routeNumber, it.time)
                            }
                        }
                    }
                    else -> Unit
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
            val importance = NotificationManager.IMPORTANCE_HIGH
            val mChannel = NotificationChannel("arrival-time-reminder", name, importance)
            mChannel.setSound(
                Const.notificationSound, AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION).build()
            )
            mChannel.enableVibration(true)
            mChannel.vibrationPattern = longArrayOf(1000, 500, 1000, 500, 2500)
            mChannel.description = descriptionText
            manager.deleteNotificationChannel("arrival-time-reminder")
            manager.createNotificationChannel(mChannel)
        }

        val builder = NotificationCompat.Builder(context, "arrival-time-reminder")
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setAutoCancel(true)
            .setContentTitle("$busNumber მოსვლის დრო")
            .setContentText("$arrivalTime წუთში")
            .setContentIntent(pendingIntent)
            .setVibrate(longArrayOf(1000, 500, 1000, 500, 2500))
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