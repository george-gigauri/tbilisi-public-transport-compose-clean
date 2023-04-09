package ge.tbilisipublictransport.common.other.enums

import ge.tbilisipublictransport.common.other.Const

enum class SupportedCity(
    val id: String, val title: String, val baseUrl: String
) {
    TBILISI("tbilisi", "თბილისი", Const.TBILISI_BASE_URL),
    RUSTAVI("rustavi", "რუსთავი", Const.RUSTAVI_BASE_URL),
}