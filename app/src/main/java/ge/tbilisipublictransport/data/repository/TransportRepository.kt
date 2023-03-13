package ge.tbilisipublictransport.data.repository

import ge.tbilisipublictransport.data.other.mapper.toDomain
import ge.tbilisipublictransport.data.remote.api.TransportApi
import ge.tbilisipublictransport.domain.model.*
import ge.tbilisipublictransport.domain.repository.ITransportRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransportRepository @Inject constructor(
    private val api: TransportApi
) : ITransportRepository {

    override suspend fun getRoutes(): List<Route> {
        return api.getRoutes().body()?.routes?.map { it.toDomain() }.orEmpty()
    }

    override suspend fun getRouteByBus(busNumber: Int): RouteInfo {
        return api.getBusInfo(busNumber).body()?.toDomain() ?: throw Exception("Error occurred")
    }

    override suspend fun getBusStopsByBusNumber(busNumber: Int): List<RouteStop> {
        return api.getRouteByBusNumber(busNumber).body()?.stops?.map { it.toDomain() }.orEmpty()
    }

    override suspend fun getBusPositions(busNumber: Int): List<Bus> {
        return api.getBusPositions(busNumber).body()?.map { it.toDomain() }.orEmpty()
    }

    override suspend fun getStops(): List<BusStop> {
        return api.getBusStops().body()?.map { it.toDomain() }.orEmpty()
    }
}