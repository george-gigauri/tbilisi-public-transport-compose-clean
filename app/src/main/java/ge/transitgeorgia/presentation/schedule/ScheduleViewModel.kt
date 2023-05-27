package ge.transitgeorgia.presentation.schedule

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ge.transitgeorgia.common.other.mapper.toDomain
import ge.transitgeorgia.data.local.db.AppDatabase
import ge.transitgeorgia.domain.model.CurrentTimeStationSchedule
import ge.transitgeorgia.domain.model.Route
import ge.transitgeorgia.domain.repository.ITransportRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import okio.IOException
import retrofit2.HttpException
import java.time.DayOfWeek
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class ScheduleViewModel @Inject constructor(
    private val repository: ITransportRepository,
    private val db: AppDatabase,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    val routeNumber: Int get() = savedStateHandle["route_number"] ?: -1
    val routeColor: String get() = savedStateHandle["route_color"] ?: "#000000"

    val isLoading: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val error: MutableSharedFlow<String?> = MutableSharedFlow()
    val data: MutableStateFlow<List<CurrentTimeStationSchedule>> = MutableStateFlow(emptyList())
    val route: MutableStateFlow<Route> = MutableStateFlow(Route.empty())
    var isForward: Boolean = true
        set(value) {
            field = value
            fetch()
        }

    init {
        fetch()
    }

    private fun fetch() = viewModelScope.launch {
        try {
            isLoading.value = true
            route.value = db.routeDao().getRoute(routeNumber)?.toDomain() ?: Route.empty()
            val result = repository.getSchedule(routeNumber, isForward)

            val currentDateTime = LocalDateTime.now()
            val currentTime = currentDateTime.toLocalTime()
            val currentHourAndMinute = "${currentTime.hour}:${currentTime.minute}"
            val currentWeekDay = currentDateTime.dayOfWeek

            val currentWeekDaySchedules = result.find {
                val from = DayOfWeek.valueOf(it.fromDay.uppercase())
                val to = DayOfWeek.valueOf(it.toDay.uppercase())
                currentWeekDay >= from && currentWeekDay <= to
            }?.stops?.map {

                val soonest = it.arrivalTimes.filter { hhmm ->
                    currentHourAndMinute <= hhmm
                }.minOrNull() ?: "00:00"

                CurrentTimeStationSchedule(
                    soonest,
                    it.id,
                    it.name,
                    it.arrivalTimes.filter { time -> currentHourAndMinute < time && time != soonest }
                )
            }

            data.value = currentWeekDaySchedules.orEmpty()
        } catch (e: HttpException) {
            error.emit(e.message())
            e.printStackTrace()
        } catch (e: IOException) {
            error.emit(e.message)
            e.printStackTrace()
        } catch (e: Exception) {
            error.emit(e.message)
            e.printStackTrace()
        } finally {
            isLoading.emit(false)
        }
    }
}