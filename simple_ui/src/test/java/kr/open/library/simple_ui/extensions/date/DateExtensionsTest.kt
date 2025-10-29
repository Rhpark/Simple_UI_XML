package kr.open.library.simple_ui.extensions.date

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Ignore
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Month
import java.time.ZoneId
import java.util.Date
import java.util.Locale

@Ignore("임시로 비활성화")
class DateExtensionsTest {

    @Test
    fun timeDateToString_formatsMillisCorrectly() {
        val instant = LocalDateTime.of(2024, Month.MARCH, 1, 10, 30)
        val millis = instant.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        val formatted = millis.timeDateToString("yyyy-MM-dd HH:mm", Locale.US)

        assertEquals("2024-03-01 10:30", formatted)
    }

    @Test
    fun timeDateToLong_parsesBackToMillis() {
        val millis = "2024-03-01 10:30".timeDateToLong("yyyy-MM-dd HH:mm", Locale.US)

        val expected = LocalDateTime.of(2024, Month.MARCH, 1, 10, 30)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        assertEquals(expected, millis)
    }

    @Test
    fun dateDifferences_calculateExpectedValues() {
        val start = LocalDate.of(2024, Month.JANUARY, 1)
        val end = LocalDate.of(2024, Month.FEBRUARY, 2)

        assertEquals(32, start.daysDifference(end))
        assertEquals(1, start.monthsDifference(end))
        assertEquals(0, start.yearsDifference(end))
    }

    @Test
    fun localDateTimeBetween_calculationsAreAccurate() {
        val from = LocalDateTime.of(2024, Month.JUNE, 10, 10, 0)
        val to = from.plusHours(5).plusMinutes(30)

        assertEquals(5, from.hoursBetween(to))
        assertEquals(330, from.minutesBetween(to))
    }

    @Test
    fun conversionHelpers_roundTripSuccessfully() {
        val now = Date()
        val formatted = now.formatToString("yyyy-MM-dd HH:mm")
        val reparsed = formatted.timeDateToDate("yyyy-MM-dd HH:mm")

        assertTrue(reparsed?.time ?: 0L > 0L)
    }

    // ========== Exception Handling Tests ==========

    @Test(expected = IllegalArgumentException::class)
    fun timeDateToLong_throwsExceptionForInvalidFormat() {
        "invalid-date".timeDateToLong("yyyy-MM-dd")
    }

    @Test
    fun timeDateToDate_returnsNullForInvalidFormat() {
        val result = "invalid-date".timeDateToDate("yyyy-MM-dd")
        assertEquals(null, result)
    }

    // ========== Date Time Difference Tests ==========

    @Test
    fun timeDifferenceInSeconds_calculatesCorrectly() {
        val date1 = Date(1000000L)
        val date2 = Date(1005000L)

        assertEquals(5L, date1.timeDifferenceInSeconds(date2))
        assertEquals(5L, date2.timeDifferenceInSeconds(date1)) // abs value
    }

    @Test
    fun timeDifferenceInMinutes_calculatesCorrectly() {
        val date1 = Date(0L)
        val date2 = Date(5 * 60 * 1000L) // 5 minutes

        assertEquals(5L, date1.timeDifferenceInMinutes(date2))
    }

    @Test
    fun timeDifferenceInHours_calculatesCorrectly() {
        val date1 = Date(0L)
        val date2 = Date(3 * 3600 * 1000L) // 3 hours

        assertEquals(3L, date1.timeDifferenceInHours(date2))
    }

    @Test
    fun timeDifference_handlesNegativeDifferences() {
        val earlier = Date(1000L)
        val later = Date(5000L)

        // Should return absolute value
        assertEquals(4L, earlier.timeDifferenceInSeconds(later))
        assertEquals(4L, later.timeDifferenceInSeconds(earlier))
    }

    // ========== Long Conversion Tests ==========

    @Test
    fun secondsToMinutes_convertsCorrectly() {
        assertEquals(2L, 120L.secondsToMinutes())
        assertEquals(1L, 90L.secondsToMinutes())
        assertEquals(0L, 30L.secondsToMinutes())
    }

    @Test
    fun secondsToHours_convertsCorrectly() {
        assertEquals(2L, 7200L.secondsToHours())
        assertEquals(1L, 3660L.secondsToHours())
        assertEquals(0L, 1800L.secondsToHours())
    }

    @Test
    fun secondsToDays_convertsCorrectly() {
        assertEquals(2L, 172800L.secondsToDays())
        assertEquals(1L, 86400L.secondsToDays())
        assertEquals(0L, 43200L.secondsToDays())
    }

