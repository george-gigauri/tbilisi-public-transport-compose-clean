package ge.transitgeorgia.module.data.mapper

import ge.transitgeorgia.data.remote.dto.BusDto
import ge.transitgeorgia.data.remote.dto.BusStopDto
import ge.transitgeorgia.domain.model.Bus
import ge.transitgeorgia.module.domain.model.BusStop

fun BusDto.toDomain(): Bus {
    return Bus(
        this.routeNumber.toIntOrNull() ?: -1,
        this.nextStopId,
        this.isForward,
        this.lat,
        this.lng
    )
}

fun BusStopDto.toDomain(): BusStop {
    return BusStop(
        this.id ?: "",
        this.code ?: "",
        this.name ?: "",
        this.lat ?: 0.0,
        this.lng ?: 0.0
    )
}