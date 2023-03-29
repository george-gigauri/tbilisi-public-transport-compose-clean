package ge.tbilisipublictransport.common.other.mapper

import com.mapbox.mapboxsdk.geometry.LatLng
import ge.tbilisipublictransport.data.remote.dto.RouteDto
import ge.tbilisipublictransport.data.remote.dto.RouteInfoDto
import ge.tbilisipublictransport.data.remote.dto.RouteStopDto
import ge.tbilisipublictransport.domain.model.Route
import ge.tbilisipublictransport.domain.model.RouteInfo
import ge.tbilisipublictransport.domain.model.RouteStop

fun RouteDto.toDomain(): Route {
    return Route(
        "#${this.color}",
        this.number,
        this.longName,
        this.startStop,
        this.lastStop
    )
}

fun RouteInfoDto.toDomain(): RouteInfo {
    return RouteInfo(
        "#${this.color}",
        this.routeNumber.toIntOrNull() ?: -1,
        this.title,
        this.shape.replace(" ", "").split(",").map {
            val latLng = it.trim().split(":")
            LatLng(latLng[1].toDoubleOrNull() ?: 0.0, latLng[0].toDoubleOrNull() ?: 0.0)
        },
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