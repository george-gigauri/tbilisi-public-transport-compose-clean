package ge.transitgeorgia.common.util

import kotlin.random.Random

enum class WeekDay {
    MONDAY,
    TUESDAY,
    WEDNESDAY,
    THURSDAY,
    FRIDAY,
    SATURDAY,
    SUNDAY;

    companion object {
        fun randomized(): WeekDay {
            val randomIndex = Random.nextInt(values().size - 1)
            return values()[randomIndex]
        }

        fun randomizedAsString(): String {
            return randomized().name
        }
    }
}