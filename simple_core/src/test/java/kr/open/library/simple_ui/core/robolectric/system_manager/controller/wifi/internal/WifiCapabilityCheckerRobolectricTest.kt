package kr.open.library.simple_ui.core.robolectric.system_manager.controller.wifi.internal

import android.content.Context
import android.net.wifi.WifiManager
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import kr.open.library.simple_ui.core.system_manager.controller.wifi.internal.WifiCapabilityChecker
import kr.open.library.simple_ui.core.system_manager.controller.wifi.internal.WifiOperationGuard
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
class WifiCapabilityCheckerRobolectricTest {
    private val context: Context = ApplicationProvider.getApplicationContext()
    private val realWifiManager: WifiManager =
        context.getSystemService(Context.WIFI_SERVICE) as WifiManager

    private lateinit var guard: WifiOperationGuard

    @Before
    fun setUp() {
        guard =
            WifiOperationGuard { defaultValue, block ->
                try {
                    block()
                } catch (e: Exception) {
                    defaultValue
                }
            }
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q])
    fun is6GHzBandSupported_preR_returnsFalse() {
        val wifiManager = mock(WifiManager::class.java)
        val checker = WifiCapabilityChecker(wifiManager, guard)

        val supported = checker.is6GHzBandSupported()

        assertFalse(supported)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.R])
    fun is6GHzBandSupported_onR_returnsManagerValue() {
        val wifiManager = mock(WifiManager::class.java)
        `when`(wifiManager.is6GHzBandSupported).thenReturn(true)
        val checker = WifiCapabilityChecker(wifiManager, guard)

        val supported = checker.is6GHzBandSupported()

        assertTrue(supported)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.R])
    fun isWpa3SaeSupported_onR_returnsManagerValue() {
        val wifiManager = mock(WifiManager::class.java)
        `when`(wifiManager.isWpa3SaeSupported).thenReturn(true)
        val checker = WifiCapabilityChecker(wifiManager, guard)

        val supported = checker.isWpa3SaeSupported()

        assertTrue(supported)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q])
    fun isWpa3SaeSupported_preR_returnsFalse() {
        val wifiManager = mock(WifiManager::class.java)
        val checker = WifiCapabilityChecker(wifiManager, guard)

        val supported = checker.isWpa3SaeSupported()

        assertFalse(supported)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q])
    fun isEnhancedOpenSupported_onQ_returnsManagerValue() {
        val wifiManager = mock(WifiManager::class.java)
        `when`(wifiManager.isEnhancedOpenSupported).thenReturn(true)
        val checker = WifiCapabilityChecker(wifiManager, guard)

        val supported = checker.isEnhancedOpenSupported()

        assertTrue(supported)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.P])
    fun isEnhancedOpenSupported_preQ_returnsFalse() {
        val wifiManager = mock(WifiManager::class.java)
        val checker = WifiCapabilityChecker(wifiManager, guard)

        val supported = checker.isEnhancedOpenSupported()

        assertFalse(supported)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.R])
    fun capabilityInvoker_whenReturnsNull_returnsFalse() {
        val wifiManager = mock(WifiManager::class.java)
        val nullInvoker = WifiCapabilityChecker.CapabilityInvoker { _, _ -> null }
        val checker = WifiCapabilityChecker(wifiManager, guard, nullInvoker)

        val supported = checker.is6GHzBandSupported()

        assertFalse(supported)
    }

    @Test
    fun isEnhancedOpenSupported_withReflectionInvoker_usesWifiManagerValue() {
        val checker = WifiCapabilityChecker(realWifiManager, guard)

        val supported = checker.isEnhancedOpenSupported()

        assertFalse(supported)
    }

    @Test
    fun isWpa3SaeSupported_whenInvokerThrows_returnsFalse() {
        val throwingInvoker =
            WifiCapabilityChecker.CapabilityInvoker { _, _ ->
                throw NoSuchMethodException("missing")
            }
        val checker = WifiCapabilityChecker(realWifiManager, guard, throwingInvoker)

        val supported = checker.isWpa3SaeSupported()

        assertFalse(supported)
    }

    @Test
    fun is5GHzBandSupported_whenTrue_returnsTrue() {
        val wifiManager = mock(WifiManager::class.java)
        `when`(wifiManager.is5GHzBandSupported).thenReturn(true)
        val checker = WifiCapabilityChecker(wifiManager, guard)

        val supported = checker.is5GHzBandSupported()

        assertTrue(supported)
    }

    @Test
    fun is5GHzBandSupported_whenFalse_returnsFalse() {
        val wifiManager = mock(WifiManager::class.java)
        `when`(wifiManager.is5GHzBandSupported).thenReturn(false)
        val checker = WifiCapabilityChecker(wifiManager, guard)

        val supported = checker.is5GHzBandSupported()

        assertFalse(supported)
    }
}
