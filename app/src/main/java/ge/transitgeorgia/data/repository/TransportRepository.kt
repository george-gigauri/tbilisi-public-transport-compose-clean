package ge.transitgeorgia.data.repository

import ge.transitgeorgia.common.other.mapper.toDomain
import ge.transitgeorgia.common.other.mapper.toEntity
import ge.transitgeorgia.data.local.datastore.AppDataStore
import ge.transitgeorgia.data.local.db.AppDatabase
import ge.transitgeorgia.data.remote.api.TransportApi
import ge.transitgeorgia.domain.model.*
import ge.transitgeorgia.domain.repository.ITransportRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransportRepository @Inject constructor(
    private val api: TransportApi,
    private val db: AppDatabase,
    private val dataStore: AppDataStore
) : ITransportRepository {

    override suspend fun getRoutes(): List<Route> = withContext(Dispatchers.IO) {
        return@withContext api.getRoutes().body()?.routes?.map { it.toDomain() }.orEmpty().also {
            db.routeDao().deleteAll()
            db.routeDao().insert(it.map { i -> i.toEntity() })

//            // Translate and insert each item to the db
//            if (dataStore.language.first() == AppLanguage.Language.ENG) {
//                it.map { i ->
//                    async {
//                        i.toEntity().apply {
//                            longName = Translator.toEnglish(i.longName)
//                            firstStation = Translator.toEnglish(i.firstStation)
//                            lastStation = Translator.toEnglish(i.lastStation)
//                            db.routeDao().insert(this)
//                        }
//                    }
//                }.awaitAll()
//            }
        }
    }

    override suspend fun getRouteByBus(busNumber: Int, isForward: Boolean): RouteInfo {
        return api.getBusInfo(
            busNumber,
            forward = if (isForward) 1 else 0
        ).body()?.toDomain() ?: throw Exception("Error occurred")
    }

    override suspend fun getBusStopsByBusNumber(busNumber: Int): List<RouteStop> {
        return api.getRouteByBusNumber(busNumber).body()?.stops?.map { it.toDomain() }.orEmpty()
    }

    override suspend fun getBusPositions(busNumber: Int, isForward: Boolean): List<Bus> {
        return api.getBusPositions(
            busNumber,
            if (isForward) 1 else 0
        ).body()?.buses?.map { it.toDomain() }.orEmpty()
    }

    override suspend fun getStops(): List<BusStop> = withContext(Dispatchers.IO) {
        val body = api.getBusStops().body()?.map { it.toDomain() }.orEmpty().also {
            db.busStopDao().deleteAll()
            db.busStopDao().insertAll(it.map { b -> b.toEntity() })
        }
        return@withContext body
    }

    override suspend fun getTimeTable(stopId: String): List<ArrivalTime> {
        return api.getTimeTableInformation(stopId).body()?.arrivalTimes?.map {
            it.toDomain()
        }.orEmpty()
    }
}