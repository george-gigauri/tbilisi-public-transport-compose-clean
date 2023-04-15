package ge.transitgeorgia.domain.repository

import ge.transitgeorgia.domain.model.*

interface ITransportRepository {
    suspend fun getRoutes(): List<Route>
    suspend fun getRouteByBus(busNumber: Int, isForward: Boolean = true): RouteInfo
    suspend fun getBusStopsByBusNumber(busNumber: Int): List<RouteStop>
    suspend fun getBusPositions(busNumber: Int, isForward: Boolean = true): List<Bus>
    suspend fun getStops(): List<BusStop>
    suspend fun getTimeTable(stopId: String): List<ArrivalTime>
}