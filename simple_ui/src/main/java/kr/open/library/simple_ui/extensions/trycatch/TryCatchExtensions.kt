package kr.open.library.simple_ui.extensions.trycatch

import kotlinx.coroutines.CancellationException


public inline fun safeCatch(block: () -> Unit) {
    try {
        block()
    } catch (e: CancellationException) { // 코루틴 취소는 반드시 전파
        throw e
    } catch (e: Error) { // OOM 등은 절대 삼키지 않음
        throw e
    } catch (e: Exception) {
        e.printStackTrace()
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
        e.printStackTrace()
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
        e.printStackTrace()
        onCatch(e)
    }
}



