package ge.transitgeorgia.module.data.mapper.rustavi

import ge.transitgeorgia.module.data.remote.dto.rustavi.ArrivalTimeDto
import ge.transitgeorgia.module.domain.model.ArrivalTime

fun ArrivalTimeDto.toDomain(): ArrivalTime {
    return ArrivalTime(
        routeNumber.toIntOrNull() ?: -1,
        destination,
        time
    )
}