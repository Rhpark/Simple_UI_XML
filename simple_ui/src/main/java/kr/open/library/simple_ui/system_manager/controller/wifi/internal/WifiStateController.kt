package kr.open.library.simple_ui.system_manager.controller.wifi.internal

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.Manifest.permission.ACCESS_WIFI_STATE
import android.Manifest.permission.CHANGE_WIFI_STATE
import android.net.DhcpInfo
import android.net.wifi.ScanResult
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.os.Build
import androidx.annotation.RequiresPermission
import kr.open.library.simple_ui.extensions.conditional.checkSdkVersion
import kr.open.library.simple_ui.logcat.Logx

internal class WifiStateController(
    private val wifiManager: WifiManager,
    private val guard: WifiOperationGuard
) {

    @RequiresPermission(ACCESS_WIFI_STATE)
    fun isWifiEnabled(): Boolean = guard.run(false) {
        wifiManager.isWifiEnabled
    }

    @RequiresPermission(ACCESS_WIFI_STATE)
    fun getWifiState(): Int = guard.run(WifiManager.WIFI_STATE_UNKNOWN) {
        wifiManager.wifiState
    }

    @RequiresPermission(CHANGE_WIFI_STATE)
    fun setWifiEnabled(enabled: Boolean): Boolean = guard.run(false) {
        checkSdkVersion(Build.VERSION_CODES.Q,
            positiveWork = {
                Logx.w("WiFi control deprecated on API 29+, user must enable manually")
                false
            },
            negativeWork = {
                @Suppress("DEPRECATION")
                wifiManager.setWifiEnabled(enabled)
            }
        )
    }

    @RequiresPermission(ACCESS_WIFI_STATE)
    fun getDhcpInfo(): DhcpInfo? = guard.run(null) {
        wifiManager.dhcpInfo
    }

    @RequiresPermission(CHANGE_WIFI_STATE)
    fun startScan(): Boolean = guard.run(false) {
        @Suppress("DEPRECATION")
        wifiManager.startScan()
    }

    @RequiresPermission(allOf = [ACCESS_WIFI_STATE, ACCESS_FINE_LOCATION])
    fun getScanResults(): List<ScanResult> = guard.run(emptyList()) {
        wifiManager.scanResults ?: emptyList()
    }

    @RequiresPermission(allOf = [ACCESS_WIFI_STATE, ACCESS_FINE_LOCATION])
    fun getConfiguredNetworks(): List<WifiConfiguration> = guard.run(emptyList()) {
        checkSdkVersion(Build.VERSION_CODES.Q,
            positiveWork = {
                Logx.w("getConfiguredNetworks deprecated on API 29+, use WiFi suggestion API")
                emptyList()
            },
            negativeWork = {
                @Suppress("DEPRECATION")
                wifiManager.configuredNetworks ?: emptyList()
            }
        )
    }
}
