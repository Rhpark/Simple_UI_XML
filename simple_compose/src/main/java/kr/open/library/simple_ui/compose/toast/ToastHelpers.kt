package kr.open.library.simple_ui.compose.toast

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import kr.open.library.simple_ui.core.extensions.trycatch.safeCatch

// ---------------------------------------------------------------------------
// 공개 API
// Public API
// ---------------------------------------------------------------------------

/**
 * Composable에서 Toast를 표시하는 함수를 생성합니다.<br>
 * Creates a function that shows a Toast from a Composable.<br>
 *
 * 호출부마다 [LocalContext]를 조회하고 [Toast.makeText]를 구성하는 반복을 제거합니다.
 * 반환된 함수는 컴포지션 동안 동일한 인스턴스로 유지되므로 람다 파라미터로 전달해도
 * 불필요한 재구성을 유발하지 않습니다.<br>
 * Removes the repetition of resolving [LocalContext] and building [Toast.makeText] at every call
 * site. The returned function keeps the same instance for the composition lifetime, so passing it
 * as a lambda parameter does not cause unnecessary recomposition.<br>
 *
 * 이전 Toast를 취소하지 않으므로 연속 호출 시 Android 기본 정책에 따라 큐에 쌓입니다.
 * 연타를 막아야 하면 호출부에서 차단하세요.<br>
 * Previous Toasts are not cancelled, so consecutive calls queue up per Android's default policy.
 * Guard the call site when rapid repeats must be prevented.<br>
 *
 * **사용 예시 / Usage example**:
 * ```kotlin
 * val showToast = rememberToast()
 * ExampleButton("저장") { showToast("저장했습니다") }
 * ```
 *
 * @param durationLong Whether to use [Toast.LENGTH_LONG] instead of [Toast.LENGTH_SHORT].<br><br>
 *                     [Toast.LENGTH_SHORT] 대신 [Toast.LENGTH_LONG]을 사용할지 여부입니다.<br>
 * @return Function that shows a Toast with the given message.<br><br>
 *         전달한 메시지로 Toast를 표시하는 함수입니다.<br>
 */
@Composable
public fun rememberToast(durationLong: Boolean = false): (CharSequence) -> Unit {
    val context = LocalContext.current
    return remember(context, durationLong) {
        { message -> context.showToast(message, durationLong) }
    }
}

/**
 * Toast를 즉시 표시합니다.<br>
 * Shows a Toast immediately.<br>
 *
 * Composable 외부(ViewModel 콜백, 이벤트 핸들러 등)에서 [Context]를 이미 보유한 경우 사용합니다.
 * 표시 실패는 로그로 남기고 무시합니다.<br>
 * Use this when a [Context] is already available outside a Composable, such as in a ViewModel
 * callback or an event handler. Failures are logged and ignored.<br>
 *
 * @param message Message to display.<br><br>
 *                표시할 메시지입니다.<br>
 * @param durationLong Whether to use [Toast.LENGTH_LONG] instead of [Toast.LENGTH_SHORT].<br><br>
 *                     [Toast.LENGTH_SHORT] 대신 [Toast.LENGTH_LONG]을 사용할지 여부입니다.<br>
 */
public fun Context.showToast(message: CharSequence, durationLong: Boolean = false) {
    val duration = if (durationLong) Toast.LENGTH_LONG else Toast.LENGTH_SHORT
    safeCatch {
        Toast.makeText(this, message, duration).show()
    }
}
