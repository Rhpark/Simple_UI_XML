package kr.open.library.simple_ui.core.system_manager.controller.wifi.internal

import android.net.wifi.WifiManager
import android.os.Build
import androidx.annotation.RequiresApi
import kr.open.library.simple_ui.core.extensions.conditional.checkSdkVersion

/**
 * Internal class for checking WiFi capability support.<br><br>
 * WiFi 기능 지원 여부를 확인하는 내부 클래스입니다.<br>
 *
 * @param wifiManager WifiManager instance.<br><br>
 *                    WifiManager 인스턴스.
 * @param guard Operation guard for safe execution.<br><br>
 *              안전한 실행을 위한 작업 가드.
 * @param capabilityInvoker Invoker for capability methods.<br><br>
 *                          기능 메서드 호출자.
 */
internal class WifiCapabilityChecker(
    private val wifiManager: WifiManager,
    private val guard: WifiOperationGuard,
    private val capabilityInvoker: CapabilityInvoker = CapabilityInvoker.Reflection
) {

    /**
     * Functional interface for invoking capability check methods.<br><br>
     * 기능 확인 메서드를 호출하기 위한 함수형 인터페이스입니다.<br>
     */
    internal fun interface CapabilityInvoker {
        /**
         * Invokes a capability check method on WifiManager.<br><br>
         * WifiManager에서 기능 확인 메서드를 호출합니다.<br>
         *
         * @param manager WifiManager instance.<br><br>
         *                WifiManager 인스턴스.
         * @param methodName Name of the method to invoke.<br><br>
         *                   호출할 메서드 이름.
         * @return Boolean result or null if invocation fails.<br><br>
         *         Boolean 결과 또는 호출 실패 시 null.<br>
         */
        fun invoke(manager: WifiManager, methodName: String): Boolean?

        companion object {
            /**
             * Default reflection-based invoker implementation.<br><br>
             * 기본 리플렉션 기반 호출자 구현입니다.<br>
             */
            val Reflection: CapabilityInvoker = CapabilityInvoker { manager, methodName ->
                val method = manager.javaClass.getMethod(methodName)
                method.invoke(manager) as? Boolean
            }
        }
    }

    /**
     * Checks if 5GHz WiFi band is supported.<br><br>
     * 5GHz WiFi 대역 지원 여부를 확인합니다.<br>
     *
     * @return `true` if 5GHz band is supported, `false` otherwise.<br><br>
     *         5GHz 대역이 지원되면 `true`, 그렇지 않으면 `false`.<br>
     */
    fun is5GHzBandSupported(): Boolean = guard.run(false) { wifiManager.is5GHzBandSupported }

    /**
     * Checks if 6GHz WiFi band is supported (WiFi 6E).<br>
     * Requires Android R (API 30) or higher.<br><br>
     * 6GHz WiFi 대역 지원 여부를 확인합니다 (WiFi 6E).<br>
     * Android R (API 30) 이상이 필요합니다.<br>
     *
     * @return `true` if 6GHz band is supported, `false` otherwise.<br><br>
     *         6GHz 대역이 지원되면 `true`, 그렇지 않으면 `false`.<br>
     */
    @RequiresApi(Build.VERSION_CODES.R)
    fun is6GHzBandSupported(): Boolean = guard.run(false) {
        checkSdkVersion(Build.VERSION_CODES.R,
            positiveWork = { invokeBooleanCapability("is6GHzBandSupported") },
            negativeWork = { false }
        )
    }

    /**
     * Checks if WPA3 SAE (Simultaneous Authentication of Equals) is supported.<br>
     * Requires Android Q (API 29) or higher.<br><br>
     * WPA3 SAE (Simultaneous Authentication of Equals) 지원 여부를 확인합니다.<br>
     * Android Q (API 29) 이상이 필요합니다.<br>
     *
     * @return `true` if WPA3 SAE is supported, `false` otherwise.<br><br>
     *         WPA3 SAE가 지원되면 `true`, 그렇지 않으면 `false`.<br>
     */
    @RequiresApi(Build.VERSION_CODES.Q)
    fun isWpa3SaeSupported(): Boolean = guard.run(false) {
        checkSdkVersion(Build.VERSION_CODES.R,
            positiveWork = { invokeBooleanCapability("isWpa3SaeSupported") },
            negativeWork = { false }
        )
    }

    /**
     * Checks if Enhanced Open (OWE - Opportunistic Wireless Encryption) is supported.<br>
     * Requires Android Q (API 29) or higher.<br><br>
     * Enhanced Open (OWE - Opportunistic Wireless Encryption) 지원 여부를 확인합니다.<br>
     * Android Q (API 29) 이상이 필요합니다.<br>
     *
     * @return `true` if Enhanced Open is supported, `false` otherwise.<br><br>
     *         Enhanced Open이 지원되면 `true`, 그렇지 않으면 `false`.<br>
     */
    @RequiresApi(Build.VERSION_CODES.Q)
    fun isEnhancedOpenSupported(): Boolean = guard.run(false) {
        checkSdkVersion(Build.VERSION_CODES.Q,
            positiveWork = { invokeBooleanCapability("isEnhancedOpenSupported") },
            negativeWork = { false }
        )
    }

    /**
     * Invokes a boolean capability check method via reflection.<br><br>
     * 리플렉션을 통해 boolean 기능 확인 메서드를 호출합니다.<br>
     *
     * @param methodName Name of the capability method to invoke.<br><br>
     *                   호출할 기능 메서드 이름.
     * @return `true` if capability is supported, `false` otherwise.<br><br>
     *         기능이 지원되면 `true`, 그렇지 않으면 `false`.<br>
     */
    private fun invokeBooleanCapability(methodName: String): Boolean = guard.run(false) {
        capabilityInvoker.invoke(wifiManager, methodName) ?: false
    }
}