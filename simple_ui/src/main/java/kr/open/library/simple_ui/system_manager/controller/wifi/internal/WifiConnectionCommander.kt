package kr.open.library.simple_ui.system_manager.controller.wifi.internal

import android.Manifest.permission.CHANGE_WIFI_STATE
import android.net.wifi.WifiManager
import androidx.annotation.RequiresPermission

internal class WifiConnectionCommander(
    private val wifiManager: WifiManager,
    private val guard: WifiOperationGuard
) {

    @RequiresPermission(CHANGE_WIFI_STATE)
    fun reconnect(): Boolean = guard.run(false) {
        wifiManager.reconnect()
    }

    @RequiresPermission(CHANGE_WIFI_STATE)
    fun reassociate(): Boolean = guard.run(false) {
        wifiManager.reassociate()
    }

    @RequiresPermission(CHANGE_WIFI_STATE)
    fun disconnect(): Boolean = guard.run(false) {
        wifiManager.disconnect()
    }
}
