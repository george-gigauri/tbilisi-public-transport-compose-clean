package ge.tbilisipublictransport.data.repository

import ge.tbilisipublictransport.common.other.mapper.toDomain
import ge.tbilisipublictransport.common.other.mapper.toEntity
import ge.tbilisipublictransport.data.local.db.AppDatabase
import ge.tbilisipublictransport.data.remote.api.TransportApi
import ge.tbilisipublictransport.domain.model.*
import ge.tbilisipublictransport.domain.repository.ITransportRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransportRepository @Inject constructor(
    private val api: TransportApi,
    private val db: AppDatabase
) : ITransportRepository {

    override suspend fun getRoutes(): List<Route> {
        return api.getRoutes().body()?.routes?.map { it.toDomain() }.orEmpty().also {
            db.routeDao().deleteAll()
            db.routeDao().insert(it.map { it.toEntity() })
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

    override suspend fun getStops(): List<BusStop> {
        val body = api.getBusStops().body()?.map { it.toDomain() }.orEmpty().also {
            db.busStopDao().deleteAll()
            db.busStopDao().insertAll(it.map { b -> b.toEntity() })
        }
        return body
    }
}