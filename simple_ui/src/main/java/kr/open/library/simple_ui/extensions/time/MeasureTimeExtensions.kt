package kr.open.library.simple_ui.extensions.time

/**
 * Measures execution time in milliseconds and returns both result and time taken
 * 실행 시간을 밀리초로 측정하고 결과와 소요 시간을 모두 반환
 *
 * Note: For measuring time only, use kotlin.system.measureTimeMillis instead
 * 시간만 측정하려면 kotlin.system.measureTimeMillis 사용 권장
 *
 * @param block The code block to measure
 * @return Pair containing the result and execution time in milliseconds
 */
public inline fun <T> measureTimeMillis(block: () -> T): Pair<T, Long> =
    measureTimeWithResult(System::currentTimeMillis, block)

/**
 * Measures execution time in nanoseconds and returns both result and time taken
 * 실행 시간을 나노초로 측정하고 결과와 소요 시간을 모두 반환
 *
 * Note: For measuring time only, use kotlin.system.measureNanoTime instead
 * 시간만 측정하려면 kotlin.system.measureNanoTime 사용 권장
 *
 * @param block The code block to measure
 * @return Pair containing the result and execution time in nanoseconds
 */
public inline fun <T> measureTimeNanos(block: () -> T): Pair<T, Long> =
    measureTimeWithResult(System::nanoTime, block)

public inline fun measureTime(timeProvider: () -> Long, block: () -> Unit
): Long {
    val start = timeProvider()
    block()
    return timeProvider() - start
}

public inline fun <T> measureTimeWithResult(timeProvider: () -> Long, block: () -> T
): Pair<T, Long> {
    val start = timeProvider()
    val result = block()
    return Pair(result, timeProvider() - start)
}
