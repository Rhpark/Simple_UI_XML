package kr.open.library.simple_ui.permissions


interface PermissionRequester {
    fun onRequestPermissions(permissions: List<String>, onResult: ((deniedPermissions: List<String>) -> Unit))
}