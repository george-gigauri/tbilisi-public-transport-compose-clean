package ge.transitgeorgia.module.data.repository

import ge.transitgeorgia.module.data.mapper.rustavi.toDomain
import ge.transitgeorgia.module.data.mapper.rustavi.toEntity
import ge.transitgeorgia.data.remote.api.RustaviTransportApi
import ge.transitgeorgia.module.domain.model.ArrivalTime
import ge.transitgeorgia.domain.model.Bus
import ge.transitgeorgia.domain.model.RouteStop
import ge.transitgeorgia.domain.model.Schedule
import ge.transitgeorgia.module.domain.repository.ITransportRepository
import ge.transitgeorgia.module.data.di.qualifier.dispatcher.IODispatcher
import ge.transitgeorgia.module.data.local.db.AppDatabase
import ge.transitgeorgia.module.domain.model.BusStop
import ge.transitgeorgia.module.domain.model.Route
import ge.transitgeorgia.module.domain.model.RouteInfo
import ge.transitgeorgia.module.domain.util.ErrorType
import ge.transitgeorgia.module.domain.util.ResultWrapper
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import javax.inject.Inject

class RustaviTransportRepository @Inject constructor(
    private val api: RustaviTransportApi,
    private val db: AppDatabase,
    @IODispatcher private val ioDispatcher: CoroutineDispatcher
) : ITransportRepository {

    override suspend fun getRoutes(): ResultWrapper<List<Route>> = withContext(ioDispatcher) {
        return@withContext try {
            ResultWrapper.Success(
                api.getRoutes().body()?.routes?.map { it.toDomain() }.orEmpty().also {
                    db.routeDao().deleteAll()
                    db.routeDao().insert(it.map { i -> i.toEntity().copy(id = i.id) })
                }
            )
        } catch (e: HttpException) {
            e.printStackTrace()
            ResultWrapper.Error(ErrorType.Http(e.code(), e.message()))
        } catch (e: Exception) {
            e.printStackTrace()
            ResultWrapper.Error(ErrorType.Unknown(e.message))
        }
    }

    override suspend fun getRouteByBus(
        routeId: String,
        isForward: Boolean
    ): ResultWrapper<RouteInfo> = withContext(ioDispatcher) {
        val route = db.routeDao().getRouteById(routeId)
        return@withContext try {
            ResultWrapper.Success(
                api.getBusInfo(
                    route?.number?.toIntOrNull() ?: -1,
                    forward = if (isForward) 1 else 0
                ).body()?.toDomain() ?: throw Exception("Error occurred")
            )
        } catch (e: HttpException) {
            ResultWrapper.Error(ErrorType.Http(e.code(), e.message()))
        } catch (e: Exception) {
            ResultWrapper.Error(ErrorType.Unknown(e.message))
        }
    }

    override suspend fun getBusStopsByBusNumber(
        routeId: String
    ): ResultWrapper<List<RouteStop>> = withContext(ioDispatcher) {
        val route = db.routeDao().getRouteById(routeId)
        return@withContext try {
            val route1 = api.getBusInfo(
                route?.number?.toIntOrNull() ?: -1,
                forward = 1
            )
                .body()?.stops?.map { it.toDomain() }
                .orEmpty()

            val route2 = api.getBusInfo(
                route?.number?.toIntOrNull() ?: -1,
                forward = 0
            )
                .body()?.stops?.map { it.toDomain() }
                .orEmpty()

            ResultWrapper.Success(
                listOf(route1, route2).flatten()
            )
        } catch (e: HttpException) {
            ResultWrapper.Error(ErrorType.Http(e.code(), e.message()))
        } catch (e: Exception) {
            ResultWrapper.Error(ErrorType.Unknown(e.message))
        }
    }

    override suspend fun getBusPositions(
        routeId: String,
        isForward: Boolean
    ): ResultWrapper<List<Bus>> = withContext(ioDispatcher) {
        val route = db.routeDao().getRouteById(routeId)
        return@withContext try {
            ResultWrapper.Success(
                api.getBusPositions(
                    route?.number?.toIntOrNull() ?: -1,
                    if (isForward) 1 else 0
                ).body()?.buses?.map { it.toDomain() }.orEmpty()
            )
        } catch (e: HttpException) {
            ResultWrapper.Error(ErrorType.Http(e.code(), e.message()))
        } catch (e: Exception) {
            ResultWrapper.Error(ErrorType.Unknown(e.message))
        }
    }

    override suspend fun getStops(): ResultWrapper<List<BusStop>> = withContext(ioDispatcher) {
        return@withContext try {
            val body = api.getBusStops().body()?.map { it.toDomain() }.orEmpty().also {
                db.busStopDao().deleteAll()
                db.busStopDao().insertAll(it.map { b -> b.toEntity().copy(id = b.code) })
            }
            ResultWrapper.Success(body)
        } catch (e: HttpException) {
            ResultWrapper.Error(ErrorType.Http(e.code(), e.message()))
        } catch (e: Exception) {
            ResultWrapper.Error(ErrorType.Unknown(e.message))
        }
    }

    override suspend fun getTimeTable(
        stopId: String
    ): ResultWrapper<List<ArrivalTime>> = withContext(ioDispatcher) {
        return@withContext try {
            ResultWrapper.Success(
                api.getTimeTableInformation(stopId).body()?.arrivalTimes?.map {
                    it.toDomain()
                }.orEmpty().sortedBy { it.time }
            )
        } catch (e: HttpException) {
            ResultWrapper.Error(ErrorType.Http(e.code(), e.message()))
        } catch (e: Exception) {
            ResultWrapper.Error(ErrorType.Unknown(e.message))
        }
    }

    override suspend fun getSchedule(
        routeId: String,
        isForward: Boolean
    ): ResultWrapper<List<Schedule>> = withContext(Dispatchers.IO) {
        val route = db.routeDao().getRouteById(routeId)
        return@withContext try {
            ResultWrapper.Success(
                api.getSchedule(
                    route?.number?.toIntOrNull() ?: -1,
                    if (isForward) 1 else 0,
                    if (routeId.toIntOrNull() in 1..2) 1 else 3
                ).body()
                    ?.toDomain()
                    .orEmpty()
            )
        } catch (e: HttpException) {
            ResultWrapper.Error(ErrorType.Http(e.code(), e.message()))
        } catch (e: Exception) {
            ResultWrapper.Error(ErrorType.Unknown(e.message))
        }
    }
}