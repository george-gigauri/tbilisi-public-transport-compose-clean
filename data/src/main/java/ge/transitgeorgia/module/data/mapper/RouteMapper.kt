package ge.transitgeorgia.module.data.mapper

import com.mapbox.mapboxsdk.geometry.LatLng
import ge.transitgeorgia.data.local.entity.RouteEntity
import ge.transitgeorgia.data.remote.dto.RouteDto
import ge.transitgeorgia.data.remote.dto.RouteInfoDto
import ge.transitgeorgia.data.remote.dto.RouteStopDto
import ge.transitgeorgia.domain.model.RouteStop
import ge.transitgeorgia.module.common.other.enums.TransportType
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

    val type = when(this.color) {
        "ff505b" -> RouteTransportType.METRO
        "117a65" -> RouteTransportType.BUS
        "1f618d" -> RouteTransportType.MICRO_BUS
        else -> RouteTransportType.fromTransportType(this.type)
    }

    return Route(
        this.id,
        if (isMetro) "#FF4433" else "#${this.color}",
        if (isMetro) metroLine.toString() else this.number,
        type,
        if (isMetro) this.number else this.longName ?: "--- - ---",
        this.startStop ?: "",
        this.lastStop ?: ""
    )
}

fun Route.toEntity(): RouteEntity {
    val type = when(this.color.lowercase()) {
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
        "#${this.color}",
        this.routeNumber.toIntOrNull() ?: -1,
        this.title,
        if (this.shape.isNotEmpty()) {
            this.shape.replace(" ", "").split(",").map {
                val latLng = it.trim().split(":")
                LatLng(latLng[1].toDoubleOrNull() ?: 0.0, latLng[0].toDoubleOrNull() ?: 0.0)
            }
        } else emptyList(),
        this.stops.map { it.toDomain() },
        true,
        color == "ff505b",
        color == "1f618d",
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