package kr.open.library.simple_ui.xml.ui.activity.root

import android.os.Bundle
import android.os.PersistableBundle
import androidx.annotation.CallSuper
import androidx.appcompat.app.AppCompatActivity
import kr.open.library.simple_ui.xml.permissions.register.PermissionDelegate
import kr.open.library.simple_ui.xml.permissions.register.PermissionRequester

/**
 * Root Activity class providing comprehensive permission management and early initialization hooks.<br>
 * Serves as the base for all Activity classes in the library with API version awareness.<br><br>
 *
 * 포괄적인 권한 관리 및 조기 초기화 훅을 제공하는 루트 Activity 클래스입니다.<br>
 * API 버전을 인식하는 구현으로 라이브러리의 모든 Activity 클래스의 기반이 됩니다.<br>
 *
 * **Features / 기능:**<br>
 * - Runtime permission management via PermissionDelegate<br>
 * - Lifecycle-aware permission state preservation<br>
 * - Early initialization hook (beforeOnCreated) for theme/window setup before super.onCreate()<br>
 * - Serves as the parent class for ParentsBindingActivity, which provides binding lifecycle for ViewBinding/DataBinding activities<br><br>
 * - PermissionDelegate를 통한 런타임 권한 관리<br>
 * - 생명주기 인식 권한 상태 보존<br>
 * - super.onCreate() 이전에 테마/윈도우 설정을 위한 조기 초기화 훅 (beforeOnCreated)<br>
 * - ViewBinding/DataBinding Activity에 바인딩 생명주기를 제공하는 ParentsBindingActivity의 부모 클래스 역할<br>
 *
 * @see ParentsBindingActivity For the abstract parent class of all binding-enabled activities.<br><br>
 *      모든 바인딩 지원 Activity의 추상 부모 클래스는 ParentsBindingActivity를 참조하세요.<br>
 *
 * @see BaseActivity For simple layout-based Activity.<br><br>
 *      간단한 레이아웃 기반 Activity는 BaseActivity를 참조하세요.<br>
 *
 * @see BaseDataBindingActivity For DataBinding-enabled Activity.<br><br>
 *      DataBinding을 사용하는 Activity는 BaseDataBindingActivity를 참조하세요.<br>
 *
 * @see BaseViewBindingActivity For ViewBinding-enabled Activity.<br><br>
 *      ViewBinding을 사용하는 Activity는 BaseViewBindingActivity를 참조하세요.<br>
 */
abstract class RootActivity :
    AppCompatActivity(),
    PermissionRequester {
    /**
     * Delegate for handling runtime permission requests.<br><br>
     * 런타임 권한 요청을 처리하는 델리게이트입니다.<br>
     */
    private lateinit var permissionDelegate: PermissionDelegate<AppCompatActivity>

    /**
     * Requests runtime permissions and returns the result via callback.
     * Delegates to PermissionDelegate for actual permission handling.<br><br>
     * 런타임 권한을 요청하고 콜백을 통해 결과를 반환합니다.<br>
     * 실제 권한 처리는 PermissionDelegate에 위임합니다.<br>
     *
     * @param permissions The list of permissions to request.<br><br>
     *                    요청할 권한 목록.<br>
     * @param onResult Callback invoked with the list of denied permissions after the request completes.<br><br>
     *                 요청 완료 후 거부된 권한 목록과 함께 호출되는 콜백.<br>
     */
    @CallSuper
    final override fun onRequestPermissions(permissions: List<String>, onResult: (deniedPermissions: List<String>) -> Unit) {
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
     *                           이 Bundle에는 onSaveInstanceState에서 가장 최근에 제공된 데이터가 포함됩니다.<br>
     */
    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        beforeOnCreated(savedInstanceState)
        super.onCreate(savedInstanceState)
        permissionDelegate = PermissionDelegate(this)
        permissionDelegate.onRestoreInstanceState(savedInstanceState)
    }

    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        beforeOnCreated(savedInstanceState)
        super.onCreate(savedInstanceState)
        permissionDelegate = PermissionDelegate(this)
        permissionDelegate.onRestoreInstanceState(savedInstanceState)
    }

    /**
     * Called BEFORE super.onCreate() for early initialization such as Theme or Window configuration.<br>
     * ⚠️ WARNING: Most Activity resources (getString, getResources, FragmentManager, etc.)
     * are NOT available at this point.<br><br>
     * super.onCreate() 전에 호출되며, Theme이나 Window 설정 등 조기 초기화에 사용합니다.<br>
     * ⚠️ 경고: 이 시점에서는 대부분의 Activity 리소스(getString, getResources, FragmentManager 등)를
     * 사용할 수 없습니다.<br>
     *
     * Usage example / 사용 예시:<br>
     * ```kotlin
     * override fun beforeOnCreated(savedInstanceState: Bundle?) {
     *     setTheme(R.style.MyCustomTheme)  // ✅ OK
     *     window.requestFeature(Window.FEATURE_NO_TITLE)  // ✅ OK
     *     // getString(R.string.app_name)  // ❌ Crash!
     * }
     * ```
     *
     * @param savedInstanceState The saved instance state bundle, if any.<br><br>
     *                           저장된 인스턴스 상태 번들 (있는 경우).<br><br>
     */
    protected open fun beforeOnCreated(savedInstanceState: Bundle?) {}

    /**
     * Called to retrieve per-instance state from an activity before being killed.
     * Saves the current permission state to the Bundle.<br><br>
     * 액티비티가 종료되기 전에 인스턴스별 상태를 검색하기 위해 호출됩니다.<br>
     * 현재 권한 상태를 Bundle에 저장합니다.<br>
     *
     * @param outState Bundle in which to place your saved state.<br><br>
     *                 저장된 상태를 배치할 Bundle.<br>
     */
    @CallSuper
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        permissionDelegate.onSaveInstanceState(outState)
    }
}
