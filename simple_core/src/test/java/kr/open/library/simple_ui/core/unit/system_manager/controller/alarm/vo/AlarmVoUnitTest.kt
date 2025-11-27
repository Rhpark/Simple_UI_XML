package kr.open.library.simple_ui.core.unit.system_manager.controller.alarm.vo

import kr.open.library.simple_ui.core.system_manager.controller.alarm.vo.AlarmConstants
import kr.open.library.simple_ui.core.system_manager.controller.alarm.vo.AlarmVo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for AlarmVo and AlarmConstants
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

    @Test
    fun alarmConstants_hasCorrectCalendarSettings() {
        assertEquals(1000L, AlarmConstants.MILLISECONDS_IN_SECOND)
        assertEquals(60L, AlarmConstants.SECONDS_IN_MINUTE)
        assertEquals(60L, AlarmConstants.MINUTES_IN_HOUR)
    }

    @Test
    fun alarmConstants_hasCorrectAlarmTypes() {
        assertEquals("ALARM_CLOCK", AlarmConstants.ALARM_TYPE_CLOCK)
        assertEquals("ALLOW_WHILE_IDLE", AlarmConstants.ALARM_TYPE_IDLE)
        assertEquals("EXACT_AND_ALLOW_WHILE_IDLE", AlarmConstants.ALARM_TYPE_EXACT_IDLE)
    }

    @Test
    fun alarmConstants_hasCorrectErrorCodes() {
        assertEquals(-1001, AlarmConstants.ERROR_INVALID_TIME)
        assertEquals(-1002, AlarmConstants.ERROR_PENDING_INTENT_FAILED)
        assertEquals(-1003, AlarmConstants.ERROR_ALARM_REGISTRATION_FAILED)
    }

    // ==============================================
    // AlarmVo Creation Tests
    // ==============================================

    @Test
    fun alarmVo_createsSuccessfullyWithValidData() {
        val alarm = AlarmVo(
            key = 1,
            title = "Wake Up",
            message = "Time to wake up!",
            hour = 7,
            minute = 30
        )

        assertEquals(1, alarm.key)
        assertEquals("Wake Up", alarm.title)
        assertEquals("Time to wake up!", alarm.message)
        assertEquals(7, alarm.hour)
        assertEquals(30, alarm.minute)
        assertEquals(0, alarm.second) // Default
        assertTrue(alarm.isActive) // Default
        assertFalse(alarm.isAllowIdle) // Default
        assertNull(alarm.vibrationPattern)
        assertNull(alarm.soundUri)
        assertEquals(AlarmConstants.DEFAULT_ACQUIRE_TIME_MS, alarm.acquireTime)
    }

    @Test
    fun alarmVo_createsWithAllParameters() {
        val vibrationPattern = listOf(0L, 100L, 200L, 100L)
        val alarm = AlarmVo(
            key = 2,
            title = "Meeting",
            message = "Team meeting in 5 minutes",
            isActive = false,
            isAllowIdle = true,
            vibrationPattern = vibrationPattern,
            soundUri = null,
            hour = 14,
            minute = 55,
            second = 30,
            acquireTime = 5000L
        )

        assertEquals(2, alarm.key)
        assertFalse(alarm.isActive)
        assertTrue(alarm.isAllowIdle)
        assertEquals(vibrationPattern, alarm.vibrationPattern)
        assertEquals(14, alarm.hour)
        assertEquals(55, alarm.minute)
        assertEquals(30, alarm.second)
        assertEquals(5000L, alarm.acquireTime)
    }

    // ==============================================
    // AlarmVo Validation Tests
    // ==============================================

    @Test(expected = IllegalArgumentException::class)
    fun alarmVo_throwsExceptionWhenKeyIsZero() {
        AlarmVo(
            key = 0,
            title = "Test",
            message = "Test message",
            hour = 10,
            minute = 0
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun alarmVo_throwsExceptionWhenKeyIsNegative() {
        AlarmVo(
            key = -1,
            title = "Test",
            message = "Test message",
            hour = 10,
            minute = 0
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun alarmVo_throwsExceptionWhenTitleIsBlank() {
        AlarmVo(
            key = 1,
            title = "   ",
            message = "Test message",
            hour = 10,
            minute = 0
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun alarmVo_throwsExceptionWhenMessageIsBlank() {
        AlarmVo(
            key = 1,
            title = "Test",
            message = "",
            hour = 10,
            minute = 0
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun alarmVo_throwsExceptionWhenHourIsNegative() {
        AlarmVo(
            key = 1,
            title = "Test",
            message = "Test message",
            hour = -1,
            minute = 0
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun alarmVo_throwsExceptionWhenHourIsGreaterThan23() {
        AlarmVo(
            key = 1,
            title = "Test",
            message = "Test message",
            hour = 24,
            minute = 0
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun alarmVo_throwsExceptionWhenMinuteIsNegative() {
        AlarmVo(
            key = 1,
            title = "Test",
            message = "Test message",
            hour = 10,
            minute = -1
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun alarmVo_throwsExceptionWhenMinuteIsGreaterThan59() {
        AlarmVo(
            key = 1,
            title = "Test",
            message = "Test message",
            hour = 10,
            minute = 60
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun alarmVo_throwsExceptionWhenSecondIsNegative() {
        AlarmVo(
            key = 1,
            title = "Test",
            message = "Test message",
            hour = 10,
            minute = 30,
            second = -1
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun alarmVo_throwsExceptionWhenSecondIsGreaterThan59() {
        AlarmVo(
            key = 1,
            title = "Test",
            message = "Test message",
            hour = 10,
            minute = 30,
            second = 60
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun alarmVo_throwsExceptionWhenAcquireTimeIsZero() {
        AlarmVo(
            key = 1,
            title = "Test",
            message = "Test message",
            hour = 10,
            minute = 0,
            acquireTime = 0L
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun alarmVo_throwsExceptionWhenAcquireTimeIsNegative() {
        AlarmVo(
            key = 1,
            title = "Test",
            message = "Test message",
            hour = 10,
            minute = 0,
            acquireTime = -1000L
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun alarmVo_throwsExceptionWhenVibrationPatternIsEmpty() {
        AlarmVo(
            key = 1,
            title = "Test",
            message = "Test message",
            hour = 10,
            minute = 0,
            vibrationPattern = emptyList()
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun alarmVo_throwsExceptionWhenVibrationPatternHasNegativeValue() {
        AlarmVo(
            key = 1,
            title = "Test",
            message = "Test message",
            hour = 10,
            minute = 0,
            vibrationPattern = listOf(0L, -100L, 200L)
        )
    }

    // ==============================================
    // AlarmVo Method Tests
    // ==============================================

    @Test
    fun withActiveState_createsNewAlarmWithModifiedState() {
        val original = AlarmVo(
            key = 1,
            title = "Test",
            message = "Test message",
            isActive = true,
            hour = 10,
            minute = 0
        )

        val modified = original.withActiveState(false)

        assertFalse(modified.isActive)
        assertEquals(original.key, modified.key)
        assertEquals(original.title, modified.title)
    }

    @Test
    fun withTime_createsNewAlarmWithModifiedTime() {
        val original = AlarmVo(
            key = 1,
            title = "Test",
            message = "Test message",
            hour = 10,
            minute = 30,
            second = 15
        )

        val modified = original.withTime(14, 45)

        assertEquals(14, modified.hour)
        assertEquals(45, modified.minute)
        assertEquals(15, modified.second) // Preserved
    }

    @Test
    fun withTime_canModifySeconds() {
        val original = AlarmVo(
            key = 1,
            title = "Test",
            message = "Test message",
            hour = 10,
            minute = 30
        )

        val modified = original.withTime(14, 45, 30)

        assertEquals(14, modified.hour)
        assertEquals(45, modified.minute)
        assertEquals(30, modified.second)
    }

    @Test
    fun getFormattedTime_returnsCorrectFormat() {
        val alarm = AlarmVo(
            key = 1,
            title = "Test",
            message = "Test message",
            hour = 7,
            minute = 5,
            second = 3
        )

        assertEquals("07:05:03", alarm.getFormattedTime())
    }

    @Test
    fun getTotalSeconds_calculatesCorrectly() {
        val alarm = AlarmVo(
            key = 1,
            title = "Test",
            message = "Test message",
            hour = 2,
            minute = 30,
            second = 45
        )

        // 2 * 3600 + 30 * 60 + 45 = 7200 + 1800 + 45 = 9045
        assertEquals(9045, alarm.getTotalSeconds())
    }

    @Test
    fun getDescription_returnsCorrectFormat() {
        val alarm = AlarmVo(
            key = 123,
            title = "Morning Alarm",
            message = "Test message",
            hour = 6,
            minute = 30,
            isActive = true
        )

        val description = alarm.getDescription()

        assertTrue(description.contains("Alarm[123]"))
        assertTrue(description.contains("Morning Alarm"))
        assertTrue(description.contains("06:30:00"))
        assertTrue(description.contains("active: true"))
    }

    // ==============================================
    // AlarmVo Companion Object Tests
    // ==============================================

    @Test
    fun createSimple_createsAlarmWithMinimalConfiguration() {
        val alarm = AlarmVo.createSimple(
            key = 5,
            title = "Simple Alarm",
            message = "Simple message",
            hour = 8,
            minute = 0
        )

        assertEquals(5, alarm.key)
        assertEquals("Simple Alarm", alarm.title)
        assertEquals("Simple message", alarm.message)
        assertEquals(8, alarm.hour)
        assertEquals(0, alarm.minute)
        assertEquals(0, alarm.second)
        assertTrue(alarm.isActive)
        assertFalse(alarm.isAllowIdle)
    }

    @Test
    fun createIdleAllowed_createsAlarmWithIdlePermission() {
        val alarm = AlarmVo.createIdleAllowed(
            key = 10,
            title = "Idle Alarm",
            message = "Idle message",
            hour = 12,
            minute = 30,
            second = 15
        )

        assertEquals(10, alarm.key)
        assertTrue(alarm.isAllowIdle)
        assertEquals(12, alarm.hour)
        assertEquals(30, alarm.minute)
        assertEquals(15, alarm.second)
    }

    @Test
    fun createIdleAllowed_usesDefaultSecondWhenNotProvided() {
        val alarm = AlarmVo.createIdleAllowed(
            key = 11,
            title = "Idle Alarm Default",
            message = "Default second test",
            hour = 15,
            minute = 45
            // second parameter omitted to test default value
        )

        assertEquals(11, alarm.key)
        assertTrue(alarm.isAllowIdle)
        assertEquals(15, alarm.hour)
        assertEquals(45, alarm.minute)
        assertEquals(0, alarm.second) // Should use default value 0
    }

    // ==============================================
    // AlarmVo Deprecated Property Tests
    // ==============================================

    @Test
    fun vibrationEffect_returnsLongArrayFromPattern() {
        val pattern = listOf(0L, 100L, 200L, 100L)
        val alarm = AlarmVo(
            key = 1,
            title = "Test",
            message = "Test message",
            hour = 10,
            minute = 0,
            vibrationPattern = pattern
        )

        @Suppress("DEPRECATION")
        val effect = alarm.vibrationEffect

        assertNotNull(effect)
        assertEquals(4, effect!!.size)
        assertEquals(0L, effect[0])
        assertEquals(100L, effect[1])
    }

    @Test
    fun vibrationEffect_returnsNullWhenPatternIsNull() {
        val alarm = AlarmVo(
            key = 1,
            title = "Test",
            message = "Test message",
            hour = 10,
            minute = 0,
            vibrationPattern = null
        )

        @Suppress("DEPRECATION")
        val effect = alarm.vibrationEffect

        assertNull(effect)
    }

    @Test
    fun msg_returnsMessageValue() {
        val alarm = AlarmVo(
            key = 1,
            title = "Test",
            message = "Original message",
            hour = 10,
            minute = 0
        )

        @Suppress("DEPRECATION")
        assertEquals("Original message", alarm.msg)
    }
}
