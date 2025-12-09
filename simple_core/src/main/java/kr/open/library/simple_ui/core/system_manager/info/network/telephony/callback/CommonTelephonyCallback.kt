package kr.open.library.simple_ui.core.system_manager.info.network.telephony.callback
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.Manifest.permission.READ_PHONE_STATE
import android.annotation.SuppressLint
import android.os.Build
import android.telephony.CellInfo
import android.telephony.PhoneStateListener
import android.telephony.ServiceState
import android.telephony.SignalStrength
import android.telephony.TelephonyCallback
import android.telephony.TelephonyDisplayInfo
import android.telephony.TelephonyManager
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import kr.open.library.simple_ui.core.extensions.conditional.checkSdkVersion
import kr.open.library.simple_ui.core.logcat.Logx
import kr.open.library.simple_ui.core.system_manager.info.network.telephony.data.current.CurrentCellInfo
import kr.open.library.simple_ui.core.system_manager.info.network.telephony.data.current.CurrentServiceState
import kr.open.library.simple_ui.core.system_manager.info.network.telephony.data.current.CurrentSignalStrength
import kr.open.library.simple_ui.core.system_manager.info.network.telephony.data.state.TelephonyNetworkDetailType
import kr.open.library.simple_ui.core.system_manager.info.network.telephony.data.state.TelephonyNetworkState
import kr.open.library.simple_ui.core.system_manager.info.network.telephony.data.state.TelephonyNetworkType

/**
 * Common callback class for TelephonyManager events.<br><br>
 * TelephonyManager 이벤트를 위한 공통 콜백 클래스입니다.<br>
 *
 * This class provides a unified interface for handling telephony events,
 * supporting both the new TelephonyCallback (API 31+) and the legacy PhoneStateListener.<br><br>
 * 이 클래스는 telephony 이벤트를 처리하기 위한 통합 인터페이스를 제공하며,
 * 새로운 TelephonyCallback (API 31+)과 기존 PhoneStateListener를 모두 지원합니다.<br>
 *
 * You must call TelephonyStateInfo.registerCallBack() or TelephonyStateInfo.registerListen()
 * before using this class.<br><br>
 * 이 클래스를 사용하기 전에 TelephonyStateInfo.registerCallBack() 또는
 * TelephonyStateInfo.registerListen()을 호출해야 합니다.<br>
 *
 * @param telephonyManager The TelephonyManager instance.<br><br>
 *                         TelephonyManager 인스턴스.
 */
