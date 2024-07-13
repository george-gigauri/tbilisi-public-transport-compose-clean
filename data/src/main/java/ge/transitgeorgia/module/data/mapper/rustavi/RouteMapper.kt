package ge.transitgeorgia.module.data.mapper.rustavi

import ge.transitgeorgia.module.data.local.entity.RouteEntity
import ge.transitgeorgia.module.data.remote.dto.rustavi.RouteDto
import ge.transitgeorgia.module.data.remote.dto.rustavi.RouteInfoDto
import ge.transitgeorgia.module.data.remote.dto.rustavi.RouteStopDto
import ge.transitgeorgia.domain.model.RouteStop
import ge.transitgeorgia.module.common.model.LatLngPoint
import ge.transitgeorgia.module.common.other.enums.SupportedCity
import ge.transitgeorgia.module.common.other.enums.TransportType
import ge.transitgeorgia.module.data.local.entity.RouteInfoEntity
import ge.transitgeorgia.module.domain.model.Route
import ge.transitgeorgia.module.domain.model.RouteInfo
import ge.transitgeorgia.module.domain.model.RouteTransportType

fun RouteDto.toDomain(): Route {
    val isMetro = this.type == TransportType.METRO
    val metroLine = when (this.id.lowercase()) {
        "metro_1" -> 1
        "metro_2" -> 2
        else -> 1
    }

    val type = when (this.color) {
        "ff505b" -> RouteTransportType.METRO
        "117a65" -> RouteTransportType.BUS
        "1f618d" -> RouteTransportType.MICRO_BUS
        else -> RouteTransportType.fromTransportType(this.type)
    }

    return Route(
        this.id,
        if (isMetro) "#FF4433" else if (this.color != null) "#${this.color}" else "#04BF6E",
        if (isMetro) metroLine.toString() else this.number,
        type,
        if (isMetro) this.number else this.longName ?: "--- - ---",
        this.startStop ?: "",
        this.lastStop ?: ""
    )
}

fun Route.toEntity(): RouteEntity {
    val type = when (this.color.lowercase()) {
        "#ff505b" -> RouteTransportType.METRO
        "#117a65" -> RouteTransportType.BUS
        "#1f618d" -> RouteTransportType.MICRO_BUS
        else -> this.type
    }

    return RouteEntity(
        id, color, number, type, longName, firstStation, lastStation
    )
}

fun RouteEntity.toDomain(): Route {
    return Route(
        id,
        color,
        number,
        type,
        longName,
        firstStation,
        lastStation
    )
}

fun RouteInfoDto.toDomain(): RouteInfo {
    return RouteInfo(
        id,
        "#${this.color}",
        this.routeNumber,
        this.title,
        this.title.split(" - ").firstOrNull() ?: "*",
        this.title.split(" - ").lastOrNull() ?: "*",
        if (this.shape.isNotEmpty()) {
            this.shape.replace(" ", "").split(",").map {
                val latLng = it.trim().split(":")
                LatLngPoint(latLng[1].toDoubleOrNull() ?: 0.0, latLng[0].toDoubleOrNull() ?: 0.0)
            }
        } else emptyList(),
        null,
        this.stops.map { it.toDomain() },
        true,
        color == "ff505b",
        color == "1f618d",
        false
    )
}

fun RouteInfo.toEntity(
    city: SupportedCity,
    polyline2: List<LatLngPoint> = emptyList(),
    polyline2Hash: String? = null
): RouteInfoEntity {
    return RouteInfoEntity(
        this.id,
        this.number,
        this.longName,
        this.color,
        when {
            this.isMetro -> "SUBWAY"
            this.isBus -> "BUS"
            this.isMicroBus -> "MINIBUS"
            else -> "-"
        },
        forwardHeadSign,
        backwardHeadSign,
        isCircular,
        RouteInfoEntity.toPolyline(polyline, polylineHash),
        RouteInfoEntity.toPolyline(polyline2, polyline2Hash),
        System.currentTimeMillis()
    )
}

fun RouteInfoEntity.toDomain(isForward: Boolean = true): RouteInfo {
    val polylineList =
        RouteInfoEntity.fromPolyline(if (isForward) forwardPolyline else backwardPolyline)
    return RouteInfo(
        id,
        "#${this.color}",
        this.shortName,
        this.longName,
        this.forwardHeadSign,
        this.backwardHeadSign,
        polylineList,
        if (polylineList.isEmpty()) if (isForward) forwardPolyline else backwardPolyline else null,
        emptyList(),
        true,
        color == "ff505b",
        color == "1f618d",
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