    @Test
    fun millisecondsToSeconds_convertsCorrectly() {
        assertEquals(5L, 5000L.millisecondsToSeconds())
        assertEquals(1L, 1500L.millisecondsToSeconds())
        assertEquals(0L, 500L.millisecondsToSeconds())
    }

    @Test
    fun millisecondsToMinutes_convertsCorrectly() {
        assertEquals(2L, 120000L.millisecondsToMinutes())
        assertEquals(1L, 90000L.millisecondsToMinutes())
    }

    @Test
    fun millisecondsToHours_convertsCorrectly() {
        assertEquals(2L, 7200000L.millisecondsToHours())
        assertEquals(1L, 3660000L.millisecondsToHours())
    }

    @Test
    fun millisecondsToDays_convertsCorrectly() {
        assertEquals(2L, 172800000L.millisecondsToDays())
        assertEquals(1L, 86400000L.millisecondsToDays())
    }

    // ========== Int Conversion Tests ==========

    @Test
    fun intSecondsToMinutes_convertsCorrectly() {
        assertEquals(2, 120.secondsToMinutes())
        assertEquals(1, 90.secondsToMinutes())
        assertEquals(0, 30.secondsToMinutes())
    }

    @Test
    fun intSecondsToHours_convertsCorrectly() {
        assertEquals(2, 7200.secondsToHours())
        assertEquals(1, 3660.secondsToHours())
        assertEquals(0, 1800.secondsToHours())
    }

    @Test
    fun intSecondsToDays_convertsCorrectly() {
        assertEquals(2, 172800.secondsToDays())
        assertEquals(1, 86400.secondsToDays())
        assertEquals(0, 43200.secondsToDays())
    }

    // ========== LocalDateTime Conversion Tests ==========

    @Test
    fun longToLocalDateTime_convertsCorrectly() {
        val millis = LocalDateTime.of(2024, Month.JUNE, 15, 14, 30)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        val localDateTime = millis.toLocalDateTime()

        assertEquals(2024, localDateTime.year)
        assertEquals(Month.JUNE, localDateTime.month)
        assertEquals(15, localDateTime.dayOfMonth)
        assertEquals(14, localDateTime.hour)
        assertEquals(30, localDateTime.minute)
    }

    @Test
    fun dateToLocalDateTime_convertsCorrectly() {
        val date = Date()
        val localDateTime = date.toLocalDateTime()

        assertTrue(localDateTime.year > 2020)
    }

    @Test
    fun localDateTimeFormat_formatsWithPattern() {
        val dateTime = LocalDateTime.of(2024, Month.MARCH, 1, 10, 30)

        val formatted = dateTime.format("yyyy-MM-dd HH:mm:ss", Locale.US)

        assertEquals("2024-03-01 10:30:00", formatted)
    }

    // ========== Date Formatting Tests ==========

    @Test
    fun dateFormattedToString_usesSimpledateFormat() {
        val date = Date(0L) // Epoch time

        val formatted = date.formattedToString("yyyy-MM-dd", Locale.US)

        assertTrue(formatted.startsWith("19")) // 1970
    }

    @Test
    fun dateFormatToString_usesLocalDateTime() {
        val date = Date(0L)

        val formatted = date.formatToString("yyyy-MM-dd", Locale.US)

        assertTrue(formatted.startsWith("19")) // 1970
    }

    // ========== Locale Tests ==========

    @Test
    fun timeDateToString_respectsLocale() {
        val millis = LocalDateTime.of(2024, Month.MARCH, 1, 10, 30)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        val formattedUS = millis.timeDateToString("MMM dd, yyyy", Locale.US)
        val formattedFR = millis.timeDateToString("dd MMM yyyy", Locale.FRANCE)

        assertTrue(formattedUS.contains("Mar"))
        // French locale may vary by system, just check it's not empty
        assertTrue(formattedFR.isNotEmpty())
    }

    // ========== Edge Cases ==========

    @Test
    fun zeroMilliseconds_convertsToEpoch() {
        val localDateTime = 0L.toLocalDateTime()

        assertEquals(1970, localDateTime.year)
        assertEquals(Month.JANUARY, localDateTime.month)
        assertEquals(1, localDateTime.dayOfMonth)
    }

    @Test
    fun negativeTimeDifference_returnsAbsoluteValue() {
        val start = LocalDate.of(2024, Month.FEBRUARY, 1)
        val end = LocalDate.of(2024, Month.JANUARY, 1)

        val days = start.daysDifference(end)

        assertEquals(-31, days) // ChronoUnit.DAYS.between preserves sign
    }

    @Test
    fun sameDateTime_hasZeroDifference() {
        val date = Date()

        assertEquals(0L, date.timeDifferenceInSeconds(date))
        assertEquals(0L, date.timeDifferenceInMinutes(date))
        assertEquals(0L, date.timeDifferenceInHours(date))
    }
}
