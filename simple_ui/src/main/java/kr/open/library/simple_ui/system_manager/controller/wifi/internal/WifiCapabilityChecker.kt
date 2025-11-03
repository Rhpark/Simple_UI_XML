package kr.open.library.simple_ui.system_manager.controller.wifi.internal

import android.net.wifi.WifiManager
import android.os.Build
import androidx.annotation.RequiresApi
import kr.open.library.simple_ui.extensions.conditional.checkSdkVersion

internal class WifiCapabilityChecker(
    private val wifiManager: WifiManager,
    private val guard: WifiOperationGuard
) {

    fun is5GHzBandSupported(): Boolean = guard.run(false) {
        wifiManager.is5GHzBandSupported
    }

    @RequiresApi(Build.VERSION_CODES.R)
    fun is6GHzBandSupported(): Boolean = guard.run(false) {
        checkSdkVersion(Build.VERSION_CODES.R,
            positiveWork = { wifiManager.is6GHzBandSupported },
            negativeWork = { false }
        )
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun isWpa3SaeSupported(): Boolean = guard.run(false) {
        checkSdkVersion(Build.VERSION_CODES.R,
            positiveWork = { wifiManager.isWpa3SaeSupported },
            negativeWork = { false }
        )
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun isEnhancedOpenSupported(): Boolean = guard.run(false) {
        checkSdkVersion(Build.VERSION_CODES.Q,
            positiveWork = { wifiManager.isEnhancedOpenSupported },
            negativeWork = { false }
        )
    }
}
