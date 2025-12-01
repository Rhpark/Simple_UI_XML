package kr.open.library.simpleui_xml.permission

sealed interface PermissionsActivityVmEvent {
    data object OnClickPermissionsCamera : PermissionsActivityVmEvent

    data object OnClickPermissionsLocation : PermissionsActivityVmEvent

    data object OnClickPermissionsMulti : PermissionsActivityVmEvent
}
