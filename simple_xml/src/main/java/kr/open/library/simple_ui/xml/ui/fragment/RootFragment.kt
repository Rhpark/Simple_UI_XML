package kr.open.library.simple_ui.xml.ui.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import kr.open.library.simple_ui.xml.permissions.register.PermissionRequester
import kr.open.library.simple_ui.xml.permissions.register.PermissionDelegate

/**
 * Root Fragment class providing runtime permission management.<br>
 * Serves as the foundation for all Fragment classes in the library.<br><br>
 * 런타임 권한 관리를 제공하는 루트 Fragment 클래스입니다.<br>
 * 라이브러리의 모든 Fragment 클래스의 기반이 됩니다.<br>
 *
 * Features:<br>
 * - Runtime permission management via PermissionDelegate<br>
 * - Lifecycle-aware permission state preservation<br>
 * - Automatic permission result handling<br><br>
 * 기능:<br>
 * - PermissionDelegate를 통한 런타임 권한 관리<br>
 * - 생명주기 인식 권한 상태 보존<br>
 * - 자동 권한 결과 처리<br>
 *
 * @see BaseFragment For simple layout-based Fragment.<br><br>
 *      간단한 레이아웃 기반 Fragment는 BaseFragment를 참조하세요.<br>
 *
 * @see BaseBindingFragment For DataBinding-enabled Fragment.<br><br>
 *      DataBinding을 사용하는 Fragment는 BaseBindingFragment를 참조하세요.<br>
 */
abstract class RootFragment: Fragment(), PermissionRequester {

    /**
     * Delegate for handling runtime permission requests.<br><br>
     * 런타임 권한 요청을 처리하는 델리게이트입니다.<br>
     */
    protected lateinit var permissionDelegate : PermissionDelegate<Fragment>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        permissionDelegate = PermissionDelegate(this)
        permissionDelegate.onRestoreInstanceState(savedInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        permissionDelegate.onSaveInstanceState(outState)
    }

    override fun onRequestPermissions(permissions: List<String>, onResult: (deniedPermissions: List<String>) -> Unit) {
        permissionDelegate.requestPermissions(permissions, onResult)
    }
}
