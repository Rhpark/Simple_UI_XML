package kr.open.library.simpleui_xml.system_service_manager.info

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.Manifest.permission.BATTERY_STATS
import android.annotation.SuppressLint
import android.location.LocationManager
import android.os.Bundle
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kr.open.library.simple_ui.logcat.Logx
import kr.open.library.simple_ui.presenter.extensions.view.toastShort
import kr.open.library.simple_ui.presenter.ui.activity.BaseBindingActivity
import kr.open.library.simple_ui.presenter.ui.adapter.normal.simple.SimpleRcvAdapter
import kr.open.library.simple_ui.system_manager.extensions.getDisplayInfo
import kr.open.library.simple_ui.system_manager.info.battery.BatteryStateEvent
import kr.open.library.simple_ui.system_manager.info.battery.BatteryStateInfo
import kr.open.library.simple_ui.system_manager.info.location.LocationStateEvent
import kr.open.library.simple_ui.system_manager.info.location.LocationStateInfo
import kr.open.library.simpleui_xml.R
import kr.open.library.simpleui_xml.databinding.ActivityServiceManagerInfoBinding

class ServiceManagerInfoActivity : BaseBindingActivity<ActivityServiceManagerInfoBinding>(R.layout.activity_service_manager_info) {

    private val adapter = SimpleRcvAdapter<String>(android.R.layout.test_list_item) {
            holder, item, position -> holder.findViewById<TextView>(android.R.id.text1).text = item
    }

    private val batteryInfo :BatteryStateInfo by lazy { BatteryStateInfo(this) }
    private val locationInfo: LocationStateInfo by lazy { LocationStateInfo(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.rcvResult.adapter = adapter
        initListener()
    }

    @SuppressLint("MissingPermission")
    private fun initListener() {
        binding.run {

            btnDisplay.setOnClickListener {
                adapter.addItem("Display Full Screen Size :" + getDisplayInfo().getFullScreenSize())
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
        }
    }

    private fun addItem(item: String) = adapter.addItem(item)
}