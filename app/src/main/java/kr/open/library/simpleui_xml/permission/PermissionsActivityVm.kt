package kr.open.library.simpleui_xml.permission

import kr.open.library.simple_ui.core.viewmodel.BaseViewModelEvent

class PermissionsActivityVm : BaseViewModelEvent<PermissionsActivityVmEvent>() {

    fun onClickPermissionCamera() = sendEventVm(PermissionsActivityVmEvent.OnClickPermissionsCamera)

    fun onClickPermissionLocation() = sendEventVm(PermissionsActivityVmEvent.OnClickPermissionsLocation)

    fun onClickPermissionMulti() = sendEventVm(PermissionsActivityVmEvent.OnClickPermissionsMulti)
}