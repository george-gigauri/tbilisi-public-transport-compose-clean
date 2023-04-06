package ge.tbilisipublictransport.domain.model

data class Route(
    val id: String,
    val color: String,
    val number: String,
    val longName: String,
    val firstStation: String,
    val lastStation: String
) {

    companion object {
        fun empty(): Route {
            return Route(
                "-",
                "#000000",
                "351",
                "ვაკე-ბაგები - გლდანულა-გლდანი",
                "---",
                "---"
            )
        }
    }
}