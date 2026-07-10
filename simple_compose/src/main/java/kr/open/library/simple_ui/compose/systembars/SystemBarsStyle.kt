package kr.open.library.simple_ui.compose.systembars

import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import kr.open.library.simple_ui.core.logcat.Logx

// ---------------------------------------------------------------------------
// 내부 상수
// Internal constants
// ---------------------------------------------------------------------------

private const val TAG = "SystemBarsStyle"

// ---------------------------------------------------------------------------
// 공개 Composable API
// Public Composable API
// ---------------------------------------------------------------------------

/**
 * 시스템 바 아이콘 명암(appearance)을 Composable 생명주기 동안 설정합니다.<br>
 * Sets the system bar icon appearance (light/dark icons) for the duration of this Composable's lifetime.<br>
 *
 * compileSdk 35(Android 15) enforced edge-to-edge 환경에서는 시스템 바 색상 설정 API
 * (`window.statusBarColor` 등)가 deprecated이므로 이 함수는 **아이콘 명암만** 제어합니다.<br>
 * Because `window.statusBarColor` and similar APIs are deprecated under compileSdk 35
 * (Android 15 enforced edge-to-edge), this function controls **icon appearance only**.<br>
 *
 * 이 호출 지점이 컴포지션에 진입할 때의 아이콘 명암 값을 캡처하고, 이탈할 때 해당 값으로 복원합니다.
 * 여러 `SystemBarsStyle`을 동시에 구성하면 복원 순서가 서로 영향을 줄 수 있으므로 한 Window에서는
 * 현재 화면을 대표하는 호출 한 곳만 활성화합니다.<br>
 * The icon appearance present when this call site enters the composition is captured and restored
 * when it leaves. Keep one active owner for the current screen in a Window; concurrent
 * `SystemBarsStyle` call sites can affect each other's restoration order.<br>
 *
 * Window를 얻을 수 없는 환경(프리뷰 등)에서는 아무 동작 없이 안전 종료하며 경고 로그를 남깁니다.<br>
 * If the Window cannot be obtained (e.g., preview environment), this function exits safely
 * without performing any action and logs a warning.<br>
 *
 * **사용 예시 / Usage example**:
 * ```kotlin
 * SystemBarsStyle(statusBarDarkIcons = true)
 * ```
 *
 * @param statusBarDarkIcons 상태 바 아이콘을 어둡게(true) 또는 밝게(false) 설정합니다.<br><br>
 *                           Set status bar icons to dark (true) or light (false).<br>
 * @param navigationBarDarkIcons 내비게이션 바 아이콘을 어둡게(true) 또는 밝게(false) 설정합니다.
 *                               기본값은 [statusBarDarkIcons]와 동일합니다.<br><br>
 *                               Set navigation bar icons to dark (true) or light (false).
 *                               Defaults to the same value as [statusBarDarkIcons].<br>
 */
@Composable
public fun SystemBarsStyle(
    statusBarDarkIcons: Boolean,
    navigationBarDarkIcons: Boolean = statusBarDarkIcons,
) {
    val view = LocalView.current
    val activity = LocalActivity.current
    val window = activity?.window

    if (window == null) {
        // Window 없음(프리뷰 등) — 설정 불가, 안전 종료
        // No window (e.g., preview) — cannot apply, exit safely
        LaunchedEffect(Unit) {
            Logx.w(
                TAG,
                "SystemBarsStyle: Window를 얻을 수 없습니다. 아무 동작도 수행하지 않습니다. " +
                    "Window could not be obtained — no action taken.",
            )
        }
        return
    }

    val controller = remember(window, view) { WindowCompat.getInsetsController(window, view) }

    // 원래 값 저장은 최초 1회, 복원은 컴포지션 이탈 시 1회
    // Save original values once; restore once when leaving the composition
    DisposableEffect(controller) {
        val originalStatusBarDarkIcons = controller.isAppearanceLightStatusBars
        val originalNavigationBarDarkIcons = controller.isAppearanceLightNavigationBars

        onDispose {
            controller.isAppearanceLightStatusBars = originalStatusBarDarkIcons
            controller.isAppearanceLightNavigationBars = originalNavigationBarDarkIcons
        }
    }

    // 파라미터가 바뀔 때마다 명암을 다시 적용
    // Re-apply the appearance whenever the parameters change
    LaunchedEffect(controller, statusBarDarkIcons, navigationBarDarkIcons) {
        controller.isAppearanceLightStatusBars = statusBarDarkIcons
        controller.isAppearanceLightNavigationBars = navigationBarDarkIcons
    }
}
