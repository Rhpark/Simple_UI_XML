package kr.open.library.simple_ui.core.system_manager.info.network.telephony.callback

import android.Manifest.permission.ACCESS_FINE_LOCATION
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
import android.util.SparseArray
import android.util.SparseBooleanArray
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.core.util.forEach
import androidx.core.util.set
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kr.open.library.simple_ui.core.extensions.conditional.checkSdkVersion
import kr.open.library.simple_ui.core.logcat.Logx
import kr.open.library.simple_ui.core.permissions.extentions.hasPermissions
import kr.open.library.simple_ui.core.system_manager.base.BaseSystemService
import kr.open.library.simple_ui.core.system_manager.extensions.getTelephonyManager
import kr.open.library.simple_ui.core.system_manager.info.network.telephony.data.current.CurrentCellInfo
import kr.open.library.simple_ui.core.system_manager.info.network.telephony.data.current.CurrentServiceState
import kr.open.library.simple_ui.core.system_manager.info.network.telephony.data.current.CurrentSignalStrength
import kr.open.library.simple_ui.core.system_manager.info.network.telephony.data.state.TelephonyNetworkState
import java.util.concurrent.Executor

/**
 * TelephonyCallbackManager - Telephony 콜백 관리 클래스
 * Telephony Callback Management Class
 *
 * 이 클래스는 TelephonyManager의 콜백 시스템을 관리합니다.
 * This class manages TelephonyManager's callback system.
 *
 * 주요 기능 / Main Features:
 * - Simple callback API (기본 SIM, 3개 콜백)
 * - Advanced callback API (슬롯 선택, 8개 콜백)
 * - Multi-SIM support / 멀티 SIM 지원
 * - StateFlow integration / StateFlow 통합
 * - API compatibility handling / API 호환성 처리
 *
 * 필수 권한 / Required Permissions:
 * - android.permission.READ_PHONE_STATE (필수/Required)
 * - android.permission.ACCESS_FINE_LOCATION (셀 정보/Cell info)
 *
 * 사용 예시 / Usage Example:
 * ```
 * val callbackManager = TelephonyCallbackManager(context)
 *
 * // Simple API
 * callbackManager.registerSimpleCallback(
 *     onSignalStrengthChanged = { signal -> ... }
 * )
 *
 * // Advanced API (API 31+)
 * callbackManager.registerAdvancedCallback(
 *     simSlotIndex = 0,
 *     onCellInfo = { info -> ... }
 * )
 * ```
 */
