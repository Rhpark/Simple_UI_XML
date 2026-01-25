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
import kr.open.library.simple_ui.core.extensions.conditional.checkSdkVersion
import kr.open.library.simple_ui.core.logcat.Logx
import kr.open.library.simple_ui.core.system_manager.info.battery.BatteryStateEvent
import kr.open.library.simple_ui.core.system_manager.info.battery.BatteryStateInfo
import kr.open.library.simple_ui.core.system_manager.info.location.LocationStateEvent
import kr.open.library.simple_ui.core.system_manager.info.location.LocationStateInfo
import kr.open.library.simple_ui.core.system_manager.info.network.connectivity.NetworkConnectivityInfo
import kr.open.library.simple_ui.core.system_manager.info.network.sim.SimInfo
import kr.open.library.simple_ui.core.system_manager.info.network.telephony.TelephonyInfo
import kr.open.library.simple_ui.xml.extensions.view.toastShowShort
import kr.open.library.simple_ui.xml.system_manager.extensions.getDisplayInfo
import kr.open.library.simple_ui.xml.ui.adapter.normal.simple.SimpleRcvAdapter
import kr.open.library.simple_ui.xml.ui.components.activity.binding.BaseDataBindingActivity
import kr.open.library.simpleui_xml.R
import kr.open.library.simpleui_xml.databinding.ActivityServiceManagerInfoBinding

class ServiceManagerInfoActivity : BaseDataBindingActivity<ActivityServiceManagerInfoBinding>(R.layout.activity_service_manager_info) {
    private val adapter = SimpleRcvAdapter<String>(android.R.layout.test_list_item) { holder, item, position ->
        holder.findViewById<TextView>(android.R.id.text1).text = item
    }.apply {
        setDiffUtilItemSame { oldItem, newItem -> oldItem == newItem }
        setDiffUtilContentsSame { oldItem, newItem -> oldItem == newItem }
    }

