package kr.open.library.simple_ui.xml.robolectric.system_manager.extensions

import android.app.AlarmManager
import android.app.Application
import android.app.NotificationManager
import android.bluetooth.BluetoothManager
import android.content.Context
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import android.os.BatteryManager
import android.os.Build
import android.os.PowerManager
import android.os.Vibrator
import android.os.VibratorManager
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import android.telephony.euicc.EuiccManager
import androidx.test.core.app.ApplicationProvider
import kr.open.library.simple_ui.core.system_manager.controller.notification.option.SimpleNotificationType
import kr.open.library.simple_ui.core.system_manager.controller.wifi.WifiController
import kr.open.library.simple_ui.core.system_manager.extensions.getAlarmController
import kr.open.library.simple_ui.core.system_manager.extensions.getAlarmManager
import kr.open.library.simple_ui.core.system_manager.extensions.getBatteryManager
import kr.open.library.simple_ui.core.system_manager.extensions.getBatteryStateInfo
import kr.open.library.simple_ui.core.system_manager.extensions.getBluetoothManager
import kr.open.library.simple_ui.core.system_manager.extensions.getConnectivityManager
import kr.open.library.simple_ui.core.system_manager.extensions.getEuiccManager
import kr.open.library.simple_ui.core.system_manager.extensions.getInputMethodManager
import kr.open.library.simple_ui.core.system_manager.extensions.getLocationManager
import kr.open.library.simple_ui.core.system_manager.extensions.getLocationStateInfo
import kr.open.library.simple_ui.core.system_manager.extensions.getNotificationController
import kr.open.library.simple_ui.core.system_manager.extensions.getNotificationManager
import kr.open.library.simple_ui.core.system_manager.extensions.getPowerManager
import kr.open.library.simple_ui.core.system_manager.extensions.getSubscriptionManager
import kr.open.library.simple_ui.core.system_manager.extensions.getSystemNotificationManager
import kr.open.library.simple_ui.core.system_manager.extensions.getTelephonyManager
import kr.open.library.simple_ui.core.system_manager.extensions.getVibrator
import kr.open.library.simple_ui.core.system_manager.extensions.getVibratorController
import kr.open.library.simple_ui.core.system_manager.extensions.getVibratorManager
import kr.open.library.simple_ui.core.system_manager.extensions.getWifiController
import kr.open.library.simple_ui.core.system_manager.extensions.getWifiManager
import kr.open.library.simple_ui.core.system_manager.extensions.getWindowManager
import kr.open.library.simple_ui.xml.system_manager.controller.softkeyboard.SoftKeyboardController
import kr.open.library.simple_ui.xml.system_manager.extensions.getDisplayInfo
import kr.open.library.simple_ui.xml.system_manager.extensions.getFloatingViewController
import kr.open.library.simple_ui.xml.system_manager.extensions.getSoftKeyboardController
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertSame
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config

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
        val floatingViewController = application.getFloatingViewController()
        val batteryStateInfo = application.getBatteryStateInfo()
        val locationStateInfo = application.getLocationStateInfo()
        val notificationController = application.getNotificationController(SimpleNotificationType.ACTIVITY)
        val displayInfo = application.getDisplayInfo()
        val vibratorController = application.getVibratorController()
        val alarmController = application.getAlarmController()

        assertNotNull(wifiController)
        assertNotNull(softKeyboardController)
        assertNotNull(floatingViewController)
        assertNotNull(batteryStateInfo)
        assertNotNull(locationStateInfo)
        assertNotNull(notificationController)
        assertNotNull(displayInfo)
        assertNotNull(vibratorController)
        assertNotNull(alarmController)
    }

    @Test
    fun `system service wrappers return registered instances`() {
        val shadow = Shadows.shadowOf(application)
        val batteryManager = mock(BatteryManager::class.java)
        val notificationManager = mock(NotificationManager::class.java)
        val powerManager = mock(PowerManager::class.java)
        val locationManager = mock(LocationManager::class.java)
        val connectivityManager = mock(ConnectivityManager::class.java)
        val wifiManager = mock(WifiManager::class.java)
        val vibrator = mock(Vibrator::class.java)
        val subscriptionManager = mock(SubscriptionManager::class.java)
        val euiccManager = mock(EuiccManager::class.java)
        val bluetoothManager = mock(BluetoothManager::class.java)

        shadow.setSystemService(Context.BATTERY_SERVICE, batteryManager)
        shadow.setSystemService(Context.NOTIFICATION_SERVICE, notificationManager)
        shadow.setSystemService(Context.POWER_SERVICE, powerManager)
        shadow.setSystemService(Context.LOCATION_SERVICE, locationManager)
        shadow.setSystemService(Context.CONNECTIVITY_SERVICE, connectivityManager)
        shadow.setSystemService(Context.WIFI_SERVICE, wifiManager)
        shadow.setSystemService(Context.VIBRATOR_SERVICE, vibrator)
        shadow.setSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE, subscriptionManager)
        shadow.setSystemService(Context.EUICC_SERVICE, euiccManager)
        shadow.setSystemService(Context.BLUETOOTH_SERVICE, bluetoothManager)

        assertSame(batteryManager, application.getBatteryManager())
        assertSame(notificationManager, application.getNotificationManager())
        assertSame(notificationManager, application.getSystemNotificationManager())
        assertSame(powerManager, application.getPowerManager())
        assertSame(locationManager, application.getLocationManager())
        assertSame(connectivityManager, application.getConnectivityManager())
        assertSame(wifiManager, application.getWifiManager())
        assertSame(vibrator, application.getVibrator())
        assertSame(subscriptionManager, application.getSubscriptionManager())
        assertSame(euiccManager, application.getEuiccManager())
        assertSame(bluetoothManager, application.getBluetoothManager())
        assertNotNull(application.getWindowManager())
        assertNotNull(application.getInputMethodManager())
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.S])
    fun `getVibratorManager returns mocked instance on android s`() {
        val vibratorManager = mock(VibratorManager::class.java)
        Shadows.shadowOf(application).setSystemService(Context.VIBRATOR_MANAGER_SERVICE, vibratorManager)

        assertSame(vibratorManager, application.getVibratorManager())
    }
}
