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
 * Using for telephonyManager.registerTelephonyCallback or telephonyManager.listen
 *
 *
 * You must call
 * TelephonyStateInfo.registerCallBack() or
 * TelephonyStateInfo.registerListen()
 * first before using it.
 *
 */
public open class CommonTelephonyCallback(private val telephonyManager: TelephonyManager) {

    /***************************
     * CallBack Listener List  *
     ***************************/
    private var onActiveDataSubId: ((subId: Int) -> Unit)? = null

    private var onDataConnectionState: ((state: Int, networkType: Int) -> Unit)? = null

    private var onCellInfo: ((currentCellInfo: CurrentCellInfo) -> Unit)? = null

    private var onSignalStrength: ((currentSignalStrength: CurrentSignalStrength) -> Unit)? = null

    private var onServiceState: ((currentServiceState: CurrentServiceState) -> Unit)? = null

    private var onCallState: ((callState: Int, phoneNumber: String?) -> Unit)? = null

    private var onDisplayInfo: ((telephonyDisplayInfo: TelephonyDisplayInfo) -> Unit)? = null

    private var onTelephonyNetworkType: ((telephonyNetworkState: TelephonyNetworkState) -> Unit)? = null

    private var currentTelephonyDisplayInfo: TelephonyDisplayInfo? = null

    private var currentTelephonyState: TelephonyNetworkState? = null

    @delegate:RequiresApi(Build.VERSION_CODES.S)
    public val baseTelephonyCallback: BaseTelephonyCallback by lazy { BaseTelephonyCallback() }

    @delegate:RequiresApi(Build.VERSION_CODES.S)
    public val baseGpsTelephonyCallback: BaseGpsTelephonyCallback by lazy { BaseGpsTelephonyCallback() }

    public val basePhoneStateListener: BasePhoneStateListener by lazy { BasePhoneStateListener()}


    public fun setOnActiveDataSubId(onActiveDataSubId: ((subId: Int) -> Unit)?) {
        this.onActiveDataSubId = onActiveDataSubId
    }

    public fun setOnDataConnectionState(onDataConnectionState: ((state: Int, networkType: Int) -> Unit)?) {
        this.onDataConnectionState = onDataConnectionState
    }

    public fun setOnCellInfo(onCellInfo: ((currentCellInfo: CurrentCellInfo) -> Unit)?) {
        this.onCellInfo = onCellInfo
    }

    public fun setOnSignalStrength(onSignalStrength: ((currentSignalStrength: CurrentSignalStrength) -> Unit)?) {
        this.onSignalStrength = onSignalStrength
    }

    public fun setOnServiceState(onServiceState: ((currentServiceState: CurrentServiceState) -> Unit)?) {
        this.onServiceState = onServiceState
    }

    public fun setOnCallState(onCallState: ((callState: Int, phoneNumber: String?) -> Unit)?) {
        this.onCallState = onCallState
    }

    public fun setOnDisplay(onDisplay: ((telephonyDisplayInfo: TelephonyDisplayInfo) -> Unit)?) {
        this.onDisplayInfo = onDisplay
    }

    public fun setOnTelephonyNetworkType(onTelephonyNetworkType: ((telephonyNetworkState: TelephonyNetworkState) -> Unit)?) {
        this.onTelephonyNetworkType = onTelephonyNetworkType
    }

    /**
     * Using for telephonyManager.registerTelephonyCallback
     */
    @RequiresApi(Build.VERSION_CODES.S)
    public open inner class BaseTelephonyCallback : TelephonyCallback(),
        TelephonyCallback.DataConnectionStateListener,
        TelephonyCallback.ServiceStateListener,
        TelephonyCallback.SignalStrengthsListener,
        TelephonyCallback.CallStateListener,
        TelephonyCallback.DisplayInfoListener,
        TelephonyCallback.ActiveDataSubscriptionIdListener {

        /**
         * Using for telephonyManager.registerTelephonyCallback onDataConnectionStateChanged
         */
        @RequiresPermission(READ_PHONE_STATE)
        public override fun onDataConnectionStateChanged(state: Int, networkType: Int) {
            onDataConnectionState?.invoke(state, networkType)
            updateDataState(state)
        }

        override fun onCallStateChanged(state: Int) {
            onCallState?.invoke(state,null)
        }

        /**
         * Using for telephonyManager.registerTelephonyCallback onServiceStateChanged
         */
        @RequiresPermission(READ_PHONE_STATE)
        public override fun onServiceStateChanged(serviceState: ServiceState) {
            getTelephonyServiceStateNetworkCheck(serviceState)
            onServiceState?.invoke(CurrentServiceState(serviceState))
        }

        /**
         * Using for telephonyManager.registerTelephonyCallback onSignalStrengthsChanged
         */
        public override fun onSignalStrengthsChanged(signalStrength: SignalStrength) {
            onSignalStrength?.invoke(CurrentSignalStrength(signalStrength))
        }

        /**
         * Using for telephonyManager.registerTelephonyCallback onActiveDataSubscriptionIdChanged
         */
        public override fun onActiveDataSubscriptionIdChanged(subId: Int) {
            onActiveDataSubId?.invoke(subId)
        }

        @RequiresPermission(READ_PHONE_STATE)
        override fun onDisplayInfoChanged(telephonyDisplayInfo: TelephonyDisplayInfo) {
            setNetworkType(telephonyDisplayInfo)
            onDisplayInfo?.invoke(telephonyDisplayInfo)
        }
    }

