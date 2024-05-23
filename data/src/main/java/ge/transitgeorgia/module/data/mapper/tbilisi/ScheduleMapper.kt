package ge.transitgeorgia.module.data.mapper.tbilisi

import ge.transitgeorgia.module.data.remote.dto.tbilisi.ScheduleResponseDto
import ge.transitgeorgia.module.data.remote.dto.tbilisi.ScheduleStopDto
import ge.transitgeorgia.domain.model.Schedule
import ge.transitgeorgia.domain.model.ScheduleStop

fun ScheduleResponseDto.toDomain(): List<Schedule> {
    return this.schedules?.map {
        Schedule(
            it.fromDay,
            it.toDay,
            false,
            it.stops?.map { s -> s.toDomain() } ?: emptyList()
        )
    } ?: emptyList()
}

fun ScheduleStopDto.toDomain(): ScheduleStop {
    return ScheduleStop(
        this.stopId ?: "",
        this.name,
        0.0,
        0.0,
        this.arrivalTimes.split(",").map {
            val timeSplit = it.split(":")
            val hour = timeSplit[0]
            val minute = timeSplit[1]
            if (hour.length == 1) "0$hour:$minute"
            else it
        }
    )
}