public open class CommonTelephonyCallback(
    private val telephonyManager: TelephonyManager,
) {
    /**
     * Callback for active data subscription ID changes.<br><br>
     * 활성 데이터 SUb ID 변경 콜백입니다.<br>
     */
    private var onActiveDataSubId: ((subId: Int) -> Unit)? = null

    /**
     * Callback for data connection state changes.<br><br>
     * 데이터 연결 상태 변경 콜백입니다.<br>
     */
    private var onDataConnectionState: ((state: Int, networkType: Int) -> Unit)? = null

    /**
     * Callback for cell info changes.<br><br>
     * 셀 정보 변경 콜백입니다.<br>
     */
    private var onCellInfo: ((currentCellInfo: CurrentCellInfo) -> Unit)? = null

    /**
     * Callback for signal strength changes.<br><br>
     * 신호 강도 변경 콜백입니다.<br>
     */
    private var onSignalStrength: ((currentSignalStrength: CurrentSignalStrength) -> Unit)? = null

    /**
     * Callback for service state changes.<br><br>
     * 서비스 상태 변경 콜백입니다.<br>
     */
    private var onServiceState: ((currentServiceState: CurrentServiceState) -> Unit)? = null

    /**
     * Callback for call state changes.<br><br>
     * 통화 상태 변경 콜백입니다.<br>
     */
    private var onCallState: ((callState: Int, phoneNumber: String?) -> Unit)? = null

    /**
     * Callback for display info changes.<br><br>
     * 디스플레이 정보 변경 콜백입니다.<br>
     */
    private var onDisplayInfo: ((telephonyDisplayInfo: TelephonyDisplayInfo) -> Unit)? = null

    /**
     * Callback for telephony network type changes.<br><br>
     * 통신망 타입 변경 콜백입니다.<br>
     */
    private var onTelephonyNetworkType: ((telephonyNetworkState: TelephonyNetworkState) -> Unit)? = null

    /**
     * Current telephony display info.<br><br>
     * 현재 Telephony 디스플레이 정보입니다.<br>
     */
    private var currentTelephonyDisplayInfo: TelephonyDisplayInfo? = null

    /**
     * Current telephony network state.<br><br>
     * 현재 Telephony 네트워크 상태입니다.<br>
     */
    private var currentTelephonyState: TelephonyNetworkState? = null

    /**
     * Base telephony callback instance (API 31+).<br><br>
     * 기본 Telephony 콜백 인스턴스입니다 (API 31+).<br>
     */
    @delegate:RequiresApi(Build.VERSION_CODES.S)
    public val baseTelephonyCallback: BaseTelephonyCallback by lazy { BaseTelephonyCallback() }

    /**
     * GPS-dependent telephony callback instance (API 31+).<br><br>
     * GPS 의존 Telephony 콜백 인스턴스입니다 (API 31+).<br>
     */
    @delegate:RequiresApi(Build.VERSION_CODES.S)
    public val baseGpsTelephonyCallback: BaseGpsTelephonyCallback by lazy { BaseGpsTelephonyCallback() }

    /**
     * Legacy phone state listener instance.<br><br>
     * 기존 PhoneStateListener 인스턴스입니다.<br>
     */
    public val basePhoneStateListener: BasePhoneStateListener by lazy { BasePhoneStateListener() }

    /**
     * Sets the callback for active data subscription ID changes.<br><br>
     * 활성 데이터 구독 ID 변경에 대한 콜백을 설정합니다.<br>
     *
     * @param onActiveDataSubId Callback function.<br><br>
     *                          콜백 함수.
     */
    public fun setOnActiveDataSubId(onActiveDataSubId: ((subId: Int) -> Unit)?) {
        this.onActiveDataSubId = onActiveDataSubId
    }

    /**
     * Sets the callback for data connection state changes.<br><br>
     * 데이터 연결 상태 변경에 대한 콜백을 설정합니다.<br>
     *
     * @param onDataConnectionState Callback function.<br><br>
     *                              콜백 함수.
     */
    public fun setOnDataConnectionState(onDataConnectionState: ((state: Int, networkType: Int) -> Unit)?) {
        this.onDataConnectionState = onDataConnectionState
    }

    /**
     * Sets the callback for cell info changes.<br><br>
     * 셀 정보 변경에 대한 콜백을 설정합니다.<br>
     *
     * @param onCellInfo Callback function.<br><br>
     *                   콜백 함수.
     */
    public fun setOnCellInfo(onCellInfo: ((currentCellInfo: CurrentCellInfo) -> Unit)?) {
        this.onCellInfo = onCellInfo
    }

    /**
     * Sets the callback for signal strength changes.<br><br>
     * 신호 강도 변경에 대한 콜백을 설정합니다.<br>
     *
     * @param onSignalStrength Callback function.<br><br>
     *                         콜백 함수.
     */
    public fun setOnSignalStrength(onSignalStrength: ((currentSignalStrength: CurrentSignalStrength) -> Unit)?) {
        this.onSignalStrength = onSignalStrength
    }

    /**
     * Sets the callback for service state changes.<br><br>
     * 서비스 상태 변경에 대한 콜백을 설정합니다.<br>
     *
     * @param onServiceState Callback function.<br><br>
     *                       콜백 함수.
     */
    public fun setOnServiceState(onServiceState: ((currentServiceState: CurrentServiceState) -> Unit)?) {
        this.onServiceState = onServiceState
    }

    /**
     * Sets the callback for call state changes.<br><br>
     * 통화 상태 변경에 대한 콜백을 설정합니다.<br>
     *
     * @param onCallState Callback function.<br><br>
     *                    콜백 함수.
     */
    public fun setOnCallState(onCallState: ((callState: Int, phoneNumber: String?) -> Unit)?) {
        this.onCallState = onCallState
    }

    /**
     * Sets the callback for display info changes.<br><br>
     * 디스플레이 정보 변경에 대한 콜백을 설정합니다.<br>
     *
     * @param onDisplay Callback function.<br><br>
     *                  콜백 함수.
     */
    public fun setOnDisplay(onDisplay: ((telephonyDisplayInfo: TelephonyDisplayInfo) -> Unit)?) {
        this.onDisplayInfo = onDisplay
    }

    /**
     * Sets the callback for telephony network type changes.<br><br>
     * 통신망 타입 변경에 대한 콜백을 설정합니다.<br>
     *
     * @param onTelephonyNetworkType Callback function.<br><br>
     *                               콜백 함수.
     */
    public fun setOnTelephonyNetworkType(onTelephonyNetworkType: ((telephonyNetworkState: TelephonyNetworkState) -> Unit)?) {
        this.onTelephonyNetworkType = onTelephonyNetworkType
    }

    /**
     * TelephonyCallback implementation for API 31+.<br><br>
     * API 31 이상을 위한 TelephonyCallback 구현체입니다.<br>
     *
     * Used with telephonyManager.registerTelephonyCallback.<br><br>
     * telephonyManager.registerTelephonyCallback과 함께 사용됩니다.<br>
     */
    @RequiresApi(Build.VERSION_CODES.S)
    public open inner class BaseTelephonyCallback :
        TelephonyCallback(),
        TelephonyCallback.DataConnectionStateListener,
        TelephonyCallback.ServiceStateListener,
        TelephonyCallback.SignalStrengthsListener,
        TelephonyCallback.CallStateListener,
        TelephonyCallback.DisplayInfoListener,
        TelephonyCallback.ActiveDataSubscriptionIdListener {
        /**
         * Called when data connection state changes.<br><br>
         * 데이터 연결 상태가 변경될 때 호출됩니다.<br>
         */
        @RequiresPermission(READ_PHONE_STATE)
        public override fun onDataConnectionStateChanged(
            state: Int,
            networkType: Int,
        ) {
            onDataConnectionState?.invoke(state, networkType)
            updateDataState(state)
        }

        /**
         * Called when call state changes.<br><br>
         * 통화 상태가 변경될 때 호출됩니다.<br>
         */
        override fun onCallStateChanged(state: Int) {
            onCallState?.invoke(state, null)
        }

        /**
         * Called when service state changes.<br><br>
         * 서비스 상태가 변경될 때 호출됩니다.<br>
         */
        @RequiresPermission(READ_PHONE_STATE)
        public override fun onServiceStateChanged(serviceState: ServiceState) {
            getTelephonyServiceStateNetworkCheck(serviceState)
            onServiceState?.invoke(CurrentServiceState(serviceState))
        }

        /**
         * Called when signal strengths change.<br><br>
         * 신호 강도가 변경될 때 호출됩니다.<br>
         */
        public override fun onSignalStrengthsChanged(signalStrength: SignalStrength) {
            onSignalStrength?.invoke(CurrentSignalStrength(signalStrength))
        }

        /**
         * Called when active data subscription ID changes.<br><br>
         * 활성 데이터 구독 ID가 변경될 때 호출됩니다.<br>
         */
        public override fun onActiveDataSubscriptionIdChanged(subId: Int) {
            onActiveDataSubId?.invoke(subId)
        }

        /**
         * Called when display info changes.<br><br>
         * 디스플레이 정보가 변경될 때 호출됩니다.<br>
         */
        @RequiresPermission(READ_PHONE_STATE)
        override fun onDisplayInfoChanged(telephonyDisplayInfo: TelephonyDisplayInfo) {
            setNetworkType(telephonyDisplayInfo)
            onDisplayInfo?.invoke(telephonyDisplayInfo)
        }
    }

    /**
     * TelephonyCallback implementation that requires location permission.<br><br>
     * 위치 권한이 필요한 TelephonyCallback 구현체입니다.<br>
     *
     * Declared separately as it may not respond depending on GPS status.<br><br>
     * GPS 상태에 따라 응답하지 않을 수 있으므로 별도로 선언되었습니다.<br>
     */
    @RequiresApi(Build.VERSION_CODES.S)
    public open inner class BaseGpsTelephonyCallback :
        BaseTelephonyCallback(),
        TelephonyCallback.CellInfoListener {
        /**
         * Called when cell info changes.<br><br>
         * 셀 정보가 변경될 때 호출됩니다.<br>
         */
        @RequiresPermission(allOf = [READ_PHONE_STATE, ACCESS_FINE_LOCATION])
        override fun onCellInfoChanged(cellInfo: MutableList<CellInfo>) {
            onCellInfo?.invoke(CurrentCellInfo(cellInfo))
        }
    }

    /**
     * Legacy PhoneStateListener implementation for API < 31.<br><br>
     * API 31 미만을 위한 기존 PhoneStateListener 구현체입니다.<br>
     *
     * Required permissions:<br>
     * - `READ_PHONE_STATE` (Default)<br>
     * - `ACCESS_FINE_LOCATION` (onCellInfoChanged)<br><br>
     * 필수 권한:<br>
     * - `READ_PHONE_STATE` (기본)<br>
     * - `ACCESS_FINE_LOCATION` (onCellInfoChanged)<br>
     */
    public open inner class BasePhoneStateListener : PhoneStateListener() {
        /**
         * Called when active data subscription ID changes.<br><br>
         * 활성 데이터 구독 ID가 변경될 때 호출됩니다.<br>
         */
        @RequiresPermission(READ_PHONE_STATE)
        public override fun onActiveDataSubscriptionIdChanged(subId: Int) {
            super.onActiveDataSubscriptionIdChanged(subId)
            onActiveDataSubId?.invoke(subId)
        }

        /**
         * Called when data connection state changes.<br><br>
         * 데이터 연결 상태가 변경될 때 호출됩니다.<br>
         */
        @RequiresPermission(READ_PHONE_STATE)
        override fun onDataConnectionStateChanged(
            state: Int,
            networkType: Int,
        ) {
            super.onDataConnectionStateChanged(state, networkType)
            updateDataState(state)
            onDataConnectionState?.invoke(state, networkType)
        }

        /**
         * Called when service state changes.<br><br>
         * 서비스 상태가 변경될 때 호출됩니다.<br>
         */
        @RequiresApi(Build.VERSION_CODES.R)
        @RequiresPermission(READ_PHONE_STATE)
        public override fun onServiceStateChanged(serviceState: ServiceState?) {
            super.onServiceStateChanged(serviceState)
            serviceState?.let { data ->
                getTelephonyServiceStateNetworkCheck(data)
                onServiceState?.invoke(CurrentServiceState(data))
            }
        }

        /**
         * Called when signal strengths change.<br><br>
         * 신호 강도가 변경될 때 호출됩니다.<br>
         */
        @RequiresApi(Build.VERSION_CODES.Q)
        public override fun onSignalStrengthsChanged(signalStrength: SignalStrength?) {
            super.onSignalStrengthsChanged(signalStrength)
            signalStrength?.let { data -> onSignalStrength?.invoke(CurrentSignalStrength(data)) }
        }

        /**
         * Called when call state changes.<br><br>
         * 통화 상태가 변경될 때 호출됩니다.<br>
         */
        override fun onCallStateChanged(
            state: Int,
            phoneNumber: String?,
        ) {
            super.onCallStateChanged(state, phoneNumber)
            onCallState?.invoke(state, phoneNumber)
        }

        /**
         * Called when display info changes.<br><br>
         * 디스플레이 정보가 변경될 때 호출됩니다.<br>
         */
        @RequiresPermission(READ_PHONE_STATE)
        override fun onDisplayInfoChanged(telephonyDisplayInfo: TelephonyDisplayInfo) {
            super.onDisplayInfoChanged(telephonyDisplayInfo)
            setNetworkType(telephonyDisplayInfo)
            onDisplayInfo?.invoke(telephonyDisplayInfo)
        }

        /**
         * Called when cell info changes.<br><br>
         * 셀 정보가 변경될 때 호출됩니다.<br>
         *
         * Requires `ACCESS_FINE_LOCATION` permission.<br><br>
         * `ACCESS_FINE_LOCATION` 권한이 필요합니다.<br>
         */
        @RequiresPermission(allOf = [READ_PHONE_STATE, ACCESS_FINE_LOCATION])
        public override fun onCellInfoChanged(cellInfo: MutableList<CellInfo>?) {
            super.onCellInfoChanged(cellInfo)
            cellInfo?.let { data -> onCellInfo?.invoke(CurrentCellInfo(data)) }
        }
    }

    /**
     * Updates the network state based on data connection state.<br><br>
     * 데이터 연결 상태에 따라 네트워크 상태를 업데이트합니다.<br>
     *
     * @param state Data connection state.<br><br>
     *              데이터 연결 상태.
     */
    @RequiresPermission(allOf = [READ_PHONE_STATE])
    private fun updateDataState(state: Int) {
        if (state == TelephonyManager.DATA_DISCONNECTED) {
            updateNetwork(TelephonyNetworkState(TelephonyNetworkType.DISCONNECT, TelephonyNetworkDetailType.DISCONNECT))
        } else if (state == TelephonyManager.DATA_CONNECTING) {
            updateNetwork(TelephonyNetworkState(TelephonyNetworkType.CONNECTING, TelephonyNetworkDetailType.CONNECTING))
        } else if (state == TelephonyManager.DATA_CONNECTED) {
            updateNetwork(getTelephonyManagerNetworkState())
        }
    }

    /**
     * Sets the network type based on display info.<br><br>
     * 디스플레이 정보에 따라 네트워크 타입을 설정합니다.<br>
     *
     * @param telephonyDisplayInfo Telephony display info.<br><br>
     *                             Telephony 디스플레이 정보.
     */
    @RequiresPermission(allOf = [READ_PHONE_STATE])
    private fun setNetworkType(telephonyDisplayInfo: TelephonyDisplayInfo) {
        checkSdkVersion(
            Build.VERSION_CODES.S,
            positiveWork = {
                val telephonyNetworkState: TelephonyNetworkState =
                    if (telephonyDisplayInfo.overrideNetworkType ==
                        TelephonyDisplayInfo.OVERRIDE_NETWORK_TYPE_NR_ADVANCED
                    ) {
                        TelephonyNetworkState(
                            TelephonyNetworkType.CONNECT_5G,
                            TelephonyNetworkDetailType.OVERRIDE_NETWORK_TYPE_NR_ADVANCED,
                        )
                    } else if (telephonyDisplayInfo.overrideNetworkType ==
                        TelephonyDisplayInfo.OVERRIDE_NETWORK_TYPE_NR_NSA_MMWAVE
                    ) {
                        TelephonyNetworkState(
                            TelephonyNetworkType.CONNECT_5G,
                            TelephonyNetworkDetailType.OVERRIDE_NETWORK_TYPE_NR_NSA_MMWAVE,
                        )
                    } else if (telephonyDisplayInfo.overrideNetworkType ==
                        TelephonyDisplayInfo.OVERRIDE_NETWORK_TYPE_NR_NSA
                    ) {
                        TelephonyNetworkState(
                            TelephonyNetworkType.CONNECT_5G,
                            TelephonyNetworkDetailType.OVERRIDE_NETWORK_TYPE_NR_NSA,
                        )
                    } else {
                        getTelephonyManagerNetworkState()
                    }

                updateNetwork(telephonyNetworkState)
            },
            negativeWork = { Logx.w("Can not update Network TelephonyDisplayInfo") },
        )
    }

    /**
     * Updates the current network state if changed.<br><br>
     * 변경된 경우 현재 네트워크 상태를 업데이트합니다.<br>
     *
     * @param telephonyNetworkState New network state.<br><br>
     *                              새로운 네트워크 상태.
     */
    private fun updateNetwork(telephonyNetworkState: TelephonyNetworkState) {
        if (!isSameTelephonyNetworkState(telephonyNetworkState)) {
            currentTelephonyState = telephonyNetworkState
            onTelephonyNetworkType?.invoke(telephonyNetworkState)
        }
    }

    /**
     * Gets the current network state from TelephonyManager.<br><br>
     * TelephonyManager에서 현재 네트워크 상태를 가져옵니다.<br>
     *
     * @return Current TelephonyNetworkState.<br><br>
     *         현재 TelephonyNetworkState.
     */
    @SuppressLint("SwitchIntDef")
    @RequiresPermission(allOf = [READ_PHONE_STATE])
    private fun getTelephonyManagerNetworkState(): TelephonyNetworkState =
        when (telephonyManager.dataNetworkType) {
            TelephonyManager.NETWORK_TYPE_GPRS -> {
                TelephonyNetworkState(TelephonyNetworkType.CONNECT_2G, TelephonyNetworkDetailType.NETWORK_TYPE_GPRS)
            }

            TelephonyManager.NETWORK_TYPE_EDGE -> {
                TelephonyNetworkState(TelephonyNetworkType.CONNECT_2G, TelephonyNetworkDetailType.NETWORK_TYPE_EDGE)
            }

            TelephonyManager.NETWORK_TYPE_CDMA -> {
                TelephonyNetworkState(TelephonyNetworkType.CONNECT_2G, TelephonyNetworkDetailType.NETWORK_TYPE_CDMA)
            }

            TelephonyManager.NETWORK_TYPE_1xRTT -> {
                TelephonyNetworkState(TelephonyNetworkType.CONNECT_2G, TelephonyNetworkDetailType.NETWORK_TYPE_1xRTT)
            }

            TelephonyManager.NETWORK_TYPE_IDEN -> {
                TelephonyNetworkState(TelephonyNetworkType.CONNECT_2G, TelephonyNetworkDetailType.NETWORK_TYPE_IDEN)
            }

            TelephonyManager.NETWORK_TYPE_GSM -> {
                TelephonyNetworkState(TelephonyNetworkType.CONNECT_2G, TelephonyNetworkDetailType.NETWORK_TYPE_GSM)
            }

            TelephonyManager.NETWORK_TYPE_UMTS -> {
                TelephonyNetworkState(TelephonyNetworkType.CONNECT_3G, TelephonyNetworkDetailType.NETWORK_TYPE_UMTS)
            }

            TelephonyManager.NETWORK_TYPE_EVDO_0 -> {
                TelephonyNetworkState(TelephonyNetworkType.CONNECT_3G, TelephonyNetworkDetailType.NETWORK_TYPE_EVDO_0)
            }

            TelephonyManager.NETWORK_TYPE_EVDO_A -> {
                TelephonyNetworkState(TelephonyNetworkType.CONNECT_3G, TelephonyNetworkDetailType.NETWORK_TYPE_EVDO_A)
            }

            TelephonyManager.NETWORK_TYPE_HSDPA -> {
                TelephonyNetworkState(TelephonyNetworkType.CONNECT_3G, TelephonyNetworkDetailType.NETWORK_TYPE_HSDPA)
            }

            TelephonyManager.NETWORK_TYPE_HSUPA -> {
                TelephonyNetworkState(TelephonyNetworkType.CONNECT_3G, TelephonyNetworkDetailType.NETWORK_TYPE_HSUPA)
            }

            TelephonyManager.NETWORK_TYPE_HSPA -> {
                TelephonyNetworkState(TelephonyNetworkType.CONNECT_3G, TelephonyNetworkDetailType.NETWORK_TYPE_HSPA)
            }

            TelephonyManager.NETWORK_TYPE_EVDO_B -> {
                TelephonyNetworkState(TelephonyNetworkType.CONNECT_3G, TelephonyNetworkDetailType.NETWORK_TYPE_EVDO_B)
            }

            TelephonyManager.NETWORK_TYPE_EHRPD -> {
                TelephonyNetworkState(TelephonyNetworkType.CONNECT_3G, TelephonyNetworkDetailType.NETWORK_TYPE_EHRPD)
            }

            TelephonyManager.NETWORK_TYPE_HSPAP -> {
                TelephonyNetworkState(TelephonyNetworkType.CONNECT_3G, TelephonyNetworkDetailType.NETWORK_TYPE_HSPAP)
            }

            TelephonyManager.NETWORK_TYPE_TD_SCDMA -> {
                TelephonyNetworkState(TelephonyNetworkType.CONNECT_3G, TelephonyNetworkDetailType.NETWORK_TYPE_TD_SCDMA)
            }

            TelephonyManager.NETWORK_TYPE_LTE -> {
                TelephonyNetworkState(TelephonyNetworkType.CONNECT_4G, TelephonyNetworkDetailType.NETWORK_TYPE_LTE)
            }

            TelephonyManager.NETWORK_TYPE_IWLAN -> {
                TelephonyNetworkState(TelephonyNetworkType.CONNECT_4G, TelephonyNetworkDetailType.NETWORK_TYPE_IWLAN)
            }

            19 -> { // NETWORK_TYPE_LTE_CA
                TelephonyNetworkState(TelephonyNetworkType.CONNECT_4G, TelephonyNetworkDetailType.NETWORK_TYPE_LTE_CA)
            }

            20 -> { // NETWORK_TYPE_NR
                TelephonyNetworkState(TelephonyNetworkType.CONNECT_5G, TelephonyNetworkDetailType.NETWORK_TYPE_NR)
            }

            else -> {
                TelephonyNetworkState(TelephonyNetworkType.UNKNOWN, TelephonyNetworkDetailType.UNKNOWN)
            }
        }

    /**
     * Checks network state using ServiceState for more accuracy.<br><br>
     * 더 정확한 확인을 위해 ServiceState를 사용하여 네트워크 상태를 확인합니다.<br>
     *
     * @param serviceState Current ServiceState.<br><br>
     *                     현재 ServiceState.
     */
    @RequiresPermission(allOf = [READ_PHONE_STATE])
    private fun getTelephonyServiceStateNetworkCheck(serviceState: ServiceState) {
        var telephonyNetworkState = getTelephonyManagerNetworkState()

        if (telephonyNetworkState.networkTypeState == TelephonyNetworkType.CONNECT_4G) {
            checkSdkVersion(
                Build.VERSION_CODES.R,
                positiveWork = {
                    currentTelephonyDisplayInfo?.let {
                        telephonyNetworkState =
                            if (it.overrideNetworkType == TelephonyDisplayInfo.OVERRIDE_NETWORK_TYPE_NR_NSA) {
                                TelephonyNetworkState(
                                    TelephonyNetworkType.CONNECT_5G,
                                    TelephonyNetworkDetailType.OVERRIDE_NETWORK_TYPE_NR_NSA,
                                )
                            } else if (it.overrideNetworkType ==
                                TelephonyDisplayInfo.OVERRIDE_NETWORK_TYPE_NR_NSA_MMWAVE
                            ) {
                                TelephonyNetworkState(
                                    TelephonyNetworkType.CONNECT_5G,
                                    TelephonyNetworkDetailType.OVERRIDE_NETWORK_TYPE_NR_NSA_MMWAVE,
                                )
                            } else if (it.overrideNetworkType ==
                                TelephonyDisplayInfo.OVERRIDE_NETWORK_TYPE_NR_ADVANCED
                            ) {
                                TelephonyNetworkState(
                                    TelephonyNetworkType.CONNECT_5G,
                                    TelephonyNetworkDetailType.OVERRIDE_NETWORK_TYPE_NR_ADVANCED,
                                )
                            } else {
                                telephonyNetworkState
                            }

                        updateNetwork(telephonyNetworkState)
                    }
                },
                negativeWork = {
                    val str = serviceState.toString()
                    if (str.contains("nrState=CONNECTED") && str.contains("nsaState=5")) {
                        telephonyNetworkState =
                            TelephonyNetworkState(
                                TelephonyNetworkType.CONNECT_5G,
                                TelephonyNetworkDetailType.NETWORK_TYPE_NR,
                            )
                    }
                    updateNetwork(telephonyNetworkState)
                },
            )
        }
    }

    /**
     * Checks if the new state is the same as the current state.<br><br>
     * 새 상태가 현재 상태와 같은지 확인합니다.<br>
     *
     * @param telephonyNetworkState New network state.<br><br>
     *                              새로운 네트워크 상태.
     * @return `true` if same, `false` otherwise.<br><br>
     *         같으면 `true`, 그렇지 않으면 `false`.
     */
    private fun isSameTelephonyNetworkState(telephonyNetworkState: TelephonyNetworkState): Boolean =
        currentTelephonyState?.let {
            (
                it.networkTypeState == telephonyNetworkState.networkTypeState &&
                    it.networkTypeDetailState == telephonyNetworkState.networkTypeDetailState
            )
        } ?: false
}
