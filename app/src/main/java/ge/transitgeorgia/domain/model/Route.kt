package ge.transitgeorgia.domain.model

data class Route(
    val id: String,
    val color: String,
    val number: String,
    var longName: String,
    var firstStation: String,
    var lastStation: String
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