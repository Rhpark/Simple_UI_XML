package kr.open.library.simple_ui.presenter.ui.fragment

import androidx.fragment.app.Fragment
import kr.open.library.simple_ui.permissions.register.PermissionRequester
import kr.open.library.simple_ui.permissions.register.PermissionDelegate

abstract class RootFragment: Fragment(), PermissionRequester {

    /************************
     *   Permission Check   *
     ************************/
    private val permissionDelegate : PermissionDelegate<Fragment> by lazy { PermissionDelegate(this) }



    override fun onRequestPermissions(permissions: List<String>, onResult: (deniedPermissions: List<String>) -> Unit) {
        permissionDelegate.requestPermissions(permissions, onResult)
    }
}