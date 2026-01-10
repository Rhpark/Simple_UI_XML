package kr.open.library.simple_ui.xml.ui.components.fragment.root

import android.os.Bundle
import androidx.annotation.CallSuper
import androidx.fragment.app.Fragment
import kr.open.library.simple_ui.xml.permissions.register.PermissionDelegate
import kr.open.library.simple_ui.xml.permissions.register.PermissionRequester

/**
 * Root Fragment class providing runtime permission management.<br>
 * Serves as the foundation for all Fragment classes in the library.<br><br>
 * 런타임 권한 관리를 제공하는 루트 Fragment 클래스입니다.<br>
 * 라이브러리의 모든 Fragment 클래스의 기반이 됩니다.<br>
 *
 * **Features / 기능:**<br>
 * - Runtime permission management via PermissionDelegate<br>
 * - Lifecycle-aware permission state preservation<br>
 * - Automatic permission result handling<br><br>
 * - PermissionDelegate를 통한 런타임 권한 관리<br>
 * - 생명주기 인식 권한 상태 보존<br>
 * - 자동 권한 결과 처리<br>
 *
 * **Important notes / 주의사항:**<br>
 * - Permission requests must be made only after the Fragment is attached (isAdded == true).<br><br>
 * - 권한 요청은 Fragment가 attach된 이후(isAdded == true)에만 수행해야 합니다.<br>
 *
 * @see BaseFragment For simple layout-based Fragment.<br><br>
 *      간단한 레이아웃 기반 Fragment는 BaseFragment를 참조하세요.<br>
 *
 * @see ParentsBindingFragment For binding-enabled Fragment base class.<br><br>
 *      바인딩을 지원하는 Fragment 기본 클래스는 ParentsBindingFragment를 참조하세요.<br>
 */
abstract class RootFragment :
    Fragment(),
    PermissionRequester {
    /**
     * Delegate for handling runtime permission requests.<br><br>
     * 런타임 권한 요청을 처리하는 델리게이트입니다.<br>
     */
    private lateinit var permissionDelegate: PermissionDelegate<Fragment>

    /**
     * Initializes the PermissionDelegate and restores state.<br><br>
     * PermissionDelegate를 초기화하고 상태를 복원합니다.<br>
     */
    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        permissionDelegate = PermissionDelegate(this)
        permissionDelegate.onRestoreInstanceState(savedInstanceState)
    }

    /**
     * Saves the state of the PermissionDelegate.<br><br>
     * PermissionDelegate의 상태를 저장합니다.<br>
     */
    @CallSuper
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        permissionDelegate.onSaveInstanceState(outState)
    }

    /**
     * Requests permissions using the delegate.<br><br>
     * 델리게이트를 사용하여 권한을 요청합니다.<br>
     *
     * **Important / 주의사항:**<br>
     * - Call only after the Fragment is attached (isAdded == true).<br>
     * - Calling before attachment or after detachment may throw because PermissionDelegate uses requireContext().<br><br>
     * - Fragment가 attach된 이후(isAdded == true)에만 호출하세요.<br>
     * - attach 이전/분리 이후 호출 시 PermissionDelegate가 requireContext()를 호출해 크래시가 발생할 수 있습니다.<br>
     *
     * @param permissions List of permissions to request.<br><br>
     *                    요청할 권한 목록.<br>
     * @param onResult Callback for permission results.<br><br>
     *                 권한 결과에 대한 콜백.<br>
     */
    @CallSuper
    final override fun onRequestPermissions(permissions: List<String>, onResult: (deniedPermissions: List<String>) -> Unit) {
        check(::permissionDelegate.isInitialized) {
            "PermissionDelegate is not initialized. Please call super.onCreate() first."
        }
        permissionDelegate.requestPermissions(permissions, onResult)
    }
}