public class TelephonyCallbackManager(context: Context) :
    BaseSystemService(context, listOf(READ_PHONE_STATE, ACCESS_FINE_LOCATION)) {

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

    /**
     * CommonTelephonyCallback instance for unified callback handling
     * 통합된 콜백 처리를 위한 CommonTelephonyCallback 인스턴스
     */
    private val telephonyCallback by lazy { CommonTelephonyCallback(telephonyManager) }

    // =================================================
    // Multi-SIM Callback Management / 멀티 SIM 콜백 관리
    // =================================================

    /**
     * SIM 슬롯별 TelephonyManager 인스턴스 저장
     * Stores TelephonyManager instances per SIM slot
     */
    private val uSimTelephonyManagerList = SparseArray<TelephonyManager>()

    /**
     * SIM 슬롯별 CommonTelephonyCallback 인스턴스 저장
     * Stores CommonTelephonyCallback instances per SIM slot
     */
    private val uSimTelephonyCallbackList = SparseArray<CommonTelephonyCallback>()

    /**
     * 슬롯별 콜백 등록 상태
     * Per-slot callback registration status
     */
    private val isRegistered = SparseBooleanArray()

    // =================================================
    // State Management (Simple API용)
    // =================================================

    private val _currentSignalStrength = MutableStateFlow<SignalStrength?>(null)
    public val currentSignalStrength: StateFlow<SignalStrength?> = _currentSignalStrength.asStateFlow()

    private val _currentServiceState = MutableStateFlow<ServiceState?>(null)
    public val currentServiceState: StateFlow<ServiceState?> = _currentServiceState.asStateFlow()

    private val _currentNetworkState = MutableStateFlow<TelephonyNetworkState?>(null)
    public val currentNetworkState: StateFlow<TelephonyNetworkState?> = _currentNetworkState.asStateFlow()

    private var isCallbackRegistered = false

    // =================================================
    // Multi-SIM Initialization / 멀티 SIM 초기화
    // =================================================

    init {
        initializeMultiSimSupport()
    }

    /**
     * 멀티 SIM 지원 초기화
     * Initialize multi-SIM support
     */
    @SuppressLint("MissingPermission")
    private fun initializeMultiSimSupport() {
        if (!context.hasPermissions(READ_PHONE_STATE)) {
            Logx.e("Permissions denied. Cannot initialize multi-SIM support")
            return
        }
        try {
            updateUSimTelephonyManagerList()
        } catch (e: SecurityException) {
            Logx.w("TelephonyCallbackManager: Permission required for multi-SIM initialization", e)
        } catch (e: Exception) {
            Logx.e("TelephonyCallbackManager: Error during multi-SIM initialization", e)
        }
    }

    /**
     * SIM 슬롯별 TelephonyManager 목록 업데이트
     * Update TelephonyManager list per SIM slot
     */
    @RequiresPermission(READ_PHONE_STATE)
    private fun updateUSimTelephonyManagerList() {
        val subscriptionInfoList = getActiveSubscriptionInfoListInternal()
        Logx.d("TelephonyCallbackManager: activeSubscriptionInfoList size ${subscriptionInfoList.size}")

        subscriptionInfoList.forEach { subscriptionInfo ->
            Logx.d("TelephonyCallbackManager: SubID $subscriptionInfo")
            val slotIndex = subscriptionInfo.simSlotIndex
            uSimTelephonyManagerList[slotIndex] = telephonyManager.createForSubscriptionId(subscriptionInfo.subscriptionId)
            uSimTelephonyCallbackList[slotIndex] = CommonTelephonyCallback(uSimTelephonyManagerList[slotIndex])
        }
    }

    /**
     * 활성화된 구독 정보 목록 반환
     * Get active subscription info list
     */
    @RequiresPermission(READ_PHONE_STATE)
    private fun getActiveSubscriptionInfoListInternal(): List<SubscriptionInfo> =
        tryCatchSystemManager(emptyList()) {
            subscriptionManager.activeSubscriptionInfoList ?: emptyList()
        }

    // =================================================
    // Simple Callback API (기본 SIM, StateFlow 통합)
    // =================================================

    /**
     * Registers simple telephony callback for real-time updates (기본 SIM용)
     * 실시간 업데이트를 위한 간단한 telephony 콜백을 등록합니다.
     *
     * Features / 특징:
     * - 기본 SIM에 대해서만 동작 / Works only for default SIM
     * - 3개의 핵심 콜백 제공 / Provides 3 core callbacks
     * - StateFlow 자동 업데이트 / Auto-updates StateFlow
     * - API 레벨 자동 처리 / Auto-handles API level
     */
    @RequiresPermission(READ_PHONE_STATE)
    public fun registerSimpleCallback(
        handler: Handler? = null,
        onSignalStrengthChanged: ((SignalStrength) -> Unit)? = null,
        onServiceStateChanged: ((ServiceState) -> Unit)? = null,
        onNetworkStateChanged: ((TelephonyNetworkState) -> Unit)? = null
    ): Boolean = tryCatchSystemManager(false) {
        checkSdkVersion(Build.VERSION_CODES.S,
            positiveWork = {
                registerModernCallback(handler, onSignalStrengthChanged, onServiceStateChanged, onNetworkStateChanged)
                true
            },
            negativeWork = {
                registerLegacyCallback(onSignalStrengthChanged, onServiceStateChanged, onNetworkStateChanged)
                true
            }
        )
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun registerModernCallback(
        handler: Handler?,
        onSignalStrengthChanged: ((SignalStrength) -> Unit)?,
        onServiceStateChanged: ((ServiceState) -> Unit)?,
        onNetworkStateChanged: ((TelephonyNetworkState) -> Unit)?
    ) {
        setupCallbackListeners(onSignalStrengthChanged, onServiceStateChanged, onNetworkStateChanged)

        val executor = handler?.let { h -> Executor { h.post(it) } } ?: context.mainExecutor

        telephonyManager.registerTelephonyCallback(
            executor,
            telephonyCallback.baseTelephonyCallback
        )

        isCallbackRegistered = true
        Logx.d("TelephonyCallbackManager: Modern callback registered")
    }

    @Suppress("DEPRECATION")
    private fun registerLegacyCallback(
        onSignalStrengthChanged: ((SignalStrength) -> Unit)?,
        onServiceStateChanged: ((ServiceState) -> Unit)?,
        onNetworkStateChanged: ((TelephonyNetworkState) -> Unit)?
    ) {
        setupCallbackListeners(onSignalStrengthChanged, onServiceStateChanged, onNetworkStateChanged)

        val events = android.telephony.PhoneStateListener.LISTEN_SIGNAL_STRENGTHS or
                android.telephony.PhoneStateListener.LISTEN_SERVICE_STATE or
                android.telephony.PhoneStateListener.LISTEN_DATA_CONNECTION_STATE

        telephonyManager.listen(telephonyCallback.basePhoneStateListener, events)
        isCallbackRegistered = true
        Logx.d("TelephonyCallbackManager: Legacy callback registered")
    }

    private fun setupCallbackListeners(
        onSignalStrengthChanged: ((SignalStrength) -> Unit)?,
        onServiceStateChanged: ((ServiceState) -> Unit)?,
        onNetworkStateChanged: ((TelephonyNetworkState) -> Unit)?
    ) {
        telephonyCallback.setOnSignalStrength { currentSignalStrength ->
            _currentSignalStrength.value = currentSignalStrength.signalStrength
            currentSignalStrength.signalStrength?.let { onSignalStrengthChanged?.invoke(it) }
        }

        telephonyCallback.setOnServiceState { currentServiceState ->
            _currentServiceState.value = currentServiceState.serviceState
            currentServiceState.serviceState?.let { onServiceStateChanged?.invoke(it) }
        }

        telephonyCallback.setOnTelephonyNetworkType { networkState ->
            _currentNetworkState.value = networkState
            onNetworkStateChanged?.invoke(networkState)
        }
    }

    /**
     * Unregisters simple telephony callback.
     * 간단한 telephony 콜백을 해제합니다.
     */
    @SuppressLint("MissingPermission")
    public fun unregisterSimpleCallback(): Boolean = tryCatchSystemManager(false) {
        if (!isCallbackRegistered) {
            Logx.w("TelephonyCallbackManager: No callback registered")
            return@tryCatchSystemManager false
        }

        checkSdkVersion(Build.VERSION_CODES.S,
            positiveWork = {
                telephonyManager.unregisterTelephonyCallback(telephonyCallback.baseTelephonyCallback)
            },
            negativeWork = {
                @Suppress("DEPRECATION")
                telephonyManager.listen(
                    telephonyCallback.basePhoneStateListener,
                    android.telephony.PhoneStateListener.LISTEN_NONE
                )
            }
        )

        isCallbackRegistered = false
        Logx.d("TelephonyCallbackManager: Callback unregistered")
        true
    }

    // =================================================
    // Advanced Callback API (멀티 SIM, 상세 콜백)
    // =================================================

    /**
     * 기본 SIM에 대한 고급 Telephony 콜백 등록 (API 31+)
     * Register advanced telephony callback for default SIM (API 31+)
     */
    @RequiresApi(Build.VERSION_CODES.S)
    @RequiresPermission(READ_PHONE_STATE)
    public fun registerAdvancedCallbackFromDefaultUSim(
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
    ): Boolean = tryCatchSystemManager(false) {
        val subscriptionInfoList = getActiveSubscriptionInfoListInternal()
        val defaultSim = subscriptionInfoList.firstOrNull() ?: throw IllegalStateException("No default SIM found")

        registerAdvancedCallback(
            defaultSim.simSlotIndex, executor, isGpsOn, onActiveDataSubId,
            onDataConnectionState, onCellInfo, onSignalStrength, onServiceState,
            onCallState, onDisplayInfo, onTelephonyNetworkState
        )
        true
    }

    /**
     * 특정 SIM 슬롯에 대한 고급 Telephony 콜백 등록 (API 31+)
     * Register advanced telephony callback for specific SIM slot (API 31+)
     */
    @RequiresApi(Build.VERSION_CODES.S)
    @RequiresPermission(READ_PHONE_STATE)
    public fun registerAdvancedCallback(
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
    ) {
        val tm = uSimTelephonyManagerList[simSlotIndex]
            ?: throw IllegalStateException("TelephonyManager [$simSlotIndex] is null")
        val callback = uSimTelephonyCallbackList[simSlotIndex]
            ?: throw IllegalStateException("telephonyCallbackList [$simSlotIndex] is null")

        unregisterAdvancedCallback(simSlotIndex)

        if (isGpsOn) {
            tm.registerTelephonyCallback(executor, callback.baseGpsTelephonyCallback)
        } else {
            tm.registerTelephonyCallback(executor, callback.baseTelephonyCallback)
        }

        setupSlotCallbackListeners(simSlotIndex, onActiveDataSubId, onDataConnectionState,
            onCellInfo, onSignalStrength, onServiceState, onCallState, onDisplayInfo, onTelephonyNetworkState)
        isRegistered[simSlotIndex] = true
    }

    /**
     * 슬롯별 콜백 리스너 설정
     * Setup callback listeners for specific slot
     */
    @RequiresPermission(READ_PHONE_STATE)
    private fun setupSlotCallbackListeners(
        simSlotIndex: Int,
        onActiveDataSubId: ((subId: Int) -> Unit)?,
        onDataConnectionState: ((state: Int, networkType: Int) -> Unit)?,
        onCellInfo: ((currentCellInfo: CurrentCellInfo) -> Unit)?,
        onSignalStrength: ((currentSignalStrength: CurrentSignalStrength) -> Unit)?,
        onServiceState: ((currentServiceState: CurrentServiceState) -> Unit)?,
        onCallState: ((callState: Int, phoneNumber: String?) -> Unit)?,
        onDisplayInfo: ((telephonyDisplayInfo: TelephonyDisplayInfo) -> Unit)?,
        onTelephonyNetworkState: ((telephonyNetworkState: TelephonyNetworkState) -> Unit)?
    ) {
        setOnActiveDataSubId(simSlotIndex, onActiveDataSubId)
        setOnDataConnectionState(simSlotIndex, onDataConnectionState)
        setOnCellInfo(simSlotIndex, onCellInfo)
        setOnSignalStrength(simSlotIndex, onSignalStrength)
        setOnServiceState(simSlotIndex, onServiceState)
        setOnCallState(simSlotIndex, onCallState)
        setOnDisplayState(simSlotIndex, onDisplayInfo)
        setOnTelephonyNetworkType(simSlotIndex, onTelephonyNetworkState)
    }

    /**
     * 특정 SIM 슬롯의 콜백 해제 (API 31+)
     * Unregister callback for specific SIM slot (API 31+)
     */
    @RequiresApi(Build.VERSION_CODES.S)
    public fun unregisterAdvancedCallback(simSlotIndex: Int) {
        val tm = uSimTelephonyManagerList[simSlotIndex]
        val callback = uSimTelephonyCallbackList[simSlotIndex]

        if (callback == null || tm == null) {
            Logx.w("TelephonyCallbackManager: telephonyCallbackList[$simSlotIndex] is null")
            return
        }

        try {
            tm.unregisterTelephonyCallback(callback.baseTelephonyCallback)
        } catch (e: SecurityException) {
            Logx.w("TelephonyCallbackManager: Permission issue during unregistering callback", e)
        } catch (e: IllegalArgumentException) {
            Logx.w("TelephonyCallbackManager: Invalid callback provided", e)
        }

        try {
            tm.unregisterTelephonyCallback(callback.baseGpsTelephonyCallback)
        } catch (e: SecurityException) {
            Logx.w("TelephonyCallbackManager: Permission issue during unregistering callback", e)
        } catch (e: IllegalArgumentException) {
            Logx.w("TelephonyCallbackManager: Invalid callback provided", e)
        }

        isRegistered[simSlotIndex] = false
    }

    // =================================================
    // Individual Callback Setters (Advanced API용)
    // =================================================

    /**
     * 신호 강도 콜백 설정
     * Set signal strength callback
     */
    @RequiresPermission(READ_PHONE_STATE)
    public fun setOnSignalStrength(simSlotIndex: Int, onSignalStrength: ((currentSignalStrength: CurrentSignalStrength) -> Unit)? = null) {
        uSimTelephonyCallbackList[simSlotIndex]?.setOnSignalStrength(onSignalStrength)
            ?: Logx.w("TelephonyCallbackManager: setOnSignalStrength telephonyCallbackList[$simSlotIndex] is null")
    }

    /**
     * 서비스 상태 콜백 설정
     * Set service state callback
     */
    @RequiresPermission(READ_PHONE_STATE)
    public fun setOnServiceState(simSlotIndex: Int, onServiceState: ((currentServiceState: CurrentServiceState) -> Unit)? = null) {
        uSimTelephonyCallbackList[simSlotIndex]?.setOnServiceState(onServiceState)
            ?: Logx.w("TelephonyCallbackManager: setOnServiceState telephonyCallbackList[$simSlotIndex] is null")
    }

    /**
     * 활성 데이터 구독 ID 콜백 설정
     * Set active data subscription ID callback
     */
    @RequiresPermission(READ_PHONE_STATE)
    public fun setOnActiveDataSubId(simSlotIndex: Int, onActiveDataSubId: ((subId: Int) -> Unit)? = null) {
        uSimTelephonyCallbackList[simSlotIndex]?.setOnActiveDataSubId(onActiveDataSubId)
            ?: Logx.w("TelephonyCallbackManager: setOnActiveDataSubId telephonyCallbackList[$simSlotIndex] is null")
    }

    /**
     * 데이터 연결 상태 콜백 설정
     * Set data connection state callback
     */
    @RequiresPermission(READ_PHONE_STATE)
    public fun setOnDataConnectionState(simSlotIndex: Int, onDataConnectionState: ((state: Int, networkType: Int) -> Unit)? = null) {
        uSimTelephonyCallbackList[simSlotIndex]?.setOnDataConnectionState(onDataConnectionState)
            ?: Logx.w("TelephonyCallbackManager: setOnDataConnectionState telephonyCallbackList[$simSlotIndex] is null")
    }

    /**
     * 셀 정보 콜백 설정
     * Set cell info callback
     */
    @RequiresPermission(READ_PHONE_STATE)
    public fun setOnCellInfo(simSlotIndex: Int, onCellInfo: ((currentCellInfo: CurrentCellInfo) -> Unit)? = null) {
        uSimTelephonyCallbackList[simSlotIndex]?.setOnCellInfo(onCellInfo)
            ?: Logx.w("TelephonyCallbackManager: setOnCellInfo telephonyCallbackList[$simSlotIndex] is null")
    }

    /**
     * 통화 상태 콜백 설정
     * Set call state callback
     */
    @RequiresPermission(READ_PHONE_STATE)
    public fun setOnCallState(simSlotIndex: Int, onCallState: ((callState: Int, phoneNumber: String?) -> Unit)? = null) {
        uSimTelephonyCallbackList[simSlotIndex]?.setOnCallState(onCallState)
            ?: Logx.w("TelephonyCallbackManager: setOnCallState telephonyCallbackList[$simSlotIndex] is null")
    }

    /**
     * 디스플레이 정보 콜백 설정
     * Set display info callback
     */
    @RequiresPermission(READ_PHONE_STATE)
    public fun setOnDisplayState(simSlotIndex: Int, onDisplay: ((telephonyDisplayInfo: TelephonyDisplayInfo) -> Unit)? = null) {
        uSimTelephonyCallbackList[simSlotIndex]?.setOnDisplay(onDisplay)
            ?: Logx.w("TelephonyCallbackManager: setOnDisplayState telephonyCallbackList[$simSlotIndex] is null")
    }

    /**
     * 통신망 타입 콜백 설정
     * Set telephony network type callback
     */
    public fun setOnTelephonyNetworkType(simSlotIndex: Int, onTelephonyNetworkType: ((telephonyNetworkState: TelephonyNetworkState) -> Unit)? = null) {
        uSimTelephonyCallbackList[simSlotIndex]?.setOnTelephonyNetworkType(onTelephonyNetworkType)
            ?: Logx.w("TelephonyCallbackManager: setOnTelephonyNetworkType telephonyCallbackList[$simSlotIndex] is null")
    }

    /**
     * 콜백 등록 상태 확인
     * Check callback registration status
     */
    public fun isRegistered(simSlotIndex: Int): Boolean = isRegistered[simSlotIndex]

    /**
     * 특정 SIM 슬롯의 TelephonyManager 반환
     * Get TelephonyManager for specific SIM slot
     */
    public fun getTelephonyManagerFromUSim(slotIndex: Int): TelephonyManager? =
        uSimTelephonyManagerList[slotIndex]

    // =================================================
    // Signal/Service State Getters (Simple API용)
    // =================================================

    /**
     * Gets the current signal strength (cached from callbacks).
     * 현재 신호 강도를 가져옵니다 (콜백에서 캐시된 값).
     */
    public fun getCurrentSignalStrength(): SignalStrength? = _currentSignalStrength.value

    /**
     * Gets the current service state (cached from callbacks).
     * 현재 서비스 상태를 가져옵니다 (콜백에서 캐시된 값).
     */
    public fun getCurrentServiceState(): ServiceState? = _currentServiceState.value

    // =================================================
    // Cleanup / 정리
    // =================================================

    override fun onDestroy() {
        try {
            // 기본 콜백 해제
            if (isCallbackRegistered) {
                unregisterSimpleCallback()
            }

            // 멀티 SIM 콜백들 해제
            uSimTelephonyCallbackList.forEach { key: Int, _: CommonTelephonyCallback ->
                checkSdkVersion(Build.VERSION_CODES.S) {
                    try {
                        unregisterAdvancedCallback(key)
                    } catch (e: Exception) {
                        Logx.w("TelephonyCallbackManager: Error unregistering callback for slot $key", e)
                    }
                }
            }

            Logx.d("TelephonyCallbackManager destroyed")
        } catch (e: Exception) {
            Logx.e("Error during TelephonyCallbackManager cleanup: ${e.message}")
        } finally {
            super.onDestroy()
        }
    }
}
