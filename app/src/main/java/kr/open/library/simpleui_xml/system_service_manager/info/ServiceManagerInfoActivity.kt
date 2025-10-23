package kr.open.library.simpleui_xml.system_service_manager.info

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.Manifest.permission.BATTERY_STATS
import android.Manifest.permission.READ_PHONE_STATE
import android.annotation.SuppressLint
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kr.open.library.simple_ui.extensions.conditional.checkSdkVersion
import kr.open.library.simple_ui.logcat.Logx
import kr.open.library.simple_ui.presenter.extensions.view.toastShort
import kr.open.library.simple_ui.presenter.ui.activity.BaseBindingActivity
import kr.open.library.simple_ui.presenter.ui.adapter.normal.simple.SimpleRcvAdapter
import kr.open.library.simple_ui.system_manager.extensions.getDisplayInfo
import kr.open.library.simple_ui.system_manager.info.battery.BatteryStateEvent
import kr.open.library.simple_ui.system_manager.info.battery.BatteryStateInfo
import kr.open.library.simple_ui.system_manager.info.location.LocationStateEvent
import kr.open.library.simple_ui.system_manager.info.location.LocationStateInfo
import kr.open.library.simple_ui.system_manager.info.network.connectivity.NetworkConnectivityInfo
import kr.open.library.simple_ui.system_manager.info.network.sim.SimInfo
import kr.open.library.simple_ui.system_manager.info.network.telephony.TelephonyInfo
import kr.open.library.simpleui_xml.R
import kr.open.library.simpleui_xml.databinding.ActivityServiceManagerInfoBinding

class ServiceManagerInfoActivity : BaseBindingActivity<ActivityServiceManagerInfoBinding>(R.layout.activity_service_manager_info) {

    private val adapter = SimpleRcvAdapter<String>(android.R.layout.test_list_item) {
            holder, item, position -> holder.findViewById<TextView>(android.R.id.text1).text = item
    }

