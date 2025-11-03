package kr.open.library.simple_ui.system_manager.info.network.telephony

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.Manifest.permission.READ_PHONE_NUMBERS
import android.Manifest.permission.READ_PHONE_STATE
import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Handler
import android.telephony.ServiceState
import android.telephony.SignalStrength
import android.telephony.SubscriptionInfo
import android.telephony.SubscriptionManager
import android.telephony.TelephonyDisplayInfo
import android.telephony.TelephonyManager
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import kotlinx.coroutines.flow.StateFlow
import kr.open.library.simple_ui.system_manager.base.BaseSystemService
import kr.open.library.simple_ui.system_manager.info.network.telephony.basic.TelephonyBasicInfo
import kr.open.library.simple_ui.system_manager.info.network.telephony.callback.TelephonyCallbackManager
import kr.open.library.simple_ui.system_manager.info.network.telephony.data.current.CurrentCellInfo
import kr.open.library.simple_ui.system_manager.info.network.telephony.data.current.CurrentServiceState
import kr.open.library.simple_ui.system_manager.info.network.telephony.data.current.CurrentSignalStrength
import kr.open.library.simple_ui.system_manager.info.network.telephony.data.state.TelephonyNetworkState
import java.util.concurrent.Executor

/**
 * TelephonyInfo - 통합된 Telephony 정보 관리 클래스 (Facade)
 * Unified Telephony Information Management Class (Facade)
 *
 * 이 클래스는 TelephonyManager의 복잡한 API를 단순화하고 통합된 인터페이스를 제공합니다.
 * This class simplifies TelephonyManager's complex APIs and provides a unified interface.
 *
 * 내부적으로 책임에 따라 두 개의 컴포넌트로 분리되어 있습니다:
 * Internally separated into two components by responsibility:
 * - TelephonyBasicInfo: 기본 정보 조회 / Basic information retrieval
 * - TelephonyCallbackManager: 콜백 관리 / Callback management
 *
 * 주요 기능 / Main Features:
 * - SIM 카드 정보 관리 / SIM card information management
 * - 통신사 정보 조회 / Carrier information retrieval
 * - 신호 강도 모니터링 / Signal strength monitoring
 * - 네트워크 상태 추적 / Network state tracking
 * - API 호환성 처리 (TelephonyCallback vs PhoneStateListener) / API compatibility handling
 * - 멀티 SIM 지원 / Multi-SIM support
 * - 슬롯별 콜백 관리 / Per-slot callback management
 * - 고급 콜백 시스템 / Advanced callback system
 *
 * 필수 권한 / Required Permissions:
 * - android.permission.READ_PHONE_STATE (필수/Required)
 * - android.permission.ACCESS_FINE_LOCATION (셀 정보/Cell info)
 * - android.permission.READ_PHONE_NUMBERS (전화번호/Phone numbers)
 *
 * 사용 예시 / Usage Example:
 * ```
 * val telephonyInfo = TelephonyInfo(context)
 *
 * // 기본 정보 조회 / Basic info retrieval
 * val carrierName = telephonyInfo.getCarrierName()
 * val simState = telephonyInfo.getSimState()
 *
 * // 실시간 모니터링 / Real-time monitoring
 * telephonyInfo.registerCallback { state ->
 *     // 상태 변경 처리 / Handle state changes
 * }
 *
 * // 슬롯별 콜백 등록 / Per-slot callback registration
 * telephonyInfo.registerTelephonyCallBack(
 *     simSlotIndex = 0,
 *     executor = context.mainExecutor,
 *     onSignalStrength = { signal -> /* 신호 강도 변경 */ }
 * )
 * ```
 */
public class TelephonyInfo(context: Context) :
    BaseSystemService(context, listOf(READ_PHONE_STATE, ACCESS_FINE_LOCATION, READ_PHONE_NUMBERS)) {

    // =================================================
    // Core Components (Delegation)
    // =================================================

    /**
     * Basic information retrieval component
     * 기본 정보 조회 컴포넌트
     */
    private val basicInfo = TelephonyBasicInfo(context)

    /**
     * Callback management component
     * 콜백 관리 컴포넌트
     */
    private val callbackManager = TelephonyCallbackManager(context)

    // =================================================
    // Public Fields (delegated to components)
    // =================================================

    /**
     * Main TelephonyManager instance
     * 기본 TelephonyManager 인스턴스
     */
    public val telephonyManager: TelephonyManager
        get() = basicInfo.telephonyManager

    /**
     * SubscriptionManager for multi-SIM support
     * 멀티 SIM 지원을 위한 SubscriptionManager
     */
    public val subscriptionManager: SubscriptionManager
        get() = basicInfo.subscriptionManager

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
    public fun getCarrierName(): String? = basicInfo.getCarrierName()

    /**
     * Gets the Mobile Country Code (MCC) from the default SIM.
     * 기본 SIM에서 Mobile Country Code (MCC)를 가져옵니다.
     */
    @RequiresPermission(READ_PHONE_STATE)
    public fun getMobileCountryCode(): String? = basicInfo.getMobileCountryCode()

    /**
     * Gets the Mobile Network Code (MNC) from the default SIM.
     * 기본 SIM에서 Mobile Network Code (MNC)를 가져옵니다.
     */
    @RequiresPermission(READ_PHONE_STATE)
    public fun getMobileNetworkCode(): String? = basicInfo.getMobileNetworkCode()

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
    public fun getSimState(): Int = basicInfo.getSimState()

    /**
     * Checks if SIM is ready.
     * SIM이 준비되었는지 확인합니다.
     */
    @RequiresPermission(READ_PHONE_STATE)
    public fun isSimReady(): Boolean = basicInfo.isSimReady()

    /**
     * Gets the SIM operator name.
     * SIM 운영자 이름을 가져옵니다.
     */
    @RequiresPermission(READ_PHONE_STATE)
    public fun getSimOperatorName(): String? = basicInfo.getSimOperatorName()

    /**
     * Gets the ISO country code for the SIM provider.
     * SIM 제공업체의 ISO 국가 코드를 가져옵니다.
     */
    @RequiresPermission(READ_PHONE_STATE)
    public fun getSimCountryIso(): String? = basicInfo.getSimCountryIso()

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
    public fun getPhoneNumber(): String? = basicInfo.getPhoneNumber()

    /**
     * Gets the call state.
     * 통화 상태를 가져옵니다.
     */
    public fun getCallState(): Int = basicInfo.getCallState()

    // =================================================
    // Network Information / 네트워크 정보
    // =================================================

    /**
     * Gets the current network type.
     * 현재 네트워크 타입을 가져옵니다.
     */
    @RequiresPermission(READ_PHONE_STATE)
    public fun getNetworkType(): Int = basicInfo.getNetworkType()

    /**
     * Gets the current data network type.
     * 현재 데이터 네트워크 타입을 가져옵니다.
     */
    @RequiresPermission(READ_PHONE_STATE)
    public fun getDataNetworkType(): Int = basicInfo.getDataNetworkType()

    /**
     * Checks if the device is roaming.
     * 디바이스가 로밍 중인지 확인합니다.
     */
    @RequiresPermission(READ_PHONE_STATE)
    public fun isNetworkRoaming(): Boolean = basicInfo.isNetworkRoaming()

    // =================================================
    // Multi-SIM Support / 멀티 SIM 지원
    // =================================================

    /**
     * Gets the number of active SIM cards.
     * 활성화된 SIM 카드 수를 가져옵니다.
     */
    @RequiresPermission(READ_PHONE_STATE)
    public fun getActiveSimCount(): Int = basicInfo.getActiveSimCount()

    /**
     * Gets active subscription info list.
     * 활성 구독 정보 목록을 가져옵니다.
     */
    @RequiresPermission(READ_PHONE_STATE)
    public fun getActiveSubscriptionInfoList(): List<SubscriptionInfo> =
        basicInfo.getActiveSubscriptionInfoList()

    /**
     * Gets subscription info for the default data SIM.
     * 기본 데이터 SIM의 구독 정보를 가져옵니다.
     */
    @RequiresPermission(READ_PHONE_STATE)
    public fun getDefaultDataSubscriptionInfo(): SubscriptionInfo? =
        basicInfo.getDefaultDataSubscriptionInfo()

    // =================================================
    // Utility Methods / 유틸리티 메서드
    // =================================================

    /**
     * Gets network type as human-readable string.
     * 네트워크 타입을 읽기 쉬운 문자열로 가져옵니다.
     */
    @RequiresPermission(READ_PHONE_STATE)
    public fun getNetworkTypeString(): String = basicInfo.getNetworkTypeString()

    /**
     * Gets SIM state as human-readable string.
     * SIM 상태를 읽기 쉬운 문자열로 가져옵니다.
     */
    @RequiresPermission(READ_PHONE_STATE)
    public fun getSimStateString(): String = basicInfo.getSimStateString()

    // =================================================
    // State Management (Simple API)
    // =================================================

    /**
     * StateFlow for signal strength updates
     * 신호 강도 업데이트를 위한 StateFlow
     */
    public val currentSignalStrength: StateFlow<SignalStrength?>
        get() = callbackManager.currentSignalStrength

    /**
     * StateFlow for service state updates
     * 서비스 상태 업데이트를 위한 StateFlow
     */
    public val currentServiceState: StateFlow<ServiceState?>
        get() = callbackManager.currentServiceState

    /**
     * StateFlow for network state updates
     * 네트워크 상태 업데이트를 위한 StateFlow
     */
    public val currentNetworkState: StateFlow<TelephonyNetworkState?>
        get() = callbackManager.currentNetworkState

    // =================================================
    // Signal Information / 신호 정보
    // =================================================

    /**
     * Gets the current signal strength.
     * 현재 신호 강도를 가져옵니다.
     *
     * Note: This returns the cached value from callbacks. Register callback first.
     * 참고: 콜백에서 캐시된 값을 반환합니다. 먼저 콜백을 등록하세요.
     */
    public fun getCurrentSignalStrength(): SignalStrength? =
        callbackManager.getCurrentSignalStrength()

    /**
     * Gets the current service state.
     * 현재 서비스 상태를 가져옵니다.
     */
    public fun getCurrentServiceState(): ServiceState? =
        callbackManager.getCurrentServiceState()

    // =================================================
    // Callback Management (Simple API)
    // =================================================

    /**
     * Registers telephony callback for real-time updates.
     * 실시간 업데이트를 위한 telephony 콜백을 등록합니다.
     */
    @RequiresPermission(READ_PHONE_STATE)
    public fun registerCallback(
        handler: Handler? = null,
        onSignalStrengthChanged: ((SignalStrength) -> Unit)? = null,
        onServiceStateChanged: ((ServiceState) -> Unit)? = null,
        onNetworkStateChanged: ((TelephonyNetworkState) -> Unit)? = null
    ): Boolean = callbackManager.registerSimpleCallback(
        handler, onSignalStrengthChanged, onServiceStateChanged, onNetworkStateChanged
    )

    /**
     * Unregisters telephony callback.
     * telephony 콜백을 해제합니다.
     */
    @SuppressLint("MissingPermission")
    public fun unregisterCallback(): Boolean = callbackManager.unregisterSimpleCallback()

    // =================================================
    // Advanced Multi-SIM Callback System (API 31+)
    // =================================================

    /**
     * 기본 SIM에 대한 Telephony 콜백 등록 (API 31+)
     * Register telephony callback for default SIM (API 31+)
     */
    @RequiresApi(Build.VERSION_CODES.S)
    @RequiresPermission(READ_PHONE_STATE)
    public fun registerTelephonyCallBackFromDefaultUSim(
        executor: Executor,
        isGpsOn: Boolean,
        onActiveDataSubId: ((subId: Int) -> Unit)? = null,
        onDataConnectionState: ((state: Int, networkType: Int) -> Unit)? = null,
        onCellInfo: ((currentCellInfo: CurrentCellInfo) -> Unit)? = null,
        onSignalStrength: ((currentSignalStrength: CurrentSignalStrength) -> Unit)? = null,
        onServiceState: ((currentServiceState: CurrentServiceState) -> Unit)? = null,
        onCallState: ((callState: Int, phoneNumber: String?) -> Unit)? = null,
        onDisplayInfo: ((telephonyDisplayInfo: TelephonyDisplayInfo) -> Unit)? = null,
        onTelephonyNetworkState: ((telephonyNetworkState: TelephonyNetworkState) -> Unit)? = null
    ): Boolean = callbackManager.registerAdvancedCallbackFromDefaultUSim(
        executor, isGpsOn, onActiveDataSubId, onDataConnectionState, onCellInfo,
        onSignalStrength, onServiceState, onCallState, onDisplayInfo, onTelephonyNetworkState
    )

    /**
     * 특정 SIM 슬롯에 대한 Telephony 콜백 등록 (API 31+)
     * Register telephony callback for specific SIM slot (API 31+)
     */
    @RequiresApi(Build.VERSION_CODES.S)
    @RequiresPermission(READ_PHONE_STATE)
    public fun registerTelephonyCallBack(
        simSlotIndex: Int,
        executor: Executor,
        isGpsOn: Boolean,
        onActiveDataSubId: ((subId: Int) -> Unit)? = null,
        onDataConnectionState: ((state: Int, networkType: Int) -> Unit)? = null,
        onCellInfo: ((currentCellInfo: CurrentCellInfo) -> Unit)? = null,
        onSignalStrength: ((currentSignalStrength: CurrentSignalStrength) -> Unit)? = null,
        onServiceState: ((currentServiceState: CurrentServiceState) -> Unit)? = null,
        onCallState: ((callState: Int, phoneNumber: String?) -> Unit)? = null,
        onDisplayInfo: ((telephonyDisplayInfo: TelephonyDisplayInfo) -> Unit)? = null,
        onTelephonyNetworkState: ((telephonyNetworkState: TelephonyNetworkState) -> Unit)? = null
    ) = callbackManager.registerAdvancedCallback(
        simSlotIndex, executor, isGpsOn, onActiveDataSubId, onDataConnectionState,
        onCellInfo, onSignalStrength, onServiceState, onCallState, onDisplayInfo, onTelephonyNetworkState
    )

    /**
     * 특정 SIM 슬롯의 콜백 해제 (API 31+)
     * Unregister callback for specific SIM slot (API 31+)
     */
    @RequiresApi(Build.VERSION_CODES.S)
    public fun unregisterCallBack(simSlotIndex: Int) =
        callbackManager.unregisterAdvancedCallback(simSlotIndex)

    // =================================================
    // Individual Callback Setters (Advanced API)
    // =================================================

    /**
     * 신호 강도 콜백 설정
     * Set signal strength callback
     */
    @RequiresPermission(READ_PHONE_STATE)
    public fun setOnSignalStrength(
        simSlotIndex: Int,
        onSignalStrength: ((currentSignalStrength: CurrentSignalStrength) -> Unit)? = null
    ) = callbackManager.setOnSignalStrength(simSlotIndex, onSignalStrength)

    /**
     * 서비스 상태 콜백 설정
     * Set service state callback
     */
    @RequiresPermission(READ_PHONE_STATE)
    public fun setOnServiceState(
        simSlotIndex: Int,
        onServiceState: ((currentServiceState: CurrentServiceState) -> Unit)? = null
    ) = callbackManager.setOnServiceState(simSlotIndex, onServiceState)

    /**
     * 활성 데이터 구독 ID 콜백 설정
     * Set active data subscription ID callback
     */
    @RequiresPermission(READ_PHONE_STATE)
    public fun setOnActiveDataSubId(
        simSlotIndex: Int,
        onActiveDataSubId: ((subId: Int) -> Unit)? = null
    ) = callbackManager.setOnActiveDataSubId(simSlotIndex, onActiveDataSubId)

    /**
     * 데이터 연결 상태 콜백 설정
     * Set data connection state callback
     */
    @RequiresPermission(READ_PHONE_STATE)
    public fun setOnDataConnectionState(
        simSlotIndex: Int,
        onDataConnectionState: ((state: Int, networkType: Int) -> Unit)? = null
    ) = callbackManager.setOnDataConnectionState(simSlotIndex, onDataConnectionState)

    /**
     * 셀 정보 콜백 설정
     * Set cell info callback
     */
    @RequiresPermission(READ_PHONE_STATE)
    public fun setOnCellInfo(
        simSlotIndex: Int,
        onCellInfo: ((currentCellInfo: CurrentCellInfo) -> Unit)? = null
    ) = callbackManager.setOnCellInfo(simSlotIndex, onCellInfo)

    /**
     * 통화 상태 콜백 설정
     * Set call state callback
     */
    @RequiresPermission(READ_PHONE_STATE)
    public fun setOnCallState(
        simSlotIndex: Int,
        onCallState: ((callState: Int, phoneNumber: String?) -> Unit)? = null
    ) = callbackManager.setOnCallState(simSlotIndex, onCallState)

    /**
     * 디스플레이 정보 콜백 설정
     * Set display info callback
     */
    @RequiresPermission(READ_PHONE_STATE)
    public fun setOnDisplayState(
        simSlotIndex: Int,
        onDisplay: ((telephonyDisplayInfo: TelephonyDisplayInfo) -> Unit)? = null
    ) = callbackManager.setOnDisplayState(simSlotIndex, onDisplay)

    /**
     * 통신망 타입 콜백 설정
     * Set telephony network type callback
     */
    public fun setOnTelephonyNetworkType(
        simSlotIndex: Int,
        onTelephonyNetworkType: ((telephonyNetworkState: TelephonyNetworkState) -> Unit)? = null
    ) = callbackManager.setOnTelephonyNetworkType(simSlotIndex, onTelephonyNetworkType)

    // =================================================
    // Utility Methods (Advanced API)
    // =================================================

    /**
     * 콜백 등록 상태 확인
     * Check callback registration status
     */
    public fun isRegistered(simSlotIndex: Int): Boolean =
        callbackManager.isRegistered(simSlotIndex)

    /**
     * 특정 SIM 슬롯의 TelephonyManager 반환
     * Get TelephonyManager for specific SIM slot
     */
    public fun getTelephonyManagerFromUSim(slotIndex: Int): TelephonyManager? =
        callbackManager.getTelephonyManagerFromUSim(slotIndex)

    // =================================================
    // Cleanup / 정리
    // =================================================

    override fun onDestroy() {
        try {
            callbackManager.onDestroy()
            basicInfo.onDestroy()
        } finally {
            super.onDestroy()
        }
    }
}
