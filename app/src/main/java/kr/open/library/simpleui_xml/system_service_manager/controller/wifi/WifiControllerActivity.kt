package kr.open.library.simpleui_xml.system_service_manager.controller.wifi

import android.Manifest
import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kr.open.library.simple_ui.extensions.conditional.checkSdkVersion
import kr.open.library.simple_ui.presenter.extensions.view.toastShort
import kr.open.library.simple_ui.presenter.ui.activity.BaseBindingActivity
import kr.open.library.simple_ui.system_manager.extensions.getWifiController
import kr.open.library.simpleui_xml.R
import kr.open.library.simpleui_xml.databinding.ActivityWifiControllerBinding

class WifiControllerActivity : BaseBindingActivity<ActivityWifiControllerBinding>(R.layout.activity_wifi_controller) {

    private val vm: WifiControllerActivityVm by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.vm = vm

        requestWifiPermissions()
        eventVmCollect()
    }

    private fun requestWifiPermissions() {
        val permissions = mutableListOf<String>()

        checkSdkVersion(Build.VERSION_CODES.Q) {
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        if (permissions.isNotEmpty()) {
            onRequestPermissions(permissions) { deniedPermissions ->
                if (deniedPermissions.isNotEmpty()) {
                    toastShort("Location permission required for WiFi scan")
                }
            }
        }
    }

    override fun eventVmCollect() {
        lifecycleScope.launch {
            vm.mEventVm.collect { event ->
                when (event) {
                    is WifiControllerActivityVmEvent.GetWifiInfo -> {
                        getWifiInfo()
                    }
                    is WifiControllerActivityVmEvent.CheckStatus -> {
                        checkWifiStatus()
                    }
                    is WifiControllerActivityVmEvent.ScanWifi -> {
                        scanWifi()
                    }
                    is WifiControllerActivityVmEvent.CheckBands -> {
                        checkWifiBands()
                    }
                }
            }
        }
    }

    private fun getWifiInfo() {
        val wifiInfo = getWifiController().getConnectionInfo()

        if (wifiInfo != null) {
            val ssid = getWifiController().getCurrentSsid() ?: "Unknown"
            val rssi = getWifiController().getCurrentRssi()
            val linkSpeed = getWifiController().getCurrentLinkSpeed()

            val result = StringBuilder()
            result.append("WiFi Connection Info:\n\n")
            result.append("SSID: $ssid\n")
            result.append("RSSI: $rssi dBm\n")
            result.append("Link Speed: $linkSpeed Mbps\n")
            result.append("BSSID: ${wifiInfo.bssid}\n")

            binding.tvResult.text = result.toString()
            toastShort("WiFi info retrieved")
        } else {
            binding.tvResult.text = "No WiFi connection available"
            toastShort("No WiFi connection")
        }
    }

    private fun checkWifiStatus() {
        val isConnected = getWifiController().isConnectedWifi()
        val isEnabled = getWifiController().isWifiEnabled()
        val rssi = getWifiController().getCurrentRssi()
        val signalLevel = getWifiController().calculateSignalLevel(rssi, 5)

        val result = StringBuilder()
        result.append("WiFi Status:\n\n")
        result.append("Connected: $isConnected\n")
        result.append("Enabled: $isEnabled\n")
        result.append("RSSI: $rssi dBm\n")
        result.append("Signal Level: $signalLevel / 5\n")

        binding.tvResult.text = result.toString()
        toastShort("WiFi status checked")
    }

    @SuppressLint("MissingPermission")
    private fun scanWifi() {
        onRequestPermissions(
            listOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.CHANGE_WIFI_STATE,
                Manifest.permission.ACCESS_WIFI_STATE
            )
        ) { deniedPermissions ->
            if (deniedPermissions.isEmpty()) {
                val scanStarted = getWifiController().startScan()

                if (scanStarted) {
                    val results = getWifiController().getScanResults()

                    if (results.isNotEmpty()) {
                        val result = StringBuilder()
                        result.append("WiFi Scan Results (${results.size} networks):\n\n")

                        results.forEachIndexed { index, scanResult ->
                            result.append("${index + 1}. SSID: ${scanResult.SSID}\n")
                            result.append("   BSSID: ${scanResult.BSSID}\n")
                            result.append("   Level: ${scanResult.level} dBm\n")
                            result.append("   Frequency: ${scanResult.frequency} MHz\n")
                            result.append("\n")
                        }

                        binding.tvResult.text = result.toString()
                        toastShort("Found ${results.size} networks")
                    } else {
                        binding.tvResult.text = "No WiFi networks found"
                        toastShort("No networks found")
                    }
                } else {
                    binding.tvResult.text = "Failed to start WiFi scan"
                    toastShort("Scan failed")
                }
            } else {
                binding.tvResult.text = "Permissions required:\n${deniedPermissions.joinToString("\n")}"
                toastShort("Permissions denied")
            }
        }
    }

    private fun checkWifiBands() {
        val is5GHz = getWifiController().is5GHzBandSupported()
        val is6GHz =  checkSdkVersion(Build.VERSION_CODES.R,
            positiveWork = {    getWifiController().is6GHzBandSupported()   },
            negativeWork = {    false   }
        )
        val result = StringBuilder()
        result.append("WiFi Band Support:\n\n")
        result.append("5GHz Band: ${if (is5GHz) "Supported" else "Not Supported"}\n")
        result.append("6GHz Band: ${if (is6GHz) "Supported" else checkSdkVersion(Build.VERSION_CODES.R, positiveWork = {"Not Supported"}, negativeWork = {"Requires Android 11" })}\n")

        binding.tvResult.text = result.toString()
        toastShort("Band support checked")
    }
}
