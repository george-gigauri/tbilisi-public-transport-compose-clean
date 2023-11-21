package ge.transitgeorgia.domain.repository

import ge.transitgeorgia.domain.model.ArrivalTime
import ge.transitgeorgia.domain.model.Bus
import ge.transitgeorgia.domain.model.RouteStop
import ge.transitgeorgia.domain.model.Schedule
import ge.transitgeorgia.module.domain.model.BusStop
import ge.transitgeorgia.module.domain.model.Route
import ge.transitgeorgia.module.domain.model.RouteInfo

interface ITransportRepository {
    suspend fun getRoutes(): List<Route>
    suspend fun getRouteByBus(busNumber: Int, isForward: Boolean = true): RouteInfo
    suspend fun getBusStopsByBusNumber(busNumber: Int): List<RouteStop>
    suspend fun getBusPositions(busNumber: Int, isForward: Boolean = true): List<Bus>
    suspend fun getStops(): List<BusStop>
    suspend fun getTimeTable(stopId: String): List<ArrivalTime>
    suspend fun getSchedule(routeNumber: Int, isForward: Boolean): List<Schedule>
}