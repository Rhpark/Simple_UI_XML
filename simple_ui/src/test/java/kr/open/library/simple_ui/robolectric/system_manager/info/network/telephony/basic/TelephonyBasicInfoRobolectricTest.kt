package kr.open.library.simple_ui.robolectric.system_manager.info.network.telephony.basic

import android.content.Context
import android.telephony.TelephonyManager
import androidx.test.core.app.ApplicationProvider
import kr.open.library.simple_ui.system_manager.info.network.telephony.basic.TelephonyBasicInfo
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Robolectric test for TelephonyBasicInfo
 *
 * Purpose: 기본 정보 조회 기능 테스트
 * Test basic information retrieval functions
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class TelephonyBasicInfoRobolectricTest {

    private lateinit var context: Context
    private lateinit var basicInfo: TelephonyBasicInfo

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        basicInfo = TelephonyBasicInfo(context)
    }

    // =================================================
    // Carrier Information Tests
    // =================================================

    @Test
    fun getCarrierName_doesNotCrash() {
        // When
        val result = basicInfo.getCarrierName()

        // Then: 크래시 없이 실행됨
    }

    @Test
    fun getMobileCountryCode_doesNotCrash() {
        // When
        val result = basicInfo.getMobileCountryCode()

        // Then: 크래시 없이 실행됨
    }

    @Test
    fun getMobileNetworkCode_doesNotCrash() {
        // When
        val result = basicInfo.getMobileNetworkCode()

        // Then: 크래시 없이 실행됨
    }

    // =================================================
    // SIM Information Tests
    // =================================================

    @Test
    fun getSimState_returnsValidState() {
        // When
        val result = basicInfo.getSimState()

        // Then: 유효한 SIM 상태 값
        assertTrue(result >= TelephonyManager.SIM_STATE_UNKNOWN)
    }

    @Test
    fun isSimReady_doesNotCrash() {
        // When
        val result = basicInfo.isSimReady()

        // Then: boolean 반환
    }

    @Test
    fun getSimOperatorName_doesNotCrash() {
        // When
        val result = basicInfo.getSimOperatorName()

        // Then: 크래시 없이 실행됨
    }

    @Test
    fun getSimCountryIso_doesNotCrash() {
        // When
        val result = basicInfo.getSimCountryIso()

        // Then: 크래시 없이 실행됨
    }

    @Test
    fun getSimStateString_returnsValidString() {
        // When
        val result = basicInfo.getSimStateString()

        // Then: 빈 문자열이 아님
        assertNotNull(result)
    }

    // =================================================
    // Phone Information Tests
    // =================================================

    @Test
    fun getPhoneNumber_doesNotCrash() {
        // When
        val result = basicInfo.getPhoneNumber()

        // Then: 크래시 없이 실행됨
    }

    @Test
    fun getCallState_returnsValidState() {
        // When
        val result = basicInfo.getCallState()

        // Then: 유효한 통화 상태
        assertTrue(result >= TelephonyManager.CALL_STATE_IDLE)
    }

    // =================================================
    // Network Information Tests
    // =================================================

    @Test
    fun getNetworkType_returnsValidType() {
        // When
        val result = basicInfo.getNetworkType()

        // Then: 유효한 네트워크 타입
        assertTrue(result >= TelephonyManager.NETWORK_TYPE_UNKNOWN)
    }

    @Test
    fun getDataNetworkType_returnsValidType() {
        // When
        val result = basicInfo.getDataNetworkType()

        // Then: 유효한 네트워크 타입
        assertTrue(result >= TelephonyManager.NETWORK_TYPE_UNKNOWN)
    }

    @Test
    fun isNetworkRoaming_doesNotCrash() {
        // When
        val result = basicInfo.isNetworkRoaming()

        // Then: boolean 반환
    }

    @Test
    fun getNetworkTypeString_returnsValidString() {
        // When
        val result = basicInfo.getNetworkTypeString()

        // Then: 빈 문자열이 아님
        assertNotNull(result)
    }

    // =================================================
    // Multi-SIM Tests
    // =================================================

    @Test
    fun getActiveSimCount_returnsNonNegative() {
        // When
        val result = basicInfo.getActiveSimCount()

        // Then: 0 이상
        assertTrue(result >= 0)
    }

    @Test
    fun getActiveSubscriptionInfoList_doesNotCrash() {
        // When
        val result = basicInfo.getActiveSubscriptionInfoList()

        // Then: 빈 리스트이거나 값이 있음
        assertNotNull(result)
    }

    @Test
    fun getDefaultDataSubscriptionInfo_doesNotCrash() {
        // When
        val result = basicInfo.getDefaultDataSubscriptionInfo()

        // Then: null이거나 SubscriptionInfo 객체
    }

    // =================================================
    // Public Fields Tests
    // =================================================

    @Test
    fun telephonyManager_isAccessible() {
        // When
        val manager = basicInfo.telephonyManager

        // Then: TelephonyManager 객체 존재
        assertNotNull(manager)
    }

    @Test
    fun subscriptionManager_isAccessible() {
        // When
        val manager = basicInfo.subscriptionManager

        // Then: SubscriptionManager 객체 존재
        assertNotNull(manager)
    }
}
