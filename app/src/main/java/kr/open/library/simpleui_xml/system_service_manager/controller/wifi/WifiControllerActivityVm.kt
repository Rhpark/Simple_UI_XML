package kr.open.library.simpleui_xml.system_service_manager.controller.wifi

import kr.open.library.simple_ui.presenter.viewmodel.BaseViewModelEvent

sealed class WifiControllerActivityVmEvent {
    object GetWifiInfo : WifiControllerActivityVmEvent()
    object CheckStatus : WifiControllerActivityVmEvent()
    object ScanWifi : WifiControllerActivityVmEvent()
    object CheckBands : WifiControllerActivityVmEvent()
}

class WifiControllerActivityVm : BaseViewModelEvent<WifiControllerActivityVmEvent>() {

    fun onClickGetWifiInfo() = sendEventVm(WifiControllerActivityVmEvent.GetWifiInfo)

    fun onClickCheckStatus() = sendEventVm(WifiControllerActivityVmEvent.CheckStatus)

    fun onClickScanWifi() = sendEventVm(WifiControllerActivityVmEvent.ScanWifi)

    fun onClickCheckBands() = sendEventVm(WifiControllerActivityVmEvent.CheckBands)
}
