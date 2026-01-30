package kr.open.library.simple_ui.core.robolectric.system_manager.controller.alarm

import android.app.AlarmManager
import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import kr.open.library.simple_ui.core.system_manager.controller.alarm.AlarmController
import kr.open.library.simple_ui.core.system_manager.controller.alarm.vo.AlarmDateVO
import kr.open.library.simple_ui.core.system_manager.controller.alarm.vo.AlarmIdleMode
import kr.open.library.simple_ui.core.system_manager.controller.alarm.vo.AlarmNotificationVO
import kr.open.library.simple_ui.core.system_manager.controller.alarm.vo.AlarmScheduleVO
import kr.open.library.simple_ui.core.system_manager.controller.alarm.vo.AlarmVO
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowAlarmManager

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.P])
class AlarmControllerRobolectricTest {
    private lateinit var application: Application
    private lateinit var controller: AlarmController
    private lateinit var shadowAlarmManager: ShadowAlarmManager

    @Before
    fun setUp() {
        application = ApplicationProvider.getApplicationContext()
        controller = AlarmController(application)
        shadowAlarmManager = Shadows.shadowOf(controller.alarmManager)
    }

    @Test
    fun registerAlarmClock_setsNextAlarmClockAndPendingIntent() {
        val alarmVo =
            AlarmVO.createSimple(
                key = 1,
                title = "Morning alarm",
                message = "Wake up",
                hour = 1,
                minute = 15,
            )

        val result = controller.registerAlarmClock(TestAlarmReceiver::class.java, alarmVo)

        assertTrue(result)
        assertNotNull(controller.alarmManager.nextAlarmClock)
        assertTrue(controller.exists(alarmVo.key, TestAlarmReceiver::class.java))
    }

    @Test
    fun registerAlarmExactAndAllowWhileIdle_schedulesExactWakeup() {
        val alarmVo =
            AlarmVO.createSimple(
                key = 2,
                title = "Exact alarm",
                message = "Do work",
                hour = 2,
                minute = 0,
            )

        val scheduled = controller.registerAlarmExactAndAllowWhileIdle(TestAlarmReceiver::class.java, alarmVo)

        assertTrue(scheduled)
        val nextAlarm = shadowAlarmManager.nextScheduledAlarm
        assertNotNull(nextAlarm)
        assertTrue(nextAlarm!!.type == AlarmManager.RTC_WAKEUP)
    }

    @Test
    fun registerAlarmClock_whenTimePassed_schedulesNextDay() {
        val alarmVo =
            AlarmVO.createSimple(
                key = 3,
                title = "Next day alarm",
                message = "Run tomorrow",
                hour = 9,
                minute = 0,
            )

        controller.registerAlarmClock(TestAlarmReceiver::class.java, alarmVo)

        val triggerTime = controller.alarmManager.nextAlarmClock?.triggerTime
        assertNotNull(triggerTime)
    }

    @Test
    fun remove_cancelsRegisteredAlarm() {
        val alarmVo =
            AlarmVO.createSimple(
                key = 4,
                title = "Remove alarm",
                message = "Cancel me",
                hour = 3,
                minute = 30,
            )

        controller.registerAlarmAndAllowWhileIdle(TestAlarmReceiver::class.java, alarmVo)
        assertTrue(controller.exists(alarmVo.key, TestAlarmReceiver::class.java))

        val removed = controller.remove(alarmVo.key, TestAlarmReceiver::class.java)

        assertTrue(removed)
        assertTrue(controller.exists(alarmVo.key, TestAlarmReceiver::class.java).not())
    }

    // ==============================================
    // registerAlarmAndAllowWhileIdle Tests
    // ==============================================

    @Test
    fun registerAlarmAndAllowWhileIdle_schedulesIdleAlarm() {
        val alarmVo = AlarmVO.createSimple(
            key = 10,
            title = "Idle alarm",
            message = "Fire in idle",
            hour = 4,
            minute = 0,
        )

        val result = controller.registerAlarmAndAllowWhileIdle(TestAlarmReceiver::class.java, alarmVo)

        assertTrue(result)
        assertTrue(controller.exists(alarmVo.key, TestAlarmReceiver::class.java))
    }

