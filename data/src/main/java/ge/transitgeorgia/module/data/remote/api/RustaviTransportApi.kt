package ge.transitgeorgia.module.data.remote.api

import ge.transitgeorgia.module.data.remote.dto.tbilisi.ArrivalTimeDto
import ge.transitgeorgia.module.data.remote.dto.tbilisi.BusPositionDto
import ge.transitgeorgia.module.data.remote.dto.tbilisi.RouteDto
import ge.transitgeorgia.module.data.remote.dto.tbilisi.RoutePolylineDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface RustaviTransportApi {

    @GET("routes")
    suspend fun getRoutes(
        @Query("modes") from: String = "BUS,SUBWAY",
    ): List<RouteDto>

    @GET("routes/{id}")
    suspend fun getRouteInfo(
        @Path("id") id: String
    ): ge.transitgeorgia.module.data.remote.dto.tbilisi.RouteInfoDto

    @GET("routes/{id}/stops")
    suspend fun getRouteStops(
        @Path("id") id: String,
        @Query("forward") isForward: Boolean
    ): List<ge.transitgeorgia.module.data.remote.dto.tbilisi.BusStopDto>

    @GET("routes/{id}/polyline")
    suspend fun getRoutePolyline(
        @Path("id") id: String,
        @Query("forward") isForward: Boolean
    ): RoutePolylineDto

    @GET("routes/{id}/positions")
    suspend fun getBusPositions(
        @Path("id") id: String,
        @Query("forward") isForward: Boolean
    ): List<BusPositionDto>

    @GET("stops")
    suspend fun getAllStops(): List<ge.transitgeorgia.module.data.remote.dto.tbilisi.BusStopDto>

    @GET("stops/{id}/arrival-times")
    suspend fun getBusStopTimetable(
        @Path("id") id: String,
        @Query("ignoreScheduledArrivalTimes") ignoreSchedule: Boolean = true
    ): List<ArrivalTimeDto>

    @GET("routes/{id}/schedule")
    suspend fun getSchedule(
        @Path("id") id: String,
        @Query("forward") isForward: Boolean
    ): ge.transitgeorgia.module.data.remote.dto.tbilisi.ScheduleResponseDto
}