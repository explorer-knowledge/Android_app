package com.example.billease.util

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

class DateUtilsTest {
    private lateinit var originalTimeZone: TimeZone

    @Before
    fun setUp() {
        originalTimeZone = TimeZone.getDefault()
    }

    @After
    fun tearDown() {
        TimeZone.setDefault(originalTimeZone)
    }

    private fun dayOf(millis: Long): Triple<Int, Int, Int> {
        val cal = Calendar.getInstance().apply { timeInMillis = millis }
        return Triple(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
    }

    @Test
    fun `round trip through the picker preserves the calendar day west of UTC`() {
        // America/New_York is UTC-4/-5; a naive pass-through would land on Aug 19.
        TimeZone.setDefault(TimeZone.getTimeZone("America/New_York"))
        val localMidnightAug20 =
            Calendar.getInstance().apply {
                clear()
                set(2026, Calendar.AUGUST, 20)
            }.timeInMillis

        val utcMidnight = localMillisToUtcMidnight(localMidnightAug20)
        val roundTripped = utcMidnightToLocalMillis(utcMidnight)

        assertEquals(Triple(2026, Calendar.AUGUST, 20), dayOf(roundTripped))
    }

    @Test
    fun `utc midnight maps to the same calendar day in a far-west negative offset zone`() {
        // America/Los_Angeles is UTC-7/-8, the worst case for the off-by-one.
        TimeZone.setDefault(TimeZone.getTimeZone("America/Los_Angeles"))
        val utcMidnightAug20 =
            Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                clear()
                set(2026, Calendar.AUGUST, 20)
            }.timeInMillis

        val localMillis = utcMidnightToLocalMillis(utcMidnightAug20)

        assertEquals(Triple(2026, Calendar.AUGUST, 20), dayOf(localMillis))
    }

    @Test
    fun `conversion is a no-op for the picker in UTC-positive zones like IST`() {
        // IST (UTC+5:30) is why this bug went unnoticed - it never shifts the day.
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Kolkata"))
        val localMidnightAug20 =
            Calendar.getInstance().apply {
                clear()
                set(2026, Calendar.AUGUST, 20)
            }.timeInMillis

        val roundTripped = utcMidnightToLocalMillis(localMillisToUtcMidnight(localMidnightAug20))

        assertEquals(Triple(2026, Calendar.AUGUST, 20), dayOf(roundTripped))
    }
}
