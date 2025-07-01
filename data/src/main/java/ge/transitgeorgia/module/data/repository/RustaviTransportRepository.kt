package ge.transitgeorgia.module.data.repository

import ge.transitgeorgia.module.data.mapper.rustavi.toEntity
import ge.transitgeorgia.module.data.remote.api.RustaviTransportApi
import ge.transitgeorgia.module.domain.model.ArrivalTime
import ge.transitgeorgia.domain.model.Bus
import ge.transitgeorgia.domain.model.RouteStop
import ge.transitgeorgia.domain.model.Schedule
import ge.transitgeorgia.module.domain.repository.ITransportRepository
import ge.transitgeorgia.module.data.di.qualifier.dispatcher.IODispatcher
import ge.transitgeorgia.module.data.local.db.AppDatabase
import ge.transitgeorgia.module.data.mapper.tbilisi.toDomain
import ge.transitgeorgia.module.domain.model.BusStop
import ge.transitgeorgia.module.domain.model.Route
import ge.transitgeorgia.module.domain.model.RouteInfo
import ge.transitgeorgia.module.domain.util.ErrorType
import ge.transitgeorgia.module.domain.util.ResultWrapper
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import javax.inject.Inject
import kotlin.collections.map

class RustaviTransportRepository @Inject constructor(
    private val api: RustaviTransportApi,
    private val db: AppDatabase,
    @IODispatcher private val dispatcher: CoroutineDispatcher
) : ITransportRepository {

    override suspend fun getRoutes(): ResultWrapper<List<Route>> = withContext(dispatcher) {
        return@withContext try {
            ResultWrapper.Success(
                api.getRoutes().map { it.toDomain() }.also {
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
        routeId: String,
        isForward: Boolean
    ): ResultWrapper<RouteInfo> = withContext(dispatcher) {
        return@withContext try {
            ResultWrapper.Success(
                api.getRouteInfo(routeId).toDomain().copy(
                    polylineHash = api.getRoutePolyline(routeId, isForward).encodedValue
                )
            )
        } catch (e: HttpException) {
            e.printStackTrace()
            ResultWrapper.Error(ErrorType.Http(e.code(), e.message()))
        } catch (e: Exception) {
            e.printStackTrace()
            ResultWrapper.Error(ErrorType.Unknown(e.message))
        }
    }

    override suspend fun getBusStopsByBusNumber(routeId: String): ResultWrapper<List<RouteStop>> =
        withContext(dispatcher) {
            return@withContext try {
                val forward = try {
                    api.getRouteStops(routeId, true).map {
                        it.toDomain()
                    }.map { s: BusStop ->
                        RouteStop(
                            s.id,
                            s.name,
                            true,
                            s.lat,
                            s.lng
                        )
                    }
                } catch (e: Exception) {
                    emptyList()
                }

                val backward = try {
                    api.getRouteStops(routeId, false).map {
                        it.toDomain()
                    }.map { s: BusStop ->
                        RouteStop(
                            s.id,
                            s.name,
                            false,
                            s.lat,
                            s.lng
                        )
                    }
                } catch (e: Exception) {
                    emptyList()
                }

                ResultWrapper.Success(
                    listOf(forward, backward).flatten()
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
    ): ResultWrapper<List<Bus>> = withContext(dispatcher) {
        return@withContext try {
            ResultWrapper.Success(
                api.getBusPositions(
                    routeId,
                    isForward
                ).map { it.toDomain() }
            )
        } catch (e: HttpException) {
            e.printStackTrace()
            ResultWrapper.Error(ErrorType.Http(e.code(), e.message()))
        } catch (e: Exception) {
            e.printStackTrace()
            ResultWrapper.Error(ErrorType.Unknown(e.message))
        }
    }

    override suspend fun getStops(): ResultWrapper<List<BusStop>> = withContext(dispatcher) {
        return@withContext try {
            val body = api.getAllStops().map { it.toDomain() }.also {
                db.busStopDao().deleteAll()
                db.busStopDao().insertAll(it.map { b -> b.toEntity().copy(code = b.id) })
            }
            ResultWrapper.Success(body)
        } catch (e: HttpException) {
            e.printStackTrace()
            ResultWrapper.Error(ErrorType.Http(e.code(), e.message()))
        } catch (e: Exception) {
            e.printStackTrace()
            ResultWrapper.Error(ErrorType.Unknown(e.message))
        }
    }

    override suspend fun getTimeTable(stopId: String): ResultWrapper<List<ArrivalTime>> =
        withContext(dispatcher) {
            return@withContext try {
                ResultWrapper.Success(
                    api.getBusStopTimetable(stopId).map {
                        it.toDomain()
                    }.sortedBy { it.time }
                )
            } catch (e: HttpException) {
                e.printStackTrace()
                ResultWrapper.Error(ErrorType.Http(e.code(), e.message()))
            } catch (e: Exception) {
                e.printStackTrace()
                ResultWrapper.Error(ErrorType.Unknown(e.message))
            }
        }

    override suspend fun getSchedule(
        routeId: String,
        isForward: Boolean
    ): ResultWrapper<List<Schedule>> = withContext(dispatcher) {
        return@withContext try {
            ResultWrapper.Success(
                api.getSchedule(
                    routeId,
                    isForward,
                ).toDomain()
            )
        } catch (e: HttpException) {
            ResultWrapper.Error(ErrorType.Http(e.code(), e.message()))
        } catch (e: Exception) {
            ResultWrapper.Error(ErrorType.Unknown(e.message))
        }
    }
}