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
 * Telephony Callback Management Class.<br><br>
 * Telephony 콜백 관리 클래스입니다.<br>
 *
 * This class manages TelephonyManager's callback system.<br><br>
 * 이 클래스는 TelephonyManager의 콜백 시스템을 관리합니다.<br>
 *
 * Main Features:<br>
 * - Simple callback API (Default SIM, 3 callbacks)<br>
 * - Advanced callback API (Slot selection, 8 callbacks)<br>
 * - Multi-SIM support<br>
 * - StateFlow integration<br>
 * - API compatibility handling<br><br>
 * 주요 기능:<br>
 * - Simple callback API (기본 SIM, 3개 콜백)<br>
 * - Advanced callback API (슬롯 선택, 8개 콜백)<br>
 * - 멀티 SIM 지원<br>
 * - StateFlow 통합<br>
 * - API 호환성 처리<br>
 *
 * Required Permissions:<br>
 * - `android.permission.READ_PHONE_STATE` (Required)<br>
 * - `android.permission.ACCESS_FINE_LOCATION` (Cell info)<br><br>
 * 필수 권한:<br>
 * - `android.permission.READ_PHONE_STATE` (필수)<br>
 * - `android.permission.ACCESS_FINE_LOCATION` (셀 정보)<br>
 *
 * Usage Example:<br>
 * ```kotlin
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
public class TelephonyCallbackManager(
    context: Context,
) : BaseSystemService(context, listOf(READ_PHONE_STATE, ACCESS_FINE_LOCATION)) {
    // =================================================
    // Core Components
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
     * CommonTelephonyCallback instance for unified callback handling.<br><br>
     * 통합된 콜백 처리를 위한 CommonTelephonyCallback 인스턴스입니다.<br>
     */
    private val telephonyCallback by lazy { CommonTelephonyCallback(telephonyManager) }

    // =================================================
    // Multi-SIM Callback Management / 멀티 SIM 콜백 관리
    // =================================================

    /**
     * Stores TelephonyManager instances per SIM slot.<br><br>
     * SIM 슬롯별 TelephonyManager 인스턴스를 저장합니다.<br>
     */
    private val uSimTelephonyManagerList = SparseArray<TelephonyManager>()

    /**
     * Stores CommonTelephonyCallback instances per SIM slot.<br><br>
     * SIM 슬롯별 CommonTelephonyCallback 인스턴스를 저장합니다.<br>
     */
    private val uSimTelephonyCallbackList = SparseArray<CommonTelephonyCallback>()

    /**
     * Per-slot callback registration status.<br><br>
     * 슬롯별 콜백 등록 상태입니다.<br>
     */
    private val isRegistered = SparseBooleanArray()

    // =================================================
    // State Management (Simple API용)
    // =================================================

    private val _currentSignalStrength = MutableStateFlow<SignalStrength?>(null)

    /**
     * StateFlow for current signal strength.<br><br>
     * 현재 신호 강도에 대한 StateFlow입니다.<br>
     */
    public val currentSignalStrength: StateFlow<SignalStrength?> = _currentSignalStrength.asStateFlow()

    private val _currentServiceState = MutableStateFlow<ServiceState?>(null)

    /**
     * StateFlow for current service state.<br><br>
     * 현재 서비스 상태에 대한 StateFlow입니다.<br>
     */
    public val currentServiceState: StateFlow<ServiceState?> = _currentServiceState.asStateFlow()

    private val _currentNetworkState = MutableStateFlow<TelephonyNetworkState?>(null)

    /**
     * StateFlow for current network state.<br><br>
     * 현재 네트워크 상태에 대한 StateFlow입니다.<br>
     */
    public val currentNetworkState: StateFlow<TelephonyNetworkState?> = _currentNetworkState.asStateFlow()

    /**
     * Flag indicating if the callback is currently registered.<br><br>
     * 콜백이 현재 등록되어 있는지 여부를 나타내는 플래그입니다.<br>
     */
    private var isCallbackRegistered = false

    // =================================================
    // Multi-SIM Initialization / 멀티 SIM 초기화
    // =================================================

    init {
        initializeMultiSimSupport()
    }

    /**
     * Initializes multi-SIM support.<br><br>
     * 멀티 SIM 지원을 초기화합니다.<br>
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
     * Updates TelephonyManager list per SIM slot.<br><br>
     * SIM 슬롯별 TelephonyManager 목록을 업데이트합니다.<br>
     */
    @RequiresPermission(READ_PHONE_STATE)
    private fun updateUSimTelephonyManagerList() {
        val subscriptionInfoList = getActiveSubscriptionInfoListInternal()
        Logx.d("TelephonyCallbackManager: activeSubscriptionInfoList size ${subscriptionInfoList.size}")

        subscriptionInfoList.forEach { subscriptionInfo ->
            Logx.d("TelephonyCallbackManager: SubID $subscriptionInfo")
            val slotIndex = subscriptionInfo.simSlotIndex
            uSimTelephonyManagerList[slotIndex] =
                telephonyManager.createForSubscriptionId(subscriptionInfo.subscriptionId)
            uSimTelephonyCallbackList[slotIndex] = CommonTelephonyCallback(uSimTelephonyManagerList[slotIndex])
        }
    }

    /**
     * Gets active subscription info list.<br><br>
     * 활성화된 구독 정보 목록을 반환합니다.<br>
     */
    @RequiresPermission(READ_PHONE_STATE)
    private fun getActiveSubscriptionInfoListInternal(): List<SubscriptionInfo> = tryCatchSystemManager(emptyList()) {
        return subscriptionManager.activeSubscriptionInfoList ?: emptyList()
    }

    // =================================================
    // Simple Callback API (기본 SIM, StateFlow 통합)
    // =================================================

    /**
     * Registers simple telephony callback for real-time updates (Default SIM).<br><br>
     * 실시간 업데이트를 위한 간단한 telephony 콜백을 등록합니다 (기본 SIM용).<br>
     *
     * Features:<br>
     * - Works only for default SIM<br>
     * - Provides 3 core callbacks<br>
     * - Auto-updates StateFlow<br>
     * - Auto-handles API level<br><br>
     * 특징:<br>
     * - 기본 SIM에 대해서만 동작<br>
     * - 3개의 핵심 콜백 제공<br>
     * - StateFlow 자동 업데이트<br>
     * - API 레벨 자동 처리<br>
     *
     * @param handler Handler for callback execution.<br><br>
     *                콜백 실행을 위한 핸들러.
     * @param onSignalStrengthChanged Callback for signal strength changes.<br><br>
     *                                신호 강도 변경 콜백.
     * @param onServiceStateChanged Callback for service state changes.<br><br>
     *                              서비스 상태 변경 콜백.
     * @param onNetworkStateChanged Callback for network state changes.<br><br>
     *                              네트워크 상태 변경 콜백.
     * @return `true` if successful, `false` otherwise.<br><br>
     *         성공 시 `true`, 그렇지 않으면 `false`.
     */
    @RequiresPermission(READ_PHONE_STATE)
    public fun registerSimpleCallback(
        handler: Handler? = null,
        onSignalStrengthChanged: ((SignalStrength) -> Unit)? = null,
        onServiceStateChanged: ((ServiceState) -> Unit)? = null,
        onNetworkStateChanged: ((TelephonyNetworkState) -> Unit)? = null,
    ): Boolean = tryCatchSystemManager(false) {
        checkSdkVersion(
            Build.VERSION_CODES.S,
            positiveWork = {
                registerModernCallback(
                    handler,
                    onSignalStrengthChanged,
                    onServiceStateChanged,
                    onNetworkStateChanged,
                )
                return true
            },
            negativeWork = {
                registerLegacyCallback(onSignalStrengthChanged, onServiceStateChanged, onNetworkStateChanged)
                return true
            },
        )
    }

    /**
     * Registers telephony callback for API 31+.<br><br>
     * API 31 이상을 위한 Telephony 콜백을 등록합니다.<br>
     *
     * @param handler Handler for callback execution.<br><br>
     *                콜백 실행을 위한 핸들러.
     * @param onSignalStrengthChanged Callback for signal strength changes.<br><br>
     *                                신호 강도 변경 콜백.
     * @param onServiceStateChanged Callback for service state changes.<br><br>
     *                              서비스 상태 변경 콜백.
     * @param onNetworkStateChanged Callback for network state changes.<br><br>
     *                              네트워크 상태 변경 콜백.
     */
    @RequiresApi(Build.VERSION_CODES.S)
    private fun registerModernCallback(
        handler: Handler?,
        onSignalStrengthChanged: ((SignalStrength) -> Unit)?,
        onServiceStateChanged: ((ServiceState) -> Unit)?,
        onNetworkStateChanged: ((TelephonyNetworkState) -> Unit)?,
    ) {
        setupCallbackListeners(onSignalStrengthChanged, onServiceStateChanged, onNetworkStateChanged)

        val executor = handler?.let { h -> Executor { h.post(it) } } ?: context.mainExecutor

        telephonyManager.registerTelephonyCallback(executor, telephonyCallback.baseTelephonyCallback)

        isCallbackRegistered = true
        Logx.d("TelephonyCallbackManager: Modern callback registered")
    }

    /**
     * Registers legacy PhoneStateListener for API < 31.<br><br>
     * API 31 미만을 위한 기존 PhoneStateListener를 등록합니다.<br>
     *
     * @param onSignalStrengthChanged Callback for signal strength changes.<br><br>
     *                                신호 강도 변경 콜백.
     * @param onServiceStateChanged Callback for service state changes.<br><br>
     *                              서비스 상태 변경 콜백.
     * @param onNetworkStateChanged Callback for network state changes.<br><br>
     *                              네트워크 상태 변경 콜백.
     */
    @Suppress("DEPRECATION")
    private fun registerLegacyCallback(
        onSignalStrengthChanged: ((SignalStrength) -> Unit)?,
        onServiceStateChanged: ((ServiceState) -> Unit)?,
        onNetworkStateChanged: ((TelephonyNetworkState) -> Unit)?,
    ) {
        setupCallbackListeners(onSignalStrengthChanged, onServiceStateChanged, onNetworkStateChanged)

        val events =
            android.telephony.PhoneStateListener.LISTEN_SIGNAL_STRENGTHS or
                android.telephony.PhoneStateListener.LISTEN_SERVICE_STATE or
                android.telephony.PhoneStateListener.LISTEN_DATA_CONNECTION_STATE

        telephonyManager.listen(telephonyCallback.basePhoneStateListener, events)
        isCallbackRegistered = true
        Logx.d("TelephonyCallbackManager: Legacy callback registered")
    }

    /**
     * Sets up internal callback listeners.<br><br>
     * 내부 콜백 리스너를 설정합니다.<br>
     *
     * @param onSignalStrengthChanged Callback for signal strength changes.<br><br>
     *                                신호 강도 변경 콜백.
     * @param onServiceStateChanged Callback for service state changes.<br><br>
     *                              서비스 상태 변경 콜백.
     * @param onNetworkStateChanged Callback for network state changes.<br><br>
     *                              네트워크 상태 변경 콜백.
     */
    private fun setupCallbackListeners(
        onSignalStrengthChanged: ((SignalStrength) -> Unit)?,
        onServiceStateChanged: ((ServiceState) -> Unit)?,
        onNetworkStateChanged: ((TelephonyNetworkState) -> Unit)?,
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
     * Unregisters simple telephony callback.<br><br>
     * 간단한 telephony 콜백을 해제합니다.<br>
     *
     * @return `true` if successful, `false` otherwise.<br><br>
     *         성공 시 `true`, 그렇지 않으면 `false`.
     */
    @SuppressLint("MissingPermission")
    public fun unregisterSimpleCallback(): Boolean =
        tryCatchSystemManager(false) {
            if (!isCallbackRegistered) {
                Logx.w("TelephonyCallbackManager: No callback registered")
                return false
            }

            checkSdkVersion(
                Build.VERSION_CODES.S,
                positiveWork = { telephonyManager.unregisterTelephonyCallback(telephonyCallback.baseTelephonyCallback) },
                negativeWork = {
                    @Suppress("DEPRECATION")
                    telephonyManager.listen(telephonyCallback.basePhoneStateListener, android.telephony.PhoneStateListener.LISTEN_NONE)
                },
            )

            isCallbackRegistered = false
            Logx.d("TelephonyCallbackManager: Callback unregistered")
            true
        }

    // =================================================
    // Advanced Callback API (멀티 SIM, 상세 콜백)
    // =================================================

    /**
     * Registers advanced telephony callback for default SIM (API 31+).<br><br>
     * 기본 SIM에 대한 고급 Telephony 콜백을 등록합니다 (API 31+).<br>
     *
     * @param executor Executor for callback execution.<br><br>
     *                 콜백 실행을 위한 Executor.
     * @param isGpsOn Whether to include GPS-dependent callbacks.<br><br>
     *                GPS 의존 콜백 포함 여부.
     * @return `true` if successful, `false` otherwise.<br><br>
     *         성공 시 `true`, 그렇지 않으면 `false`.
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
        onTelephonyNetworkState: ((telephonyNetworkState: TelephonyNetworkState) -> Unit)? = null,
    ): Boolean = tryCatchSystemManager(false) {
        val subscriptionInfoList = getActiveSubscriptionInfoListInternal()
        val defaultSim = subscriptionInfoList.firstOrNull() ?: throw IllegalStateException("No default SIM found")

        registerAdvancedCallback(
            defaultSim.simSlotIndex,
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
        return true
    }

    /**
     * Registers advanced telephony callback for specific SIM slot (API 31+).<br><br>
     * 특정 SIM 슬롯에 대한 고급 Telephony 콜백을 등록합니다 (API 31+).<br>
     *
     * @param simSlotIndex SIM slot index.<br><br>
     *                     SIM 슬롯 인덱스.
     * @param executor Executor for callback execution.<br><br>
     *                 콜백 실행을 위한 Executor.
     * @param isGpsOn Whether to include GPS-dependent callbacks.<br><br>
     *                GPS 의존 콜백 포함 여부.
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
        onTelephonyNetworkState: ((telephonyNetworkState: TelephonyNetworkState) -> Unit)? = null,
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

        setupSlotCallbackListeners(
            simSlotIndex,
            onActiveDataSubId,
            onDataConnectionState,
            onCellInfo,
            onSignalStrength,
            onServiceState,
            onCallState,
            onDisplayInfo,
            onTelephonyNetworkState,
        )
        isRegistered[simSlotIndex] = true
    }

    /**
     * Sets up callback listeners for specific slot.<br><br>
     * 특정 슬롯에 대한 콜백 리스너를 설정합니다.<br>
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
        onTelephonyNetworkState: ((telephonyNetworkState: TelephonyNetworkState) -> Unit)?,
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
     * Unregisters callback for specific SIM slot (API 31+).<br><br>
     * 특정 SIM 슬롯의 콜백을 해제합니다 (API 31+).<br>
     *
     * @param simSlotIndex SIM slot index.<br><br>
     *                     SIM 슬롯 인덱스.
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
     * Sets signal strength callback.<br><br>
     * 신호 강도 콜백을 설정합니다.<br>
     */
    @RequiresPermission(READ_PHONE_STATE)
    public fun setOnSignalStrength(simSlotIndex: Int, onSignalStrength: ((currentSignalStrength: CurrentSignalStrength) -> Unit)? = null) {
        uSimTelephonyCallbackList[simSlotIndex]?.setOnSignalStrength(onSignalStrength)
            ?: Logx.w("TelephonyCallbackManager: setOnSignalStrength telephonyCallbackList[$simSlotIndex] is null")
    }

    /**
     * Sets service state callback.<br><br>
     * 서비스 상태 콜백을 설정합니다.<br>
     */
    @RequiresPermission(READ_PHONE_STATE)
    public fun setOnServiceState(simSlotIndex: Int, onServiceState: ((currentServiceState: CurrentServiceState) -> Unit)? = null) {
        uSimTelephonyCallbackList[simSlotIndex]?.setOnServiceState(onServiceState)
            ?: Logx.w("TelephonyCallbackManager: setOnServiceState telephonyCallbackList[$simSlotIndex] is null")
    }

    /**
     * Sets active data subscription ID callback.<br><br>
     * 활성 데이터 구독 ID 콜백을 설정합니다.<br>
     */
    @RequiresPermission(READ_PHONE_STATE)
    public fun setOnActiveDataSubId(simSlotIndex: Int, onActiveDataSubId: ((subId: Int) -> Unit)? = null) {
        uSimTelephonyCallbackList[simSlotIndex]?.setOnActiveDataSubId(onActiveDataSubId)
            ?: Logx.w("TelephonyCallbackManager: setOnActiveDataSubId telephonyCallbackList[$simSlotIndex] is null")
    }

    /**
     * Sets data connection state callback.<br><br>
     * 데이터 연결 상태 콜백을 설정합니다.<br>
     */
    @RequiresPermission(READ_PHONE_STATE)
    public fun setOnDataConnectionState(simSlotIndex: Int, onDataConnectionState: ((state: Int, networkType: Int) -> Unit)? = null) {
        uSimTelephonyCallbackList[simSlotIndex]?.setOnDataConnectionState(onDataConnectionState)
            ?: Logx.w("TelephonyCallbackManager: setOnDataConnectionState telephonyCallbackList[$simSlotIndex] is null")
    }

    /**
     * Sets cell info callback.<br><br>
     * 셀 정보 콜백을 설정합니다.<br>
     */
    @RequiresPermission(READ_PHONE_STATE)
    public fun setOnCellInfo(
        simSlotIndex: Int,
        onCellInfo: ((currentCellInfo: CurrentCellInfo) -> Unit)? = null,
    ) {
        uSimTelephonyCallbackList[simSlotIndex]?.setOnCellInfo(onCellInfo)
            ?: Logx.w("TelephonyCallbackManager: setOnCellInfo telephonyCallbackList[$simSlotIndex] is null")
    }

    /**
     * Sets call state callback.<br><br>
     * 통화 상태 콜백을 설정합니다.<br>
     */
    @RequiresPermission(READ_PHONE_STATE)
    public fun setOnCallState(simSlotIndex: Int, onCallState: ((callState: Int, phoneNumber: String?) -> Unit)? = null) {
        uSimTelephonyCallbackList[simSlotIndex]?.setOnCallState(onCallState)
            ?: Logx.w("TelephonyCallbackManager: setOnCallState telephonyCallbackList[$simSlotIndex] is null")
    }

    /**
     * Sets display info callback.<br><br>
     * 디스플레이 정보 콜백을 설정합니다.<br>
     */
    @RequiresPermission(READ_PHONE_STATE)
    public fun setOnDisplayState(simSlotIndex: Int, onDisplay: ((telephonyDisplayInfo: TelephonyDisplayInfo) -> Unit)? = null) {
        uSimTelephonyCallbackList[simSlotIndex]?.setOnDisplay(onDisplay)
            ?: Logx.w("TelephonyCallbackManager: setOnDisplayState telephonyCallbackList[$simSlotIndex] is null")
    }

    /**
     * Sets telephony network type callback.<br><br>
     * 통신망 타입 콜백을 설정합니다.<br>
     */
    public fun setOnTelephonyNetworkType(
        simSlotIndex: Int,
        onTelephonyNetworkType: ((telephonyNetworkState: TelephonyNetworkState) -> Unit)? = null,
    ) {
        uSimTelephonyCallbackList[simSlotIndex]?.setOnTelephonyNetworkType(onTelephonyNetworkType)
            ?: Logx.w(
                "TelephonyCallbackManager: setOnTelephonyNetworkType telephonyCallbackList[$simSlotIndex] is null",
            )
    }

    /**
     * Checks callback registration status.<br><br>
     * 콜백 등록 상태를 확인합니다.<br>
     *
     * @param simSlotIndex SIM slot index.<br><br>
     *                     SIM 슬롯 인덱스.
     * @return `true` if registered, `false` otherwise.<br><br>
     *         등록되었으면 `true`, 그렇지 않으면 `false`.
     */
    public fun isRegistered(simSlotIndex: Int): Boolean = isRegistered[simSlotIndex]

    /**
     * Gets TelephonyManager for specific SIM slot.<br><br>
     * 특정 SIM 슬롯의 TelephonyManager를 반환합니다.<br>
     *
     * @param slotIndex SIM slot index.<br><br>
     *                  SIM 슬롯 인덱스.
     * @return TelephonyManager instance, or null.<br><br>
     *         TelephonyManager 인스턴스, 또는 null.
     */
    public fun getTelephonyManagerFromUSim(slotIndex: Int): TelephonyManager? = uSimTelephonyManagerList[slotIndex]

    // =================================================
    // Signal/Service State Getters (Simple API용)
    // =================================================

    /**
     * Gets the current signal strength (cached from callbacks).<br><br>
     * 현재 신호 강도를 가져옵니다 (콜백에서 캐시된 값).<br>
     *
     * @return Current SignalStrength, or null.<br><br>
     *         현재 SignalStrength, 또는 null.
     */
    public fun getCurrentSignalStrength(): SignalStrength? = _currentSignalStrength.value

    /**
     * Gets the current service state (cached from callbacks).<br><br>
     * 현재 서비스 상태를 가져옵니다 (콜백에서 캐시된 값).<br>
     *
     * @return Current ServiceState, or null.<br><br>
     *         현재 ServiceState, 또는 null.
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