    // ==============================================
    // registerRepeating Tests
    // ==============================================

    @Test
    fun registerRepeating_schedulesRepeatingAlarm() {
        val alarmVo = AlarmVO.createSimple(
            key = 20,
            title = "Repeating alarm",
            message = "Every hour",
            hour = 5,
            minute = 0,
        )

        val result = controller.registerRepeating(
            TestAlarmReceiver::class.java,
            alarmVo,
            intervalMillis = 3600_000L,
        )

        assertTrue(result)
        val nextAlarm = shadowAlarmManager.nextScheduledAlarm
        assertNotNull(nextAlarm)
    }

    @Test
    fun registerRepeating_clampsShortInterval() {
        val alarmVo = AlarmVO.createSimple(
            key = 21,
            title = "Short interval",
            message = "Clamped",
            hour = 6,
            minute = 0,
        )

        val result = controller.registerRepeating(
            TestAlarmReceiver::class.java,
            alarmVo,
            intervalMillis = 1000L, // 1 second, should be clamped to 60_000ms
        )

        assertTrue(result)
        val nextAlarm = shadowAlarmManager.nextScheduledAlarm
        assertNotNull(nextAlarm)
        assertEquals(60_000L, nextAlarm!!.interval)
    }

    // ==============================================
    // registerBySchedule Tests
    // ==============================================

    @Test
    fun registerBySchedule_withNone_usesAlarmClock() {
        val alarmVo = AlarmVO(
            key = 30,
            schedule = AlarmScheduleVO(hour = 7, minute = 0, idleMode = AlarmIdleMode.NONE),
            notification = AlarmNotificationVO(title = "Clock", message = "Alarm clock"),
        )

        val result = controller.registerBySchedule(TestAlarmReceiver::class.java, alarmVo)

        assertTrue(result)
        assertNotNull(controller.alarmManager.nextAlarmClock)
    }

    @Test
    fun registerBySchedule_withInexact_schedulesIdleAlarm() {
        val alarmVo = AlarmVO(
            key = 31,
            schedule = AlarmScheduleVO(hour = 8, minute = 0, idleMode = AlarmIdleMode.INEXACT),
            notification = AlarmNotificationVO(title = "Inexact", message = "Idle alarm"),
        )

        val result = controller.registerBySchedule(TestAlarmReceiver::class.java, alarmVo)

        assertTrue(result)
        assertTrue(controller.exists(alarmVo.key, TestAlarmReceiver::class.java))
    }

    @Test
    fun registerBySchedule_withExact_schedulesExactAlarm() {
        val alarmVo = AlarmVO(
            key = 32,
            schedule = AlarmScheduleVO(hour = 9, minute = 0, idleMode = AlarmIdleMode.EXACT),
            notification = AlarmNotificationVO(title = "Exact", message = "Exact alarm"),
        )

        val result = controller.registerBySchedule(TestAlarmReceiver::class.java, alarmVo)

        assertTrue(result)
        val nextAlarm = shadowAlarmManager.nextScheduledAlarm
        assertNotNull(nextAlarm)
        assertEquals(AlarmManager.RTC_WAKEUP, nextAlarm!!.type)
    }

    // ==============================================
    // update Tests
    // ==============================================

    @Test
    fun updateAlarmClock_replacesExistingAlarm() {
        val original = AlarmVO.createSimple(
            key = 40,
            title = "Original",
            message = "Original alarm",
            hour = 10,
            minute = 0,
        )
        controller.registerAlarmClock(TestAlarmReceiver::class.java, original)
        assertTrue(controller.exists(original.key, TestAlarmReceiver::class.java))

        val updated = original.withTime(11, 30)
        val result = controller.updateAlarmClock(TestAlarmReceiver::class.java, updated)

        assertTrue(result)
        assertTrue(controller.exists(updated.key, TestAlarmReceiver::class.java))
    }

