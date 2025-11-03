package kr.open.library.simple_ui.system_manager.info.network.telephony.basic

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.Manifest.permission.READ_PHONE_NUMBERS
import android.Manifest.permission.READ_PHONE_STATE
import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.telephony.SubscriptionInfo
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import androidx.annotation.RequiresPermission
import kr.open.library.simple_ui.extensions.conditional.checkSdkVersion
import kr.open.library.simple_ui.system_manager.base.BaseSystemService
import kr.open.library.simple_ui.system_manager.extensions.getTelephonyManager

/**
 * TelephonyBasicInfo - Telephony 기본 정보 조회 클래스
 * Telephony Basic Information Retrieval Class
 *
 * 이 클래스는 통신사, SIM, 네트워크 정보 등의 기본 조회 기능만 제공합니다.
 * This class provides only basic retrieval functions for carrier, SIM, and network information.
 *
 * 주요 기능 / Main Features:
 * - Carrier information retrieval / 통신사 정보 조회
 * - SIM card information / SIM 카드 정보
 * - Phone number retrieval / 전화번호 조회
 * - Network type information / 네트워크 타입 정보
 * - Multi-SIM support / 멀티 SIM 지원
 *
 * 필수 권한 / Required Permissions:
 * - android.permission.READ_PHONE_STATE (필수/Required)
 * - android.permission.ACCESS_FINE_LOCATION (셀 정보/Cell info)
 * - android.permission.READ_PHONE_NUMBERS (전화번호/Phone numbers)
 *
 * 사용 예시 / Usage Example:
 * ```
 * val basicInfo = TelephonyBasicInfo(context)
 *
 * val carrierName = basicInfo.getCarrierName()
 * val simState = basicInfo.getSimState()
 * val networkType = basicInfo.getNetworkType()
 * ```
 */
