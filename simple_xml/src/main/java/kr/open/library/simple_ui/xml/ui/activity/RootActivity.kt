package kr.open.library.simple_ui.xml.ui.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kr.open.library.simple_ui.xml.permissions.register.PermissionDelegate
import kr.open.library.simple_ui.xml.permissions.register.PermissionRequester

/**
 * Root Activity class providing comprehensive permission management.<br>
 * 권한 관리를 종합적으로 제공하는 루트 Activity 클래스입니다.<br>
 * API 버전을 인식하는 구현으로 라이브러리의 모든 Activity 클래스의 기반이 됩니다.<br>
 *
 * Features:<br>
 * - Runtime permission management via PermissionDelegate<br>
 * - Lifecycle-aware permission state preservation<br><br>
 * 기능:<br>
 * - PermissionDelegate를 통한 런타임 권한 관리<br>
 * - 생명주기 인식 권한 상태 보존<br>
 *
 * @see BaseActivity For simple layout-based Activity.<br><br>
 *      간단한 레이아웃 기반 Activity는 BaseActivity를 참조하세요.<br>
 *
 * @see BaseBindingActivity For DataBinding-enabled Activity.<br><br>
 *      DataBinding을 사용하는 Activity는 BaseBindingActivity를 참조하세요.<br>
 */
abstract class RootActivity :
    AppCompatActivity(),
    PermissionRequester {
    /**
     * Delegate for handling runtime permission requests.<br><br>
     * 런타임 권한 요청을 처리하는 델리게이트입니다.<br>
     */
    protected lateinit var permissionDelegate: PermissionDelegate<AppCompatActivity>

    /**
     * Requests runtime permissions and returns the result via callback.
     * Delegates to PermissionDelegate for actual permission handling.<br><br>
     * 런타임 권한을 요청하고 콜백을 통해 결과를 반환합니다.<br>
     * 실제 권한 처리는 PermissionDelegate에 위임합니다.<br>
     *
     * @param permissions The list of permissions to request.<br><br>
     *                    요청할 권한 목록.
     * @param onResult Callback invoked with the list of denied permissions after the request completes.<br><br>
     *                 요청 완료 후 거부된 권한 목록과 함께 호출되는 콜백.
     */
    override fun onRequestPermissions(permissions: List<String>, onResult: (deniedPermissions: List<String>) -> Unit) {
        permissionDelegate.requestPermissions(permissions, onResult)
    }

    /**
     * Called when the activity is starting.
     * Initializes PermissionDelegate and restores permission state from savedInstanceState.<br><br>
     * 액티비티가 시작될 때 호출됩니다.<br>
     * PermissionDelegate를 초기화하고 savedInstanceState에서 권한 상태를 복원합니다.<br>
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down,
     *                           this Bundle contains the data it most recently supplied in onSaveInstanceState.<br><br>
     *                           액티비티가 이전에 종료된 후 다시 초기화되는 경우,
     *                           이 Bundle에는 onSaveInstanceState에서 가장 최근에 제공된 데이터가 포함됩니다.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        permissionDelegate = PermissionDelegate(this)
        permissionDelegate.onRestoreInstanceState(savedInstanceState)
        beforeOnCreated(savedInstanceState)
    }

    /**
     * Override this method to perform initialization before the standard onCreate logic.<br>
     * Called after super.onCreate() but before any child class initialization.<br><br>
     * 표준 onCreate 로직 전에 초기화를 수행하려면 이 메서드를 오버라이드하세요.<br>
     * super.onCreate() 후 자식 클래스 초기화 전에 호출됩니다.<br>
     *
     * @param savedInstanceState The saved instance state bundle, if any.<br><br>
     *                           저장된 인스턴스 상태 번들 (있는 경우).<br>
     */
    protected open fun beforeOnCreated(savedInstanceState: Bundle?) {}

    /**
     * Called to retrieve per-instance state from an activity before being killed.
     * Saves the current permission state to the Bundle.<br><br>
     * 액티비티가 종료되기 전에 인스턴스별 상태를 검색하기 위해 호출됩니다.<br>
     * 현재 권한 상태를 Bundle에 저장합니다.<br>
     *
     * @param outState Bundle in which to place your saved state.<br><br>
     *                 저장된 상태를 배치할 Bundle.
     */
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        permissionDelegate.onSaveInstanceState(outState)
    }
}
