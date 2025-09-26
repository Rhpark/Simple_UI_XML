package kr.open.library.simple_ui.system_manager.extensions

import android.app.AlarmManager
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
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.annotation.RequiresApi
import kr.open.library.simple_ui.system_manager.controller.alarm.AlarmController
import kr.open.library.simple_ui.system_manager.controller.notification.SimpleNotificationController
import kr.open.library.simple_ui.system_manager.controller.notification.vo.SimpleNotificationType
import kr.open.library.simple_ui.system_manager.controller.softkeyboard.SoftKeyboardController
import kr.open.library.simple_ui.system_manager.controller.vibrator.VibratorController
import kr.open.library.simple_ui.system_manager.controller.wifi.WifiController
import kr.open.library.simple_ui.system_manager.controller.window.FloatingViewController
import kr.open.library.simple_ui.system_manager.info.battery.BatteryStateInfo
import kr.open.library.simple_ui.system_manager.info.display.DisplayInfo
import kr.open.library.simple_ui.system_manager.info.location.LocationStateInfo

/*****************
 * SystemService *
 *****************/

public fun Context.getWindowManager(): WindowManager = getSystemService(WindowManager::class.java)

public fun Context.getBatteryManager(): BatteryManager = getSystemService(BatteryManager::class.java)

public fun Context.getInputMethodManager(): InputMethodManager = getSystemService(InputMethodManager::class.java)

public fun Context.getTelephonyManager(): TelephonyManager = getSystemService(TelephonyManager::class.java)

public fun Context.getSystemNotificationManager(): NotificationManager = getSystemService(
    NotificationManager::class.java)

public fun Context.getSubscriptionManager(): SubscriptionManager = getSystemService(
    SubscriptionManager::class.java)

public fun Context.getEuiccManager(): EuiccManager = getSystemService(EuiccManager::class.java)

public fun Context.getConnectivityManager(): ConnectivityManager = getSystemService(ConnectivityManager::class.java)

public fun Context.getWifiManager(): WifiManager = getSystemService(WifiManager::class.java)

public fun Context.getLocationManager(): LocationManager = getSystemService(LocationManager::class.java)

public fun Context.getAlarmManager(): AlarmManager = getSystemService(AlarmManager::class.java)

public fun Context.getNotificationManager(): NotificationManager = getSystemService(NotificationManager::class.java)

public fun Context.getPowerManager(): PowerManager = getSystemService(PowerManager::class.java)

public fun Context.getBluetoothManager(): BluetoothManager = getSystemService(BluetoothManager::class.java)

/**
 * be used Build.VERSION.SDK_INT < Build.VERSION_CODES.S(31)
 */
public fun Context.getVibrator(): Vibrator = getSystemService(Vibrator::class.java)


/**
 * be used Build.VERSION.SDK_INT >= Build.VERSION_CODES.S(31)
 */
@RequiresApi(Build.VERSION_CODES.S)
public fun Context.getVibratorManager(): VibratorManager = getSystemService(VibratorManager::class.java)


/****************************
 * SystemService Controller *
 ****************************/

public fun Context.getSoftKeyboardController(): SoftKeyboardController = SoftKeyboardController(this)

public fun Context.getFloatingViewController(): FloatingViewController = FloatingViewController(this)

public fun Context.getAlarmController(): AlarmController = AlarmController(this)

public fun Context.getNotificationController(showType: SimpleNotificationType): SimpleNotificationController =
    SimpleNotificationController(this, showType)

public fun Context.getDisplayInfo(): DisplayInfo = DisplayInfo(this)

public fun Context.getVibratorController(): VibratorController = VibratorController(this)

public fun Context.getWifiController(): WifiController = WifiController(this)

/*****************************
 * SystemService Access Info *
 *****************************/

//public fun Context.getNetworkStateInfo(): NetworkStateInfo =
//    NetworkStateInfo(this)

public fun Context.getBatteryStateInfo(): BatteryStateInfo = BatteryStateInfo(this)

public fun Context.getLocationStateInfo(): LocationStateInfo = LocationStateInfo(this)
