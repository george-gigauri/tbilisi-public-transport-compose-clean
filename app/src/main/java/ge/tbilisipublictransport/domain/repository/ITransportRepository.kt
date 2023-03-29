package ge.tbilisipublictransport.domain.repository

import ge.tbilisipublictransport.domain.model.*

interface ITransportRepository {
    suspend fun getRoutes(): List<Route>
    suspend fun getRouteByBus(busNumber: Int, isForward: Boolean): RouteInfo
    suspend fun getBusStopsByBusNumber(busNumber: Int): List<RouteStop>
    suspend fun getBusPositions(busNumber: Int): List<Bus>
    suspend fun getStops(): List<BusStop>
}