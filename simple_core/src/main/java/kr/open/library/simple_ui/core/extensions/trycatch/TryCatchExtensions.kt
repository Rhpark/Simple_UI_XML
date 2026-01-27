package kr.open.library.simple_ui.core.extensions.trycatch

import android.os.Build
import kotlinx.coroutines.CancellationException
import kr.open.library.simple_ui.core.logcat.Logx

/**
 * Executes a code block with safe exception handling, logging exceptions without returning a value.<br><br>
 * 안전한 예외 처리로 코드 블록을 실행하고, 반환값 없이 예외를 로깅합니다.<br>
 *
 * Exception handling rules:<br>
 * - CancellationException: Always rethrown (coroutine cancellation must propagate)<br>
 * - Error: Always rethrown (critical errors like OOM must not be caught)<br>
 * - Exception: Caught and logged via Logx<br><br>
 * 예외 처리 규칙:<br>
 * - CancellationException: 항상 재throw (코루틴 취소는 반드시 전파)<br>
 * - Error: 항상 재throw (OOM 등 치명적 에러는 절대 삼키지 않음)<br>
 * - Exception: 잡아서 Logx로 로깅<br>
 *
 * @param block The code block to execute with exception protection.<br><br>
 *              예외 보호와 함께 실행할 코드 블록.
 */
public inline fun safeCatch(block: () -> Unit) {
    try {
        block()
    } catch (e: CancellationException) {
        // Coroutine cancellation must propagate | 코루틴 취소는 반드시 전파
        throw e
    } catch (e: Error) {
        // Critical errors like OOM must not be caught | OOM 등은 절대 삼키지 않음
        throw e
    } catch (e: Exception) {
        Logx.e("safeCatch(Unit): ${e.message}", e)
    }
}

/**
 * Executes a code block with safe exception handling, returning a default value on exception.<br><br>
 * 안전한 예외 처리로 코드 블록을 실행하고, 예외 발생 시 기본값을 반환합니다.<br>
 *
 * Exception handling rules:<br>
 * - CancellationException: Always rethrown (coroutine cancellation must propagate)<br>
 * - Error: Always rethrown (critical errors like OOM must not be caught)<br>
 * - Exception: Caught, logged via Logx, and returns the default value<br><br>
 * 예외 처리 규칙:<br>
 * - CancellationException: 항상 재throw (코루틴 취소는 반드시 전파)<br>
 * - Error: 항상 재throw (OOM 등 치명적 에러는 절대 삼키지 않음)<br>
 * - Exception: 잡아서 Logx로 로깅하고 기본값 반환<br>
 *
 * @param T The return type of the block and default value.<br><br>
 *          블록과 기본값의 반환 타입.
 *
 * @param defaultValue The value to return when an exception occurs.<br><br>
 *                     예외 발생 시 반환할 값.
 *
 * @param block The code block to execute with exception protection.<br><br>
 *              예외 보호와 함께 실행할 코드 블록.
 *
 * @return The result of the block execution, or the default value if an exception occurs.<br><br>
 *         블록 실행 결과, 또는 예외 발생 시 기본값.<br>
 */
public inline fun <T> safeCatch(
    defaultValue: T,
    block: () -> T,
): T = try {
    block()
} catch (e: CancellationException) {
    // Coroutine cancellation must propagate | 코루틴 취소는 반드시 전파
    throw e
} catch (e: Error) {
    // Critical errors like OOM must not be caught | OOM 등은 절대 삼키지 않음
    throw e
} catch (e: Exception) {
    Logx.e("safeCatch(defaultValue): ${e.message}", e)
    defaultValue
}

/**
 * Executes a code block with safe exception handling, using a custom exception handler on exception.<br><br>
 * 안전한 예외 처리로 코드 블록을 실행하고, 예외 발생 시 커스텀 예외 핸들러를 사용합니다.<br>
 *
 * Exception handling rules:<br>
 * - CancellationException: Always rethrown (coroutine cancellation must propagate)<br>
 * - Error: Always rethrown (critical errors like OOM must not be caught)<br>
 * - Exception: Caught, logged via Logx, and passed to the custom handler<br><br>
 * 예외 처리 규칙:<br>
 * - CancellationException: 항상 재throw (코루틴 취소는 반드시 전파)<br>
 * - Error: 항상 재throw (OOM 등 치명적 에러는 절대 삼키지 않음)<br>
 * - Exception: 잡아서 Logx로 로깅하고 커스텀 핸들러로 전달<br>
 *
 * @param T The return type of the block and exception handler.<br><br>
 *          블록과 예외 핸들러의 반환 타입.
 *
 * @param block The code block to execute with exception protection.<br><br>
 *              예외 보호와 함께 실행할 코드 블록.
 *
 * @param onCatch The exception handler that receives the caught exception and returns a fallback value.<br><br>
 *                잡힌 예외를 받아 대체 값을 반환하는 예외 핸들러.
 *
 * @return The result of the block execution, or the result of the exception handler if an exception occurs.<br><br>
 *         블록 실행 결과, 또는 예외 발생 시 예외 핸들러의 결과.<br>
 */
