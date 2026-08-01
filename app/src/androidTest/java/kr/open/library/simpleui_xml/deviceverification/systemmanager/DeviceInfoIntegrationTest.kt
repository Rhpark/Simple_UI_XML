package kr.open.library.simpleui_xml.deviceverification.systemmanager

import android.Manifest
import android.content.pm.PackageManager
import android.location.LocationManager
import android.telephony.TelephonyManager
import androidx.core.content.ContextCompat
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kr.open.library.simple_ui.system_manager.core.extensions.getBatteryStateInfo
import kr.open.library.simple_ui.system_manager.core.extensions.getLocationManager
import kr.open.library.simple_ui.system_manager.core.extensions.getLocationStateInfo
import kr.open.library.simple_ui.system_manager.core.info.network.connectivity.NetworkConnectivityInfo
import kr.open.library.simple_ui.system_manager.core.info.network.sim.SimInfo
import kr.open.library.simple_ui.system_manager.core.info.network.telephony.TelephonyInfo
import kr.open.library.simpleui_xml.deviceverification.harness.PhysicalDeviceRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class DeviceInfoIntegrationTest {
    @get:Rule
    val physicalDeviceRule = PhysicalDeviceRule()

    private val context
        get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun sysP0006_locationStateMatchesPlatformManager() {
        val info = context.getLocationStateInfo()
        val manager: LocationManager = context.getLocationManager()
        physicalDeviceRule.cleanup.register("system_manager 위치 상태 수신 해제") {
            info.unRegister()
        }

        assertEquals(manager.isLocationEnabled, info.isLocationEnabled())
        assertEquals(
            manager.isProviderEnabled(LocationManager.GPS_PROVIDER),
            info.isGpsEnabled(),
        )
    }

    @Test
    fun sysP0007_batteryStateIsValidOnPhysicalDevice() {
        val info = context.getBatteryStateInfo()
        physicalDeviceRule.cleanup.register("system_manager 배터리 상태 수신 해제") {
            info.unRegister()
        }

        assertTrue(info.getCapacity() in 0..100)
        assertTrue(info.getChargeStatus() > 0)
        assertNotNull(info.getPresent())
        assertTrue(info.getVoltage() > 0.0)
    }

    @Test
    fun sysP0008_networkSummaryReflectsActiveWifiConnection() {
        val info = NetworkConnectivityInfo(context)
        physicalDeviceRule.cleanup.register("system_manager 네트워크 콜백 해제") {
            info.unregisterNetworkCallback()
            info.unregisterDefaultNetworkCallback()
        }

        val isNetworkConnected = info.isNetworkConnected()
        val isWifiConnected = info.isConnectedWifi()
        physicalDeviceRule.requirePrecondition(
            scenarioId = "SYS-P0-008",
            expected = "networkConnected=true, wifiConnected=true",
            actual = "networkConnected=$isNetworkConnected, wifiConnected=$isWifiConnected",
            isSatisfied = isNetworkConnected && isWifiConnected,
        )
        assertTrue(isNetworkConnected)
        assertTrue(isWifiConnected)
        assertNotNull(info.getNetworkCapabilities())
        info.getNetworkConnectivitySummary()
    }

    @Test
    fun sysP0009_noSimFallbackIsSafe() {
        val simInfo = SimInfo(context)
        val telephonyInfo = TelephonyInfo(context)
        physicalDeviceRule.cleanup.register("system_manager 전화망 콜백 해제") {
            telephonyInfo.unregisterCallback()
        }

        val hasPhonePermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_PHONE_STATE,
        ) == PackageManager.PERMISSION_GRANTED
        assumeTrue(
            "READ_PHONE_STATE 권한이 없어 SYS-P0-009는 ENV_UNAVAILABLE입니다.",
            hasPhonePermission,
        )
        assumeTrue(
            "실제 SIM이 있어 SYS-P0-009는 ENV_UNAVAILABLE입니다.",
            telephonyInfo.getSimState() == TelephonyManager.SIM_STATE_ABSENT,
        )

        assertEquals(TelephonyManager.SIM_STATE_ABSENT, telephonyInfo.getSimState())
        assertFalse(telephonyInfo.isSimReady())
        assertEquals(0, telephonyInfo.getActiveSimCount())
        assertEquals(0, simInfo.getActiveSimCount())
        assertTrue(simInfo.getActiveSubscriptionInfoList().isEmpty())
    }

    @Test
    fun missingPhonePermissionFallbackIsSafe() {
        val simInfo = SimInfo(context)
        val telephonyInfo = TelephonyInfo(context)
        val hasPhonePermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_PHONE_STATE,
        ) == PackageManager.PERMISSION_GRANTED
        assumeTrue(
            "READ_PHONE_STATE 권한이 있어 권한 폴백 검증은 ENV_UNAVAILABLE입니다.",
            !hasPhonePermission,
        )

        assertEquals(TelephonyManager.SIM_STATE_UNKNOWN, telephonyInfo.getSimState())
        assertFalse(telephonyInfo.isSimReady())
        assertEquals(0, telephonyInfo.getActiveSimCount())
        assertEquals(0, simInfo.getActiveSimCount())
        assertTrue(simInfo.getActiveSubscriptionInfoList().isEmpty())
    }

    @Test
    fun sysP1001_actualSimInformationMatchesSubscription() {
        val telephonyInfo = TelephonyInfo(context)
        val simInfo = SimInfo(context)
        val hasPhonePermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_PHONE_STATE,
        ) == PackageManager.PERMISSION_GRANTED
        assumeTrue(
            "READ_PHONE_STATE 권한이 없어 SYS-P1-001은 ENV_UNAVAILABLE입니다.",
            hasPhonePermission,
        )
        assumeTrue(
            "실제 SIM이 없어 SYS-P1-001은 ENV_UNAVAILABLE입니다.",
            telephonyInfo.isSimReady(),
        )

        assertTrue(telephonyInfo.getActiveSimCount() > 0)
        assertTrue(simInfo.getActiveSimCount() > 0)
        assertTrue(simInfo.getActiveSubscriptionInfoList().isNotEmpty())
        assertNotNull(telephonyInfo.getDefaultDataSubscriptionInfo())
    }

    @Test
    fun sysP2001_multiSimSlotsMatchActiveSubscriptions() {
        val simInfo = SimInfo(context)
        assumeTrue(
            "멀티 SIM 환경이 없어 SYS-P2-001은 실행하지 않습니다.",
            simInfo.isMultiSim(),
        )

        assertTrue(simInfo.getMaximumUSimCount() > 1)
        assertTrue(simInfo.getActiveSimSlotIndexList().distinct().size == simInfo.getActiveSimSlotIndexList().size)
    }
}
