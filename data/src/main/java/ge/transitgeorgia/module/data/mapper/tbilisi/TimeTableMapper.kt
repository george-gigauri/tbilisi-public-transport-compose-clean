package ge.transitgeorgia.module.data.mapper.tbilisi

import ge.transitgeorgia.module.data.remote.dto.tbilisi.ArrivalTimeDto
import ge.transitgeorgia.module.domain.model.ArrivalTime

fun ArrivalTimeDto.toDomain(): ArrivalTime {
    return ArrivalTime(
        this.shortName.toIntOrNull() ?: 0,
        this.headSign ?: "-",
        this.realtimeArrivalMinutes
    )
}