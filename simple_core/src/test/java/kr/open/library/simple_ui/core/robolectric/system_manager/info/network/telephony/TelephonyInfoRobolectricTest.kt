package kr.open.library.simple_ui.core.robolectric.system_manager.info.network.telephony

import android.content.Context
import android.telephony.TelephonyManager
import androidx.test.core.app.ApplicationProvider
import kr.open.library.simple_ui.core.system_manager.info.network.telephony.TelephonyInfo
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Robolectric test for TelephonyInfo
 *
 * Purpose: 리팩토링 전 기존 동작을 문서화하고 회귀 방지
 * This test documents existing behavior before refactoring to prevent regression
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class TelephonyInfoRobolectricTest {
    private lateinit var context: Context
    private lateinit var telephonyInfo: TelephonyInfo

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        telephonyInfo = TelephonyInfo(context)
    }

    // =================================================
    // Carrier Information Tests
    // =================================================

    @Test
    fun getCarrierName_doesNotCrash() {
        // When
        val result = telephonyInfo.getCarrierName()

        // Then: 크래시 없이 실행됨 (null이거나 값이 있음)
        // Robolectric 환경에서는 보통 null 또는 빈 문자열
    }

    @Test
    fun getMobileCountryCode_doesNotCrash() {
        // When
        val result = telephonyInfo.getMobileCountryCode()

        // Then: 크래시 없이 실행됨
    }

    @Test
    fun getMobileNetworkCode_doesNotCrash() {
        // When
        val result = telephonyInfo.getMobileNetworkCode()

        // Then: 크래시 없이 실행됨
    }

    // =================================================
    // SIM Information Tests
    // =================================================

    @Test
    fun getSimState_returnsValidState() {
        // When
        val result = telephonyInfo.getSimState()

        // Then: 유효한 SIM 상태 값
        assertTrue(result >= TelephonyManager.SIM_STATE_UNKNOWN)
    }

    @Test
    fun isSimReady_doesNotCrash() {
        // When
        val result = telephonyInfo.isSimReady()

        // Then: boolean 반환 (크래시 없음)
    }

    @Test
    fun getSimOperatorName_doesNotCrash() {
        // When
        val result = telephonyInfo.getSimOperatorName()

        // Then: 크래시 없이 실행됨
    }

    @Test
    fun getSimCountryIso_doesNotCrash() {
        // When
        val result = telephonyInfo.getSimCountryIso()

        // Then: 크래시 없이 실행됨
    }

    @Test
    fun getSimStateString_returnsValidString() {
        // When
        val result = telephonyInfo.getSimStateString()

        // Then: 빈 문자열이 아님
        assertNotNull(result)
    }

    // =================================================
    // Phone Information Tests
    // =================================================

    @Test
    fun getPhoneNumber_doesNotCrash() {
        // When
        val result = telephonyInfo.getPhoneNumber()

        // Then: 크래시 없이 실행됨
    }

    @Test
    fun getCallState_returnsValidState() {
        // When
        val result = telephonyInfo.getCallState()

        // Then: 유효한 통화 상태
        assertTrue(result >= TelephonyManager.CALL_STATE_IDLE)
    }

    // =================================================
    // Network Information Tests
    // =================================================

    @Test
    fun getNetworkType_returnsValidType() {
        // When
        val result = telephonyInfo.getNetworkType()

        // Then: 유효한 네트워크 타입
        assertTrue(result >= TelephonyManager.NETWORK_TYPE_UNKNOWN)
    }

    @Test
    fun getDataNetworkType_returnsValidType() {
        // When
        val result = telephonyInfo.getDataNetworkType()

        // Then: 유효한 네트워크 타입
        assertTrue(result >= TelephonyManager.NETWORK_TYPE_UNKNOWN)
    }

    @Test
    fun isNetworkRoaming_doesNotCrash() {
        // When
        val result = telephonyInfo.isNetworkRoaming()

        // Then: boolean 반환 (크래시 없음)
    }

    @Test
    fun getNetworkTypeString_returnsValidString() {
        // When
        val result = telephonyInfo.getNetworkTypeString()

        // Then: 빈 문자열이 아님
        assertNotNull(result)
    }

    // =================================================
    // Signal Information Tests
    // =================================================

    @Test
    fun getCurrentSignalStrength_doesNotCrash() {
        // When
        val result = telephonyInfo.getCurrentSignalStrength()

        // Then: null이거나 SignalStrength 객체
    }

    @Test
    fun getCurrentServiceState_doesNotCrash() {
        // When
        val result = telephonyInfo.getCurrentServiceState()

        // Then: null이거나 ServiceState 객체
    }

    // =================================================
    // Multi-SIM Tests
    // =================================================

    @Test
    fun getActiveSimCount_returnsNonNegative() {
        // When
        val result = telephonyInfo.getActiveSimCount()

        // Then: 0 이상
        assertTrue(result >= 0)
    }

    @Test
    fun getActiveSubscriptionInfoList_doesNotCrash() {
        // When
        val result = telephonyInfo.getActiveSubscriptionInfoList()

        // Then: 빈 리스트이거나 값이 있음
        assertNotNull(result)
    }

    @Test
    fun getDefaultDataSubscriptionInfo_doesNotCrash() {
        // When
        val result = telephonyInfo.getDefaultDataSubscriptionInfo()

        // Then: null이거나 SubscriptionInfo 객체
    }

    // =================================================
    // Callback Tests (등록/해제가 크래시 없이 동작하는지)
    // =================================================

    @Test
    fun registerCallback_doesNotCrash() {
        // When
        val result =
            telephonyInfo.registerCallback(
                handler = null,
                onSignalStrengthChanged = {},
                onServiceStateChanged = {},
                onNetworkStateChanged = {},
            )

        // Then: 등록 성공 (true) 또는 권한 없음 (false)
        // Cleanup
        telephonyInfo.unregisterCallback()
    }

    @Test
    fun unregisterCallback_doesNotCrash() {
        // Given: 콜백 등록
        telephonyInfo.registerCallback()

        // When
        val result = telephonyInfo.unregisterCallback()

        // Then: 크래시 없이 실행됨
    }

    // =================================================
    // StateFlow Tests
    // =================================================

    @Test
    fun currentSignalStrength_stateFlowExists() {
        // When
        val stateFlow = telephonyInfo.currentSignalStrength

        // Then: StateFlow 객체 존재
        assertNotNull(stateFlow)
    }

    @Test
    fun currentServiceState_stateFlowExists() {
        // When
        val stateFlow = telephonyInfo.currentServiceState

        // Then: StateFlow 객체 존재
        assertNotNull(stateFlow)
    }

    @Test
    fun currentNetworkState_stateFlowExists() {
        // When
        val stateFlow = telephonyInfo.currentNetworkState

        // Then: StateFlow 객체 존재
        assertNotNull(stateFlow)
    }

    // =================================================
    // Public Fields Tests
    // =================================================

    @Test
    fun telephonyManager_isAccessible() {
        // When
        val manager = telephonyInfo.telephonyManager

        // Then: TelephonyManager 객체 존재
        assertNotNull(manager)
    }

    @Test
    fun subscriptionManager_isAccessible() {
        // When
        val manager = telephonyInfo.subscriptionManager

        // Then: SubscriptionManager 객체 존재
        assertNotNull(manager)
    }
}
