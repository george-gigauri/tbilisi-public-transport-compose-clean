package ge.transitgeorgia.module.data.mapper.tbilisi

import ge.transitgeorgia.domain.model.Bus
import ge.transitgeorgia.module.data.remote.dto.tbilisi.BusPositionDto
import ge.transitgeorgia.module.data.remote.dto.tbilisi.BusStopDto
import ge.transitgeorgia.module.domain.model.BusStop

fun BusPositionDto.toDomain(): Bus {
    return Bus(
        -1,
        this.nextStopId ?: "",
        false,
        this.lat ?: 0.0,
        this.lng ?: 0.0,
        bearing
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