public class TelephonyBasicInfo(context: Context) :
    BaseSystemService(context, listOf(READ_PHONE_STATE, ACCESS_FINE_LOCATION, READ_PHONE_NUMBERS)) {

    // =================================================
    // Core Components
    // =================================================

    /**
     * Main TelephonyManager instance
     * 기본 TelephonyManager 인스턴스
     */
    public val telephonyManager: TelephonyManager by lazy { context.getTelephonyManager() }

    /**
     * SubscriptionManager for multi-SIM support
     * 멀티 SIM 지원을 위한 SubscriptionManager
     */
    public val subscriptionManager: SubscriptionManager by lazy {
        context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager
    }

    // =================================================
    // Carrier Information / 통신사 정보
    // =================================================

    /**
     * Gets the carrier name for the default SIM.
     * 기본 SIM의 통신사 이름을 가져옵니다.
     *
     * @return Carrier name or null if unavailable / 통신사 이름 또는 사용 불가 시 null
     */
    @RequiresPermission(READ_PHONE_STATE)
    public fun getCarrierName(): String? = tryCatchSystemManager(null) {
        telephonyManager.networkOperatorName?.takeIf { it.isNotBlank() }
    }


    /**
     * Gets the Mobile Country Code (MCC) from the default SIM.
     * 기본 SIM에서 Mobile Country Code (MCC)를 가져옵니다.
     */
    @RequiresPermission(READ_PHONE_STATE)
    public fun getMobileCountryCode(): String? = tryCatchSystemManager(null) {
        telephonyManager.networkOperator?.take(3)?.takeIf { it.length == 3 }
    }

    /**
     * Gets the Mobile Network Code (MNC) from the default SIM.
     * 기본 SIM에서 Mobile Network Code (MNC)를 가져옵니다.
     */
    @RequiresPermission(READ_PHONE_STATE)
    public fun getMobileNetworkCode(): String? = tryCatchSystemManager(null) {
        val operator = telephonyManager.networkOperator
        if (operator?.length in 5..6) {
            operator.substring(3)
        } else null
    }

    // =================================================
    // SIM Information / SIM 정보
    // =================================================

    /**
     * Gets the SIM state of the default SIM.
     * 기본 SIM의 상태를 가져옵니다.
     *
     * @return SIM state constant from TelephonyManager / TelephonyManager의 SIM 상태 상수
     */
    @RequiresPermission(READ_PHONE_STATE)
    public fun getSimState(): Int = tryCatchSystemManager(TelephonyManager.SIM_STATE_UNKNOWN) {
        telephonyManager.simState
    }


    /**
     * Checks if SIM is ready.
     * SIM이 준비되었는지 확인합니다.
     */
    @RequiresPermission(READ_PHONE_STATE)
    public fun isSimReady(): Boolean = getSimState() == TelephonyManager.SIM_STATE_READY

    /**
     * Gets the SIM operator name.
     * SIM 운영자 이름을 가져옵니다.
     */
    @RequiresPermission(READ_PHONE_STATE)
    public fun getSimOperatorName(): String? = tryCatchSystemManager(null) {
        telephonyManager.simOperatorName?.takeIf { it.isNotBlank() }
    }

    /**
     * Gets the ISO country code for the SIM provider.
     * SIM 제공업체의 ISO 국가 코드를 가져옵니다.
     */
    @RequiresPermission(READ_PHONE_STATE)
    public fun getSimCountryIso(): String? = tryCatchSystemManager(null) {
        telephonyManager.simCountryIso?.takeIf { it.isNotBlank() }
    }

    // =================================================
    // Phone Information / 전화 정보
    // =================================================

    /**
     * Gets the phone number of the default SIM.
     * 기본 SIM의 전화번호를 가져옵니다.
     *
     * Note: This may return null or empty string depending on SIM configuration
     * 참고: SIM 구성에 따라 null이나 빈 문자열을 반환할 수 있습니다.
     */
    @SuppressLint("HardwareIds")
    @RequiresPermission(anyOf = [READ_PHONE_STATE, READ_PHONE_NUMBERS])
    public fun getPhoneNumber(): String? = tryCatchSystemManager(null) {
        @Suppress("DEPRECATION")
        telephonyManager.line1Number?.takeIf { it.isNotBlank() }
    }

    /**
     * Gets the call state.
     * 통화 상태를 가져옵니다.
     */
    public fun getCallState(): Int = tryCatchSystemManager(TelephonyManager.CALL_STATE_IDLE) {
        telephonyManager.callState
    }

    // =================================================
    // Network Information / 네트워크 정보
    // =================================================

    /**
     * Gets the current network type.
     * 현재 네트워크 타입을 가져옵니다.
     */
    @RequiresPermission(READ_PHONE_STATE)
    public fun getNetworkType(): Int = tryCatchSystemManager(TelephonyManager.NETWORK_TYPE_UNKNOWN) {
        checkSdkVersion(Build.VERSION_CODES.R,
            positiveWork = { telephonyManager.dataNetworkType },
            negativeWork = {
                @Suppress("DEPRECATION")
                telephonyManager.networkType
            }
        )
    }

    /**
     * Gets the current data network type.
     * 현재 데이터 네트워크 타입을 가져옵니다.
     */
    @RequiresPermission(READ_PHONE_STATE)
    public fun getDataNetworkType(): Int = tryCatchSystemManager(TelephonyManager.NETWORK_TYPE_UNKNOWN) {
        telephonyManager.dataNetworkType
    }

    /**
     * Checks if the device is roaming.
     * 디바이스가 로밍 중인지 확인합니다.
     */
    @RequiresPermission(READ_PHONE_STATE)
    public fun isNetworkRoaming(): Boolean = tryCatchSystemManager(false) {
        telephonyManager.isNetworkRoaming
    }

    // =================================================
    // Multi-SIM Support / 멀티 SIM 지원
    // =================================================

    /**
     * Gets the number of active SIM cards.
     * 활성화된 SIM 카드 수를 가져옵니다.
     */
    @RequiresPermission(READ_PHONE_STATE)
    public fun getActiveSimCount(): Int = tryCatchSystemManager(0) {
        subscriptionManager.activeSubscriptionInfoCount
    }

    /**
     * Gets active subscription info list.
     * 활성 구독 정보 목록을 가져옵니다.
     */
    @RequiresPermission(READ_PHONE_STATE)
    public fun getActiveSubscriptionInfoList(): List<SubscriptionInfo> =
        tryCatchSystemManager(emptyList()) {
            subscriptionManager.activeSubscriptionInfoList ?: emptyList()
        }

    /**
     * Gets subscription info for the default data SIM.
     * 기본 데이터 SIM의 구독 정보를 가져옵니다.
     */
    @RequiresPermission(READ_PHONE_STATE)
    public fun getDefaultDataSubscriptionInfo(): SubscriptionInfo? = tryCatchSystemManager(null) {
        checkSdkVersion(Build.VERSION_CODES.R,
            positiveWork = { telephonyManager.subscriptionId },
            negativeWork = { getActiveSubscriptionInfoList().firstOrNull()?.subscriptionId }
        )?.let { subscriptionManager.getActiveSubscriptionInfo(it) }
    }

    // =================================================
    // Utility Methods / 유틸리티 메서드
    // =================================================

    /**
     * Gets network type as human-readable string.
     * 네트워크 타입을 읽기 쉬운 문자열로 가져옵니다.
     */
    @RequiresPermission(READ_PHONE_STATE)
    public fun getNetworkTypeString(): String = when (getNetworkType()) {
        TelephonyManager.NETWORK_TYPE_GPRS -> "GPRS"
        TelephonyManager.NETWORK_TYPE_EDGE -> "EDGE"
        TelephonyManager.NETWORK_TYPE_UMTS -> "UMTS"
        TelephonyManager.NETWORK_TYPE_HSDPA -> "HSDPA"
        TelephonyManager.NETWORK_TYPE_HSUPA -> "HSUPA"
        TelephonyManager.NETWORK_TYPE_HSPA -> "HSPA"
        TelephonyManager.NETWORK_TYPE_CDMA -> "CDMA"
        TelephonyManager.NETWORK_TYPE_EVDO_0 -> "EVDO_0"
        TelephonyManager.NETWORK_TYPE_EVDO_A -> "EVDO_A"
        TelephonyManager.NETWORK_TYPE_EVDO_B -> "EVDO_B"
        TelephonyManager.NETWORK_TYPE_1xRTT -> "1xRTT"
        TelephonyManager.NETWORK_TYPE_IDEN -> "IDEN"
        TelephonyManager.NETWORK_TYPE_LTE -> "LTE"
        TelephonyManager.NETWORK_TYPE_EHRPD -> "EHRPD"
        TelephonyManager.NETWORK_TYPE_HSPAP -> "HSPA+"
        TelephonyManager.NETWORK_TYPE_GSM -> "GSM"
        TelephonyManager.NETWORK_TYPE_TD_SCDMA -> "TD_SCDMA"
        TelephonyManager.NETWORK_TYPE_IWLAN -> "IWLAN"
        20 /* NETWORK_TYPE_NR */ -> "5G NR"
        19 /* NETWORK_TYPE_LTE_CA */ -> "LTE_CA"
        else -> "UNKNOWN"
    }

    /**
     * Gets SIM state as human-readable string.
     * SIM 상태를 읽기 쉬운 문자열로 가져옵니다.
     */
    @RequiresPermission(READ_PHONE_STATE)
    public fun getSimStateString(): String = when (getSimState()) {
        TelephonyManager.SIM_STATE_UNKNOWN -> "UNKNOWN"
        TelephonyManager.SIM_STATE_ABSENT -> "ABSENT"
        TelephonyManager.SIM_STATE_PIN_REQUIRED -> "PIN_REQUIRED"
        TelephonyManager.SIM_STATE_PUK_REQUIRED -> "PUK_REQUIRED"
        TelephonyManager.SIM_STATE_NETWORK_LOCKED -> "NETWORK_LOCKED"
        TelephonyManager.SIM_STATE_READY -> "READY"
        TelephonyManager.SIM_STATE_NOT_READY -> "NOT_READY"
        TelephonyManager.SIM_STATE_PERM_DISABLED -> "PERM_DISABLED"
        TelephonyManager.SIM_STATE_CARD_IO_ERROR -> "CARD_IO_ERROR"
        TelephonyManager.SIM_STATE_CARD_RESTRICTED -> "CARD_RESTRICTED"
        else -> "UNKNOWN"
    }
}
