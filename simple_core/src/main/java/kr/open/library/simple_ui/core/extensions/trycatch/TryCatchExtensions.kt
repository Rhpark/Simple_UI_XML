package kr.open.library.simple_ui.core.extensions.trycatch

import android.os.Build
import kotlinx.coroutines.CancellationException
import kr.open.library.simple_ui.core.logcat.Logx

/**
 * Executes [block] safely without a return value.<br><br>
 * 반환값이 없는 [block]을 안전하게 실행합니다.<br>
 *
 * Rules:<br>
 * - `CancellationException` and `Error` are rethrown.<br>
 * - Other `Exception` types are logged and swallowed.<br><br>
 * 동작 규칙:<br>
 * - `CancellationException`, `Error`는 그대로 다시 던집니다.<br>
 * - 그 외 `Exception`은 로그 후 무시합니다.<br>
 */
public inline fun safeCatch(block: () -> Unit) {
    try {
        block()
    } catch (e: CancellationException) {
        throw e
    } catch (e: Error) {
        throw e
    } catch (e: Exception) {
        Logx.e("safeCatch(Unit): ${e.message}", e)
    }
}

/**
 * Executes [block] safely and returns [defaultValue] when an exception occurs.<br><br>
 * [block] 실행 중 예외가 발생하면 [defaultValue]를 반환합니다.<br>
 *
 * Rules:<br>
 * - `CancellationException` and `Error` are rethrown.<br>
 * - Other `Exception` types are logged and replaced with [defaultValue].<br><br>
 * 동작 규칙:<br>
 * - `CancellationException`, `Error`는 그대로 다시 던집니다.<br>
 * - 그 외 `Exception`은 로그 후 [defaultValue]를 반환합니다.<br>
 */
public inline fun <T> safeCatch(
    defaultValue: T,
    block: () -> T,
): T = try {
    block()
} catch (e: CancellationException) {
    throw e
} catch (e: Error) {
    throw e
} catch (e: Exception) {
    Logx.e("safeCatch(defaultValue): ${e.message}", e)
    defaultValue
}

/**
 * Executes [block] safely and delegates fallback handling to [onCatch].<br><br>
 * [block] 실행 중 예외가 발생하면 [onCatch]로 대체 값을 계산합니다.<br>
 *
 * Rules:<br>
 * - `CancellationException` and `Error` are rethrown.<br>
 * - Other `Exception` types are logged and passed to [onCatch].<br><br>
 * 동작 규칙:<br>
 * - `CancellationException`, `Error`는 그대로 다시 던집니다.<br>
 * - 그 외 `Exception`은 로그 후 [onCatch]에 전달합니다.<br>
 */
public inline fun <T> safeCatch(block: () -> T, onCatch: ((Exception) -> T)): T = try {
    block()
} catch (e: CancellationException) {
    throw e
} catch (e: Error) {
    throw e
} catch (e: Exception) {
    Logx.e("safeCatch(onCatch): ${e.message}", e)
    onCatch(e)
}

/**
 * Throws [IndexOutOfBoundsException] when [value] is false.<br><br>
 * [value]가 false면 [IndexOutOfBoundsException]을 던집니다.<br>
 *
 * Example:<br>
 * `requireInBounds(position in 0 until size) { "Invalid position: $position" }`<br><br>
 * 예시:<br>
 * `requireInBounds(position in 0 until size) { "잘못된 위치: $position" }`<br>
 */
public inline fun requireInBounds(value: Boolean, lazyMessage: () -> Any) {
    if (value) return
    val message = lazyMessage()
    throw IndexOutOfBoundsException(message.toString())
}

/**
 * Throws [UnsupportedOperationException] when current SDK is lower than [sdkVersion].<br><br>
 * 현재 SDK가 [sdkVersion]보다 낮으면 [UnsupportedOperationException]을 던집니다.<br>
 */
public inline fun requireMinSdkVersion(sdkVersion: Int) {
    if (sdkVersion <= Build.VERSION.SDK_INT) return
    throwMinSdkVersion(sdkVersion)
}

public inline fun throwMinSdkVersion(sdkVersion: Int): Nothing =
    throw UnsupportedOperationException("require Min SDK version $sdkVersion but current SDK version is ${Build.VERSION.SDK_INT}")

/**
 * Throws [UnsupportedOperationException] when current SDK is higher than [sdkVersion].<br><br>
 * 현재 SDK가 [sdkVersion]보다 높으면 [UnsupportedOperationException]을 던집니다.<br>
 */
public inline fun requireMaxSdkVersion(sdkVersion: Int) {
    if (sdkVersion >= Build.VERSION.SDK_INT) return
    throw UnsupportedOperationException("require Max SDK version $sdkVersion but current SDK version is ${Build.VERSION.SDK_INT}")
}
