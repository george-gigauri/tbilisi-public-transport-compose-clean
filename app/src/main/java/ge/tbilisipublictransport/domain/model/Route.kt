package ge.tbilisipublictransport.domain.model

data class Route(
    val color: String,
    val number: String,
    val longName: String,
    val firstStation: String,
    val lastStation: String
)
