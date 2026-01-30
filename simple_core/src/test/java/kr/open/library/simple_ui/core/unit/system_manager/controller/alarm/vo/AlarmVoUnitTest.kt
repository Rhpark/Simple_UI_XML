package kr.open.library.simple_ui.core.unit.system_manager.controller.alarm.vo

import kr.open.library.simple_ui.core.system_manager.controller.alarm.AlarmConstants
import kr.open.library.simple_ui.core.system_manager.controller.alarm.vo.AlarmDateVO
import kr.open.library.simple_ui.core.system_manager.controller.alarm.vo.AlarmIdleMode
import kr.open.library.simple_ui.core.system_manager.controller.alarm.vo.AlarmNotificationVO
import kr.open.library.simple_ui.core.system_manager.controller.alarm.vo.AlarmScheduleVO
import kr.open.library.simple_ui.core.system_manager.controller.alarm.vo.AlarmVO
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for AlarmVO and AlarmConstants
 */
class AlarmVoUnitTest {
    // ==============================================
    // AlarmConstants Tests
    // ==============================================

    @Test
    fun alarmConstants_hasCorrectIntentExtraValues() {
        assertEquals("AlarmKey", AlarmConstants.ALARM_KEY)
        assertEquals(-1, AlarmConstants.ALARM_KEY_DEFAULT_VALUE)
    }

    @Test
    fun alarmConstants_hasCorrectWakeLockSettings() {
        assertEquals("SystemManager:AlarmReceiver", AlarmConstants.WAKELOCK_TAG)
        assertEquals(10 * 60 * 1000L, AlarmConstants.WAKELOCK_TIMEOUT_MS)
        assertEquals(3000L, AlarmConstants.DEFAULT_ACQUIRE_TIME_MS)
    }

    // ==============================================
    // AlarmVO Creation Tests
    // ==============================================

    @Test
    fun AlarmVO_createsSuccessfullyWithValidData() {
        val schedule = AlarmScheduleVO(hour = 7, minute = 30)
        val notification = AlarmNotificationVO(title = "Wake Up", message = "Time to wake up!")
        val alarm = AlarmVO(key = 1, schedule = schedule, notification = notification)

        assertEquals(1, alarm.key)
        assertEquals(schedule, alarm.schedule)
        assertEquals(notification, alarm.notification)
        assertEquals(0, alarm.schedule.second) // Default
        assertTrue(alarm.isActive) // Default
        assertEquals(AlarmIdleMode.NONE, alarm.schedule.idleMode) // Default
        assertNull(alarm.notification.vibrationPattern)
        assertNull(alarm.notification.soundUri)
        assertEquals(AlarmConstants.DEFAULT_ACQUIRE_TIME_MS, alarm.acquireTime)
    }

    @Test
    fun AlarmVO_createsWithAllParameters() {
        val vibrationPattern = listOf(0L, 100L, 200L, 100L)
        val schedule = AlarmScheduleVO(hour = 14, minute = 55, second = 30, idleMode = AlarmIdleMode.INEXACT)
        val notification =
            AlarmNotificationVO(
                title = "Meeting",
                message = "Team meeting in 5 minutes",
                vibrationPattern = vibrationPattern,
                soundUri = null,
            )
        val alarm =
            AlarmVO(
                key = 2,
                schedule = schedule,
                notification = notification,
                isActive = false,
                acquireTime = 5000L,
            )

        assertEquals(2, alarm.key)
        assertFalse(alarm.isActive)
        assertEquals(AlarmIdleMode.INEXACT, alarm.schedule.idleMode)
        assertEquals(vibrationPattern, alarm.notification.vibrationPattern)
        assertEquals(14, alarm.schedule.hour)
        assertEquals(55, alarm.schedule.minute)
        assertEquals(30, alarm.schedule.second)
        assertEquals(5000L, alarm.acquireTime)
    }

    // ==============================================
    // AlarmVO Validation Tests
    // ==============================================

