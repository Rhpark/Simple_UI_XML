package kr.open.library.simple_ui.core.extensions.trycatch

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
): T =
    try {
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
public inline fun <T> safeCatch(
    block: () -> T,
    onCatch: ((Exception) -> T),
): T =
    try {
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