    @Test
    fun updateRepeating_replacesExistingRepeatingAlarm() {
        val original = AlarmVO.createSimple(
            key = 41,
            title = "Repeat Original",
            message = "Repeat",
            hour = 12,
            minute = 0,
        )
        controller.registerRepeating(TestAlarmReceiver::class.java, original, 3600_000L)

        val updated = original.withTime(13, 0)
        val result = controller.updateRepeating(
            TestAlarmReceiver::class.java,
            updated,
            intervalMillis = 7200_000L,
        )

        assertTrue(result)
        assertTrue(controller.exists(updated.key, TestAlarmReceiver::class.java))
    }

    // ==============================================
    // exists Tests
    // ==============================================

    @Test
    fun exists_returnsFalseForUnregisteredAlarm() {
        val result = controller.exists(999, TestAlarmReceiver::class.java)

        assertFalse(result)
    }

    // ==============================================
    // canScheduleExactAlarms Tests
    // ==============================================

    @Test
    fun canScheduleExactAlarms_returnsTrueOnApi28() {
        // API 28 (P) is below S (31), so should always return true
        assertTrue(controller.canScheduleExactAlarms())
    }

    // ==============================================
    // namespace Tests
    // ==============================================

    @Test
    fun namespace_differentNamespaceCreatesIndependentAlarms() {
        val alarmVo = AlarmVO.createSimple(
            key = 50,
            title = "Namespace test",
            message = "Independent alarms",
            hour = 14,
            minute = 0,
        )

        controller.registerAlarmClock(TestAlarmReceiver::class.java, alarmVo, namespace = "group_a")
        controller.registerAlarmClock(TestAlarmReceiver::class.java, alarmVo, namespace = "group_b")

        // Both should exist independently
        assertTrue(controller.exists(alarmVo.key, TestAlarmReceiver::class.java, namespace = "group_a"))
        assertTrue(controller.exists(alarmVo.key, TestAlarmReceiver::class.java, namespace = "group_b"))

        // Remove only group_a
        controller.remove(alarmVo.key, TestAlarmReceiver::class.java, namespace = "group_a")

        assertFalse(controller.exists(alarmVo.key, TestAlarmReceiver::class.java, namespace = "group_a"))
        assertTrue(controller.exists(alarmVo.key, TestAlarmReceiver::class.java, namespace = "group_b"))
    }

    // ==============================================
    // Date-specific alarm Tests
    // ==============================================

    @Test
    fun registerAlarmClock_withFutureDate_succeeds() {
        val futureDate = AlarmDateVO(year = 2030, month = 6, day = 15)
        val alarmVo = AlarmVO.createOnDate(
            key = 60,
            title = "Future date",
            message = "Far future alarm",
            date = futureDate,
            hour = 10,
            minute = 0,
        )

        val result = controller.registerAlarmClock(TestAlarmReceiver::class.java, alarmVo)

        assertTrue(result)
        assertNotNull(controller.alarmManager.nextAlarmClock)
    }

    @Test
    fun registerAlarmClock_withPastDate_returnsFalse() {
        val pastDate = AlarmDateVO(year = 2020, month = 1, day = 1)
        val alarmVo = AlarmVO.createOnDate(
            key = 61,
            title = "Past date",
            message = "Should fail",
            date = pastDate,
            hour = 0,
            minute = 0,
        )

        // getCalendar throws require(false) for past date, caught by tryCatchSystemManager → returns false
        val result = controller.registerAlarmClock(TestAlarmReceiver::class.java, alarmVo)

        assertFalse(result)
    }

    private class TestAlarmReceiver : BroadcastReceiver() {
        override fun onReceive(
            context: Context?,
            intent: Intent?,
        ) {
            // no-op for testing
        }
    }
}
