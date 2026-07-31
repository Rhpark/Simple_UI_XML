package kr.open.library.simple_ui.system_manager.robolectric.core.controller.alarm.receiver

import android.Manifest
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import kr.open.library.simple_ui.system_manager.core.controller.alarm.AlarmConstants
import kr.open.library.simple_ui.system_manager.core.controller.alarm.AlarmController
import kr.open.library.simple_ui.system_manager.core.controller.alarm.receiver.BaseAlarmReceiver
import kr.open.library.simple_ui.system_manager.core.controller.alarm.vo.AlarmData
import kr.open.library.simple_ui.system_manager.core.controller.alarm.vo.AlarmNotificationData
import kr.open.library.simple_ui.system_manager.core.controller.notification.option.DefaultNotificationOption
import kr.open.library.simple_ui.system_manager.core.controller.notification.option.SimpleNotificationOptionBase
import kr.open.library.simple_ui.system_manager.core.extensions.getNotificationController
import org.junit.After
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
import org.robolectric.shadows.ShadowNotificationManager
import org.robolectric.shadows.ShadowPowerManager

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.P])
class BaseAlarmReceiverRobolectricTest {
    private lateinit var application: Application
    private lateinit var alarmController: AlarmController
    private lateinit var shadowNotificationManager: ShadowNotificationManager

    @Before
    fun setUp() {
        application = ApplicationProvider.getApplicationContext()
        alarmController = AlarmController(application)
        val notificationManager = application.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        shadowNotificationManager = Shadows.shadowOf(notificationManager)
        notificationManager.cancelAll()
    }

    @After
    fun tearDown() {
        ShadowAlarmManager.setCanScheduleExactAlarms(true)
        ShadowPowerManager.clearWakeLocks()
        TEST_ALARM_KEYS.forEach { key ->
            alarmController.remove(key, TestAlarmReceiver::class.java)
        }
        val notificationManager = application.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancelAll()
    }

    @Test
    fun systemChangeActions_reregisterOnlyActiveAlarms() {
        SYSTEM_CHANGE_ACTIONS.forEachIndexed { index, action ->
            val activeAlarm = createAlarm(TEST_ALARM_KEYS[index * 2])
            val inactiveAlarm = createAlarm(TEST_ALARM_KEYS[index * 2 + 1]).withActiveState(false)
            val receiver = TestAlarmReceiver().apply {
                allAlarmData = listOf(activeAlarm, inactiveAlarm)
            }

            receiver.onReceive(application, Intent(action))

            assertEquals(1, receiver.loadAllCallCount)
            assertEquals(listOf(activeAlarm.key), receiver.resolvedRegisterKeys)
            assertTrue(alarmController.exists(activeAlarm.key, TestAlarmReceiver::class.java))
            assertFalse(alarmController.exists(inactiveAlarm.key, TestAlarmReceiver::class.java))
        }
    }

    @Test
    fun alarmTrigger_withStoredAlarm_showsNotification() {
        val alarm = createAlarm(TEST_ALARM_KEYS[6])
        val receiver = TestAlarmReceiver().apply {
            alarmDataByKey[alarm.key] = alarm
        }

        receiver.onReceive(
            application,
            Intent(TEST_ACTION).putExtra(AlarmConstants.ALARM_KEY, alarm.key),
        )

        assertEquals(alarm.key, receiver.loadedAlarmKey)
        assertEquals(alarm.notification, receiver.createdChannelNotification)
        assertEquals(alarm, receiver.builtNotificationAlarm)
        assertEquals(1, shadowNotificationManager.size())
    }

    @Test
    fun alarmTrigger_withoutAlarmKey_skipsAlarmLoadingAndNotification() {
        val receiver = TestAlarmReceiver()

        receiver.onReceive(application, Intent(TEST_ACTION))

        assertNull(receiver.loadedAlarmKey)
        assertNull(receiver.createdChannelNotification)
        assertNull(receiver.builtNotificationAlarm)
        assertEquals(0, shadowNotificationManager.size())
    }

    @Test
    fun alarmTrigger_whenStoredAlarmIsMissing_skipsNotificationSetup() {
        val receiver = TestAlarmReceiver()

        receiver.onReceive(
            application,
            Intent(TEST_ACTION).putExtra(AlarmConstants.ALARM_KEY, TEST_ALARM_KEYS[7]),
        )

        assertEquals(TEST_ALARM_KEYS[7], receiver.loadedAlarmKey)
        assertNull(receiver.createdChannelNotification)
        assertNull(receiver.builtNotificationAlarm)
        assertEquals(0, shadowNotificationManager.size())
    }