    /**
     * Declared separately as it may not respond depending on GPS status
     * GPS 상태에 따라 응답하지 않을 수 있으므로 별도로 선언.
     */
    @RequiresApi(Build.VERSION_CODES.S)
    public open inner class BaseGpsTelephonyCallback : BaseTelephonyCallback(),
        TelephonyCallback.CellInfoListener {

        /**
         * Using for telephonyManager.registerTelephonyCallback onCellInfoChanged
         */
        @RequiresPermission(allOf = [READ_PHONE_STATE, ACCESS_FINE_LOCATION])
        override fun onCellInfoChanged(cellInfo: MutableList<CellInfo>) {
            onCellInfo?.invoke(CurrentCellInfo(cellInfo))
        }
    }

    /**
     * Required permission
     * default android.Manifest.permission.READ_PHONE_STATE
     * onCellInfoChanged android.Manifest.permission.ACCESS_FINE_LOCATION
     * Check the change active data SubscriptionId (Build.VERSION.SDK_INT < Build.VERSION_CODES.S)
     */
    public open inner class BasePhoneStateListener : PhoneStateListener() {

        /**
         * RequiresPermission android.Manifest.permission.READ_PHONE_STATE
         */
        @RequiresPermission(READ_PHONE_STATE)
        public override fun onActiveDataSubscriptionIdChanged(subId: Int) {
            super.onActiveDataSubscriptionIdChanged(subId)
            onActiveDataSubId?.invoke(subId)
        }

        @RequiresPermission(READ_PHONE_STATE)
        override fun onDataConnectionStateChanged(state: Int, networkType: Int) {
            super.onDataConnectionStateChanged(state, networkType)
            updateDataState(state)
            onDataConnectionState?.invoke(state, networkType)
        }

        @RequiresApi(Build.VERSION_CODES.R)
        @RequiresPermission(READ_PHONE_STATE)
        public override fun onServiceStateChanged(serviceState: ServiceState?) {
            super.onServiceStateChanged(serviceState)
            serviceState?.let { data ->
                getTelephonyServiceStateNetworkCheck(data)
                onServiceState?.invoke(CurrentServiceState(data))
            }
        }

        @RequiresApi(Build.VERSION_CODES.Q)
        public override fun onSignalStrengthsChanged(signalStrength: SignalStrength?) {
            super.onSignalStrengthsChanged(signalStrength)
            signalStrength?.let { data -> onSignalStrength?.invoke(CurrentSignalStrength(data)) }
        }

        override fun onCallStateChanged(state: Int, phoneNumber: String?) {
            super.onCallStateChanged(state, phoneNumber)
            onCallState?.invoke(state, phoneNumber)
        }

        @RequiresPermission(READ_PHONE_STATE)
        override fun onDisplayInfoChanged(telephonyDisplayInfo: TelephonyDisplayInfo) {
            super.onDisplayInfoChanged(telephonyDisplayInfo)
            setNetworkType(telephonyDisplayInfo)
            onDisplayInfo?.invoke(telephonyDisplayInfo)
        }

        /**
         * Requires Permission
         * Manifest.permission.READ_PHONE_STATE,
         * Manifest.permission.ACCESS_FINE_LOCATION
         *
         * Using for telephonyManager.registerTelephonyListen onCellInfoChanged
         */
        @RequiresPermission(allOf = [READ_PHONE_STATE, ACCESS_FINE_LOCATION])
        public override fun onCellInfoChanged(cellInfo: MutableList<CellInfo>?) {
            super.onCellInfoChanged(cellInfo)
            cellInfo?.let { data-> onCellInfo?.invoke(CurrentCellInfo(data)) }
        }
    }

    @RequiresPermission(allOf = [READ_PHONE_STATE])
    private fun updateDataState(state: Int) {
        if(state == TelephonyManager.DATA_DISCONNECTED) {
            updateNetwork(TelephonyNetworkState(TelephonyNetworkType.DISCONNECT, TelephonyNetworkDetailType.DISCONNECT))
        } else if(state == TelephonyManager.DATA_CONNECTING) {
            updateNetwork(TelephonyNetworkState(TelephonyNetworkType.CONNECTING, TelephonyNetworkDetailType.CONNECTING))
        } else if(state == TelephonyManager.DATA_CONNECTED) {
            updateNetwork(getTelephonyManagerNetworkState())
        }
    }

