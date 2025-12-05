package kr.open.library.simple_ui.core.robolectric.system_manager.controller.alarm

import android.app.AlarmManager
import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import kr.open.library.simple_ui.core.system_manager.controller.alarm.AlarmController
import kr.open.library.simple_ui.core.system_manager.controller.alarm.vo.AlarmVo
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
            AlarmVo.createSimple(
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
            AlarmVo.createSimple(
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
            AlarmVo.createSimple(
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
            AlarmVo.createSimple(
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

    private class TestAlarmReceiver : BroadcastReceiver() {
        override fun onReceive(
            context: Context?,
            intent: Intent?,
        ) {
            // no-op for testing
        }
    }
}
