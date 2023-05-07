package ge.transitgeorgia.common.other.mapper

import ge.transitgeorgia.data.remote.dto.ScheduleResponseDto
import ge.transitgeorgia.data.remote.dto.ScheduleStopDto
import ge.transitgeorgia.domain.model.Schedule
import ge.transitgeorgia.domain.model.ScheduleStop

fun ScheduleResponseDto.toDomain(): List<Schedule> {
    return this.schedules.map {
        Schedule(
            it.fromDay,
            it.toDay,
            this.forward,
            it.stops.map { s -> s.toDomain() }
        )
    }
}

fun ScheduleStopDto.toDomain(): ScheduleStop {
    return ScheduleStop(
        this.stopId,
        this.name,
        this.lat,
        this.lng,
        this.arrivalTimes.split(",")
    )
}