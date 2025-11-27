package kr.open.library.simple_ui.core.extensions.time

/**
 * Measures execution time in milliseconds and returns both the result and elapsed duration.<br><br>
 * 코드 블록을 실행한 뒤 결과와 밀리초 단위 소요 시간을 함께 돌려줍니다.<br>
 *
 * Note: Use kotlin.system.measureTimeMillis when you only need the elapsed time without the result.<br><br>
 * 참고: 결과가 필요 없고 경과 시간만 측정하려면 kotlin.system.measureTimeMillis 를 사용하세요.<br>
 *
 * @param block The code block whose execution result and duration will be captured.<br><br>
 *              실행 결과와 소요 시간을 함께 측정할 코드 블록입니다.<br>
 *
 * @return Pair containing the block result and elapsed milliseconds.<br><br>
 *         블록 실행 결과와 밀리초 단위 실행 시간이 들어 있는 Pair 입니다.<br>
 */
public inline fun <T> measureTimeMillis(block: () -> T): Pair<T, Long> =
    measureTimeWithResult(System::currentTimeMillis, block)

/**
 * Measures execution time in nanoseconds and returns both the result and elapsed duration.<br><br>
 * 코드 블록을 실행한 뒤 결과와 나노초 단위 소요 시간을 함께 돌려줍니다.<br>
 *
 * Note: Use kotlin.system.measureNanoTime when you only need the elapsed time without the result.<br><br>
 * 참고: 결과가 필요 없고 경과 시간만 측정하려면 kotlin.system.measureNanoTime 을 사용하세요.<br>
 *
 * @param block The code block whose execution result and duration will be captured.<br><br>
 *              실행 결과와 소요 시간을 함께 측정할 코드 블록입니다.<br>
 *
 * @return Pair containing the block result and elapsed nanoseconds.<br><br>
 *         블록 실행 결과와 나노초 단위 실행 시간이 들어 있는 Pair 입니다.<br>
 */
public inline fun <T> measureTimeNanos(block: () -> T): Pair<T, Long> =
    measureTimeWithResult(System::nanoTime, block)

/**
 * Measures the execution time of a code block through a supplied time source.<br><br>
 * 전달받은 시간 공급원을 사용해 코드 블록의 실행 시간을 측정합니다.<br>
 *
 * @param timeProvider Function that supplies the current time (e.g., System::currentTimeMillis or System::nanoTime).<br><br>
 *                     System::currentTimeMillis 혹은 System::nanoTime 처럼 현재 시간을 돌려주는 함수입니다.<br>
 *
 * @param block The code block whose elapsed time will be recorded.<br><br>
 *              경과 시간을 측정할 대상 코드 블록입니다.<br>
 *
 * @return Elapsed time computed from the difference between consecutive timeProvider calls.<br><br>
 *         두 번 호출한 timeProvider 값의 차이로 계산된 경과 시간입니다.<br>
 */
public inline fun measureTime(timeProvider: () -> Long, block: () -> Unit): Long {
    val start = timeProvider()
    block()
    return timeProvider() - start
}

/**
 * Measures execution time via a supplied time source and returns both the result and elapsed duration.<br><br>
 * 지정한 시간 공급원을 사용해 실행 시간을 측정하고 결과와 경과 시간을 함께 반환합니다.<br>
 *
 * @param T The return type produced by the block.<br><br>
 *          코드 블록이 만들어 내는 반환 타입입니다.<br>
 *
 * @param timeProvider Function that supplies the current time (e.g., System::currentTimeMillis or System::nanoTime).<br><br>
 *                     System::currentTimeMillis 혹은 System::nanoTime 처럼 현재 시간을 돌려주는 함수입니다.<br>
 *
 * @param block The code block whose execution result and duration will be captured.<br><br>
 *              실행 결과와 경과 시간을 함께 측정할 코드 블록입니다.<br>
 *
 * @return Pair containing the block result and elapsed time computed from the time provider.<br><br>
 *         블록 실행 결과와 시간 공급원 차이로 계산된 경과 시간이 들어 있는 Pair 입니다.<br>
 */
public inline fun <T> measureTimeWithResult(
    timeProvider: () -> Long,
    block: () -> T
): Pair<T, Long> {
    val start = timeProvider()
    val result = block()
    return Pair(result, timeProvider() - start)
}
