package ge.transitgeorgia.module.data.mapper.rustavi

import ge.transitgeorgia.data.local.entity.BusStopEntity
import ge.transitgeorgia.module.domain.model.BusStop

fun BusStop.toEntity(): BusStopEntity {
    return BusStopEntity(
        id,
        code,
        name,
        lat,
        lng
    )
}

fun BusStopEntity.toDomain(): BusStop {
    return BusStop(
        id, code, name, lat, lng
    )
}