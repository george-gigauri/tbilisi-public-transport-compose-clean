package ge.transitgeorgia.common.service.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import ge.transitgeorgia.common.util.Translator
import ge.transitgeorgia.data.local.datastore.AppDataStore
import ge.transitgeorgia.data.local.db.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltWorker
class TranslateDataWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted private val params: WorkerParameters
) : CoroutineWorker(context, params) {

    @Inject
    lateinit var db: AppDatabase

    @Inject
    lateinit var dataStore: AppDataStore

    override suspend fun doWork(): Result {
        withContext(Dispatchers.IO) {
            delay(35 * 1000L)
            listOf(
                async { translateBusStops() },
                async { translateRoutes() },
            ).joinAll()
        }
        return Result.success()
    }

    private suspend fun translateBusStops() = withContext(Dispatchers.IO) {
        val translatedList = db.busStopDao().getStops().map {
            it.apply { this.name = Translator.toEnglish(this.name) }
        }
        db.busStopDao().updateAll(translatedList)
    }

    private suspend fun translateRoutes() {
        val translatedList = db.routeDao().getAll().map {
            it.apply {
                this.firstStation = Translator.toEnglish(this.firstStation)
                this.lastStation = Translator.toEnglish(this.lastStation)
                this.longName = Translator.toEnglish(this.longName)
            }
        }
        db.routeDao().update(translatedList)
    }


    companion object {
        fun start(context: Context) {
            WorkManager.getInstance(context).beginUniqueWork(
                "translator",
                ExistingWorkPolicy.REPLACE,
                OneTimeWorkRequest.Companion.from(TranslateDataWorker::class.java)
            ).enqueue()
        }
    }
}