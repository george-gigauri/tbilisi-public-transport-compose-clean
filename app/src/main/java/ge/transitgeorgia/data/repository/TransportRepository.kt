package ge.transitgeorgia.data.repository

import ge.transitgeorgia.common.other.mapper.toDomain
import ge.transitgeorgia.common.other.mapper.toEntity
import ge.transitgeorgia.data.local.db.AppDatabase
import ge.transitgeorgia.data.remote.api.TransportApi
import ge.transitgeorgia.domain.model.ArrivalTime
import ge.transitgeorgia.domain.model.Bus
import ge.transitgeorgia.domain.model.BusStop
import ge.transitgeorgia.domain.model.Route
import ge.transitgeorgia.domain.model.RouteInfo
import ge.transitgeorgia.domain.model.RouteStop
import ge.transitgeorgia.domain.repository.ITransportRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Singleton

@Singleton
class TransportRepository constructor(
    private val api: TransportApi,
    private val db: AppDatabase,
    private val ioDispatcher: CoroutineDispatcher
) : ITransportRepository {

    override suspend fun getRoutes(): List<Route> = withContext(ioDispatcher) {
        return@withContext api.getRoutes().body()?.routes?.map { it.toDomain() }.orEmpty().also {
            db.routeDao().deleteAll()
            db.routeDao().insert(it.map { i -> i.toEntity() })
        }
    }

    override suspend fun getRouteByBus(busNumber: Int, isForward: Boolean): RouteInfo =
        withContext(ioDispatcher) {
            return@withContext api.getBusInfo(
                busNumber,
                forward = if (isForward) 1 else 0
            ).body()?.toDomain() ?: throw Exception("Error occurred")
        }

    override suspend fun getBusStopsByBusNumber(
        busNumber: Int
    ): List<RouteStop> = withContext(ioDispatcher) {
        return@withContext api.getRouteByBusNumber(busNumber).body()?.stops?.map { it.toDomain() }
            .orEmpty()
    }

    override suspend fun getBusPositions(
        busNumber: Int,
        isForward: Boolean
    ): List<Bus> = withContext(ioDispatcher) {
        return@withContext api.getBusPositions(
            busNumber,
            if (isForward) 1 else 0
        ).body()?.buses?.map { it.toDomain() }.orEmpty()
    }

    override suspend fun getStops(): List<BusStop> = withContext(ioDispatcher) {
        val body = api.getBusStops().body()?.map { it.toDomain() }.orEmpty().also {
            db.busStopDao().deleteAll()
            db.busStopDao().insertAll(it.map { b -> b.toEntity() })
        }
        return@withContext body
    }

    override suspend fun getTimeTable(
        stopId: String
    ): List<ArrivalTime> = withContext(ioDispatcher) {
        return@withContext api.getTimeTableInformation(stopId).body()?.arrivalTimes?.map {
            it.toDomain()
        }.orEmpty()
    }
}