    @RequiresPermission(allOf = [READ_PHONE_STATE])
    private fun setNetworkType(telephonyDisplayInfo:TelephonyDisplayInfo ) {

        checkSdkVersion(Build.VERSION_CODES.S,
            positiveWork = {
                val telephonyNetworkState : TelephonyNetworkState = if(telephonyDisplayInfo.overrideNetworkType == TelephonyDisplayInfo.OVERRIDE_NETWORK_TYPE_NR_ADVANCED) {
                    TelephonyNetworkState(TelephonyNetworkType.CONNECT_5G,TelephonyNetworkDetailType.OVERRIDE_NETWORK_TYPE_NR_ADVANCED)
                } else if(telephonyDisplayInfo.overrideNetworkType == TelephonyDisplayInfo.OVERRIDE_NETWORK_TYPE_NR_NSA_MMWAVE) {
                    TelephonyNetworkState(TelephonyNetworkType.CONNECT_5G,TelephonyNetworkDetailType.OVERRIDE_NETWORK_TYPE_NR_NSA_MMWAVE)
                } else if(telephonyDisplayInfo.overrideNetworkType == TelephonyDisplayInfo.OVERRIDE_NETWORK_TYPE_NR_NSA) {
                    TelephonyNetworkState(TelephonyNetworkType.CONNECT_5G,TelephonyNetworkDetailType.OVERRIDE_NETWORK_TYPE_NR_NSA)
                } else {
                    getTelephonyManagerNetworkState()
                }

                updateNetwork(telephonyNetworkState)
            },
            negativeWork = {  Logx.w("Can not update Network TelephonyDisplayInfo") }
        )
    }

    private fun updateNetwork(telephonyNetworkState: TelephonyNetworkState) {
        if (!isSameTelephonyNetworkState(telephonyNetworkState)) {
            currentTelephonyState = telephonyNetworkState
            onTelephonyNetworkType?.invoke(telephonyNetworkState)
        }
    }

    @SuppressLint("SwitchIntDef")
    @RequiresPermission(allOf = [READ_PHONE_STATE])
    private fun getTelephonyManagerNetworkState():TelephonyNetworkState =  when (telephonyManager.dataNetworkType) {
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

        19 /* NETWORK_TYPE_LTE_CA */ -> {
            TelephonyNetworkState(TelephonyNetworkType.CONNECT_4G, TelephonyNetworkDetailType.NETWORK_TYPE_LTE_CA)
        }

        20 /* NETWORK_TYPE_NR */ -> {
            TelephonyNetworkState(TelephonyNetworkType.CONNECT_5G, TelephonyNetworkDetailType.NETWORK_TYPE_NR)
        }

        else -> {
            TelephonyNetworkState(TelephonyNetworkType.UNKNOWN, TelephonyNetworkDetailType.UNKNOWN)
        }
    }

    @RequiresPermission(allOf = [READ_PHONE_STATE])
    private fun getTelephonyServiceStateNetworkCheck(serviceState: ServiceState) {

        var telephonyNetworkState = getTelephonyManagerNetworkState()

        if (telephonyNetworkState.networkTypeState == TelephonyNetworkType.CONNECT_4G) {
            checkSdkVersion(Build.VERSION_CODES.R,
                positiveWork = {
                    currentTelephonyDisplayInfo?.let {
                        telephonyNetworkState = if (it.overrideNetworkType == TelephonyDisplayInfo.OVERRIDE_NETWORK_TYPE_NR_NSA) {
                            TelephonyNetworkState(TelephonyNetworkType.CONNECT_5G, TelephonyNetworkDetailType.OVERRIDE_NETWORK_TYPE_NR_NSA)
                        } else if (it.overrideNetworkType == TelephonyDisplayInfo.OVERRIDE_NETWORK_TYPE_NR_NSA_MMWAVE) {
                            TelephonyNetworkState(TelephonyNetworkType.CONNECT_5G, TelephonyNetworkDetailType.OVERRIDE_NETWORK_TYPE_NR_NSA_MMWAVE)
                        } else if (it.overrideNetworkType == TelephonyDisplayInfo.OVERRIDE_NETWORK_TYPE_NR_ADVANCED) {
                            TelephonyNetworkState(TelephonyNetworkType.CONNECT_5G, TelephonyNetworkDetailType.OVERRIDE_NETWORK_TYPE_NR_ADVANCED)
                        } else telephonyNetworkState

                        updateNetwork(telephonyNetworkState)
                    }
                },
                negativeWork = {
                    val str = serviceState.toString()
                    if (str.contains("nrState=CONNECTED") && str.contains("nsaState=5")) {
                        telephonyNetworkState = TelephonyNetworkState(TelephonyNetworkType.CONNECT_5G, TelephonyNetworkDetailType.NETWORK_TYPE_NR)
                    }
                    updateNetwork(telephonyNetworkState)
                }
            )
        }
    }

    private fun isSameTelephonyNetworkState(telephonyNetworkState: TelephonyNetworkState): Boolean  {
        return currentTelephonyState?.let {
            (it.networkTypeState == telephonyNetworkState.networkTypeState &&
                    it.networkTypeDetailState == telephonyNetworkState.networkTypeDetailState)
        }?:false
    }
}
