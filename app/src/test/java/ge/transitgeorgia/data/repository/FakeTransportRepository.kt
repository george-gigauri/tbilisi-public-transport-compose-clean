package ge.transitgeorgia.data.repository

import com.mapbox.mapboxsdk.geometry.LatLng
import ge.transitgeorgia.common.util.WeekDay
import ge.transitgeorgia.domain.model.ArrivalTime
import ge.transitgeorgia.domain.model.Bus
import ge.transitgeorgia.domain.model.BusStop
import ge.transitgeorgia.domain.model.Route
import ge.transitgeorgia.domain.model.RouteInfo
import ge.transitgeorgia.domain.model.RouteStop
import ge.transitgeorgia.domain.model.Schedule
import ge.transitgeorgia.domain.model.ScheduleStop
import ge.transitgeorgia.domain.repository.ITransportRepository
import java.util.UUID
import kotlin.random.Random

class FakeTransportRepository : ITransportRepository {

    private var routes = arrayListOf<Route>()

    init {
        routes.add(Route("1", "", "351", "", "", ""))
        routes.add(Route("2", "", "350", "", "", ""))
        routes.add(Route("3", "", "353", "", "", ""))
        routes.add(Route("4", "", "354", "", "", ""))
        routes.add(Route("5", "", "359", "", "", ""))
        routes.add(Route("6", "", "446", "", "", ""))
        routes.add(Route("7", "", "449", "", "", ""))
        routes.add(Route("8", "", "496", "", "", ""))
        routes.add(Route("9", "", "486", "", "", ""))
    }

    override suspend fun getRoutes(): List<Route> {
        return routes
    }

    override suspend fun getRouteByBus(busNumber: Int, isForward: Boolean): RouteInfo {
        return RouteInfo(
            "",
            busNumber,
            "",
            listOf(LatLng(0.0, 1.1), LatLng(0.1, 1.1)),
            listOf(RouteStop("1", "Xergiani st", true, 0.0, 0.0))
        )
    }

    override suspend fun getBusStopsByBusNumber(busNumber: Int): List<RouteStop> {
        return listOf(
            RouteStop("", "M/S Akhmeteli Theatre", true, 0.0, 0.0),
            RouteStop("", "M/S Akhmeteli Theatre (Opp)", false, 0.0, 0.0),
            RouteStop("", "M/S Sarajishvili", true, 0.0, 0.0),
        )
    }

    override suspend fun getBusPositions(busNumber: Int, isForward: Boolean): List<Bus> {
        return listOf(
            Bus(busNumber, Random.nextInt().toString(), Random.nextBoolean(), 0.0, 0.0),
            Bus(busNumber, Random.nextInt().toString(), Random.nextBoolean(), 0.0, 0.0),
            Bus(busNumber, Random.nextInt().toString(), Random.nextBoolean(), 0.0, 0.0),
            Bus(busNumber, Random.nextInt().toString(), Random.nextBoolean(), 0.0, 0.0),
        )
    }

    override suspend fun getStops(): List<BusStop> {
        return listOf(
            BusStop(Random.nextInt().toString(), Random.nextInt().toString(), "safsdf", 0.0, 0.0),
            BusStop(Random.nextInt().toString(), Random.nextInt().toString(), "safsdf", 0.0, 0.0),
            BusStop(Random.nextInt().toString(), Random.nextInt().toString(), "safsdf", 0.0, 0.0),
            BusStop(Random.nextInt().toString(), Random.nextInt().toString(), "safsdf", 0.0, 0.0),
            BusStop(Random.nextInt().toString(), Random.nextInt().toString(), "safsdf", 0.0, 0.0),
            BusStop(Random.nextInt().toString(), Random.nextInt().toString(), "safsdf", 0.0, 0.0),
            BusStop(Random.nextInt().toString(), Random.nextInt().toString(), "safsdf", 0.0, 0.0),
        )
    }

    override suspend fun getTimeTable(stopId: String): List<ArrivalTime> {
        return listOf(
            ArrivalTime(351, "Gldani-Gldanula", Random.nextInt(15)),
            ArrivalTime(304, "Gldani VII M/D", Random.nextInt(15)),
            ArrivalTime(531, "Gldani VI M/D", Random.nextInt(15)),
            ArrivalTime(496, "Gldani VIII M/D", Random.nextInt(15)),
            ArrivalTime(323, "Gldani's Lake", Random.nextInt(15)),
        ).sortedByDescending { it.time }
    }

    override suspend fun getSchedule(routeNumber: Int, isForward: Boolean): List<Schedule> {
        return (0..(Random.nextInt(15))).map {
            generateSchedule()
        }
    }

    private fun generateSchedule(): Schedule {
        return Schedule(
            WeekDay.randomizedAsString(),
            WeekDay.randomizedAsString(),
            Random.nextBoolean(),
            listOf(
                ScheduleStop(
                    UUID.randomUUID().toString(),
                    UUID.randomUUID().toString(),
                    Random.nextDouble(),
                    Random.nextDouble(),
                    listOf(
                        "06:00",
                        "06:30",
                        "07:00",
                        "07:45",
                        "08:15",
                        "09:00",
                        "09:30",
                        "10:01",
                        "10:30",
                        "11:25",
                        "12:30",
                        "13:00",
                        "14:23",
                        "15:18",
                        "16:27",
                        "18:00",
                    )
                )
            )
        )
    }
}