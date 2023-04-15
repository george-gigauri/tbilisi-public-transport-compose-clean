package ge.transitgeorgia.common.other.mapper

import ge.transitgeorgia.data.remote.dto.ArrivalTimeDto
import ge.transitgeorgia.domain.model.ArrivalTime

fun ArrivalTimeDto.toDomain(): ArrivalTime {
    return ArrivalTime(
        routeNumber.toIntOrNull() ?: -1,
        destination,
        time
    )
}