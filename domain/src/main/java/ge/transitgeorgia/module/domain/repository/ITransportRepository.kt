package ge.transitgeorgia.domain.repository

import ge.transitgeorgia.domain.model.ArrivalTime
import ge.transitgeorgia.domain.model.Bus
import ge.transitgeorgia.domain.model.RouteStop
import ge.transitgeorgia.domain.model.Schedule
import ge.transitgeorgia.module.domain.model.BusStop
import ge.transitgeorgia.module.domain.model.Route
import ge.transitgeorgia.module.domain.model.RouteInfo
import ge.transitgeorgia.module.domain.util.ResultWrapper

interface ITransportRepository {
    suspend fun getRoutes(): ResultWrapper<List<Route>>
    suspend fun getRouteByBus(busNumber: Int, isForward: Boolean = true): ResultWrapper<RouteInfo>
    suspend fun getBusStopsByBusNumber(busNumber: Int): ResultWrapper<List<RouteStop>>
    suspend fun getBusPositions(busNumber: Int, isForward: Boolean = true): ResultWrapper<List<Bus>>
    suspend fun getStops(): ResultWrapper<List<BusStop>>
    suspend fun getTimeTable(stopId: String): ResultWrapper<List<ArrivalTime>>
    suspend fun getSchedule(routeNumber: Int, isForward: Boolean): ResultWrapper<List<Schedule>>
}