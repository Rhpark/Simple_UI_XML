package kr.open.library.simple_ui.robolectric.system_manager.extensions

import android.app.AlarmManager
import android.app.Application
import android.content.Context
import android.telephony.TelephonyManager
import androidx.test.core.app.ApplicationProvider
import kr.open.library.simple_ui.system_manager.extensions.getAlarmManager
import kr.open.library.simple_ui.system_manager.extensions.getSoftKeyboardController
import kr.open.library.simple_ui.system_manager.extensions.getTelephonyManager
import kr.open.library.simple_ui.system_manager.extensions.getWifiController
import kr.open.library.simple_ui.system_manager.controller.softkeyboard.SoftKeyboardController
import kr.open.library.simple_ui.system_manager.controller.wifi.WifiController
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertSame
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows

@RunWith(RobolectricTestRunner::class)
class SystemServiceExtensionsTest {

    private val application: Application = ApplicationProvider.getApplicationContext()

    @Test
    fun `getTelephonyManager returns injected instance`() {
        val telephony = mock(TelephonyManager::class.java)
        Shadows.shadowOf(application).setSystemService(Context.TELEPHONY_SERVICE, telephony)

        assertSame(telephony, application.getTelephonyManager())
    }

    @Test
    fun `getAlarmManager delegates to system service`() {
        val alarmManager = mock(AlarmManager::class.java)
        Shadows.shadowOf(application).setSystemService(Context.ALARM_SERVICE, alarmManager)

        assertSame(alarmManager, application.getAlarmManager())
    }

    @Test
    fun `controller helpers create valid instances`() {
        val wifiController: WifiController = application.getWifiController()
        val softKeyboardController: SoftKeyboardController = application.getSoftKeyboardController()

        assertNotNull(wifiController)
        assertNotNull(softKeyboardController)
    }
}