    @Test
    fun alarmTrigger_withoutNotificationController_skipsNotificationBuild() {
        val alarm = createAlarm(TEST_ALARM_KEYS[8])
        val receiver = TestAlarmReceiver(initializeNotificationController = false).apply {
            alarmDataByKey[alarm.key] = alarm
        }

        receiver.onReceive(
            application,
            Intent(TEST_ACTION).putExtra(AlarmConstants.ALARM_KEY, alarm.key),
        )

        assertEquals(alarm.notification, receiver.createdChannelNotification)
        assertNull(receiver.builtNotificationAlarm)
        assertEquals(0, shadowNotificationManager.size())
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.TIRAMISU])
    fun alarmTrigger_withoutPostNotificationsPermission_skipsNotificationBuild() {
        Shadows.shadowOf(application).denyPermissions(Manifest.permission.POST_NOTIFICATIONS)
        val alarm = createAlarm(TEST_ALARM_KEYS[9])
        val receiver = TestAlarmReceiver().apply {
            alarmDataByKey[alarm.key] = alarm
        }

        receiver.onReceive(
            application,
            Intent(TEST_ACTION).putExtra(AlarmConstants.ALARM_KEY, alarm.key),
        )

        assertEquals(alarm.notification, receiver.createdChannelNotification)
        assertNull(receiver.builtNotificationAlarm)
        assertEquals(0, shadowNotificationManager.size())
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.S])
    fun exactAlarmPermissionChange_whenDenied_callsDeniedHook() {
        ShadowAlarmManager.setCanScheduleExactAlarms(false)
        val receiver = TestAlarmReceiver()

        receiver.onReceive(
            application,
            Intent(AlarmConstants.ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED),
        )

        assertEquals(1, receiver.exactAlarmPermissionDeniedCallCount)
        assertEquals(0, receiver.loadAllCallCount)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.S])
    fun exactAlarmPermissionChange_whenGranted_reregistersActiveAlarms() {
        ShadowAlarmManager.setCanScheduleExactAlarms(true)
        val alarm = createAlarm(TEST_ALARM_KEYS[10])
        val receiver = TestAlarmReceiver().apply {
            allAlarmData = listOf(alarm)
        }

        receiver.onReceive(
            application,
            Intent(AlarmConstants.ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED),
        )

        assertEquals(0, receiver.exactAlarmPermissionDeniedCallCount)
        assertEquals(1, receiver.loadAllCallCount)
        assertEquals(listOf(alarm.key), receiver.resolvedRegisterKeys)
        assertTrue(alarmController.exists(alarm.key, TestAlarmReceiver::class.java))
    }

    @Test
    fun onReceive_releasesWakeLockAfterProcessing() {
        ShadowPowerManager.clearWakeLocks()
        val receiver = TestAlarmReceiver()

        receiver.onReceive(application, Intent(TEST_ACTION))

        val wakeLock = ShadowPowerManager.getLatestWakeLock()
        assertNotNull(wakeLock)
        assertFalse(wakeLock.isHeld)
    }

    @Test
    fun onReceive_withNullInputs_isIgnored() {
        val receiver = TestAlarmReceiver()

        receiver.onReceive(null, Intent(TEST_ACTION))
        receiver.onReceive(application, null)

        assertEquals(0, receiver.loadAllCallCount)
        assertNull(receiver.loadedAlarmKey)
        assertEquals(0, shadowNotificationManager.size())
    }

    private fun createAlarm(key: Int): AlarmData =
        AlarmData.createIdleAllowed(
            key = key,
            title = "Alarm $key",
            message = "Alarm message $key",
            hour = 12,
            minute = 0,
        )

    private class TestAlarmReceiver(
        private val initializeNotificationController: Boolean = true,
    ) : BaseAlarmReceiver() {
        val alarmDataByKey = mutableMapOf<Int, AlarmData>()
        var allAlarmData: List<AlarmData> = emptyList()
        var loadAllCallCount: Int = 0
        val resolvedRegisterKeys = mutableListOf<Int>()
        var loadedAlarmKey: Int? = null
        var createdChannelNotification: AlarmNotificationData? = null
        var builtNotificationAlarm: AlarmData? = null
        var exactAlarmPermissionDeniedCallCount: Int = 0

        override val classType: Class<*> = TestAlarmReceiver::class.java
        override val powerManagerAcquireTime: Long = 1_000L

        override fun resolveRegisterType(alarmData: AlarmData): RegisterType {
            resolvedRegisterKeys += alarmData.key
            return RegisterType.ALARM_AND_ALLOW_WHILE_IDLE
        }

        override fun createNotificationChannel(
            context: Context,
            notification: AlarmNotificationData,
        ) {
            createdChannelNotification = notification
            if (initializeNotificationController) {
                val channel =
                    NotificationChannel(
                        TEST_CHANNEL_ID,
                        TEST_CHANNEL_NAME,
                        NotificationManager.IMPORTANCE_DEFAULT,
                    )
                notificationController = context.getNotificationController(channel)
            }
        }

        override fun buildNotificationOption(
            context: Context,
            alarmData: AlarmData,
        ): SimpleNotificationOptionBase {
            builtNotificationAlarm = alarmData
            return DefaultNotificationOption(
                notificationId = alarmData.key,
                smallIcon = android.R.drawable.ic_dialog_info,
                title = alarmData.notification.title,
                content = alarmData.notification.message,
            )
        }

        override fun loadAllAlarmDataList(context: Context): List<AlarmData> {
            loadAllCallCount += 1
            return allAlarmData
        }

        override fun loadAlarmData(
            context: Context,
            intent: Intent,
            key: Int,
        ): AlarmData? {
            loadedAlarmKey = key
            return alarmDataByKey[key]
        }

        override fun onExactAlarmPermissionDenied(context: Context) {
            exactAlarmPermissionDeniedCallCount += 1
        }
    }

    private companion object {
        const val TEST_ACTION = "test.action.ALARM"
        const val TEST_CHANNEL_ID = "base_alarm_receiver_test"
        const val TEST_CHANNEL_NAME = "Base Alarm Receiver Test"

        val SYSTEM_CHANGE_ACTIONS =
            listOf(
                Intent.ACTION_BOOT_COMPLETED,
                Intent.ACTION_TIME_CHANGED,
                Intent.ACTION_TIMEZONE_CHANGED,
            )

        val TEST_ALARM_KEYS = (8_101..8_111).toList()
    }
}
