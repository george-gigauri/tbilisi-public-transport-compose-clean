package ge.transitgeorgia.module.domain.util

sealed class ErrorType {
    data object Timeout : ErrorType()
    data object NoInternet : ErrorType()
    data class Http(val code: Int, val message: String) : ErrorType()
    data class Unknown(val message: String?) : ErrorType()
}