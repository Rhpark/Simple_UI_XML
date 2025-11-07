package kr.open.library.simple_ui.extensions.trycatch

import kotlinx.coroutines.CancellationException
import kr.open.library.simple_ui.logcat.Logx


public inline fun safeCatch(block: () -> Unit) {
    try {
        block()
    } catch (e: CancellationException) { // 코루틴 취소는 반드시 전파
        throw e
    } catch (e: Error) { // OOM 등은 절대 삼키지 않음
        throw e
    } catch (e: Exception) {
        Logx.e("safeCatch: ${e.message}", e)
    }
}


public inline fun <T> safeCatch(defaultValue: T, block: () -> T): T {
    return try {
        block()
    } catch (e: CancellationException) { // 코루틴 취소는 반드시 전파
        throw e
    } catch (e: Error) { // OOM 등은 절대 삼키지 않음
        throw e
    } catch (e: Exception) {
        Logx.e("safeCatch: ${e.message}", e)
        defaultValue
    }
}


public inline fun <T> safeCatch(block: () -> T, onCatch: ((Exception) -> T)): T {
    return try {
        block()
    } catch (e: CancellationException) { // 코루틴 취소는 반드시 전파
        throw e
    } catch (e: Error) { // OOM 등은 절대 삼키지 않음
        throw e
    } catch (e: Exception) {
        Logx.e("safeCatch: ${e.message}", e)
        onCatch(e)
    }
}



