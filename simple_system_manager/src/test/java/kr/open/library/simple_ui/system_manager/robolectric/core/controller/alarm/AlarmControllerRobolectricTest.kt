package kr.open.library.simple_ui.system_manager.robolectric.core.controller.alarm

import android.app.AlarmManager
import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import kr.open.library.simple_ui.system_manager.core.controller.alarm.AlarmController
import kr.open.library.simple_ui.system_manager.core.controller.alarm.vo.AlarmData
import kr.open.library.simple_ui.system_manager.core.controller.alarm.vo.AlarmDateData
import kr.open.library.simple_ui.system_manager.core.controller.alarm.vo.AlarmIdleMode
import kr.open.library.simple_ui.system_manager.core.controller.alarm.vo.AlarmNotificationData
import kr.open.library.simple_ui.system_manager.core.controller.alarm.vo.AlarmScheduleData
import kr.open.library.simple_ui.system_manager.testutil.assertFailure
import kr.open.library.simple_ui.system_manager.testutil.assertPermissionDenied
import kr.open.library.simple_ui.system_manager.testutil.assertSuccess
import kr.open.library.simple_ui.system_manager.testutil.assertSuccessValue
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowAlarmManager
import org.robolectric.shadows.ShadowPendingIntent

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
        val alarmData =
            AlarmData.createSimple(
                key = 1,
                title = "Morning alarm",
                message = "Wake up",
                hour = 1,
                minute = 15,
            )

        val result = controller.registerAlarmClock(TestAlarmReceiver::class.java, alarmData)

        assertSuccess(result)
        assertNotNull(controller.alarmManager.nextAlarmClock)
        assertTrue(controller.exists(alarmData.key, TestAlarmReceiver::class.java))
        val showIntent = controller.alarmManager.nextAlarmClock!!.showIntent
        val shadowShowIntent: ShadowPendingIntent = Shadows.shadowOf(showIntent)
        assertTrue(shadowShowIntent.isActivityIntent)
    }

    @Test
    fun registerAlarmExactAndAllowWhileIdle_schedulesExactWakeup() {
        val alarmData =
            AlarmData.createSimple(
                key = 2,
                title = "Exact alarm",
                message = "Do work",
                hour = 2,
                minute = 0,
            )

        val scheduled = controller.registerAlarmExactAndAllowWhileIdle(TestAlarmReceiver::class.java, alarmData)

        assertSuccess(scheduled)
        val nextAlarm = shadowAlarmManager.nextScheduledAlarm
        assertNotNull(nextAlarm)
        assertTrue(nextAlarm!!.type == AlarmManager.RTC_WAKEUP)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.S])
    fun registerAlarmClock_whenExactPermissionDenied_returnsPermissionDenied() {
        val deniedController = DeniedExactAlarmController(application)
        val alarmData = AlarmData.createSimple(
            key = 201,
            title = "Denied clock",
            message = "Permission denied",
            hour = 1,
            minute = 0,
        )

        val result = deniedController.registerAlarmClock(TestAlarmReceiver::class.java, alarmData)

        assertPermissionDenied(result)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.S])
    fun registerAlarmExactAndAllowWhileIdle_whenExactPermissionDenied_returnsPermissionDenied() {
        val deniedController = DeniedExactAlarmController(application)
        val alarmData = AlarmData.createSimple(
            key = 202,
            title = "Denied exact",
            message = "Permission denied",
            hour = 2,
            minute = 0,
        )

        val result = deniedController.registerAlarmExactAndAllowWhileIdle(TestAlarmReceiver::class.java, alarmData)

        assertPermissionDenied(result)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.S])
    fun registerAlarmAndAllowWhileIdle_whenExactPermissionDenied_stillSucceeds() {
        val deniedController = DeniedExactAlarmController(application)
        val alarmData = AlarmData.createSimple(
            key = 203,
            title = "Denied inexact",
            message = "Still allowed",
            hour = 3,
            minute = 0,
        )

        val result = deniedController.registerAlarmAndAllowWhileIdle(TestAlarmReceiver::class.java, alarmData)

        assertSuccess(result)
        assertTrue(deniedController.exists(alarmData.key, TestAlarmReceiver::class.java))
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.S])
    fun registerRepeating_whenExactPermissionDenied_stillSucceeds() {
        val deniedController = DeniedExactAlarmController(application)
        val alarmData = AlarmData.createSimple(
            key = 204,
            title = "Denied repeat",
            message = "Still allowed",
            hour = 4,
            minute = 0,
        )

        val result = deniedController.registerRepeating(
            TestAlarmReceiver::class.java,
            alarmData,
            intervalMillis = 60_000L,
        )

        assertSuccess(result)
        assertTrue(deniedController.exists(alarmData.key, TestAlarmReceiver::class.java))
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.S])
    fun registerBySchedule_withExact_whenExactPermissionDenied_returnsPermissionDenied() {
        val deniedController = DeniedExactAlarmController(application)
        val alarmData = AlarmData(
            key = 205,
            schedule = AlarmScheduleData(hour = 5, minute = 0, idleMode = AlarmIdleMode.EXACT),
            notification = AlarmNotificationData(title = "Denied exact schedule", message = "Permission denied"),
        )

        val result = deniedController.registerBySchedule(TestAlarmReceiver::class.java, alarmData)

        assertPermissionDenied(result)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.S])
    fun registerBySchedule_withInexact_whenExactPermissionDenied_stillSucceeds() {
        val deniedController = DeniedExactAlarmController(application)
        val alarmData = AlarmData(
            key = 206,
            schedule = AlarmScheduleData(hour = 6, minute = 0, idleMode = AlarmIdleMode.INEXACT),
            notification = AlarmNotificationData(title = "Allowed inexact schedule", message = "Still allowed"),
        )

        val result = deniedController.registerBySchedule(TestAlarmReceiver::class.java, alarmData)

        assertSuccess(result)
        assertTrue(deniedController.exists(alarmData.key, TestAlarmReceiver::class.java))
    }

    @Test
    fun registerAlarmClock_whenTimePassed_schedulesNextDay() {
        val alarmData =
            AlarmData.createSimple(
                key = 3,
                title = "Next day alarm",
                message = "Run tomorrow",
                hour = 9,
                minute = 0,
            )

        controller.registerAlarmClock(TestAlarmReceiver::class.java, alarmData)

        val triggerTime = controller.alarmManager.nextAlarmClock?.triggerTime
        assertNotNull(triggerTime)
    }

    @Test
    fun remove_cancelsRegisteredAlarm() {
        val alarmData =
            AlarmData.createSimple(
                key = 4,
                title = "Remove alarm",
                message = "Cancel me",
                hour = 3,
                minute = 30,
            )

        controller.registerAlarmAndAllowWhileIdle(TestAlarmReceiver::class.java, alarmData)
        assertTrue(controller.exists(alarmData.key, TestAlarmReceiver::class.java))

        val removed = controller.remove(alarmData.key, TestAlarmReceiver::class.java)

        assertSuccessValue(true, removed)
        assertTrue(controller.exists(alarmData.key, TestAlarmReceiver::class.java).not())
    }

    // ==============================================
    // registerAlarmAndAllowWhileIdle Tests
    // ==============================================

    @Test
    fun registerAlarmAndAllowWhileIdle_schedulesIdleAlarm() {
        val alarmData = AlarmData.createSimple(
            key = 10,
            title = "Idle alarm",
            message = "Fire in idle",
            hour = 4,
            minute = 0,
        )

        val result = controller.registerAlarmAndAllowWhileIdle(TestAlarmReceiver::class.java, alarmData)

        assertSuccess(result)
        assertTrue(controller.exists(alarmData.key, TestAlarmReceiver::class.java))
    }

    // ==============================================
    // registerRepeating Tests
    // ==============================================

    @Test
    fun registerRepeating_schedulesRepeatingAlarm() {
        val alarmData = AlarmData.createSimple(
            key = 20,
            title = "Repeating alarm",
            message = "Every hour",
            hour = 5,
            minute = 0,
        )

        val result = controller.registerRepeating(
            TestAlarmReceiver::class.java,
            alarmData,
            intervalMillis = 3600_000L,
        )

        assertSuccess(result)
        val nextAlarm = shadowAlarmManager.nextScheduledAlarm
        assertNotNull(nextAlarm)
    }

    @Test
    fun registerRepeating_clampsShortInterval() {
        val alarmData = AlarmData.createSimple(
            key = 21,
            title = "Short interval",
            message = "Clamped",
            hour = 6,
            minute = 0,
        )

        val result = controller.registerRepeating(
            TestAlarmReceiver::class.java,
            alarmData,
            intervalMillis = 1000L, // 1 second, should be clamped to 60_000ms
        )

        assertSuccess(result)
        val nextAlarm = shadowAlarmManager.nextScheduledAlarm
        assertNotNull(nextAlarm)
        assertEquals(60_000L, nextAlarm!!.interval)
    }

    // ==============================================
    // registerBySchedule Tests
    // ==============================================

    @Test
    fun registerBySchedule_withNone_usesAlarmClock() {
        val alarmData = AlarmData(
            key = 30,
            schedule = AlarmScheduleData(hour = 7, minute = 0, idleMode = AlarmIdleMode.NONE),
            notification = AlarmNotificationData(title = "Clock", message = "Alarm clock"),
        )

        val result = controller.registerBySchedule(TestAlarmReceiver::class.java, alarmData)

        assertSuccess(result)
        assertNotNull(controller.alarmManager.nextAlarmClock)
    }

    @Test
    fun registerBySchedule_withInexact_schedulesIdleAlarm() {
        val alarmData = AlarmData(
            key = 31,
            schedule = AlarmScheduleData(hour = 8, minute = 0, idleMode = AlarmIdleMode.INEXACT),
            notification = AlarmNotificationData(title = "Inexact", message = "Idle alarm"),
        )

        val result = controller.registerBySchedule(TestAlarmReceiver::class.java, alarmData)

        assertSuccess(result)
        assertTrue(controller.exists(alarmData.key, TestAlarmReceiver::class.java))
    }

    @Test
    fun registerBySchedule_withExact_schedulesExactAlarm() {
        val alarmData = AlarmData(
            key = 32,
            schedule = AlarmScheduleData(hour = 9, minute = 0, idleMode = AlarmIdleMode.EXACT),
            notification = AlarmNotificationData(title = "Exact", message = "Exact alarm"),
        )

        val result = controller.registerBySchedule(TestAlarmReceiver::class.java, alarmData)

        assertSuccess(result)
        val nextAlarm = shadowAlarmManager.nextScheduledAlarm
        assertNotNull(nextAlarm)
        assertEquals(AlarmManager.RTC_WAKEUP, nextAlarm!!.type)
    }

    // ==============================================
    // update Tests
    // ==============================================

    @Test
    fun updateAlarmClock_replacesExistingAlarm() {
        val original = AlarmData.createSimple(
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

        assertSuccess(result)
        assertTrue(controller.exists(updated.key, TestAlarmReceiver::class.java))
    }

    @Test
    fun updateRepeating_replacesExistingRepeatingAlarm() {
        val original = AlarmData.createSimple(
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

        assertSuccess(result)
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

    @Test
    @Config(sdk = [Build.VERSION_CODES.S])
    fun buildExactAlarmPermissionIntent_whenExactPermissionDenied_returnsSettingsIntent() {
        val deniedController = DeniedExactAlarmController(application)

        val intent = deniedController.buildExactAlarmPermissionIntent()

        assertNotNull(intent)
        assertEquals("android.settings.REQUEST_SCHEDULE_EXACT_ALARM", intent!!.action)
        assertEquals("package:${application.packageName}", intent.dataString)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.S])
    fun buildExactAlarmPermissionIntent_whenExactPermissionGranted_returnsNull() {
        val grantedController = GrantedExactAlarmController(application)

        val intent = grantedController.buildExactAlarmPermissionIntent()

        assertNull(intent)
    }

    // ==============================================
    // namespace Tests
    // ==============================================

    @Test
    fun namespace_differentNamespaceCreatesIndependentAlarms() {
        val alarmData = AlarmData.createSimple(
            key = 50,
            title = "Namespace test",
            message = "Independent alarms",
            hour = 14,
            minute = 0,
        )

        controller.registerAlarmClock(TestAlarmReceiver::class.java, alarmData, namespace = "group_a")
        controller.registerAlarmClock(TestAlarmReceiver::class.java, alarmData, namespace = "group_b")

        // Both should exist independently
        assertTrue(controller.exists(alarmData.key, TestAlarmReceiver::class.java, namespace = "group_a"))
        assertTrue(controller.exists(alarmData.key, TestAlarmReceiver::class.java, namespace = "group_b"))

        // Remove only group_a
        controller.remove(alarmData.key, TestAlarmReceiver::class.java, namespace = "group_a")

        assertFalse(controller.exists(alarmData.key, TestAlarmReceiver::class.java, namespace = "group_a"))
        assertTrue(controller.exists(alarmData.key, TestAlarmReceiver::class.java, namespace = "group_b"))
    }

    // ==============================================
    // Date-specific alarm Tests
    // ==============================================

    @Test
    fun registerAlarmClock_withFutureDate_succeeds() {
        val futureDate = AlarmDateData(year = 2030, month = 6, day = 15)
        val alarmData = AlarmData.createOnDate(
            key = 60,
            title = "Future date",
            message = "Far future alarm",
            date = futureDate,
            hour = 10,
            minute = 0,
        )

        val result = controller.registerAlarmClock(TestAlarmReceiver::class.java, alarmData)

        assertSuccess(result)
        assertNotNull(controller.alarmManager.nextAlarmClock)
    }

    @Test
    fun registerAlarmClock_withPastDate_returnsFailure() {
        val pastDate = AlarmDateData(year = 2020, month = 1, day = 1)
        val alarmData = AlarmData.createOnDate(
            key = 61,
            title = "Past date",
            message = "Should fail",
            date = pastDate,
            hour = 0,
            minute = 0,
        )

        // getCalendar throws for past date, and tryCatchSystemManagerResult maps it to Failure
        val result = controller.registerAlarmClock(TestAlarmReceiver::class.java, alarmData)

        assertFailure(result)
    }

    private class TestAlarmReceiver : BroadcastReceiver() {
        override fun onReceive(
            context: Context?,
            intent: Intent?,
        ) {
            // no-op for testing
        }
    }

    private class DeniedExactAlarmController(
        context: Context,
    ) : AlarmController(context) {
        override fun canScheduleExactAlarms(): Boolean = false
    }

    private class GrantedExactAlarmController(
        context: Context,
    ) : AlarmController(context) {
        override fun canScheduleExactAlarms(): Boolean = true
    }
}
