package ge.transitgeorgia.module.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import ge.transitgeorgia.module.common.model.LatLngPoint
import ge.transitgeorgia.module.common.other.enums.SupportedCity
import ge.transitgeorgia.module.data.mapper.rustavi.toDomain
import ge.transitgeorgia.module.data.remote.dto.tbilisi.HeadSignsDto

@Entity(tableName = "route_info")
data class RouteInfoEntity(
    var id: String,
    var shortName: String,
    var longName: String,
    var color: String,
    var mode: String,
    var forwardHeadSign: String?,
    var backwardHeadSign: String?,
    var isCircular: Boolean,
    var forwardPolyline: String?,
    var backwardPolyline: String?,
    var addedAt: Long,
) {

    @PrimaryKey(autoGenerate = true)
    var uid: Int? = null

    companion object {
        fun toPolyline(p: List<LatLngPoint>, ph: String?): String? {
            return if (p.isNotEmpty()) {
                p.joinToString(separator = ":") { "${it.latitude},${it.longitude}" }
            } else {
                ph
            }
        }

        fun fromPolyline(p: String?): List<LatLngPoint> {
            return if (!p.isNullOrEmpty()) {
                p.replace(" ", "").split(":").let { it ->
                    if (it.size > 1) {
                        it.map {
                            val latLng = it.trim().split(",")
                            LatLngPoint(
                                latLng[0].toDoubleOrNull() ?: 0.0,
                                latLng[1].toDoubleOrNull() ?: 0.0
                            )
                        }
                    } else emptyList()
                }
            } else emptyList()
        }
    }

    fun isOutdated(): Boolean {
        return (System.currentTimeMillis() - addedAt) > (1 * 1 * 60 * 60 * 1000) // 1 hour
    }
}
