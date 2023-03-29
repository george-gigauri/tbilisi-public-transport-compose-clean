package ge.tbilisipublictransport.data.remote.api

import ge.tbilisipublictransport.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface TransportApi {

    @GET("routers/ttc/routes")
    @Headers("Content-Type: application/json", "Accept: application/json")
    suspend fun getRoutes(
        @Query("type") type: Int = 3,
    ): Response<RouteResponse>

    @GET("routers/ttc/routeInfo")
    suspend fun getBusInfo(
        @Query("routeNumber") number: Int,
        @Query("type") type: String = "bus",
        @Query("forward") forward: Int = 0
    ): Response<RouteInfoDto>

    @GET("routers/ttc/index/stops")
    suspend fun getBusStops(): Response<List<BusStopDto>>

    @GET("routeStops")
    suspend fun getRouteByBusNumber(
        @Query("routeNumber") number: Int,
        @Query("forward") forward: Int = 0
    ): Response<RouteStopsResponse>

    @GET("buses")
    suspend fun getBusPositions(
        @Query("routeNumber") number: Int,
        @Query("forward") forward: Int = 0
    ): Response<List<BusDto>>
}