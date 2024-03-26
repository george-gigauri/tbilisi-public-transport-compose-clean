package ge.transitgeorgia.data.repository

import android.util.Log
import ge.transitgeorgia.common.other.mapper.toDomain
import ge.transitgeorgia.common.other.mapper.toEntity
import ge.transitgeorgia.data.remote.api.TransportApi
import ge.transitgeorgia.domain.model.ArrivalTime
import ge.transitgeorgia.domain.model.Bus
import ge.transitgeorgia.domain.model.RouteStop
import ge.transitgeorgia.domain.model.Schedule
import ge.transitgeorgia.domain.repository.ITransportRepository
import ge.transitgeorgia.module.data.local.db.AppDatabase
import ge.transitgeorgia.module.data.mapper.toDomain
import ge.transitgeorgia.module.data.mapper.toEntity
import ge.transitgeorgia.module.domain.model.BusStop
import ge.transitgeorgia.module.domain.model.Route
import ge.transitgeorgia.module.domain.model.RouteInfo
import ge.transitgeorgia.module.domain.util.ErrorType
import ge.transitgeorgia.module.domain.util.ResultWrapper
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import javax.inject.Singleton

@Singleton
class TransportRepository constructor(
    private val api: TransportApi,
    private val db: AppDatabase,
    private val ioDispatcher: CoroutineDispatcher
) : ITransportRepository {

    override suspend fun getRoutes(): ResultWrapper<List<Route>> = withContext(ioDispatcher) {
        return@withContext try {
            ResultWrapper.Success(
                api.getRoutes().body()?.routes?.map { it.toDomain() }.orEmpty().also {
                    db.routeDao().deleteAll()
                    db.routeDao().insert(it.map { i -> i.toEntity() })
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
        busNumber: Int,
        isForward: Boolean
    ): ResultWrapper<RouteInfo> = withContext(ioDispatcher) {
        return@withContext try {
            ResultWrapper.Success(
                api.getBusInfo(
                    busNumber,
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
        busNumber: Int
    ): ResultWrapper<List<RouteStop>> = withContext(ioDispatcher) {
        return@withContext try {
            ResultWrapper.Success(
                api.getRouteByBusNumber(busNumber).body()?.stops?.map { it.toDomain() }
                    .orEmpty()
            )
        } catch (e: HttpException) {
            ResultWrapper.Error(ErrorType.Http(e.code(), e.message()))
        } catch (e: Exception) {
            ResultWrapper.Error(ErrorType.Unknown(e.message))
        }
    }

    override suspend fun getBusPositions(
        busNumber: Int,
        isForward: Boolean
    ): ResultWrapper<List<Bus>> = withContext(ioDispatcher) {
        return@withContext try {
            ResultWrapper.Success(
                api.getBusPositions(
                    busNumber,
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
                db.busStopDao().insertAll(it.map { b -> b.toEntity() })
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
        routeNumber: Int,
        isForward: Boolean
    ): ResultWrapper<List<Schedule>> = withContext(Dispatchers.IO) {
        return@withContext try {
            ResultWrapper.Success(
                api.getSchedule(
                    routeNumber,
                    if (isForward) 1 else 0,
                    if (routeNumber in 1..2) 1 else 3
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