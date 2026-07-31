package kr.open.library.simpleui_xml.deviceverification.systemmanager

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kr.open.library.simple_ui.system_manager.core.extensions.getAlarmController
import kr.open.library.simpleui_xml.deviceverification.harness.PhysicalDeviceRule
import kr.open.library.simpleui_xml.system_service_manager.controller.receiver.AlarmReceiver
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class AlarmIntegrationTest {
    @get:Rule
    val physicalDeviceRule = PhysicalDeviceRule()

    @Test
    fun sysP0003_alarmRemovalIsIdempotentAndPermissionIntentIsSafe() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val controller = context.getAlarmController()
        physicalDeviceRule.cleanup.register("system_manager 테스트 알람 삭제") {
            controller.remove(ALARM_KEY, AlarmReceiver::class.java, NAMESPACE)
        }

        controller.remove(ALARM_KEY, AlarmReceiver::class.java, NAMESPACE).requireSuccess()
        assertFalse(controller.exists(ALARM_KEY, AlarmReceiver::class.java, NAMESPACE))
        controller.buildExactAlarmPermissionIntent()
    }

    private companion object {
        const val ALARM_KEY = 91003
        const val NAMESPACE = "device_verification"
    }
}
