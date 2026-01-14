package kr.open.library.simple_ui.xml.ui.components.fragment.root

import android.os.Bundle
import androidx.annotation.CallSuper
import androidx.fragment.app.Fragment
import kr.open.library.simple_ui.core.permissions.model.PermissionDeniedItem
import kr.open.library.simple_ui.core.permissions.model.PermissionRationaleRequest
import kr.open.library.simple_ui.core.permissions.model.PermissionSettingsRequest
import kr.open.library.simple_ui.xml.permissions.api.PermissionRequester
import kr.open.library.simple_ui.xml.permissions.register.PermissionRequestInterface

/**
 * Root Fragment class providing runtime permission management.<br>
 * Serves as the foundation for all Fragment classes in the library.<br><br>
 * 런타임 권한 관리를 제공하는 루트 Fragment 클래스입니다.<br>
 * 라이브러리의 모든 Fragment 클래스의 기반이 됩니다.<br>
 *
 * @see BaseFragment For simple layout-based Fragment.<br><br>
 *      간단한 레이아웃 기반 Fragment.<br>
 *
 * @see ParentsBindingFragment For binding-enabled Fragment base class.<br><br>
 *      바인딩을 지원하는 Fragment 기본 클래스.<br>
 */
abstract class RootFragment :
    Fragment(),
    PermissionRequestInterface {
    private lateinit var permissionRequester: PermissionRequester

    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        permissionRequester = PermissionRequester(this)
        permissionRequester.restoreState(savedInstanceState)
    }

    @CallSuper
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        permissionRequester.saveState(outState)
    }

    /**
     * Requests permissions using the delegate.<br>
     * Call only after the Fragment is attached (isAdded == true).<br><br>
     * 델리게이트를 사용하여 권한을 요청합니다.<br>
     * Fragment가 attach된 이후(isAdded == true)에만 호출하세요.<br>
     *
     * @param permissions List of permissions to request.<br><br>
     *                    요청할 권한 목록.<br>
     * @param onDeniedResult Callback for denied results.<br><br>
     *                       거부 결과에 대한 콜백.<br>
     */
    @CallSuper
    final override fun requestPermissions(
        permissions: List<String>,
        onDeniedResult: (List<PermissionDeniedItem>) -> Unit,
        onRationaleNeeded: ((PermissionRationaleRequest) -> Unit)?,
        onNavigateToSettings: ((PermissionSettingsRequest) -> Unit)?
    ) {
        check(::permissionRequester.isInitialized) { "permissionRequester is not initialized. Please call super.onCreate() first." }
        check(isAdded) { "Permission request must be called after Fragment is attached (isAdded == true)." }
        permissionRequester.requestPermissions(permissions, onDeniedResult, onRationaleNeeded, onNavigateToSettings)
    }

    /**
     * Requests permissions using the delegate.<br>
     * Call only after the Fragment is attached (isAdded == true).<br><br>
     * 델리게이트를 사용하여 권한을 요청합니다.<br>
     * Fragment가 attach된 이후(isAdded == true)에만 호출하세요.<br>
     *
     * @param permissions Permissions to request.<br><br>
     *                    요청할 권한 목록입니다.<br>
     * @param onDeniedResult Callback invoked with denied items.<br><br>
     *                       거부 항목을 전달받는 콜백입니다.<br>
     */
    @CallSuper
    final override fun requestPermissions(permissions: List<String>, onDeniedResult: (List<PermissionDeniedItem>) -> Unit) {
        check(::permissionRequester.isInitialized) { "permissionRequester is not initialized. Please call super.onCreate() first." }
        check(isAdded) { "Permission request must be called after Fragment is attached (isAdded == true)." }
        permissionRequester.requestPermissions(permissions, onDeniedResult, null, null)
    }
}
