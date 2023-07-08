package ge.transitgeorgia.common.other.mapper

import com.mapbox.mapboxsdk.geometry.LatLng
import ge.transitgeorgia.common.other.enums.TransportType
import ge.transitgeorgia.data.local.entity.RouteEntity
import ge.transitgeorgia.data.remote.dto.RouteDto
import ge.transitgeorgia.data.remote.dto.RouteInfoDto
import ge.transitgeorgia.data.remote.dto.RouteStopDto
import ge.transitgeorgia.domain.model.Route
import ge.transitgeorgia.domain.model.RouteInfo
import ge.transitgeorgia.domain.model.RouteStop

fun RouteDto.toDomain(): Route {
    val isMetro = this.type == TransportType.METRO
    val metroLine = when (this.id.lowercase()) {
        "metro_1" -> 1
        "metro_2" -> 2
        else -> 1
    }

    return Route(
        this.id,
        "#${this.color}",
        if (isMetro) metroLine.toString() else this.number,
        this.type,
        if (isMetro) this.number else this.longName ?: "--- - ---",
        this.startStop ?: "",
        this.lastStop ?: ""
    )
}

fun Route.toEntity(): RouteEntity {
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
        this.stops.map { it.toDomain() }
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