    private val batteryInfo: BatteryStateInfo by lazy { BatteryStateInfo(this) }
    private val locationInfo: LocationStateInfo by lazy { LocationStateInfo(this) }
    private val simInfo: SimInfo by lazy { SimInfo(this) }
    private val telephonyInfo: TelephonyInfo by lazy { TelephonyInfo(this) }
    private val networkInfo: NetworkConnectivityInfo by lazy { NetworkConnectivityInfo(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        getBinding().rcvResult.adapter = adapter
    }

    @SuppressLint("MissingPermission")
    override fun onCreate(binding: ActivityServiceManagerInfoBinding, savedInstanceState: Bundle?) {
        super.onCreate(binding, savedInstanceState)
        getBinding().run {
            btnDisplay.setOnClickListener {
                val displayInfo = getDisplayInfo()
                val res =
                    mutableListOf<String>().apply {
                        add("=== Display Info ===")
                        add("Physical Screen Size: ${displayInfo.getPhysicalScreenSize()}")
                        add("App Window Size: ${displayInfo.getAppWindowSize()}")
                        add("Status bar size: ${displayInfo.getStatusBarSize()}")
                        add("Navigation bar size: ${displayInfo.getNavigationBarSize()}")
                        add("isFullScreen: ${displayInfo.getPhysicalScreenSize()}")
                        add("======================")
                    }
                addItem(res)
            }

            btnSim.setOnClickListener {
//                READ_PHONE_NUMBERS
//                onRequestPermissions(listOf(READ_PHONE_STATE)) { deniedPermissions ->
                requestPermissions(listOf(READ_PHONE_STATE)) { deniedPermissions ->
                    if (!deniedPermissions.isEmpty()) {
                        toastShowShort("Permission Denied $deniedPermissions")
                        return@requestPermissions
                    }
                    val res =
                        mutableListOf<String>().apply {
                            add("=== SIM Info ===")

                            try {
// 湲곕낯 ?뺣낫
                                add("--- Basic Info ---")
                                add("Can Read SIM Info: ${simInfo.isCanReadSimInfo()}")
                                add("Is Dual SIM: ${simInfo.isDualSim()}")
                                add("Is Single SIM: ${simInfo.isSingleSim()}")
                                add("Is Multi SIM: ${simInfo.isMultiSim()}")
                                add("Active SIM Count: ${simInfo.getActiveSimCount()}")
                                add("Active SIM Slot Index List: ${simInfo.getActiveSimSlotIndexList()}")

                                // 湲곕낯 SIM ?뺣낫
                                add("--- Default SIM Info ---")
                                add("Default SIM SubId: ${simInfo.getSubIdFromDefaultUSim()}")
                                add("Display Name: ${simInfo.getDisplayNameFromDefaultUSim()}")
                                add("Country ISO: ${simInfo.getCountryIsoFromDefaultUSim()}")
                                add("Phone Number: ${simInfo.getPhoneNumberFromDefaultUSim()}")
                                add("MCC: ${simInfo.getMccFromDefaultUSimString()}")
                                add("MNC: ${simInfo.getMncFromDefaultUSimString()}")
                                add("SIM Status: ${simInfo.getStatusFromDefaultUSim()}")
                                add("Is Network Roaming: ${simInfo.isNetworkRoamingFromDefaultUSim()}")

                                // 怨좉툒 ?뺣낫
                                add("--- Advanced Info ---")
                                add("eSIM Supported: ${simInfo.isESimSupported()}")

                                // 硫??SIM ?щ’蹂??뺣낫
                                val activeSlots = simInfo.getActiveSimSlotIndexList()
                                if (activeSlots.isNotEmpty()) {
                                    activeSlots.forEach { slotIndex ->
                                        add("--- SIM Slot $slotIndex ---")
                                        add("SubId: ${simInfo.getSubId(slotIndex)}")
                                        add("Phone Number: ${simInfo.getPhoneNumber(slotIndex)}")
                                        add("MCC: ${simInfo.getMcc(slotIndex)}")
                                        add("MNC: ${simInfo.getMnc(slotIndex)}")
                                        add("SIM Status: ${simInfo.getActiveSimStatus(slotIndex)}")
                                        add("Is eSIM: ${simInfo.isRegisterESim(slotIndex)}")
                                    }
                                }

                                // 援щ룆 ?뺣낫 由ъ뒪??
                                add("--- Subscription Info List ---")
                                val subInfoList = simInfo.getActiveSubscriptionInfoList()
                                add("Total Subscriptions: ${subInfoList.size}")
                                subInfoList.forEachIndexed { index, subInfo ->
                                    add(
                                        "Subscription $index: SlotIndex=${subInfo.simSlotIndex}, SubId=${subInfo.subscriptionId}, DisplayName=${subInfo.displayName}",
                                    )
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    addItem(res)
                }
            }

            btnTelephonyInfo.setOnClickListener {
                requestPermissions(listOf(READ_PHONE_STATE)) { deniedPermissions ->
                    if (!deniedPermissions.isEmpty()) {
                        toastShowShort("Permission Denied $deniedPermissions")
                        return@requestPermissions
                    }
                    val res =
                        mutableListOf<String>().apply {
                            add("=== Telephony Basic Info ===")
                            try {
                                // ?ㅽ듃?뚰겕 ???
                                add("--- Network Type ---")
                                add("Network Type: ${telephonyInfo.getNetworkType()}")
                                add("Network Type String: ${telephonyInfo.getNetworkTypeString()}")
                                add("Data Network Type: ${telephonyInfo.getDataNetworkType()}")

                                // 濡쒕컢
                                add("--- Roaming ---")
                                add("Is Network Roaming: ${telephonyInfo.isNetworkRoaming()}")

                                // 硫??SIM
                                add("--- Multi SIM ---")
                                add("Active SIM Count: ${telephonyInfo.getActiveSimCount()}")

                                // ?듭떊???뺣낫
                                add("--- Carrier Info ---")
                                add("Carrier Name: ${telephonyInfo.getCarrierName()}")
                                add("MCC: ${telephonyInfo.getMobileCountryCode()}")
                                add("MNC: ${telephonyInfo.getMobileNetworkCode()}")

                                // SIM ?곹깭
                                add("--- SIM State ---")
                                add("SIM State: ${telephonyInfo.getSimState()}")
                                add("SIM State String: ${telephonyInfo.getSimStateString()}")
                                add("Is SIM Ready: ${telephonyInfo.isSimReady()}")
                                add("SIM Operator Name: ${telephonyInfo.getSimOperatorName()}")
                                add("SIM Country ISO: ${telephonyInfo.getSimCountryIso()}")

                                // ?꾪솕踰덊샇
                                add("--- Phone Number ---")
                                add("Phone Number: ${telephonyInfo.getPhoneNumber()}")

                                // ?듯솕 ?곹깭
                                add("--- Call State ---")
                                add("Call State: ${telephonyInfo.getCallState()}")
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                            add("=======================================")
                        }
                    addItem(res)
                }
            }

            btnTelephonyRegister.setOnClickListener {
                requestPermissions(listOf(READ_PHONE_STATE)) { deniedPermissions ->
                    if (deniedPermissions.isEmpty()) {
                        addItem("=== Telephony Register ===")

                        // StateFlow 湲곕컲 肄쒕갚 ?깅줉
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
                            },
                        )

                        // StateFlow濡쒕룄 ?섏쭛 媛??
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
                        toastShowShort("Telephony Registered")
                    } else {
                        toastShowShort("Permission Denied $deniedPermissions")
                    }
                }
            }

            btnTelephonyUnregister.setOnClickListener {
                telephonyInfo.unregisterCallback()
                addItem("Telephony Callback Unregistered")
                toastShowShort("Telephony Unregistered")
            }

            btnNetworkInfo.setOnClickListener {
                addItem("=== Network Connectivity Info ===")

                // 湲곕낯 ?곌껐??
                addItem("--- Basic Connectivity ---")
                addItem("Is Network Connected: ${networkInfo.isNetworkConnected()}")
                addItem("Is WiFi Enabled: ${networkInfo.isWifiEnabled()}")

                // Transport ??낅퀎 ?곌껐
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

                // IP 二쇱냼
                addItem("--- IP Addresses ---")
                addItem(
                    "WiFi IP: ${networkInfo.getIPAddressByNetworkType(android.net.NetworkCapabilities.TRANSPORT_WIFI)}",
                )
                addItem(
                    "Mobile IP: ${
                        networkInfo.getIPAddressByNetworkType(
                            android.net.NetworkCapabilities.TRANSPORT_CELLULAR,
                        )
                    }",
                )
                addItem(
                    "Ethernet IP: ${
                        networkInfo.getIPAddressByNetworkType(
                            android.net.NetworkCapabilities.TRANSPORT_ETHERNET,
                        )
                    }",
                )

                // ?ㅽ듃?뚰겕 ?붿빟
                addItem("--- Network Summary ---")
                val summary = networkInfo.getNetworkConnectivitySummary()
                addItem("Summary: $summary")
            }

            btnNetworkRegister.setOnClickListener {
                addItem("=== Network Callback Register ===")

                // 湲곕낯 ?ㅽ듃?뚰겕 肄쒕갚 ?깅줉
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
                    },
                )

                addItem("Network Callback Registered")
                toastShowShort("Network Registered")
            }

            btnNetworkUnregister.setOnClickListener {
                networkInfo.unregisterDefaultNetworkCallback()
                addItem("Network Callback Unregistered")
                toastShowShort("Network Unregistered")
            }

            btnBatteryRegister.setOnClickListener {
                requestPermissions(listOf(BATTERY_STATS)) { deniedPermissions ->
                    Logx.d("deniedPermissions $deniedPermissions")
                    if (deniedPermissions.isEmpty()) {
                        batteryInfo.registerStart(lifecycleScope)
                        addItem("Capacity :" + batteryInfo.getCapacity())
                        addItem("Technology :" + batteryInfo.getTechnology())
                        addItem("ChargePlugStr :" + batteryInfo.getChargePlugList())
                        addItem("Health :" + batteryInfo.getHealth())
                        addItem("ChargeStatus :" + batteryInfo.getChargeStatus())
                        addItem("CurrentAmpere :" + batteryInfo.getCurrentAmpere())
                        addItem("Temperature :" + batteryInfo.getTemperature())
                        lifecycleScope.launch {
                            batteryInfo.sfUpdate.collect { type ->
                                when (type) {
                                    is BatteryStateEvent.OnCapacity -> addItem("Capacity = ${type.percent}")
                                    is BatteryStateEvent.OnChargeCounter -> addItem("ChargeCounter = ${type.counter}")
                                    is BatteryStateEvent.OnChargePlug -> addItem("ChargePlugStr = ${type.type}")
                                    is BatteryStateEvent.OnChargeStatus -> addItem("OnChargeStatus = ${type.status}")
                                    is BatteryStateEvent.OnCurrentAmpere -> addItem("Current Ampere = ${type.current} mA")
                                    is BatteryStateEvent.OnEnergyCounter -> addItem("EnergyCounte = ${type.energy}")
                                    is BatteryStateEvent.OnHealth -> addItem("Health = ${type.health}")
                                    is BatteryStateEvent.OnPresent -> addItem("Present = ${type.present}")
                                    is BatteryStateEvent.OnTemperature -> addItem("Temperature = ${type.temperature}")
                                    is BatteryStateEvent.OnVoltage -> addItem("Charge voltage = ${type.voltage} v")
                                    is BatteryStateEvent.OnCurrentAverageAmpere -> addItem("Current AverageAmpere = ${type.current} mA")
                                }
                            }
                        }
                    } else {
                        toastShowShort("Permission Denied $deniedPermissions")
                    }
                }
            }
            btnBatteryUnregister.setOnClickListener { batteryInfo.unRegister() }

            btnLocationRegister.setOnClickListener {
                requestPermissions(listOf(ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION)) { deniedPermissions ->
                    Logx.d("deniedPermissions $deniedPermissions")
                    if (deniedPermissions.isEmpty()) {
                        locationInfo.registerStart(lifecycleScope, LocationManager.GPS_PROVIDER, 1000L, 10f)
                        lifecycleScope.launch {
                            locationInfo.sfUpdate.collect { type ->
                                when (type) {
                                    is LocationStateEvent.OnGpsEnabled -> addItem("OnGpsEnabled ${type.isEnabled}")
                                    is LocationStateEvent.OnNetworkEnabled -> addItem("OnNetworkEnabled ${type.isEnabled}")
                                    is LocationStateEvent.OnFusedEnabled -> addItem("OnFusedEnabled ${type.isEnabled}")
                                    is LocationStateEvent.OnPassiveEnabled -> addItem("OnPassiveEnabled ${type.isEnabled}")
                                    is LocationStateEvent.OnLocationChanged -> addItem("OnLocationChanged ${type.location}")
                                }
                            }
                        }
                    } else {
                        toastShowShort("Permission Denied $deniedPermissions")
                    }
                }
            }

            btnLocationUnregister.setOnClickListener { locationInfo.onDestroy() }

            btnLocationGetLocation.setOnClickListener {
                requestPermissions(listOf(ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION)) { deniedPermissions ->
                    if (deniedPermissions.isEmpty()) {
                        val location = locationInfo.getLocation()
                        if (location != null) {
                            addItem("Last Location: Lat=${location.latitude}, Lng=${location.longitude}")
                        } else {
                            addItem("Last Location: null (?꾩튂瑜??꾩쭅 諛쏆? 紐삵뻽?듬땲??")
                        }
                    } else {
                        toastShowShort("Permission Denied $deniedPermissions")
                    }
                }
            }

            btnLocationCalculateDistance.setOnClickListener {
                requestPermissions(listOf(ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION)) { deniedPermissions ->
                    if (deniedPermissions.isEmpty()) {
                        val currentLocation = locationInfo.getLocation()
                        if (currentLocation != null) {
                            // ?덉떆: ?쒖슱?쒖껌 醫뚰몴 (37.5665, 126.9780)
                            val seoulCityHall =
                                android.location.Location("").apply {
                                    latitude = 37.5665
                                    longitude = 126.9780
                                }
                            val distance = locationInfo.calculateDistance(currentLocation, seoulCityHall)
                            addItem("Distance to Seoul City Hall: ${distance}m (${distance / 1000}km)")
                        } else {
                            addItem("Current Location is null")
                        }
                    } else {
                        toastShowShort("Permission Denied $deniedPermissions")
                    }
                }
            }

            btnLocationCalculateBearing.setOnClickListener {
                requestPermissions(listOf(ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION)) { deniedPermissions ->
                    if (deniedPermissions.isEmpty()) {
                        val currentLocation = locationInfo.getLocation()
                        if (currentLocation != null) {
                            // ?덉떆: 遺?곗떆泥?醫뚰몴 (35.1796, 129.0756)
                            val busanCityHall =
                                android.location.Location("").apply {
                                    latitude = 35.1796
                                    longitude = 129.0756
                                }
                            val bearing = locationInfo.calculateBearing(currentLocation, busanCityHall)
                            addItem("Bearing to Busan City Hall: $bearing째 (${getBearingDirection(bearing)})")
                        } else {
                            addItem("Current Location is null")
                        }
                    } else {
                        toastShowShort("Permission Denied $deniedPermissions")
                    }
                }
            }

            btnLocationCheckRadius.setOnClickListener {
                requestPermissions(listOf(ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION)) { deniedPermissions ->
                    if (deniedPermissions.isEmpty()) {
                        val currentLocation = locationInfo.getLocation()
                        if (currentLocation != null) {
                            // ?덉떆: ?쒖슱?쒖껌 諛섍꼍 10km ?대궡 ?뺤씤
                            val seoulCityHall =
                                android.location.Location("").apply {
                                    latitude = 37.5665
                                    longitude = 126.9780
                                }
                            val isWithinRadius =
                                locationInfo.isLocationWithRadius(
                                    currentLocation,
                                    seoulCityHall,
                                    10000f,
                                )
                            addItem("Within 10km of Seoul City Hall: $isWithinRadius")
                        } else {
                            addItem("Current Location is null")
                        }
                    } else {
                        toastShowShort("Permission Denied $deniedPermissions")
                    }
                }
            }
        }
    }

    private fun addItem(item: String) = adapter.addItem(item)

    private fun addItem(items: List<String>) = adapter.addItems(items)

    private fun getBearingDirection(bearing: Float): String =
        when {
            bearing >= 337.5 || bearing < 22.5 -> "遺?N)"
            bearing >= 22.5 && bearing < 67.5 -> "遺곷룞(NE)"
            bearing >= 67.5 && bearing < 112.5 -> "??E)"
            bearing >= 112.5 && bearing < 157.5 -> "?⑤룞(SE)"
            bearing >= 157.5 && bearing < 202.5 -> "??S)"
            bearing >= 202.5 && bearing < 247.5 -> "?⑥꽌(SW)"
            bearing >= 247.5 && bearing < 292.5 -> "??W)"
            bearing >= 292.5 && bearing < 337.5 -> "遺곸꽌(NW)"
            else -> "Unknown"
        }
}
