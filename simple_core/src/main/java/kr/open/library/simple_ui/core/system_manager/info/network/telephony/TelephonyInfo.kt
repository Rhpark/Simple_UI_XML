package kr.open.library.simple_ui.core.system_manager.info.network.telephony

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
import kr.open.library.simple_ui.core.extensions.conditional.checkSdkVersion
import kr.open.library.simple_ui.core.system_manager.base.BaseSystemService
import kr.open.library.simple_ui.core.system_manager.extensions.getTelephonyManager
import kr.open.library.simple_ui.core.system_manager.info.network.telephony.callback.TelephonyCallbackManager
import kr.open.library.simple_ui.core.system_manager.info.network.telephony.data.current.CurrentCellInfo
import kr.open.library.simple_ui.core.system_manager.info.network.telephony.data.current.CurrentServiceState
import kr.open.library.simple_ui.core.system_manager.info.network.telephony.data.current.CurrentSignalStrength
import kr.open.library.simple_ui.core.system_manager.info.network.telephony.data.state.TelephonyNetworkState
import java.util.concurrent.Executor

/**
 * Unified Telephony Information Management Class (Facade).<br><br>
 * 통합된 Telephony 정보 관리 클래스 (Facade)입니다.<br>
 *
 * This class simplifies TelephonyManager's complex APIs and provides a unified interface.<br><br>
 * 이 클래스는 TelephonyManager의 복잡한 API를 단순화하고 통합된 인터페이스를 제공합니다.<br>
 *
 * Internally separated into two components by responsibility:<br>
 * - TelephonyBasicInfo: Basic information retrieval<br>
 * - TelephonyCallbackManager: Callback management<br><br>
 * 내부적으로 책임에 따라 두 개의 컴포넌트로 분리되어 있습니다:<br>
 * - TelephonyBasicInfo: 기본 정보 조회<br>
 * - TelephonyCallbackManager: 콜백 관리<br>
 *
 * Main Features:<br>
 * - SIM card information management<br>
 * - Carrier information retrieval<br>
 * - Signal strength monitoring<br>
 * - Network state tracking<br>
 * - API compatibility handling (TelephonyCallback vs PhoneStateListener)<br>
 * - Multi-SIM support<br>
 * - Per-slot callback management<br>
 * - Advanced callback system<br><br>
 * 주요 기능:<br>
 * - SIM 카드 정보 관리<br>
 * - 통신사 정보 조회<br>
 * - 신호 강도 모니터링<br>
 * - 네트워크 상태 추적<br>
 * - API 호환성 처리 (TelephonyCallback vs PhoneStateListener)<br>
 * - 멀티 SIM 지원<br>
 * - 슬롯별 콜백 관리<br>
 * - 고급 콜백 시스템<br>
 *
 * Required Permissions:<br>
 * - `android.permission.READ_PHONE_STATE` (Required)<br>
 * - `android.permission.ACCESS_FINE_LOCATION` (Cell info)<br>
 * - `android.permission.READ_PHONE_NUMBERS` (Phone numbers)<br><br>
 * 필수 권한:<br>
 * - `android.permission.READ_PHONE_STATE` (필수)<br>
 * - `android.permission.ACCESS_FINE_LOCATION` (셀 정보)<br>
 * - `android.permission.READ_PHONE_NUMBERS` (전화번호)<br>
 *
 * ⚠️ Permission fallback:<br>
 * - If any required permission is missing, public API calls return safe defaults (null, empty list, or false) via tryCatchSystemManager().<br>
 * - Always call refreshPermissions() after the host Activity/Fragment obtains runtime permissions.<br><br>
 * ⚠️ 권한 폴백:<br>
 * - 필수 권한이 누락된 경우, 공개 API 호출은 tryCatchSystemManager()를 통해 안전한 기본값(null, 빈 리스트 또는 false)을 반환합니다.<br>
 * - 호스트 Activity/Fragment가 런타임 권한을 획득한 후에는 항상 refreshPermissions()를 호출하세요.<br>
 *
 * Usage Example:<br>
 * ```kotlin
 * val telephonyInfo = TelephonyInfo(context)
 *
 * // Basic info retrieval
 * val carrierName = telephonyInfo.getCarrierName()
 * val simState = telephonyInfo.getSimState()
 *
 * // Real-time monitoring
 * telephonyInfo.registerCallback { state ->
 *     // Handle state changes
 * }
 *
 * // Per-slot callback registration
 * telephonyInfo.registerTelephonyCallBack(
 *     simSlotIndex = 0,
 *     executor = context.mainExecutor,
 *     onSignalStrength = { signal -> /* Signal strength changed */ }
 * )
 * ```
 *
 * @param context The application context.<br><br>
 *                애플리케이션 컨텍스트.
 */
public class TelephonyInfo(
    context: Context,
) : BaseSystemService(context, listOf(READ_PHONE_STATE, ACCESS_FINE_LOCATION, READ_PHONE_NUMBERS)) {
    // =================================================
    // Core Components (Delegation)
    // =================================================

    /**
     * Main TelephonyManager instance.<br><br>
     * 기본 TelephonyManager 인스턴스입니다.<br>
     */
    public val telephonyManager: TelephonyManager by lazy { context.getTelephonyManager() }

    /**
     * SubscriptionManager for multi-SIM support.<br><br>
     * 멀티 SIM 지원을 위한 SubscriptionManager입니다.<br>
     */
    public val subscriptionManager: SubscriptionManager by lazy {
        context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager
    }

    /**
     * Callback management component.<br><br>
     * 콜백 관리 컴포넌트입니다.<br>
     */
    private val callbackManager = TelephonyCallbackManager(context)

    // =================================================
    // Carrier Information / 통신사 정보
    // =================================================

    /**
     * Gets the carrier name for the default SIM.<br><br>
     * 기본 SIM의 통신사 이름을 가져옵니다.<br>
     *
     * @return Carrier name or null if unavailable.<br><br>
     *         통신사 이름 또는 사용 불가 시 null.
     */
    @RequiresPermission(READ_PHONE_STATE)
    public fun getCarrierName(): String? = tryCatchSystemManager(null) {
        return telephonyManager.networkOperatorName?.takeIf { it.isNotBlank() }
    }

    /**
     * Gets the Mobile Country Code (MCC) from the default SIM.<br><br>
     * 기본 SIM에서 Mobile Country Code (MCC)를 가져옵니다.<br>
     *
     * @return MCC string, or null if unavailable.<br><br>
     *         MCC 문자열, 사용할 수 없는 경우 null.
     */
    @RequiresPermission(READ_PHONE_STATE)
    public fun getMobileCountryCode(): String? = tryCatchSystemManager(null) {
        return telephonyManager.networkOperator?.take(3)?.takeIf { it.length == 3 }
    }

    /**
     * Gets the Mobile Network Code (MNC) from the default SIM.<br><br>
     * 기본 SIM에서 Mobile Network Code (MNC)를 가져옵니다.<br>
     *
     * @return MNC string, or null if unavailable.<br><br>
     *         MNC 문자열, 사용할 수 없는 경우 null.
     */
    @RequiresPermission(READ_PHONE_STATE)
    public fun getMobileNetworkCode(): String? = tryCatchSystemManager(null) {
        val operator = telephonyManager.networkOperator
        return if (operator?.length in 5..6) {
            operator.substring(3)
        } else {
            null
        }
    }

    // =================================================
    // SIM Information / SIM 정보
    // =================================================

    /**
     * Gets the SIM state of the default SIM.<br><br>
     * 기본 SIM의 상태를 가져옵니다.<br>
     *
     * @return SIM state constant from TelephonyManager.<br><br>
     *         TelephonyManager의 SIM 상태 상수.
     */
    @RequiresPermission(READ_PHONE_STATE)
    public fun getSimState(): Int = tryCatchSystemManager(TelephonyManager.SIM_STATE_UNKNOWN) {
        return telephonyManager.simState
    }

    /**
     * Checks if SIM is ready.<br><br>
     * SIM이 준비되었는지 확인합니다.<br>
     *
     * @return `true` if SIM is ready, `false` otherwise.<br><br>
     *         SIM이 준비되었으면 `true`, 그렇지 않으면 `false`.<br>
     */
    @RequiresPermission(READ_PHONE_STATE)
    public fun isSimReady(): Boolean = getSimState() == TelephonyManager.SIM_STATE_READY

    /**
     * Gets the SIM operator name.<br><br>
     * SIM 운영자 이름을 가져옵니다.<br>
     *
     * @return SIM operator name, or null if unavailable.<br><br>
     *         SIM 운영자 이름, 사용할 수 없는 경우 null.
     */
    @RequiresPermission(READ_PHONE_STATE)
    public fun getSimOperatorName(): String? = tryCatchSystemManager(null) {
        return telephonyManager.simOperatorName?.takeIf { it.isNotBlank() }
    }

    /**
     * Gets the ISO country code for the SIM provider.<br><br>
     * SIM 제공업체의 ISO 국가 코드를 가져옵니다.<br>
     *
     * @return ISO country code, or null if unavailable.<br><br>
     *         ISO 국가 코드, 사용할 수 없는 경우 null.
     */
    @RequiresPermission(READ_PHONE_STATE)
    public fun getSimCountryIso(): String? = tryCatchSystemManager(null) {
        return telephonyManager.simCountryIso?.takeIf { it.isNotBlank() }
    }

    // =================================================
    // Phone Information / 전화 정보
    // =================================================

    /**
     * Gets the phone number of the default SIM.<br><br>
     * 기본 SIM의 전화번호를 가져옵니다.<br>
     *
     * Note: This may return null or empty string depending on SIM configuration.<br><br>
     * 참고: SIM 구성에 따라 null이나 빈 문자열을 반환할 수 있습니다.<br>
     *
     * @return Phone number, or null if unavailable.<br><br>
     *         전화번호, 사용할 수 없는 경우 null.
     */
    @SuppressLint("HardwareIds")
    @RequiresPermission(anyOf = [READ_PHONE_STATE, READ_PHONE_NUMBERS])
    public fun getPhoneNumber(): String? = tryCatchSystemManager(null) {
        @Suppress("DEPRECATION")
        return telephonyManager.line1Number?.takeIf { it.isNotBlank() }
    }

    /**
     * Gets the call state.<br><br>
     * 통화 상태를 가져옵니다.<br>
     *
     * @return Call state constant from TelephonyManager.<br><br>
     *         TelephonyManager의 통화 상태 상수.
     */
    public fun getCallState(): Int = tryCatchSystemManager(TelephonyManager.CALL_STATE_IDLE) {
        return telephonyManager.callState
    }

    // =================================================
    // Network Information / 네트워크 정보
    // =================================================

    /**
     * Gets the current network type.<br><br>
     * 현재 네트워크 타입을 가져옵니다.<br>
     *
     * @return Network type constant from TelephonyManager.<br><br>
     *         TelephonyManager의 네트워크 타입 상수.
     */
    @RequiresPermission(READ_PHONE_STATE)
    public fun getNetworkType(): Int = tryCatchSystemManager(TelephonyManager.NETWORK_TYPE_UNKNOWN) {
        return checkSdkVersion(
            Build.VERSION_CODES.R,
            positiveWork = { telephonyManager.dataNetworkType },
            negativeWork = {
                @Suppress("DEPRECATION")
                telephonyManager.networkType
            },
        )
    }

    /**
     * Gets the current data network type.<br><br>
     * 현재 데이터 네트워크 타입을 가져옵니다.<br>
     *
     * @return Data network type constant from TelephonyManager.<br><br>
     *         TelephonyManager의 데이터 네트워크 타입 상수.
     */
    @RequiresPermission(READ_PHONE_STATE)
    public fun getDataNetworkType(): Int = tryCatchSystemManager(TelephonyManager.NETWORK_TYPE_UNKNOWN) {
        return telephonyManager.dataNetworkType
    }

    /**
     * Checks if the device is roaming.<br><br>
     * 디바이스가 로밍 중인지 확인합니다.<br>
     *
     * @return `true` if roaming, `false` otherwise.<br><br>
     *         로밍 중이면 `true`, 그렇지 않으면 `false`.<br>
     */
    @RequiresPermission(READ_PHONE_STATE)
    public fun isNetworkRoaming(): Boolean = tryCatchSystemManager(false) {
        return telephonyManager.isNetworkRoaming
    }

    // =================================================
    // Multi-SIM Support / 멀티 SIM 지원
    // =================================================

    /**
     * Gets the number of active SIM cards.<br><br>
     * 활성화된 SIM 카드 수를 가져옵니다.<br>
     *
     * @return Number of active SIM cards.<br><br>
     *         활성화된 SIM 카드 수.
     */
    @RequiresPermission(READ_PHONE_STATE)
    public fun getActiveSimCount(): Int = tryCatchSystemManager(0) {
        return subscriptionManager.activeSubscriptionInfoCount
    }

    /**
     * Gets active subscription info list.<br><br>
     * 활성 구독 정보 목록을 가져옵니다.<br>
     *
     * @return List of SubscriptionInfo.<br><br>
     *         SubscriptionInfo 목록.
     */
    @RequiresPermission(READ_PHONE_STATE)
    public fun getActiveSubscriptionInfoList(): List<SubscriptionInfo> = tryCatchSystemManager(emptyList()) {
        return subscriptionManager.activeSubscriptionInfoList ?: emptyList()
    }

    /**
     * Gets subscription info for the default data SIM.<br><br>
     * 기본 데이터 SIM의 구독 정보를 가져옵니다.<br>
     *
     * @return SubscriptionInfo, or null if unavailable.<br><br>
     *         SubscriptionInfo, 사용할 수 없는 경우 null.
     */
    @RequiresPermission(READ_PHONE_STATE)
    public fun getDefaultDataSubscriptionInfo(): SubscriptionInfo? = tryCatchSystemManager(null) {
        return checkSdkVersion(
            Build.VERSION_CODES.R,
            positiveWork = { telephonyManager.subscriptionId },
            negativeWork = { getActiveSubscriptionInfoList().firstOrNull()?.subscriptionId },
        )?.let { subscriptionManager.getActiveSubscriptionInfo(it) }
    }

    // =================================================
    // Utility Methods / 유틸리티 메서드
    // =================================================

    /**
     * Gets network type as human-readable string.<br><br>
     * 네트워크 타입을 읽기 쉬운 문자열로 가져옵니다.<br>
     *
     * @return Network type string (e.g., "LTE", "5G NR").<br><br>
     *         네트워크 타입 문자열 (예: "LTE", "5G NR").
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
        20 -> "5G NR" // NETWORK_TYPE_NR
        19 -> "LTE_CA" // NETWORK_TYPE_LTE_CA
        else -> "UNKNOWN"
    }

    /**
     * Gets SIM state as human-readable string.<br><br>
     * SIM 상태를 읽기 쉬운 문자열로 가져옵니다.<br>
     *
     * @return SIM state string (e.g., "READY", "ABSENT").<br><br>
     *         SIM 상태 문자열 (예: "READY", "ABSENT").
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

    // =================================================
    // State Management (Simple API)
    // =================================================

    /**
     * StateFlow for signal strength updates.<br><br>
     * 신호 강도 업데이트를 위한 StateFlow입니다.<br>
     */
    public val currentSignalStrength: StateFlow<SignalStrength?>
        get() = callbackManager.currentSignalStrength

    /**
     * StateFlow for service state updates.<br><br>
     * 서비스 상태 업데이트를 위한 StateFlow입니다.<br>
     */
    public val currentServiceState: StateFlow<ServiceState?>
        get() = callbackManager.currentServiceState

    /**
     * StateFlow for network state updates.<br><br>
     * 네트워크 상태 업데이트를 위한 StateFlow입니다.<br>
     */
    public val currentNetworkState: StateFlow<TelephonyNetworkState?>
        get() = callbackManager.currentNetworkState

    // =================================================
    // Signal Information / 신호 정보
    // =================================================

    /**
     * Gets the current signal strength.<br><br>
     * 현재 신호 강도를 가져옵니다.<br>
     *
     * Note: This returns the cached value from callbacks. Register callback first.<br><br>
     * 참고: 콜백에서 캐시된 값을 반환합니다. 먼저 콜백을 등록하세요.<br>
     *
     * @return Current SignalStrength, or null if unavailable.<br><br>
     *         현재 SignalStrength, 사용할 수 없는 경우 null.
     */
    public fun getCurrentSignalStrength(): SignalStrength? = callbackManager.getCurrentSignalStrength()

    /**
     * Gets the current service state.<br><br>
     * 현재 서비스 상태를 가져옵니다.<br>
     *
     * @return Current ServiceState, or null if unavailable.<br><br>
     *         현재 ServiceState, 사용할 수 없는 경우 null.
     */
    public fun getCurrentServiceState(): ServiceState? = callbackManager.getCurrentServiceState()

    // =================================================
    // Callback Management (Simple API)
    // =================================================

    /**
     * Registers telephony callback for real-time updates.<br><br>
     * 실시간 업데이트를 위한 telephony 콜백을 등록합니다.<br>
     *
     * @param handler Handler for callback execution (null for main thread).<br><br>
     *                콜백 실행을 위한 핸들러 (null이면 메인 스레드).
     * @param onSignalStrengthChanged Called when signal strength changes.<br><br>
     *                                신호 강도 변경 시 호출.
     * @param onServiceStateChanged Called when service state changes.<br><br>
     *                              서비스 상태 변경 시 호출.
     * @param onNetworkStateChanged Called when network state changes.<br><br>
     *                              네트워크 상태 변경 시 호출.
     * @return `true` if registration successful, `false` otherwise.<br><br>
     *         등록에 성공하면 `true`, 그렇지 않으면 `false`.<br>
     */
    @RequiresPermission(READ_PHONE_STATE)
    public fun registerCallback(
        handler: Handler? = null,
        onSignalStrengthChanged: ((SignalStrength) -> Unit)? = null,
        onServiceStateChanged: ((ServiceState) -> Unit)? = null,
        onNetworkStateChanged: ((TelephonyNetworkState) -> Unit)? = null,
    ): Boolean = callbackManager.registerSimpleCallback(
        handler,
        onSignalStrengthChanged,
        onServiceStateChanged,
        onNetworkStateChanged,
    )

    /**
     * Unregisters telephony callback.<br><br>
     * telephony 콜백을 해제합니다.<br>
     *
     * @return `true` if unregistration successful, `false` otherwise.<br><br>
     *         해제에 성공하면 `true`, 그렇지 않으면 `false`.<br>
     */
    @SuppressLint("MissingPermission")
    public fun unregisterCallback(): Boolean = callbackManager.unregisterSimpleCallback()

    // =================================================
    // Advanced Multi-SIM Callback System (API 31+)
    // =================================================

    /**
     * Register telephony callback for default SIM (API 31+).<br><br>
     * 기본 SIM에 대한 Telephony 콜백을 등록합니다 (API 31+).<br>
     *
     * @param executor Executor for callback execution.<br><br>
     *                 콜백 실행을 위한 Executor.
     * @param isGpsOn Whether GPS is on.<br><br>
     *                GPS 켜짐 여부.
     * @return `true` if registration successful, `false` otherwise.<br><br>
     *         등록에 성공하면 `true`, 그렇지 않으면 `false`.<br>
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
        onTelephonyNetworkState: ((telephonyNetworkState: TelephonyNetworkState) -> Unit)? = null,
    ): Boolean = callbackManager.registerAdvancedCallbackFromDefaultUSim(
        executor,
        isGpsOn,
        onActiveDataSubId,
        onDataConnectionState,
        onCellInfo,
        onSignalStrength,
        onServiceState,
        onCallState,
        onDisplayInfo,
        onTelephonyNetworkState,
    )

    /**
     * Register telephony callback for specific SIM slot (API 31+).<br><br>
     * 특정 SIM 슬롯에 대한 Telephony 콜백을 등록합니다 (API 31+).<br>
     *
     * @param simSlotIndex SIM slot index.<br><br>
     *                     SIM 슬롯 인덱스.
     * @param executor Executor for callback execution.<br><br>
     *                 콜백 실행을 위한 Executor.
     * @param isGpsOn Whether GPS is on.<br><br>
     *                GPS 켜짐 여부.
     * @return `true` if registration successful, `false` otherwise.<br><br>
     *         등록에 성공하면 `true`, 그렇지 않으면 `false`.<br>
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
        onTelephonyNetworkState: ((telephonyNetworkState: TelephonyNetworkState) -> Unit)? = null,
    ) = callbackManager.registerAdvancedCallback(
        simSlotIndex,
        executor,
        isGpsOn,
        onActiveDataSubId,
        onDataConnectionState,
        onCellInfo,
        onSignalStrength,
        onServiceState,
        onCallState,
        onDisplayInfo,
        onTelephonyNetworkState,
    )

    /**
     * Unregister callback for specific SIM slot (API 31+).<br><br>
     * 특정 SIM 슬롯의 콜백을 해제합니다 (API 31+).<br>
     *
     * @param simSlotIndex SIM slot index.<br><br>
     *                     SIM 슬롯 인덱스.
     */
    @RequiresApi(Build.VERSION_CODES.S)
    public fun unregisterCallBack(simSlotIndex: Int) = callbackManager.unregisterAdvancedCallback(simSlotIndex)

    // =================================================
    // Individual Callback Setters (Advanced API)
    // =================================================

    /**
     * Set signal strength callback.<br><br>
     * 신호 강도 콜백을 설정합니다.<br>
     *
     * @param simSlotIndex SIM slot index.<br><br>
     *                     SIM 슬롯 인덱스.
     * @param onSignalStrength Callback function.<br><br>
     *                         콜백 함수.
     */
    @RequiresPermission(READ_PHONE_STATE)
    public fun setOnSignalStrength(
        simSlotIndex: Int,
        onSignalStrength: ((currentSignalStrength: CurrentSignalStrength) -> Unit)? = null,
    ) = callbackManager.setOnSignalStrength(simSlotIndex, onSignalStrength)

    /**
     * Set service state callback.<br><br>
     * 서비스 상태 콜백을 설정합니다.<br>
     *
     * @param simSlotIndex SIM slot index.<br><br>
     *                     SIM 슬롯 인덱스.
     * @param onServiceState Callback function.<br><br>
     *                       콜백 함수.
     */
    @RequiresPermission(READ_PHONE_STATE)
    public fun setOnServiceState(
        simSlotIndex: Int,
        onServiceState: ((currentServiceState: CurrentServiceState) -> Unit)? = null,
    ) = callbackManager.setOnServiceState(simSlotIndex, onServiceState)

    /**
     * Set active data subscription ID callback.<br><br>
     * 활성 데이터 구독 ID 콜백을 설정합니다.<br>
     *
     * @param simSlotIndex SIM slot index.<br><br>
     *                     SIM 슬롯 인덱스.
     * @param onActiveDataSubId Callback function.<br><br>
     *                          콜백 함수.
     */
    @RequiresPermission(READ_PHONE_STATE)
    public fun setOnActiveDataSubId(
        simSlotIndex: Int,
        onActiveDataSubId: ((subId: Int) -> Unit)? = null,
    ) = callbackManager.setOnActiveDataSubId(simSlotIndex, onActiveDataSubId)

    /**
     * Set data connection state callback.<br><br>
     * 데이터 연결 상태 콜백을 설정합니다.<br>
     *
     * @param simSlotIndex SIM slot index.<br><br>
     *                     SIM 슬롯 인덱스.
     * @param onDataConnectionState Callback function.<br><br>
     *                              콜백 함수.
     */
    @RequiresPermission(READ_PHONE_STATE)
    public fun setOnDataConnectionState(
        simSlotIndex: Int,
        onDataConnectionState: ((state: Int, networkType: Int) -> Unit)? = null,
    ) = callbackManager.setOnDataConnectionState(simSlotIndex, onDataConnectionState)

    /**
     * Set cell info callback.<br><br>
     * 셀 정보 콜백을 설정합니다.<br>
     *
     * @param simSlotIndex SIM slot index.<br><br>
     *                     SIM 슬롯 인덱스.
     * @param onCellInfo Callback function.<br><br>
     *                   콜백 함수.
     */
    @RequiresPermission(READ_PHONE_STATE)
    public fun setOnCellInfo(
        simSlotIndex: Int,
        onCellInfo: ((currentCellInfo: CurrentCellInfo) -> Unit)? = null,
    ) = callbackManager.setOnCellInfo(simSlotIndex, onCellInfo)

    /**
     * Set call state callback.<br><br>
     * 통화 상태 콜백을 설정합니다.<br>
     *
     * @param simSlotIndex SIM slot index.<br><br>
     *                     SIM 슬롯 인덱스.
     * @param onCallState Callback function.<br><br>
     *                    콜백 함수.
     */
    @RequiresPermission(READ_PHONE_STATE)
    public fun setOnCallState(
        simSlotIndex: Int,
        onCallState: ((callState: Int, phoneNumber: String?) -> Unit)? = null,
    ) = callbackManager.setOnCallState(simSlotIndex, onCallState)

    /**
     * Set display info callback.<br><br>
     * 디스플레이 정보 콜백을 설정합니다.<br>
     *
     * @param simSlotIndex SIM slot index.<br><br>
     *                     SIM 슬롯 인덱스.
     * @param onDisplay Callback function.<br><br>
     *                  콜백 함수.
     */
    @RequiresPermission(READ_PHONE_STATE)
    public fun setOnDisplayState(
        simSlotIndex: Int,
        onDisplay: ((telephonyDisplayInfo: TelephonyDisplayInfo) -> Unit)? = null,
    ) = callbackManager.setOnDisplayState(simSlotIndex, onDisplay)

    /**
     * Set telephony network type callback.<br><br>
     * 통신망 타입 콜백을 설정합니다.<br>
     *
     * @param simSlotIndex SIM slot index.<br><br>
     *                     SIM 슬롯 인덱스.
     * @param onTelephonyNetworkType Callback function.<br><br>
     *                               콜백 함수.
     */
    public fun setOnTelephonyNetworkType(
        simSlotIndex: Int,
        onTelephonyNetworkType: ((telephonyNetworkState: TelephonyNetworkState) -> Unit)? = null,
    ) = callbackManager.setOnTelephonyNetworkType(simSlotIndex, onTelephonyNetworkType)

    // =================================================
    // Utility Methods (Advanced API)
    // =================================================

    /**
     * Check callback registration status.<br><br>
     * 콜백 등록 상태를 확인합니다.<br>
     *
     * @param simSlotIndex SIM slot index.<br><br>
     *                     SIM 슬롯 인덱스.
     * @return `true` if registered, `false` otherwise.<br><br>
     *         등록되어 있으면 `true`, 그렇지 않으면 `false`.<br>
     */
    public fun isRegistered(simSlotIndex: Int): Boolean = callbackManager.isRegistered(simSlotIndex)

    /**
     * Get TelephonyManager for specific SIM slot.<br><br>
     * 특정 SIM 슬롯의 TelephonyManager를 반환합니다.<br>
     *
     * @param slotIndex SIM slot index.<br><br>
     *                  SIM 슬롯 인덱스.
     * @return TelephonyManager for the slot, or null if unavailable.<br><br>
     *         해당 슬롯의 TelephonyManager, 사용할 수 없는 경우 null.
     */
    public fun getTelephonyManagerFromUSim(slotIndex: Int): TelephonyManager? = callbackManager.getTelephonyManagerFromUSim(slotIndex)

    // =================================================
    // Cleanup / 정리
    // =================================================

    override fun onDestroy() {
        try {
            callbackManager.onDestroy()
        } finally {
            super.onDestroy()
        }
    }
}