    private val batteryInfo :BatteryStateInfo by lazy { BatteryStateInfo(this) }
    private val locationInfo: LocationStateInfo by lazy { LocationStateInfo(this) }
    private val simInfo: SimInfo by lazy { SimInfo(this) }
    private val telephonyInfo: TelephonyInfo by lazy { TelephonyInfo(this) }
    private val networkInfo: NetworkConnectivityInfo by lazy { NetworkConnectivityInfo(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.rcvResult.adapter = adapter
        initListener()
    }

    @SuppressLint("MissingPermission")
    private fun initListener() {
        binding.run {

            btnDisplay.setOnClickListener {
                val displayInfo = getDisplayInfo()
                addItem("=== Display Info ===")
                addItem("Full Screen Size: ${displayInfo.getFullScreenSize()}")
                addItem("Available Screen: ${displayInfo.getScreen()}")
                addItem("Screen With StatusBar: ${displayInfo.getScreenWithStatusBar()}")
                addItem("StatusBar Height: ${displayInfo.getStatusBarHeight()}")
                addItem("NavigationBar Height: ${displayInfo.getNavigationBarHeight()}")
            }

            btnSim.setOnClickListener {
                onRequestPermissions(listOf(READ_PHONE_STATE)) { deniedPermissions ->
                    if (deniedPermissions.isEmpty()) {
                        addItem("=== SIM Info ===")

                        // 기본 정보
                        addItem("--- Basic Info ---")
                        addItem("Can Read SIM Info: ${simInfo.isCanReadSimInfo()}")
                        addItem("Is Dual SIM: ${simInfo.isDualSim()}")
                        addItem("Is Single SIM: ${simInfo.isSingleSim()}")
                        addItem("Is Multi SIM: ${simInfo.isMultiSim()}")
                        addItem("Active SIM Count: ${simInfo.getActiveSimCount()}")
                        addItem("Active SIM Slot Index List: ${simInfo.getActiveSimSlotIndexList()}")

                        // 기본 SIM 정보
                        addItem("--- Default SIM Info ---")
                        addItem("Default SIM SubId: ${simInfo.getSubIdFromDefaultUSim()}")
                        addItem("Phone Number: ${simInfo.getPhoneNumberFromDefaultUSim()}")
                        addItem("MCC: ${simInfo.getMccFromDefaultUSimString()}")
                        addItem("MNC: ${simInfo.getMncFromDefaultUSimString()}")
                        addItem("Display Name: ${simInfo.getDisplayNameFromDefaultUSim()}")
                        addItem("Country ISO: ${simInfo.getCountryIsoFromDefaultUSim()}")
                        addItem("SIM Status: ${simInfo.getStatusFromDefaultUSim()}")
                        addItem("Is Network Roaming: ${simInfo.isNetworkRoamingFromDefaultUSim()}")

                        // 고급 정보
                        addItem("--- Advanced Info ---")
                        addItem("eSIM Supported: ${simInfo.isESimSupported()}")

                        // 멀티 SIM 슬롯별 정보
                        val activeSlots = simInfo.getActiveSimSlotIndexList()
                        if (activeSlots.isNotEmpty()) {
                            activeSlots.forEach { slotIndex ->
                                addItem("--- SIM Slot $slotIndex ---")
                                addItem("SubId: ${simInfo.getSubId(slotIndex)}")
                                addItem("Phone Number: ${simInfo.getPhoneNumber(slotIndex)}")
                                addItem("MCC: ${simInfo.getMcc(slotIndex)}")
                                addItem("MNC: ${simInfo.getMnc(slotIndex)}")
                                addItem("SIM Status: ${simInfo.getActiveSimStatus(slotIndex)}")
                                addItem("Is eSIM: ${simInfo.isRegisterESim(slotIndex)}")
                            }
                        }

                        // 구독 정보 리스트
                        addItem("--- Subscription Info List ---")
                        val subInfoList = simInfo.getActiveSubscriptionInfoList()
                        addItem("Total Subscriptions: ${subInfoList.size}")
                        subInfoList.forEachIndexed { index, subInfo ->
                            addItem("Subscription $index: SlotIndex=${subInfo.simSlotIndex}, SubId=${subInfo.subscriptionId}, DisplayName=${subInfo.displayName}")
                        }

                    } else {
                        toastShort("Permission Denied $deniedPermissions")
                    }
                }
            }

            btnTelephonyInfo.setOnClickListener {
                onRequestPermissions(listOf(READ_PHONE_STATE)) { deniedPermissions ->
                    if (deniedPermissions.isEmpty()) {
                        addItem("=== Telephony Basic Info ===")

                        // 통신사 정보
                        addItem("--- Carrier Info ---")
                        addItem("Carrier Name: ${telephonyInfo.getCarrierName()}")
                        addItem("MCC: ${telephonyInfo.getMobileCountryCode()}")
                        addItem("MNC: ${telephonyInfo.getMobileNetworkCode()}")

                        // SIM 상태
                        addItem("--- SIM State ---")
                        addItem("SIM State: ${telephonyInfo.getSimState()}")
                        addItem("SIM State String: ${telephonyInfo.getSimStateString()}")
                        addItem("Is SIM Ready: ${telephonyInfo.isSimReady()}")
                        addItem("SIM Operator Name: ${telephonyInfo.getSimOperatorName()}")
                        addItem("SIM Country ISO: ${telephonyInfo.getSimCountryIso()}")

                        // 전화번호
                        addItem("--- Phone Number ---")
                        addItem("Phone Number: ${telephonyInfo.getPhoneNumber()}")

                        // 통화 상태
                        addItem("--- Call State ---")
                        addItem("Call State: ${telephonyInfo.getCallState()}")

                        // 네트워크 타입
                        addItem("--- Network Type ---")
                        addItem("Network Type: ${telephonyInfo.getNetworkType()}")
                        addItem("Network Type String: ${telephonyInfo.getNetworkTypeString()}")
                        addItem("Data Network Type: ${telephonyInfo.getDataNetworkType()}")

                        // 로밍
                        addItem("--- Roaming ---")
                        addItem("Is Network Roaming: ${telephonyInfo.isNetworkRoaming()}")

                        // 멀티 SIM
                        addItem("--- Multi SIM ---")
                        addItem("Active SIM Count: ${telephonyInfo.getActiveSimCount()}")

                    } else {
                        toastShort("Permission Denied $deniedPermissions")
                    }
                }
            }

            btnTelephonyRegister.setOnClickListener {
                onRequestPermissions(listOf(READ_PHONE_STATE)) { deniedPermissions ->
                    if (deniedPermissions.isEmpty()) {
                        addItem("=== Telephony Register ===")

                        // StateFlow 기반 콜백 등록
                        telephonyInfo.registerCallback(
                            handler = null,
                            onSignalStrengthChanged = { signalStrength ->
                                addItem("Signal Strength Level: ${signalStrength?.level}")
                            },
                            onServiceStateChanged = { serviceState ->
                                addItem("Service State: ${serviceState?.state}")
                            },
                            onNetworkStateChanged = { networkType ->
                                addItem("Network Type Changed: $networkType")
                            }
                        )

                        // StateFlow로도 수집 가능
                        lifecycleScope.launch {
                            telephonyInfo.currentSignalStrength.collect { signalStrength ->
                                if (signalStrength != null) {
                                    addItem("StateFlow Signal Strength: Level=${signalStrength.level}")
                                }
                            }
                        }

                        lifecycleScope.launch {
                            telephonyInfo.currentServiceState.collect { serviceState ->
                                if (serviceState != null) {
                                    addItem("StateFlow Service State: ${serviceState.state}")
                                }
                            }
                        }

                        addItem("Telephony Callback Registered")
                        toastShort("Telephony Registered")
                    } else {
                        toastShort("Permission Denied $deniedPermissions")
                    }
                }
            }

            btnTelephonyUnregister.setOnClickListener {
                telephonyInfo.unregisterCallback()
                addItem("Telephony Callback Unregistered")
                toastShort("Telephony Unregistered")
            }

            btnNetworkInfo.setOnClickListener {
                addItem("=== Network Connectivity Info ===")

                // 기본 연결성
                addItem("--- Basic Connectivity ---")
                addItem("Is Network Connected: ${networkInfo.isNetworkConnected()}")
                addItem("Is WiFi Enabled: ${networkInfo.isWifiEnabled()}")

                // Transport 타입별 연결
                addItem("--- Transport Types ---")
                addItem("Is WiFi Connected: ${networkInfo.isConnectedWifi()}")
                addItem("Is Mobile Connected: ${networkInfo.isConnectedMobile()}")
                addItem("Is VPN Connected: ${networkInfo.isConnectedVPN()}")
                addItem("Is Bluetooth Connected: ${networkInfo.isConnectedBluetooth()}")
                addItem("Is Ethernet Connected: ${networkInfo.isConnectedEthernet()}")
                checkSdkVersion(Build.VERSION_CODES.S) {
                    addItem("Is USB Connected: ${networkInfo.isConnectedUSB()}")
                }

                addItem("Is WiFi Aware Connected: ${networkInfo.isConnectedWifiAware()}")
                addItem("Is LowPan Connected: ${networkInfo.isConnectedLowPan()}")

                // IP 주소
                addItem("--- IP Addresses ---")
                addItem("WiFi IP: ${networkInfo.getIPAddressByNetworkType(android.net.NetworkCapabilities.TRANSPORT_WIFI)}")
                addItem("Mobile IP: ${networkInfo.getIPAddressByNetworkType(android.net.NetworkCapabilities.TRANSPORT_CELLULAR)}")
                addItem("Ethernet IP: ${networkInfo.getIPAddressByNetworkType(android.net.NetworkCapabilities.TRANSPORT_ETHERNET)}")

                // 네트워크 요약
                addItem("--- Network Summary ---")
                val summary = networkInfo.getNetworkConnectivitySummary()
                addItem("Summary: $summary")
            }

            btnNetworkRegister.setOnClickListener {
                addItem("=== Network Callback Register ===")

                // 기본 네트워크 콜백 등록
                networkInfo.registerDefaultNetworkCallback(
                    handler = null,
                    onNetworkAvailable = { network ->
                        addItem("Network Available: $network")
                    },
                    onNetworkLosing = { network, maxMsToLive ->
                        addItem("Network Losing: $network, MaxMs: $maxMsToLive")
                    },
                    onNetworkLost = { network ->
                        addItem("Network Lost: $network")
                    },
                    onUnavailable = {
                        addItem("Network Unavailable")
                    },
                    onNetworkCapabilitiesChanged = { network, capabilities ->
                        addItem("Capabilities Changed: Network=$network, Capabilities=$capabilities")
                    },
                    onLinkPropertiesChanged = { network, linkProperties ->
                        addItem("Link Properties Changed: Network=$network, LinkProps=$linkProperties")
                    },
                    onBlockedStatusChanged = { network, blocked ->
                        addItem("Blocked Status Changed: Network=$network, Blocked=$blocked")
                    }
                )

                addItem("Network Callback Registered")
                toastShort("Network Registered")
            }

            btnNetworkUnregister.setOnClickListener {
                networkInfo.unregisterDefaultNetworkCallback()
                addItem("Network Callback Unregistered")
                toastShort("Network Unregistered")
            }

            btnBatteryRegister.setOnClickListener {

                onRequestPermissions(listOf(BATTERY_STATS)) { deniedPermissions ->
                    Logx.d("deniedPermissions $deniedPermissions")
                    if(deniedPermissions.isEmpty()) {
                        batteryInfo.registerStart(lifecycleScope)
                        addItem("Capacity :"+ batteryInfo.getCapacity())
                        addItem("Technology :"+ batteryInfo.getTechnology())
                        addItem("ChargePlugStr :"+ batteryInfo.getChargePlugStr())
                        addItem("Health :"+ batteryInfo.getHealth())
                        addItem("ChargeStatus :"+ batteryInfo.getChargeStatus())
                        addItem("CurrentAmpere :"+ batteryInfo.getCurrentAmpere())
                        addItem("Temperature :"+ batteryInfo.getTemperature())
                        lifecycleScope.launch {
                            batteryInfo.sfUpdate.collect{ type->
                                when (type) {
                                    is BatteryStateEvent.OnCapacity ->          addItem("Capacity = ${type.percent}")
                                    is BatteryStateEvent.OnChargeCounter ->     addItem("ChargeCounter = ${type.counter}")
                                    is BatteryStateEvent.OnChargePlug ->        addItem("ChargePlugStr = ${type.type}")
                                    is BatteryStateEvent.OnChargeStatus ->      addItem("OnChargeStatus = ${type.status}")
                                    is BatteryStateEvent.OnCurrentAmpere ->     addItem("Current Ampere = ${type.current} mA")
                                    is BatteryStateEvent.OnEnergyCounter ->     addItem("EnergyCounte = ${type.energy}")
                                    is BatteryStateEvent.OnHealth ->            addItem("Health = ${type.health}")
                                    is BatteryStateEvent.OnPresent ->           addItem("Present = ${type.present}")
                                    is BatteryStateEvent.OnTemperature ->       addItem("Temperature = ${type.temperature}")
                                    is BatteryStateEvent.OnTotalCapacity ->     addItem("TotalCapacity = ${type.totalCapacity} ")
                                    is BatteryStateEvent.OnVoltage ->           addItem("Charge voltage = ${type.voltage} v")
                                    is BatteryStateEvent.OnCurrentAverageAmpere -> addItem("Current AverageAmpere = ${type.current} mA")
                                }
                            }
                        }
                    } else {
                        toastShort("Permission Denied $deniedPermissions")
                    }
                }


            }
            btnBatteryUnregister.setOnClickListener { batteryInfo.unRegister() }

            btnLocationRegister.setOnClickListener {
                onRequestPermissions(listOf(ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION)) { deniedPermissions ->
                    Logx.d("deniedPermissions $deniedPermissions")
                    if(deniedPermissions.isEmpty()) {
                        locationInfo.registerStart(lifecycleScope, LocationManager.GPS_PROVIDER,1000L,10f)
                        lifecycleScope.launch {
                            locationInfo.sfUpdate.collect { type ->
                                when (type) {
                                    is LocationStateEvent.OnGpsEnabled ->       addItem("OnGpsEnabled ${type.isEnabled}")
                                    is LocationStateEvent.OnNetworkEnabled ->   addItem("OnNetworkEnabled ${type.isEnabled}")
                                    is LocationStateEvent.OnFusedEnabled ->     addItem("OnFusedEnabled ${type.isEnabled}")
                                    is LocationStateEvent.OnPassiveEnabled ->   addItem("OnPassiveEnabled ${type.isEnabled}")
                                    is LocationStateEvent.OnLocationChanged ->  addItem("OnLocationChanged ${type.location}")
                                }
                            }
                        }
                    } else {
                        toastShort("Permission Denied $deniedPermissions")
                    }
                }
            }

            btnLocationUnregister.setOnClickListener { locationInfo.unregister()  }

            btnLocationGetLocation.setOnClickListener {
                onRequestPermissions(listOf(ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION)) { deniedPermissions ->
                    if (deniedPermissions.isEmpty()) {
                        val location = locationInfo.getLocation()
                        if (location != null) {
                            addItem("Last Location: Lat=${location.latitude}, Lng=${location.longitude}")
                        } else {
                            addItem("Last Location: null (위치를 아직 받지 못했습니다)")
                        }
                    } else {
                        toastShort("Permission Denied $deniedPermissions")
                    }
                }
            }

            btnLocationCalculateDistance.setOnClickListener {
                onRequestPermissions(listOf(ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION)) { deniedPermissions ->
                    if (deniedPermissions.isEmpty()) {
                        val currentLocation = locationInfo.getLocation()
                        if (currentLocation != null) {
                            // 예시: 서울시청 좌표 (37.5665, 126.9780)
                            val seoulCityHall = android.location.Location("").apply {
                                latitude = 37.5665
                                longitude = 126.9780
                            }
                            val distance = locationInfo.calculateDistance(currentLocation, seoulCityHall)
                            addItem("Distance to Seoul City Hall: ${distance}m (${distance / 1000}km)")
                        } else {
                            addItem("Current Location is null")
                        }
                    } else {
                        toastShort("Permission Denied $deniedPermissions")
                    }
                }
            }

            btnLocationCalculateBearing.setOnClickListener {
                onRequestPermissions(listOf(ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION)) { deniedPermissions ->
                    if (deniedPermissions.isEmpty()) {
                        val currentLocation = locationInfo.getLocation()
                        if (currentLocation != null) {
                            // 예시: 부산시청 좌표 (35.1796, 129.0756)
                            val busanCityHall = android.location.Location("").apply {
                                latitude = 35.1796
                                longitude = 129.0756
                            }
                            val bearing = locationInfo.calculateBearing(currentLocation, busanCityHall)
                            addItem("Bearing to Busan City Hall: ${bearing}° (${getBearingDirection(bearing)})")
                        } else {
                            addItem("Current Location is null")
                        }
                    } else {
                        toastShort("Permission Denied $deniedPermissions")
                    }
                }
            }

            btnLocationCheckRadius.setOnClickListener {
                onRequestPermissions(listOf(ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION)) { deniedPermissions ->
                    if (deniedPermissions.isEmpty()) {
                        val currentLocation = locationInfo.getLocation()
                        if (currentLocation != null) {
                            // 예시: 서울시청 반경 10km 이내 확인
                            val seoulCityHall = android.location.Location("").apply {
                                latitude = 37.5665
                                longitude = 126.9780
                            }
                            val isWithinRadius = locationInfo.isLocationWithRadius(currentLocation, seoulCityHall, 10000f)
                            addItem("Within 10km of Seoul City Hall: $isWithinRadius")
                        } else {
                            addItem("Current Location is null")
                        }
                    } else {
                        toastShort("Permission Denied $deniedPermissions")
                    }
                }
            }
        }
    }

    private fun addItem(item: String) = adapter.addItem(item)

    private fun getBearingDirection(bearing: Float): String {
        return when {
            bearing >= 337.5 || bearing < 22.5 -> "북(N)"
            bearing >= 22.5 && bearing < 67.5 -> "북동(NE)"
            bearing >= 67.5 && bearing < 112.5 -> "동(E)"
            bearing >= 112.5 && bearing < 157.5 -> "남동(SE)"
            bearing >= 157.5 && bearing < 202.5 -> "남(S)"
            bearing >= 202.5 && bearing < 247.5 -> "남서(SW)"
            bearing >= 247.5 && bearing < 292.5 -> "서(W)"
            bearing >= 292.5 && bearing < 337.5 -> "북서(NW)"
            else -> "Unknown"
        }
    }
}