    @Test(expected = IllegalArgumentException::class)
    fun AlarmVO_throwsExceptionWhenKeyIsZero() {
        AlarmVO(
            key = 0,
            schedule = AlarmScheduleVO(hour = 10, minute = 0),
            notification = AlarmNotificationVO(title = "Test", message = "Test message"),
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun AlarmVO_throwsExceptionWhenKeyIsNegative() {
        AlarmVO(
            key = -1,
            schedule = AlarmScheduleVO(hour = 10, minute = 0),
            notification = AlarmNotificationVO(title = "Test", message = "Test message"),
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun AlarmVO_throwsExceptionWhenAcquireTimeIsZero() {
        AlarmVO(
            key = 1,
            schedule = AlarmScheduleVO(hour = 10, minute = 0),
            notification = AlarmNotificationVO(title = "Test", message = "Test message"),
            acquireTime = 0L,
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun AlarmVO_throwsExceptionWhenAcquireTimeIsNegative() {
        AlarmVO(
            key = 1,
            schedule = AlarmScheduleVO(hour = 10, minute = 0),
            notification = AlarmNotificationVO(title = "Test", message = "Test message"),
            acquireTime = -1000L,
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun AlarmScheduleVO_throwsExceptionWhenHourIsNegative() {
        AlarmScheduleVO(hour = -1, minute = 0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun AlarmScheduleVO_throwsExceptionWhenHourIsGreaterThan23() {
        AlarmScheduleVO(hour = 24, minute = 0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun AlarmScheduleVO_throwsExceptionWhenMinuteIsNegative() {
        AlarmScheduleVO(hour = 10, minute = -1)
    }

    @Test(expected = IllegalArgumentException::class)
    fun AlarmScheduleVO_throwsExceptionWhenMinuteIsGreaterThan59() {
        AlarmScheduleVO(hour = 10, minute = 60)
    }

    @Test(expected = IllegalArgumentException::class)
    fun AlarmScheduleVO_throwsExceptionWhenSecondIsNegative() {
        AlarmScheduleVO(hour = 10, minute = 30, second = -1)
    }

    @Test(expected = IllegalArgumentException::class)
    fun AlarmScheduleVO_throwsExceptionWhenSecondIsGreaterThan59() {
        AlarmScheduleVO(hour = 10, minute = 30, second = 60)
    }

    @Test(expected = IllegalArgumentException::class)
    fun AlarmDateVO_throwsExceptionWhenDateIsInvalid() {
        AlarmDateVO(year = 2026, month = 2, day = 31)
    }

    @Test(expected = IllegalArgumentException::class)
    fun AlarmNotificationVO_throwsExceptionWhenTitleIsBlank() {
        AlarmNotificationVO(title = "   ", message = "Test message")
    }

    @Test(expected = IllegalArgumentException::class)
    fun AlarmNotificationVO_throwsExceptionWhenMessageIsBlank() {
        AlarmNotificationVO(title = "Test", message = "")
    }

    @Test(expected = IllegalArgumentException::class)
    fun AlarmNotificationVO_throwsExceptionWhenVibrationPatternIsEmpty() {
        AlarmNotificationVO(
            title = "Test",
            message = "Test message",
            vibrationPattern = emptyList(),
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun AlarmNotificationVO_throwsExceptionWhenVibrationPatternHasNegativeValue() {
        AlarmNotificationVO(
            title = "Test",
            message = "Test message",
            vibrationPattern = listOf(0L, -100L, 200L),
        )
    }

    // ==============================================
    // AlarmVO Method Tests
    // ==============================================

    @Test
    fun withActiveState_createsNewAlarmWithModifiedState() {
        val original =
            AlarmVO(
                key = 1,
                schedule = AlarmScheduleVO(hour = 10, minute = 0),
                notification = AlarmNotificationVO(title = "Test", message = "Test message"),
                isActive = true,
            )

        val modified = original.withActiveState(false)

        assertFalse(modified.isActive)
        assertEquals(original.key, modified.key)
        assertEquals(original.notification.title, modified.notification.title)
    }

    @Test
    fun withTime_createsNewAlarmWithModifiedTime() {
        val original =
            AlarmVO(
                key = 1,
                schedule = AlarmScheduleVO(hour = 10, minute = 30, second = 15),
                notification = AlarmNotificationVO(title = "Test", message = "Test message"),
            )

        val modified = original.withTime(14, 45)

        assertEquals(14, modified.schedule.hour)
        assertEquals(45, modified.schedule.minute)
        assertEquals(15, modified.schedule.second) // Preserved
    }

    @Test
    fun withTime_canModifySeconds() {
        val original =
            AlarmVO(
                key = 1,
                schedule = AlarmScheduleVO(hour = 10, minute = 30),
                notification = AlarmNotificationVO(title = "Test", message = "Test message"),
            )

        val modified = original.withTime(14, 45, 30)

        assertEquals(14, modified.schedule.hour)
        assertEquals(45, modified.schedule.minute)
        assertEquals(30, modified.schedule.second)
    }

    @Test
    fun getFormattedTime_returnsCorrectFormat() {
        val alarm =
            AlarmVO(
                key = 1,
                schedule = AlarmScheduleVO(hour = 7, minute = 5, second = 3),
                notification = AlarmNotificationVO(title = "Test", message = "Test message"),
            )

        assertEquals("07:05:03", alarm.getFormattedTime())
    }

    @Test
    fun getTotalSeconds_calculatesCorrectly() {
        val alarm =
            AlarmVO(
                key = 1,
                schedule = AlarmScheduleVO(hour = 2, minute = 30, second = 45),
                notification = AlarmNotificationVO(title = "Test", message = "Test message"),
            )

        // 2 * 3600 + 30 * 60 + 45 = 7200 + 1800 + 45 = 9045
        assertEquals(9045, alarm.getTotalSeconds())
    }

    @Test
    fun getDescription_returnsCorrectFormat() {
        val alarm =
            AlarmVO(
                key = 123,
                schedule = AlarmScheduleVO(hour = 6, minute = 30),
                notification = AlarmNotificationVO(title = "Morning Alarm", message = "Test message"),
                isActive = true,
            )

        val description = alarm.getDescription()

        assertTrue(description.contains("Alarm[123]"))
        assertTrue(description.contains("Morning Alarm"))
        assertTrue(description.contains("06:30:00"))
        assertTrue(description.contains("active: true"))
    }

    // ==============================================
    // AlarmVO Companion Object Tests
    // ==============================================

    @Test
    fun createSimple_createsAlarmWithMinimalConfiguration() {
        val alarm =
            AlarmVO.createSimple(
                key = 5,
                title = "Simple Alarm",
                message = "Simple message",
                hour = 8,
                minute = 0,
            )

        assertEquals(5, alarm.key)
        assertEquals("Simple Alarm", alarm.notification.title)
        assertEquals("Simple message", alarm.notification.message)
        assertEquals(8, alarm.schedule.hour)
        assertEquals(0, alarm.schedule.minute)
        assertEquals(0, alarm.schedule.second)
        assertTrue(alarm.isActive)
        assertEquals(AlarmIdleMode.NONE, alarm.schedule.idleMode)
    }

    @Test
    fun createIdleAllowed_createsAlarmWithIdlePermission() {
        val alarm =
            AlarmVO.createIdleAllowed(
                key = 10,
                title = "Idle Alarm",
                message = "Idle message",
                hour = 12,
                minute = 30,
                second = 15,
            )

        assertEquals(10, alarm.key)
        assertEquals(AlarmIdleMode.INEXACT, alarm.schedule.idleMode)
        assertEquals(12, alarm.schedule.hour)
        assertEquals(30, alarm.schedule.minute)
        assertEquals(15, alarm.schedule.second)
    }

    @Test
    fun createIdleAllowed_usesDefaultSecondWhenNotProvided() {
        val alarm =
            AlarmVO.createIdleAllowed(
                key = 11,
                title = "Idle Alarm Default",
                message = "Default second test",
                hour = 15,
                minute = 45,
                // second parameter omitted to test default value
            )

        assertEquals(11, alarm.key)
        assertEquals(AlarmIdleMode.INEXACT, alarm.schedule.idleMode)
        assertEquals(15, alarm.schedule.hour)
        assertEquals(45, alarm.schedule.minute)
        assertEquals(0, alarm.schedule.second) // Should use default value 0
    }

    @Test
    fun createExactIdleAllowed_createsExactIdleAlarm() {
        val alarm =
            AlarmVO.createExactIdleAllowed(
                key = 12,
                title = "Exact Idle Alarm",
                message = "Exact idle message",
                hour = 9,
                minute = 10,
                second = 5,
            )

        assertEquals(12, alarm.key)
        assertEquals(AlarmIdleMode.EXACT, alarm.schedule.idleMode)
        assertEquals(9, alarm.schedule.hour)
        assertEquals(10, alarm.schedule.minute)
        assertEquals(5, alarm.schedule.second)
    }

    @Test
    fun vibrationEffect_returnsLongArrayFromPattern() {
        val pattern = listOf(0L, 100L, 200L, 100L)
        val alarm =
            AlarmVO(
                key = 1,
                schedule = AlarmScheduleVO(hour = 10, minute = 0),
                notification =
                    AlarmNotificationVO(
                        title = "Test",
                        message = "Test message",
                        vibrationPattern = pattern,
                    ),
            )

        val effect = alarm.notification.vibrationPattern?.toLongArray()

        assertNotNull(effect)
        assertEquals(4, effect!!.size)
        assertEquals(0L, effect[0])
        assertEquals(100L, effect[1])
    }

    @Test
    fun vibrationEffect_returnsNullWhenPatternIsNull() {
        val alarm =
            AlarmVO(
                key = 1,
                schedule = AlarmScheduleVO(hour = 10, minute = 0),
                notification = AlarmNotificationVO(title = "Test", message = "Test message"),
            )

        val effect = alarm.notification.vibrationPattern?.toLongArray()

        assertNull(effect)
    }

    @Test
    fun msg_returnsMessageValue() {
        val alarm =
            AlarmVO(
                key = 1,
                schedule = AlarmScheduleVO(hour = 10, minute = 0),
                notification = AlarmNotificationVO(title = "Test", message = "Original message"),
            )

        assertEquals("Original message", alarm.notification.message)
    }

    // ==============================================
    // AlarmDateVO Tests
    // ==============================================

    @Test
    fun AlarmDateVO_createsSuccessfullyWithValidDate() {
        val date = AlarmDateVO(year = 2026, month = 6, day = 15)

        assertEquals(2026, date.year)
        assertEquals(6, date.month)
        assertEquals(15, date.day)
    }

    @Test
    fun AlarmDateVO_createsSuccessfullyWithLeapYearDate() {
        val date = AlarmDateVO(year = 2028, month = 2, day = 29)

        assertEquals(29, date.day)
    }

    @Test(expected = IllegalArgumentException::class)
    fun AlarmDateVO_throwsExceptionWhenYearIsBelow1970() {
        AlarmDateVO(year = 1969, month = 1, day = 1)
    }

    @Test(expected = IllegalArgumentException::class)
    fun AlarmDateVO_throwsExceptionWhenMonthIsZero() {
        AlarmDateVO(year = 2026, month = 0, day = 1)
    }

    @Test(expected = IllegalArgumentException::class)
    fun AlarmDateVO_throwsExceptionWhenMonthIsGreaterThan12() {
        AlarmDateVO(year = 2026, month = 13, day = 1)
    }

    @Test(expected = IllegalArgumentException::class)
    fun AlarmDateVO_throwsExceptionWhenDayIsZero() {
        AlarmDateVO(year = 2026, month = 1, day = 0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun AlarmDateVO_throwsExceptionWhenDayIsGreaterThan31() {
        AlarmDateVO(year = 2026, month = 1, day = 32)
    }

    @Test(expected = IllegalArgumentException::class)
    fun AlarmDateVO_throwsExceptionWhenFeb30() {
        AlarmDateVO(year = 2026, month = 2, day = 30)
    }

    @Test(expected = IllegalArgumentException::class)
    fun AlarmDateVO_throwsExceptionWhenFeb29OnNonLeapYear() {
        AlarmDateVO(year = 2025, month = 2, day = 29)
    }

    // ==============================================
    // AlarmScheduleVO Extended Tests
    // ==============================================

    @Test
    fun AlarmScheduleVO_createsWithDateField() {
        val date = AlarmDateVO(year = 2026, month = 12, day = 25)
        val schedule = AlarmScheduleVO(hour = 9, minute = 0, date = date)

        assertNotNull(schedule.date)
        assertEquals(2026, schedule.date!!.year)
        assertEquals(12, schedule.date!!.month)
        assertEquals(25, schedule.date!!.day)
    }

    @Test
    fun AlarmScheduleVO_dateDefaultsToNull() {
        val schedule = AlarmScheduleVO(hour = 10, minute = 30)

        assertNull(schedule.date)
    }

    @Test
    fun AlarmScheduleVO_idleModeDefaultsToNone() {
        val schedule = AlarmScheduleVO(hour = 10, minute = 30)

        assertEquals(AlarmIdleMode.NONE, schedule.idleMode)
    }

    @Test
    fun AlarmScheduleVO_createsWithInexactIdleMode() {
        val schedule = AlarmScheduleVO(hour = 10, minute = 30, idleMode = AlarmIdleMode.INEXACT)

        assertEquals(AlarmIdleMode.INEXACT, schedule.idleMode)
    }

    @Test
    fun AlarmScheduleVO_createsWithExactIdleMode() {
        val schedule = AlarmScheduleVO(hour = 10, minute = 30, idleMode = AlarmIdleMode.EXACT)

        assertEquals(AlarmIdleMode.EXACT, schedule.idleMode)
    }

    // ==============================================
    // AlarmVO.createOnDate Tests
    // ==============================================

    @Test
    fun createOnDate_createsAlarmWithSpecificDate() {
        val date = AlarmDateVO(year = 2026, month = 7, day = 20)
        val alarm = AlarmVO.createOnDate(
            key = 20,
            title = "Date Alarm",
            message = "Scheduled for a specific date",
            date = date,
            hour = 14,
            minute = 30,
        )

        assertEquals(20, alarm.key)
        assertEquals(14, alarm.schedule.hour)
        assertEquals(30, alarm.schedule.minute)
        assertEquals(0, alarm.schedule.second)
        assertNotNull(alarm.schedule.date)
        assertEquals(2026, alarm.schedule.date!!.year)
        assertEquals(7, alarm.schedule.date!!.month)
        assertEquals(20, alarm.schedule.date!!.day)
        assertEquals("Date Alarm", alarm.notification.title)
    }

    @Test
    fun createOnDate_createsAlarmWithSecondsSpecified() {
        val date = AlarmDateVO(year = 2027, month = 1, day = 1)
        val alarm = AlarmVO.createOnDate(
            key = 21,
            title = "New Year",
            message = "Happy New Year",
            date = date,
            hour = 0,
            minute = 0,
            second = 1,
        )

        assertEquals(1, alarm.schedule.second)
    }

    // ==============================================
    // AlarmVO.withTime preserves date/idleMode
    // ==============================================

    @Test
    fun withTime_preservesDateField() {
        val date = AlarmDateVO(year = 2026, month = 3, day = 15)
        val original = AlarmVO(
            key = 30,
            schedule = AlarmScheduleVO(hour = 10, minute = 0, date = date),
            notification = AlarmNotificationVO(title = "Test", message = "Test message"),
        )

        val modified = original.withTime(15, 30)

        assertEquals(15, modified.schedule.hour)
        assertEquals(30, modified.schedule.minute)
        assertNotNull(modified.schedule.date)
        assertEquals(2026, modified.schedule.date!!.year)
        assertEquals(3, modified.schedule.date!!.month)
        assertEquals(15, modified.schedule.date!!.day)
    }

    @Test
    fun withTime_preservesIdleMode() {
        val original = AlarmVO(
            key = 31,
            schedule = AlarmScheduleVO(hour = 10, minute = 0, idleMode = AlarmIdleMode.EXACT),
            notification = AlarmNotificationVO(title = "Test", message = "Test message"),
        )

        val modified = original.withTime(20, 45)

        assertEquals(AlarmIdleMode.EXACT, modified.schedule.idleMode)
    }

    // ==============================================
    // AlarmIdleMode Enum Tests
    // ==============================================

    @Test
    fun AlarmIdleMode_hasThreeValues() {
        val values = AlarmIdleMode.entries

        assertEquals(3, values.size)
        assertTrue(values.contains(AlarmIdleMode.NONE))
        assertTrue(values.contains(AlarmIdleMode.INEXACT))
        assertTrue(values.contains(AlarmIdleMode.EXACT))
    }
}