public inline fun <T> safeCatch(block: () -> T, onCatch: ((Exception) -> T)): T = try {
    block()
} catch (e: CancellationException) {
    // Coroutine cancellation must propagate | 코루틴 취소는 반드시 전파
    throw e
} catch (e: Error) {
    // Critical errors like OOM must not be caught | OOM 등은 절대 삼키지 않음
    throw e
} catch (e: Exception) {
    Logx.e("safeCatch(onCatch): ${e.message}", e)
    onCatch(e)
}

/**
 * Throws an IndexOutOfBoundsException if the given value indicates an invalid range condition.<br><br>
 * 주어진 값이 유효하지 않은 범위 조건을 나타내면 IndexOutOfBoundsException을 발생시킵니다.<br>
 *
 * Usage pattern:<br>
 * - Pass a boolean expression that evaluates to `true` when the condition is valid (in bounds)<br>
 * - Throws exception when the value is `false` (invalid/out of bounds)<br>
 * - Example: `requireInBounds(position >= 0 && position < size) { "Invalid position" }`<br><br>
 * 사용 패턴:<br>
 * - 조건이 유효할 때 (범위 내에 있을 때) `true`로 평가되는 불리언 표현식을 전달<br>
 * - 값이 `false`일 때 (유효하지 않음/범위를 벗어남) 예외 발생<br>
 * - 예시: `requireInBounds(position >= 0 && position < size) { "잘못된 위치" }`<br>
 *
 * @param value A boolean expression that is `true` when the condition is valid (in bounds).<br><br>
 *              조건이 유효할 때 (범위 내에 있을 때) `true`가 되는 불리언 표현식.
 *
 * @param lazyMessage A lambda that provides the error message to be used in the exception.<br><br>
 *                    예외에 사용할 에러 메시지를 제공하는 람다.
 *
 * @throws IndexOutOfBoundsException if the value is `false` (indicating invalid/out of bounds).<br><br>
 *                                   값이 `false`일 때 (유효하지 않음/범위를 벗어났음을 나타냄).<br>
 */
public inline fun requireInBounds(value: Boolean, lazyMessage: () -> Any) {
    if (value) return
    val message = lazyMessage()
    throw IndexOutOfBoundsException(message.toString())
}

/**
 * Throws an UnsupportedOperationException if the current SDK version is below the required minimum.<br><br>
 * 현재 SDK 버전이 요구되는 최소 버전보다 낮으면 UnsupportedOperationException을 발생시킵니다.<br>
 *
 * Usage pattern:<br>
 * - Pass the minimum required SDK version<br>
 * - Example: `requireMinSdkVersion(31)`<br><br>
 * 사용 패턴:<br>
 * - 최소 요구 SDK 버전을 전달<br>
 * - 예시: `requireMinSdkVersion(31)`<br>
 *
 * @param sdkVersion The minimum required SDK version.<br><br>
 *                   최소 요구 SDK 버전.
 *
 * @throws UnsupportedOperationException if the current SDK version is below the required minimum.<br><br>
 *                                       현재 SDK 버전이 요구되는 최소 버전보다 낮을 때.<br>
 */
public inline fun requireMinSdkVersion(sdkVersion: Int) {
    if (sdkVersion <= Build.VERSION.SDK_INT) return
    throwMinSdkVersion(sdkVersion)
}

public inline fun throwMinSdkVersion(sdkVersion: Int): Nothing =
    throw UnsupportedOperationException("require Min SDK version $sdkVersion but current SDK version is ${Build.VERSION.SDK_INT}")

/**
 * Throws an UnsupportedOperationException if the current SDK version is above the required maximum.<br><br>
 * 현재 SDK 버전이 요구되는 최대 버전보다 높으면 UnsupportedOperationException을 발생시킵니다.<br>
 *
 * Usage pattern:<br>
 * - Pass the maximum allowed SDK version<br>
 * - Example: `requireMaxSdkVersion(33)`<br><br>
 * 사용 패턴:<br>
 * - 최대 허용 SDK 버전을 전달<br>
 * - 예시: `requireMaxSdkVersion(33)`<br>
 *
 * @param sdkVersion The maximum allowed SDK version.<br><br>
 *                   최대 허용 SDK 버전.
 *
 * @throws UnsupportedOperationException if the current SDK version is above the required maximum.<br><br>
 *                                       현재 SDK 버전이 요구되는 최대 버전보다 높을 때.<br>
 */
public inline fun requireMaxSdkVersion(sdkVersion: Int) {
    if (sdkVersion >= Build.VERSION.SDK_INT) return
    throw UnsupportedOperationException("require Max SDK version $sdkVersion but current SDK version is ${Build.VERSION.SDK_INT}")
}

