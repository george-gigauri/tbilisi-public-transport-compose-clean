package ge.tbilisipublictransport.common.other.mapper

import ge.tbilisipublictransport.data.remote.dto.ArrivalTimeDto
import ge.tbilisipublictransport.domain.model.ArrivalTime

fun ArrivalTimeDto.toDomain(): ArrivalTime {
    return ArrivalTime(
        routeNumber.toIntOrNull() ?: -1,
        destination,
        time
    )
}