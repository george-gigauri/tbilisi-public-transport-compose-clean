package ge.transitgeorgia.module.domain.util

sealed class ResultWrapper<out T> {
    data class Success<T>(val data: T) : ResultWrapper<T>()
    data class Error(val type: ErrorType) : ResultWrapper<Nothing>()
    data object Loading : ResultWrapper<Nothing>()
    data object Empty : ResultWrapper<Nothing>()
}