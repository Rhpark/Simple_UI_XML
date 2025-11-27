package kr.open.library.simple_ui.core.system_manager.controller.wifi.internal

import android.net.wifi.WifiManager
import android.os.Build
import androidx.annotation.RequiresApi
import kr.open.library.simple_ui.core.extensions.conditional.checkSdkVersion

internal class WifiCapabilityChecker(
    private val wifiManager: WifiManager,
    private val guard: WifiOperationGuard,
    private val capabilityInvoker: CapabilityInvoker = CapabilityInvoker.Reflection
) {

    internal fun interface CapabilityInvoker {
        fun invoke(manager: WifiManager, methodName: String): Boolean?

        companion object {
            val Reflection: CapabilityInvoker = CapabilityInvoker { manager, methodName ->
                val method = manager.javaClass.getMethod(methodName)
                method.invoke(manager) as? Boolean
            }
        }
    }

    fun is5GHzBandSupported(): Boolean = guard.run(false) {
        wifiManager.is5GHzBandSupported
    }

    @RequiresApi(Build.VERSION_CODES.R)
    fun is6GHzBandSupported(): Boolean = guard.run(false) {
        checkSdkVersion(Build.VERSION_CODES.R,
            positiveWork = { invokeBooleanCapability("is6GHzBandSupported") },
            negativeWork = { false }
        )
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun isWpa3SaeSupported(): Boolean = guard.run(false) {
        checkSdkVersion(Build.VERSION_CODES.R,
            positiveWork = { invokeBooleanCapability("isWpa3SaeSupported") },
            negativeWork = { false }
        )
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun isEnhancedOpenSupported(): Boolean = guard.run(false) {
        checkSdkVersion(Build.VERSION_CODES.Q,
            positiveWork = { invokeBooleanCapability("isEnhancedOpenSupported") },
            negativeWork = { false }
        )
    }

    private fun invokeBooleanCapability(methodName: String): Boolean = guard.run(false) {
        capabilityInvoker.invoke(wifiManager, methodName) ?: false
    }
}