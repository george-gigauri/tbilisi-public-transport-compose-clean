package ge.transitgeorgia.module.data.mapper.tbilisi

import ge.transitgeorgia.domain.model.RouteStop
import ge.transitgeorgia.module.data.remote.dto.tbilisi.RouteDto
import ge.transitgeorgia.module.data.remote.dto.tbilisi.RouteInfoDto
import ge.transitgeorgia.module.data.remote.dto.tbilisi.RouteStopDto
import ge.transitgeorgia.module.domain.model.Route
import ge.transitgeorgia.module.domain.model.RouteInfo
import ge.transitgeorgia.module.domain.model.RouteTransportType

fun RouteDto.toDomain(): Route {
    val isMetro = this.mode == "SUBWAY"
    val metroLine = when (this.id.lowercase()) {
        "metro_1" -> 1
        "metro_2" -> 2
        else -> 1
    }

    val type = when (this.color?.lowercase()) {
        "ff505b" -> RouteTransportType.METRO
        "00b38b" -> RouteTransportType.BUS
        "0033b4" -> RouteTransportType.MICRO_BUS
        else -> RouteTransportType.fromV2(this.mode ?: "BUS")
    }

    return Route(
        this.id,
        if (isMetro) "#FF4433" else if (this.color != null) "#${this.color}" else "#04BF6E",
        if (isMetro) metroLine.toString() else this.shortName ?: "-",
        type,
        if (isMetro) (this.shortName ?: "-") else this.longName ?: "--- - ---",
        this.longName?.split("-")?.firstOrNull() ?: "--- ",
        this.longName?.split("-")?.lastOrNull() ?: " ---",
    )
}

fun RouteInfoDto.toDomain(): RouteInfo {
    val isMetro = this.mode == "SUBWAY"
    val _shortName = if (isMetro) {
        when (this.id.lowercase()) {
            "1:Metro_Metro_1" -> "Metro I"
            "1:Metro_Metro_2" -> "Metro II"
            else -> "1"
        }
    } else this.shortName

    return RouteInfo(
        id,
        "#${this.color}",
        _shortName,
        this.longName,
        headSigns?.forwardSign ?: "*",
        headSigns?.backwardSign ?: "*",
        emptyList(),
        null,
        emptyList(),
        color.lowercase() == "00b38b",
        color.lowercase() == "ff505b",
        color.lowercase() == "0033b4",
        isCircular
    )
}

fun RouteStopDto.toDomain(): RouteStop {
    return RouteStop(
        this.stopId,
        this.title,
        this.isForward,
        this.lat,
        this.lng
    )
}