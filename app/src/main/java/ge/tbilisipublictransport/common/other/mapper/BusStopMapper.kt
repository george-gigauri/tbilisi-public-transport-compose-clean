package ge.tbilisipublictransport.common.other.mapper

import ge.tbilisipublictransport.data.local.entity.BusStopEntity
import ge.tbilisipublictransport.domain.model.BusStop

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