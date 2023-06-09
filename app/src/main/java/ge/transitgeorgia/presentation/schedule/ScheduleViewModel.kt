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
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class ScheduleViewModel @Inject constructor(
    private val repository: ITransportRepository,
    private val db: AppDatabase,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    val routeNumber: String? get() = savedStateHandle["route_number"]
    val routeColor: String get() = savedStateHandle["route_color"] ?: "#000000"

    val isLoading: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val error: MutableSharedFlow<String?> = MutableSharedFlow()
    val data: MutableStateFlow<List<CurrentTimeStationSchedule>> = MutableStateFlow(emptyList())
    val route: MutableStateFlow<Route> = MutableStateFlow(Route.empty())
    val isForward: MutableStateFlow<Boolean> = MutableStateFlow(true)

    init {
        fetch()
    }

    private fun fetch() = viewModelScope.launch {
        try {
            isLoading.value = true
            route.value = db.routeDao().getRoute(routeNumber?.toIntOrNull() ?: -1)?.toDomain()
                ?: Route.empty()

            val result = repository.getSchedule(routeNumber?.toIntOrNull() ?: -1, isForward.value)

            val currentDateTime = LocalDateTime.now()
            val currentTime = currentDateTime.toLocalTime()
            val currentTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
            val currentHourAndMinute = currentTimeFormatter.format(currentTime)
            val currentWeekDay = currentDateTime.dayOfWeek

            val currentWeekDaySchedules = result.find {
                val from = DayOfWeek.valueOf(it.fromDay.uppercase())
                val to = DayOfWeek.valueOf(it.toDay.uppercase())
                currentWeekDay >= from && currentWeekDay <= to
            }?.stops?.map {

                val soonest = it.arrivalTimes.filter { hhmm ->
                    currentHourAndMinute <= hhmm
                }.minOrNull() ?: "---"

                val converteddatee = LocalDateTime.of(
                    LocalDate.now(),
                    LocalTime.of(soonest.split(":")[0].toInt(), soonest.split(":")[1].toInt())
                ).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

                val interval = (converteddatee - System.currentTimeMillis()) / 60 / 1000
                val isIntervalLessThan30Minutes = interval <= 30

                CurrentTimeStationSchedule(
                    if (isIntervalLessThan30Minutes) "$interval" else soonest,
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

    fun refresh() {
        fetch()
    }

    fun changeDirection() = viewModelScope.launch {
        isForward.value = !isForward.value
        fetch()
    }
}