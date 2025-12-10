package kr.open.library.simple_ui.xml.extensions.fragment

import android.content.Context
import android.view.View
import androidx.fragment.app.Fragment
import kr.open.library.simple_ui.core.logcat.Logx

/**
 * Executes the given block if the Fragment's context is not null.<br>
 * Logs an error if the context is null.<br><br>
 * Fragment의 context가 null이 아닌 경우 주어진 블록을 실행합니다.<br>
 * context가 null이면 에러를 로깅합니다.<br>
 *
 * @param errorMessage Custom error message to log when context is null.<br><br>
 *                     context가 null일 때 로깅할 커스텀 에러 메시지.<br>
 *
 * @param block The block to execute with the non-null context.<br><br>
 *              null이 아닌 context로 실행할 블록.<br>
 *
 * @return true if the block was executed, false if context was null.<br><br>
 *         블록이 실행되었으면 true, context가 null이면 false.<br>
 */
public inline fun Fragment.withContext(
    errorMessage: String = "Fragment context is null",
    block: (Context) -> Unit,
): Boolean = this.context?.let {
    block(it)
    true
} ?: run {
    Logx.e(errorMessage)
    false
}

/**
 * Executes the given block if the Fragment's view is not null.<br>
 * Logs an error if the view is null.<br><br>
 * Fragment의 view가 null이 아닌 경우 주어진 블록을 실행합니다.<br>
 * view가 null이면 에러를 로깅합니다.<br>
 *
 * @param errorMessage Custom error message to log when view is null.<br><br>
 *                     view가 null일 때 로깅할 커스텀 에러 메시지.<br>
 *
 * @param block The block to execute with the non-null view.<br><br>
 *              null이 아닌 view로 실행할 블록.<br>
 *
 * @return true if the block was executed, false if view was null.<br><br>
 *         블록이 실행되었으면 true, view가 null이면 false.<br>
 */
public inline fun Fragment.withView(
    errorMessage: String = "Fragment view is null",
    block: (View) -> Unit,
): Boolean = this.view?.let {
    block(it)
    true
} ?: run {
    Logx.e(errorMessage)
    false
}

/**
 * Executes the given block and returns a result if the Fragment's context is not null.<br>
 * Returns null and logs an error if the context is null.<br><br>
 * Fragment의 context가 null이 아닌 경우 주어진 블록을 실행하고 결과를 반환합니다.<br>
 * context가 null이면 null을 반환하고 에러를 로깅합니다.<br>
 *
 * @param T The type of the result returned by the block.<br><br>
 *          블록이 반환하는 결과의 타입.<br>
 *
 * @param errorMessage Custom error message to log when context is null.<br><br>
 *                     context가 null일 때 로깅할 커스텀 에러 메시지.<br>
 *
 * @param block The block to execute with the non-null context.<br><br>
 *              null이 아닌 context로 실행할 블록.<br>
 *
 * @return The result of the block, or null if context was null.<br><br>
 *         블록의 결과, 또는 context가 null이면 null.<br>
 */
public inline fun <T> Fragment.withContextResult(
    errorMessage: String = "Fragment context is null",
    block: (Context) -> T,
): T? = this.context?.let(block) ?: run {
    Logx.e(errorMessage)
    null
}

/**
 * Executes the given block and returns a result if the Fragment's view is not null.<br>
 * Returns null and logs an error if the view is null.<br><br>
 * Fragment의 view가 null이 아닌 경우 주어진 블록을 실행하고 결과를 반환합니다.<br>
 * view가 null이면 null을 반환하고 에러를 로깅합니다.<br>
 *
 * @param T The type of the result returned by the block.<br><br>
 *          블록이 반환하는 결과의 타입.<br>
 *
 * @param errorMessage Custom error message to log when view is null.<br><br>
 *                     view가 null일 때 로깅할 커스텀 에러 메시지.<br>
 *
 * @param block The block to execute with the non-null view.<br><br>
 *              null이 아닌 view로 실행할 블록.<br>
 *
 * @return The result of the block, or null if view was null.<br><br>
 *         블록의 결과, 또는 view가 null이면 null.<br>
 */
public inline fun <T> Fragment.withViewResult(
    errorMessage: String = "Fragment view is null",
    block: (View) -> T,
): T? = this.view?.let(block) ?: run {
    Logx.e(errorMessage)
    null
}
