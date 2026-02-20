package kr.open.library.simple_ui.xml.system_manager.extensions
/****************************
 * SystemService Controller (XML-specific)*
 ****************************/
import android.content.Context
import android.view.Window
import androidx.annotation.MainThread
import kr.open.library.simple_ui.xml.R
import kr.open.library.simple_ui.xml.internal.thread.assertMainThreadDebug
import kr.open.library.simple_ui.xml.system_manager.controller.softkeyboard.SoftKeyboardController
import kr.open.library.simple_ui.xml.system_manager.controller.systembar.SystemBarController
import kr.open.library.simple_ui.xml.system_manager.controller.window.FloatingViewController
import kr.open.library.simple_ui.xml.system_manager.info.display.DisplayInfo

/**
 * Creates a new SoftKeyboardController instance for this Context.<br><br>
 * 이 Context에 대한 새로운 SoftKeyboardController 인스턴스를 생성합니다.<br>
 *
 * @return New SoftKeyboardController instance.<br><br>
 *         새로운 SoftKeyboardController 인스턴스.<br>
 */
public fun Context.getSoftKeyboardController(): SoftKeyboardController = SoftKeyboardController(this)

/**
 * Creates a new FloatingViewController instance for this Context.<br><br>
 * 이 Context에 대한 새로운 FloatingViewController 인스턴스를 생성합니다.<br>
 *
 * @return New FloatingViewController instance.<br><br>
 *         새로운 FloatingViewController 인스턴스.<br>
 */
public fun Context.getFloatingViewController(): FloatingViewController = FloatingViewController(this)

/**
 * Creates a new DisplayInfo instance for this Context.<br><br>
 * 이 Context에 대한 새로운 DisplayInfo 인스턴스를 생성합니다.<br>
 *
 * @return New DisplayInfo instance.<br><br>
 *         새로운 DisplayInfo 인스턴스.<br>
 */
public fun Context.getDisplayInfo(): DisplayInfo = DisplayInfo(this)

/**
 * Gets or creates a cached SystemBarController instance for this Window.<br>
 * Uses View.setTag() to cache the controller instance in the decorView, ensuring one controller per Window.<br><br>
 * 이 Window에 대한 캐시된 SystemBarController 인스턴스를 가져오거나 생성합니다.<br>
 * View.setTag()를 사용하여 decorView에 컨트롤러 인스턴스를 캐싱하여 Window당 하나의 컨트롤러를 보장합니다.<br>
 *
 * **Why caching is needed / 캐싱이 필요한 이유:**<br>
 * - SystemBarController creates internal helper instances (StatusBarHelper, NavigationBarHelper)<br>
 * - Creating multiple instances for the same Window causes memory waste and potential conflicts<br>
 * - Caching ensures consistent state management across multiple calls<br><br>
 * - SystemBarController는 내부 헬퍼 인스턴스를 생성합니다 (StatusBarHelper, NavigationBarHelper)<br>
 * - 동일한 Window에 대해 여러 인스턴스를 생성하면 메모리 낭비 및 잠재적 충돌이 발생합니다<br>
 * - 캐싱은 여러 호출에 걸쳐 일관된 상태 관리를 보장합니다<br>
 *
 * **Important / 주의사항:**<br>
 * - The controller is NOT automatically destroyed. You must manually call `onDestroy()` when done.<br>
 * - Recommended to call `onDestroy()` in Activity/Dialog's `onDestroy()` lifecycle method.<br>
 * - Alternatively, use `destroySystemBarControllerCache()` for automatic cleanup and cache removal.<br><br>
 * - 컨트롤러는 자동으로 파괴되지 않습니다. 사용 완료 시 수동으로 `onDestroy()`를 호출해야 합니다.<br>
 * - Activity/Dialog의 `onDestroy()` 라이프사이클 메서드에서 `onDestroy()` 호출을 권장합니다.<br>
 * - 또는 자동 정리 및 캐시 제거를 위해 `destroySystemBarControllerCache()`를 사용하세요.<br>
 *
 * **Usage / 사용법:**<br>
 * ```kotlin
 * class MyActivity : AppCompatActivity() {
 *     private lateinit var systemBarController: SystemBarController
 *
 *     override fun onCreate(savedInstanceState: Bundle?) {
 *         super.onCreate(savedInstanceState)
 *
 *         // Get cached controller (or create new one)
 *         systemBarController = window.getSystemBarController()
 *         systemBarController.setStatusBarColor(Color.RED)
 *     }
 *
 *     override fun onDestroy() {
 *         systemBarController.onDestroy()
 *         super.onDestroy()
 *     }
 * }
 * ```<br><br>
 *
 * @return Cached SystemBarController instance, or newly created instance if not cached.<br><br>
 *         캐시된 SystemBarController 인스턴스, 캐시되지 않은 경우 새로 생성된 인스턴스.<br>
 */
@MainThread
public fun Window.getSystemBarController(): SystemBarController {
    assertMainThreadDebug("Window.getSystemBarController")
    val decor = decorView

    // ✅ Window당 1개: decorView에 컨트롤러를 캐싱
    val cached = decor.getTag(R.id.tag_system_bar_controller) as? SystemBarController
    if (cached != null) return cached

    return SystemBarController(this).also { controller ->
        decor.setTag(R.id.tag_system_bar_controller, controller)
    }
}

/**
 * Destroys and clears the cached SystemBarController instance from this Window.<br>
 * Automatically calls `onDestroy()` on the cached controller before removing it.<br>
 * After calling this, the next `getSystemBarController()` call will create a new instance.<br><br>
 * 이 Window에서 캐시된 SystemBarController 인스턴스를 파괴하고 제거합니다.<br>
 * 제거 전에 캐시된 컨트롤러의 `onDestroy()`를 자동으로 호출합니다.<br>
 * 호출 후 다음 `getSystemBarController()` 호출 시 새 인스턴스가 생성됩니다.<br>
 *
 * **Important / 주의사항:**<br>
 * - This method automatically handles cleanup by calling `onDestroy()` internally.<br>
 * - You do NOT need to manually call `controller.onDestroy()` before this method.<br>
 * - Use this when you want to force recreate the controller or during cleanup.<br><br>
 * - 이 메서드는 내부적으로 `onDestroy()`를 호출하여 자동으로 정리를 처리합니다.<br>
 * - 이 메서드 호출 전에 수동으로 `controller.onDestroy()`를 호출할 필요가 없습니다.<br>
 * - 컨트롤러를 강제로 재생성하거나 정리 중에 사용하세요.<br>
 *
 * **Usage / 사용법:**<br>
 * ```kotlin
 * // Automatic cleanup
 * window.destroySystemBarControllerCache()
 *
 * // Next call creates new instance
 * val newController = window.getSystemBarController()
 * ```<br><br>
 */
@MainThread
public fun Window.destroySystemBarControllerCache() {
    assertMainThreadDebug("Window.destroySystemBarControllerCache")
    val decor = decorView
    val controller = decor.getTag(R.id.tag_system_bar_controller) as? SystemBarController
    controller?.onDestroy()
    decor.setTag(R.id.tag_system_bar_controller, null)
}
