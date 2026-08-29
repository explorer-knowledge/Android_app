package com.example.billease.util

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

private const val DAYS_IN_WEEK = 7
private const val HOUR_END = 23
private const val MINUTE_END = 59
private const val SECOND_END = 59
private const val MS_END = 999

fun formatDate(millis: Long): String = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(millis))

fun formatDateLong(millis: Long): String = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).format(Date(millis))

/**
 * Material 3's DatePicker works entirely in UTC: `initialSelectedDateMillis` is read
 * as midnight UTC of the intended calendar day, and `selectedDateMillis` comes back
 * the same way. Everywhere else in this app (formatDate, day-range filters, DB
 * storage) treats a date as local-timezone midnight of that day. Feeding a local
 * millis value straight into/out of the picker shifts the calendar day by one for
 * any timezone west of UTC. These two functions convert at that boundary by
 * carrying only the year/month/day across, so the calendar day itself never moves.
 */
fun localMillisToUtcMidnight(localMillis: Long): Long {
    val local = Calendar.getInstance().apply { timeInMillis = localMillis }
    val utc =
        Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            clear()
            set(local.get(Calendar.YEAR), local.get(Calendar.MONTH), local.get(Calendar.DAY_OF_MONTH))
        }
    return utc.timeInMillis
}

fun utcMidnightToLocalMillis(utcMillis: Long): Long {
    val utc = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { timeInMillis = utcMillis }
    val local =
        Calendar.getInstance().apply {
            clear()
            set(utc.get(Calendar.YEAR), utc.get(Calendar.MONTH), utc.get(Calendar.DAY_OF_MONTH))
        }
    return local.timeInMillis
}

fun monthBounds(): Pair<Long, Long> {
    val calendar =
        Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
    val start = calendar.timeInMillis
    calendar.add(Calendar.MONTH, 1)
    calendar.add(Calendar.MILLISECOND, -1)
    return Pair(start, calendar.timeInMillis)
}

fun todayBounds(): Pair<Long, Long> {
    val cal =
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
    val start = cal.timeInMillis
    cal.set(Calendar.HOUR_OF_DAY, HOUR_END)
    cal.set(Calendar.MINUTE, MINUTE_END)
    cal.set(Calendar.SECOND, SECOND_END)
    cal.set(Calendar.MILLISECOND, MS_END)
    return Pair(start, cal.timeInMillis)
}

fun thisWeekBounds(): Pair<Long, Long> {
    val cal =
        Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
    val start = cal.timeInMillis
    cal.add(Calendar.WEEK_OF_YEAR, 1)
    cal.add(Calendar.MILLISECOND, -1)
    return Pair(start, cal.timeInMillis)
}

fun thisYearBounds(): Pair<Long, Long> {
    val cal =
        Calendar.getInstance().apply {
            set(Calendar.DAY_OF_YEAR, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
    val start = cal.timeInMillis
    cal.add(Calendar.YEAR, 1)
    cal.add(Calendar.MILLISECOND, -1)
    return Pair(start, cal.timeInMillis)
}

fun last7DaysBounds(): Pair<Long, Long> {
    val cal = Calendar.getInstance()
    val end = cal.timeInMillis
    cal.add(Calendar.DAY_OF_YEAR, -DAYS_IN_WEEK)
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    return Pair(cal.timeInMillis, end)
}

/**
 * Emits the current calendar month bounds and re-emits periodically so a long-lived
 * ViewModel (Home, Reports) crosses the 1st-of-month boundary without waiting for
 * recreation. Flat-mapping the DB queries off this makes the dashboards refresh once
 * a minute, which is far finer than a display needs.
 */
fun currentMonthBoundsFlow(intervalMillis: Long = 60_000L): Flow<Pair<Long, Long>> =
    flow {
        while (true) {
            emit(monthBounds())
            delay(intervalMillis)
        }
    }
