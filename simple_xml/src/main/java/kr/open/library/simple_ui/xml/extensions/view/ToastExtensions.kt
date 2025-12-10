/**
 * Toast display extension functions for Context and Fragment.<br>
 * Provides convenient methods to show short and long duration Toast messages.<br><br>
 * Context와 Fragment를 위한 Toast 표시 확장 함수입니다.<br>
 * 짧은 시간과 긴 시간 Toast 메시지를 표시하는 편리한 메서드를 제공합니다.<br>
 *
 * Example usage:<br>
 * ```kotlin
 * // In Activity or Context
 * context.toastShowShort("Hello World")
 * context.toastShowLong("Long message")
 *
 * // In Fragment
 * fragment.toastShowShort("Fragment toast")
 * ```
 *
 * 사용 예시:<br>
 * ```kotlin
 * // Activity 또는 Context에서
 * context.toastShowShort("안녕하세요")
 * context.toastShowLong("긴 메시지")
 *
 * // Fragment에서
 * fragment.toastShowShort("Fragment 토스트")
 * ```
 */
package kr.open.library.simple_ui.xml.extensions.view

import android.content.Context
import android.widget.Toast
import androidx.fragment.app.Fragment
import kr.open.library.simple_ui.xml.extensions.fragment.withContext

/**
 * Creates and shows a short duration Toast message.<br><br>
 * 짧은 시간 Toast 메시지를 생성하고 표시합니다.<br>
 *
 * @param msg The message to display in the Toast.<br><br>
 *            Toast에 표시할 메시지.<br>
 */
public fun Context.toastShowShort(msg: CharSequence) {
    toastShort(msg).show()
}

/**
 * Creates and shows a long duration Toast message.<br><br>
 * 긴 시간 Toast 메시지를 생성하고 표시합니다.<br>
 *
 * @param msg The message to display in the Toast.<br><br>
 *            Toast에 표시할 메시지.<br>
 */
public fun Context.toastShowLong(msg: CharSequence) {
    toastLong(msg).show()
}

/**
 * Creates a short duration Toast without showing it.<br>
 * Allows further customization before displaying.<br><br>
 * 표시하지 않고 짧은 시간 Toast를 생성합니다.<br>
 * 표시하기 전에 추가 커스터마이징이 가능합니다.<br>
 *
 * @param msg The message to display in the Toast.<br><br>
 *            Toast에 표시할 메시지.<br>
 *
 * @return The created Toast instance.<br><br>
 *         생성된 Toast 인스턴스.<br>
 */
public fun Context.toastShort(msg: CharSequence): Toast = Toast.makeText(this, msg, Toast.LENGTH_SHORT)

/**
 * Creates a long duration Toast without showing it.<br>
 * Allows further customization before displaying.<br><br>
 * 표시하지 않고 긴 시간 Toast를 생성합니다.<br>
 * 표시하기 전에 추가 커스터마이징이 가능합니다.<br>
 *
 * @param msg The message to display in the Toast.<br><br>
 *            Toast에 표시할 메시지.<br>
 *
 * @return The created Toast instance.<br><br>
 *         생성된 Toast 인스턴스.<br>
 */
public fun Context.toastLong(msg: CharSequence): Toast = Toast.makeText(this, msg, Toast.LENGTH_LONG)

/**
 * Creates and shows a short duration Toast message from a Fragment.<br>
 * Logs an error if the Fragment's context is null.<br><br>
 * Fragment에서 짧은 시간 Toast 메시지를 생성하고 표시합니다.<br>
 * Fragment의 context가 null이면 에러를 로깅합니다.<br>
 *
 * @param msg The message to display in the Toast.<br><br>
 *            Toast에 표시할 메시지.<br>
 */
public fun Fragment.toastShowShort(msg: CharSequence) {
    withContext("Can not Toast Show, Fragment Context is null!!") {
        it.toastShowShort(msg)
    }
}

/**
 * Creates and shows a long duration Toast message from a Fragment.<br>
 * Logs an error if the Fragment's context is null.<br><br>
 * Fragment에서 긴 시간 Toast 메시지를 생성하고 표시합니다.<br>
 * Fragment의 context가 null이면 에러를 로깅합니다.<br>
 *
 * @param msg The message to display in the Toast.<br><br>
 *            Toast에 표시할 메시지.<br>
 */
public fun Fragment.toastShowLong(msg: CharSequence) {
    withContext("Can not Toast Show, Fragment Context is null!!") {
        it.toastShowLong(msg)
    }
}
