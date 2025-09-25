package kr.open.library.simpleui_xml.permissions_origin

sealed interface PermissionEvent {
    data object OnClickCameraPermission : PermissionEvent
    data object OnClickLocationPermission : PermissionEvent
    data object OnClickMultiplePermissions : PermissionEvent
}