/**
 * Conditional inline helpers that keep comparison-heavy branching expressive.<br><br>
 * 비교식 기반 분기를 간결하고 읽기 쉽게 만들어 주는 인라인 확장 모음입니다.<br>
 */
package kr.open.library.simple_ui.core.extensions.conditional

/**
 * Executes [doWork] when the receiver is greater than [comparison].<br><br>
 * 수신 객체가 [comparison]보다 큰 경우 [doWork]를 실행합니다.<br>
 *
 * @param comparison Value to compare with the receiver.<br><br>
 *        수신 객체와 비교할 값입니다.<br>
 * @param doWork Action invoked when the condition is true.<br><br>
 *        조건이 참일 때 실행할 동작입니다.<br>
 * @return Result of [doWork] when the condition is met, otherwise null.<br><br>
 *         조건이 충족되면 [doWork] 결과고, 아니면 null을 반환합니다.<br>
 */
public inline fun <T> Int.ifGreaterThan(
    comparison: Int,
    doWork: () -> T,
): T? =
    if (this > comparison) {
        doWork()
    } else {
        null
    }

/**
 * Executes [doWork] when the receiver is greater than [comparison].<br><br>
 * 수신 객체가 [comparison]보다 큰 경우 [doWork]를 실행합니다.<br>
 *
 * @param comparison Value to compare with the receiver.<br><br>
 *        수신 객체와 비교할 값입니다.<br>
 * @param doWork Action invoked when the condition is true.<br><br>
 *        조건이 참일 때 실행할 동작입니다.<br>
 * @return Result of [doWork] when the condition is met, otherwise null.<br><br>
 *         조건이 충족되면 [doWork] 결과고, 아니면 null을 반환합니다.<br>
 */
public inline fun <T> Float.ifGreaterThan(
    comparison: Float,
    doWork: () -> T,
): T? =
    if (this > comparison) {
        doWork()
    } else {
        null
    }

/**
 * Executes [doWork] when the receiver is greater than [comparison].<br><br>
 * 수신 객체가 [comparison]보다 큰 경우 [doWork]를 실행합니다.<br>
 *
 * @param comparison Value to compare with the receiver.<br><br>
 *        수신 객체와 비교할 값입니다.<br>
 * @param doWork Action invoked when the condition is true.<br><br>
 *        조건이 참일 때 실행할 동작입니다.<br>
 * @return Result of [doWork] when the condition is met, otherwise null.<br><br>
 *         조건이 충족되면 [doWork] 결과고, 아니면 null을 반환합니다.<br>
 */
public inline fun <T> Double.ifGreaterThan(
    comparison: Double,
    doWork: () -> T,
): T? =
    if (this > comparison) {
        doWork()
    } else {
        null
    }

/**
 * Executes [doWork] when the receiver is greater than [comparison].<br><br>
 * 수신 객체가 [comparison]보다 큰 경우 [doWork]를 실행합니다.<br>
 *
 * @param comparison Value to compare with the receiver.<br><br>
 *        수신 객체와 비교할 값입니다.<br>
 * @param doWork Action invoked when the condition is true.<br><br>
 *        조건이 참일 때 실행할 동작입니다.<br>
 * @return Result of [doWork] when the condition is met, otherwise null.<br><br>
 *         조건이 충족되면 [doWork] 결과고, 아니면 null을 반환합니다.<br>
 */
public inline fun <T> Long.ifGreaterThan(
    comparison: Long,
    doWork: () -> T,
): T? =
    if (this > comparison) {
        doWork()
    } else {
        null
    }

/**
 * Executes [doWork] when the receiver is greater than [comparison].<br><br>
 * 수신 객체가 [comparison]보다 큰 경우 [doWork]를 실행합니다.<br>
 *
 * @param comparison Value to compare with the receiver.<br><br>
 *        수신 객체와 비교할 값입니다.<br>
 * @param doWork Action invoked when the condition is true.<br><br>
 *        조건이 참일 때 실행할 동작입니다.<br>
 * @return Result of [doWork] when the condition is met, otherwise null.<br><br>
 *         조건이 충족되면 [doWork] 결과고, 아니면 null을 반환합니다.<br>
 */
public inline fun <T> Short.ifGreaterThan(
    comparison: Short,
    doWork: () -> T,
): T? =
    if (this > comparison) {
        doWork()
    } else {
        null
    }

/**
 * Executes [doWork] when the receiver is greater than or equal to [comparison].<br><br>
 * 수신 객체가 [comparison]보다 크거나 같은 경우  [doWork]를 실행합니다.<br>
 *
 * @param comparison Value to compare with the receiver.<br><br>
 *        수신 객체와 비교할 값입니다.<br>
 * @param doWork Action invoked when the condition is true.<br><br>
 *        조건이 참일 때 실행할 동작입니다.<br>
 * @return Result of [doWork] when the condition is met, otherwise null.<br><br>
 *         조건이 충족되면 [doWork] 결과고, 아니면 null을 반환합니다.<br>
 */
public inline fun <T> Int.ifGreaterThanOrEqual(
    comparison: Int,
    doWork: () -> T,
): T? =
    if (this >= comparison) {
        doWork()
    } else {
        null
    }

/**
 * Executes [doWork] when the receiver is greater than or equal to [comparison].<br><br>
 * 수신 객체가 [comparison]보다 크거나 같은 경우  [doWork]를 실행합니다.<br>
 *
 * @param comparison Value to compare with the receiver.<br><br>
 *        수신 객체와 비교할 값입니다.<br>
 * @param doWork Action invoked when the condition is true.<br><br>
 *        조건이 참일 때 실행할 동작입니다.<br>
 * @return Result of [doWork] when the condition is met, otherwise null.<br><br>
 *         조건이 충족되면 [doWork] 결과고, 아니면 null을 반환합니다.<br>
 */
public inline fun <T> Float.ifGreaterThanOrEqual(
    comparison: Float,
    doWork: () -> T,
): T? =
    if (this >= comparison) {
        doWork()
    } else {
        null
    }

/**
 * Executes [doWork] when the receiver is greater than or equal to [comparison].<br><br>
 * 수신 객체가 [comparison]보다 크거나 같은 경우  [doWork]를 실행합니다.<br>
 *
 * @param comparison Value to compare with the receiver.<br><br>
 *        수신 객체와 비교할 값입니다.<br>
 * @param doWork Action invoked when the condition is true.<br><br>
 *        조건이 참일 때 실행할 동작입니다.<br>
 * @return Result of [doWork] when the condition is met, otherwise null.<br><br>
 *         조건이 충족되면 [doWork] 결과고, 아니면 null을 반환합니다.<br>
 */
public inline fun <T> Double.ifGreaterThanOrEqual(
    comparison: Double,
    doWork: () -> T,
): T? =
    if (this >= comparison) {
        doWork()
    } else {
        null
    }

/**
 * Executes [doWork] when the receiver is greater than or equal to [comparison].<br><br>
 * 수신 객체가 [comparison]보다 크거나 같은 경우  [doWork]를 실행합니다.<br>
 *
 * @param comparison Value to compare with the receiver.<br><br>
 *        수신 객체와 비교할 값입니다.<br>
 * @param doWork Action invoked when the condition is true.<br><br>
 *        조건이 참일 때 실행할 동작입니다.<br>
 * @return Result of [doWork] when the condition is met, otherwise null.<br><br>
 *         조건이 충족되면 [doWork] 결과고, 아니면 null을 반환합니다.<br>
 */
public inline fun <T> Long.ifGreaterThanOrEqual(
    comparison: Long,
    doWork: () -> T,
): T? =
    if (this >= comparison) {
        doWork()
    } else {
        null
    }

/**
 * Executes [doWork] when the receiver is greater than or equal to [comparison].<br><br>
 * 수신 객체가 [comparison]보다 크거나 같은 경우  [doWork]를 실행합니다.<br>
 *
 * @param comparison Value to compare with the receiver.<br><br>
 *        수신 객체와 비교할 값입니다.<br>
 * @param doWork Action invoked when the condition is true.<br><br>
 *        조건이 참일 때 실행할 동작입니다.<br>
 * @return Result of [doWork] when the condition is met, otherwise null.<br><br>
 *         조건이 충족되면 [doWork] 결과고, 아니면 null을 반환합니다.<br>
 */
public inline fun <T> Short.ifGreaterThanOrEqual(
    comparison: Short,
    doWork: () -> T,
): T? =
    if (this >= comparison) {
        doWork()
    } else {
        null
    }

/**
 * Executes [doWork] when the receiver is exactly equal to [comparison].<br><br>
 * 수신 객체가 [comparison]와 정확히 같은 경우  [doWork]를 실행합니다.<br>
 *
 * @param comparison Value to compare with the receiver.<br><br>
 *        수신 객체와 비교할 값입니다.<br>
 * @param doWork Action invoked when the condition is true.<br><br>
 *        조건이 참일 때 실행할 동작입니다.<br>
 * @return Result of [doWork] when the condition is met, otherwise null.<br><br>
 *         조건이 충족되면 [doWork] 결과고, 아니면 null을 반환합니다.<br>
 */
public inline fun <T> Int.ifEquals(
    comparison: Int,
    doWork: () -> T,
): T? =
    if (this == comparison) {
        doWork()
    } else {
        null
    }

/**
 * Executes [doWork] when the receiver is exactly equal to [comparison].<br><br>
 * 수신 객체가 [comparison]와 정확히 같은 경우  [doWork]를 실행합니다.<br>
 *
 * @param comparison Value to compare with the receiver.<br><br>
 *        수신 객체와 비교할 값입니다.<br>
 * @param doWork Action invoked when the condition is true.<br><br>
 *        조건이 참일 때 실행할 동작입니다.<br>
 * @return Result of [doWork] when the condition is met, otherwise null.<br><br>
 *         조건이 충족되면 [doWork] 결과고, 아니면 null을 반환합니다.<br>
 */
public inline fun <T> Float.ifEquals(
    comparison: Float,
    doWork: () -> T,
): T? =
    if (this == comparison) {
        doWork()
    } else {
        null
    }

/**
 * Executes [doWork] when the receiver is exactly equal to [comparison].<br><br>
 * 수신 객체가 [comparison]와 정확히 같은 경우  [doWork]를 실행합니다.<br>
 *
 * @param comparison Value to compare with the receiver.<br><br>
 *        수신 객체와 비교할 값입니다.<br>
 * @param doWork Action invoked when the condition is true.<br><br>
 *        조건이 참일 때 실행할 동작입니다.<br>
 * @return Result of [doWork] when the condition is met, otherwise null.<br><br>
 *         조건이 충족되면 [doWork] 결과고, 아니면 null을 반환합니다.<br>
 */
public inline fun <T> Double.ifEquals(
    comparison: Double,
    doWork: () -> T,
): T? =
    if (this == comparison) {
        doWork()
    } else {
        null
    }

/**
 * Executes [doWork] when the receiver is exactly equal to [comparison].<br><br>
 * 수신 객체가 [comparison]와 정확히 같은 경우  [doWork]를 실행합니다.<br>
 *
 * @param comparison Value to compare with the receiver.<br><br>
 *        수신 객체와 비교할 값입니다.<br>
 * @param doWork Action invoked when the condition is true.<br><br>
 *        조건이 참일 때 실행할 동작입니다.<br>
 * @return Result of [doWork] when the condition is met, otherwise null.<br><br>
 *         조건이 충족되면 [doWork] 결과고, 아니면 null을 반환합니다.<br>
 */
public inline fun <T> Long.ifEquals(
    comparison: Long,
    doWork: () -> T,
): T? =
    if (this == comparison) {
        doWork()
    } else {
        null
    }

/**
 * Executes [doWork] when the receiver is exactly equal to [comparison].<br><br>
 * 수신 객체가 [comparison]와 정확히 같은 경우  [doWork]를 실행합니다.<br>
 *
 * @param comparison Value to compare with the receiver.<br><br>
 *        수신 객체와 비교할 값입니다.<br>
 * @param doWork Action invoked when the condition is true.<br><br>
 *        조건이 참일 때 실행할 동작입니다.<br>
 * @return Result of [doWork] when the condition is met, otherwise null.<br><br>
 *         조건이 충족되면 [doWork] 결과고, 아니면 null을 반환합니다.<br>
 */
public inline fun <T> Short.ifEquals(
    comparison: Short,
    doWork: () -> T,
): T? =
    if (this == comparison) {
        doWork()
    } else {
        null
    }

/**
 * Executes [doWork] when the receiver is not equal to [comparison].<br><br>
 * 수신 객체가 [comparison]와 같지 않은 경우 [doWork]를 실행합니다.<br>
 *
 * @param comparison Value to compare with the receiver.<br><br>
 *        수신 객체와 비교할 값입니다.<br>
 * @param doWork Action invoked when the condition is true.<br><br>
 *        조건이 참일 때 실행할 동작입니다.<br>
 * @return Result of [doWork] when the condition is met, otherwise null.<br><br>
 *         조건이 충족되면 [doWork] 결과고, 아니면 null을 반환합니다.<br>
 */
public inline fun <T> Int.ifNotEquals(
    comparison: Int,
    doWork: () -> T,
): T? =
    if (this != comparison) {
        doWork()
    } else {
        null
    }

/**
 * Executes [doWork] when the receiver is not equal to [comparison].<br><br>
 * 수신 객체가 [comparison]와 같지 않은 경우 [doWork]를 실행합니다.<br>
 *
 * @param comparison Value to compare with the receiver.<br><br>
 *        수신 객체와 비교할 값입니다.<br>
 * @param doWork Action invoked when the condition is true.<br><br>
 *        조건이 참일 때 실행할 동작입니다.<br>
 * @return Result of [doWork] when the condition is met, otherwise null.<br><br>
 *         조건이 충족되면 [doWork] 결과고, 아니면 null을 반환합니다.<br>
 */
public inline fun <T> Float.ifNotEquals(
    comparison: Float,
    doWork: () -> T,
): T? =
    if (this != comparison) {
        doWork()
    } else {
        null
    }

/**
 * Executes [doWork] when the receiver is not equal to [comparison].<br><br>
 * 수신 객체가 [comparison]와 같지 않은 경우 [doWork]를 실행합니다.<br>
 *
 * @param comparison Value to compare with the receiver.<br><br>
 *        수신 객체와 비교할 값입니다.<br>
 * @param doWork Action invoked when the condition is true.<br><br>
 *        조건이 참일 때 실행할 동작입니다.<br>
 * @return Result of [doWork] when the condition is met, otherwise null.<br><br>
 *         조건이 충족되면 [doWork] 결과고, 아니면 null을 반환합니다.<br>
 */
public inline fun <T> Double.ifNotEquals(
    comparison: Double,
    doWork: () -> T,
): T? =
    if (this != comparison) {
        doWork()
    } else {
        null
    }

/**
 * Executes [doWork] when the receiver is not equal to [comparison].<br><br>
 * 수신 객체가 [comparison]와 같지 않은 경우 [doWork]를 실행합니다.<br>
 *
 * @param comparison Value to compare with the receiver.<br><br>
 *        수신 객체와 비교할 값입니다.<br>
 * @param doWork Action invoked when the condition is true.<br><br>
 *        조건이 참일 때 실행할 동작입니다.<br>
 * @return Result of [doWork] when the condition is met, otherwise null.<br><br>
 *         조건이 충족되면 [doWork] 결과고, 아니면 null을 반환합니다.<br>
 */
public inline fun <T> Long.ifNotEquals(
    comparison: Long,
    doWork: () -> T,
): T? =
    if (this != comparison) {
        doWork()
    } else {
        null
    }

/**
 * Executes [doWork] when the receiver is not equal to [comparison].<br><br>
 * 수신 객체가 [comparison]와 같지 않은 경우 [doWork]를 실행합니다.<br>
 *
 * @param comparison Value to compare with the receiver.<br><br>
 *        수신 객체와 비교할 값입니다.<br>
 * @param doWork Action invoked when the condition is true.<br><br>
 *        조건이 참일 때 실행할 동작입니다.<br>
 * @return Result of [doWork] when the condition is met, otherwise null.<br><br>
 *         조건이 충족되면 [doWork] 결과고, 아니면 null을 반환합니다.<br>
 */
public inline fun <T> Short.ifNotEquals(
    comparison: Short,
    doWork: () -> T,
): T? =
    if (this != comparison) {
        doWork()
    } else {
        null
    }

/**
 * Chooses between [positiveWork] and [negativeWork] depending on whether the receiver is greater than [comparison].<br><br>
 * 수신 객체가 [comparison]보다 큰지 여부에 따라 [positiveWork] 또는 [negativeWork]를 실행합니다.<br>
 *
 * @param comparison Value to compare with the receiver.<br><br>
 *        수신 객체와 비교할 값입니다.<br>
 * @param positiveWork Action to run when the condition is true.<br><br>
 *        조건이 참일 때 실행할 동작입니다.<br>
 * @param negativeWork Action to run when the condition is false.<br><br>
 *        조건이 거짓일 때 실행할 동작입니다.<br>
 * @return Result from the branch that ran.<br><br>
 *         실행된 분기에서 반환된 값을 돌려줍니다.<br>
 */
public inline fun <T> Int.ifGreaterThan(
    comparison: Int,
    positiveWork: () -> T,
    negativeWork: () -> T,
): T =
    if (this > comparison) {
        positiveWork()
    } else {
        negativeWork()
    }

/**
 * Chooses between [positiveWork] and [negativeWork] depending on whether the receiver is greater than [comparison].<br><br>
 * 수신 객체가 [comparison]보다 큰지 여부에 따라 [positiveWork] 또는 [negativeWork]를 실행합니다.<br>
 *
 * @param comparison Value to compare with the receiver.<br><br>
 *        수신 객체와 비교할 값입니다.<br>
 * @param positiveWork Action to run when the condition is true.<br><br>
 *        조건이 참일 때 실행할 동작입니다.<br>
 * @param negativeWork Action to run when the condition is false.<br><br>
 *        조건이 거짓일 때 실행할 동작입니다.<br>
 * @return Result from the branch that ran.<br><br>
 *         실행된 분기에서 반환된 값을 돌려줍니다.<br>
 */
public inline fun <T> Float.ifGreaterThan(
    comparison: Float,
    positiveWork: () -> T,
    negativeWork: () -> T,
): T =
    if (this > comparison) {
        positiveWork()
    } else {
        negativeWork()
    }

/**
 * Chooses between [positiveWork] and [negativeWork] depending on whether the receiver is greater than [comparison].<br><br>
 * 수신 객체가 [comparison]보다 큰지 여부에 따라 [positiveWork] 또는 [negativeWork]를 실행합니다.<br>
 *
 * @param comparison Value to compare with the receiver.<br><br>
 *        수신 객체와 비교할 값입니다.<br>
 * @param positiveWork Action to run when the condition is true.<br><br>
 *        조건이 참일 때 실행할 동작입니다.<br>
 * @param negativeWork Action to run when the condition is false.<br><br>
 *        조건이 거짓일 때 실행할 동작입니다.<br>
 * @return Result from the branch that ran.<br><br>
 *         실행된 분기에서 반환된 값을 돌려줍니다.<br>
 */
public inline fun <T> Double.ifGreaterThan(
    comparison: Double,
    positiveWork: () -> T,
    negativeWork: () -> T,
): T =
    if (this > comparison) {
        positiveWork()
    } else {
        negativeWork()
    }

/**
 * Chooses between [positiveWork] and [negativeWork] depending on whether the receiver is greater than [comparison].<br><br>
 * 수신 객체가 [comparison]보다 큰지 여부에 따라 [positiveWork] 또는 [negativeWork]를 실행합니다.<br>
 *
 * @param comparison Value to compare with the receiver.<br><br>
 *        수신 객체와 비교할 값입니다.<br>
 * @param positiveWork Action to run when the condition is true.<br><br>
 *        조건이 참일 때 실행할 동작입니다.<br>
 * @param negativeWork Action to run when the condition is false.<br><br>
 *        조건이 거짓일 때 실행할 동작입니다.<br>
 * @return Result from the branch that ran.<br><br>
 *         실행된 분기에서 반환된 값을 돌려줍니다.<br>
 */
public inline fun <T> Long.ifGreaterThan(
    comparison: Long,
    positiveWork: () -> T,
    negativeWork: () -> T,
): T =
    if (this > comparison) {
        positiveWork()
    } else {
        negativeWork()
    }

/**
 * Chooses between [positiveWork] and [negativeWork] depending on whether the receiver is greater than [comparison].<br><br>
 * 수신 객체가 [comparison]보다 큰지 여부에 따라 [positiveWork] 또는 [negativeWork]를 실행합니다.<br>
 *
 * @param comparison Value to compare with the receiver.<br><br>
 *        수신 객체와 비교할 값입니다.<br>
 * @param positiveWork Action to run when the condition is true.<br><br>
 *        조건이 참일 때 실행할 동작입니다.<br>
 * @param negativeWork Action to run when the condition is false.<br><br>
 *        조건이 거짓일 때 실행할 동작입니다.<br>
 * @return Result from the branch that ran.<br><br>
 *         실행된 분기에서 반환된 값을 돌려줍니다.<br>
 */
public inline fun <T> Short.ifGreaterThan(
    comparison: Short,
    positiveWork: () -> T,
    negativeWork: () -> T,
): T =
    if (this > comparison) {
        positiveWork()
    } else {
        negativeWork()
    }

/**
 * Chooses between [positiveWork] and [negativeWork] depending on whether the receiver is greater than or equal to [comparison].<br><br>
 * 수신 객체가 [comparison]보다 크거나 같을 시, 여부에 따라 [positiveWork] 또는 [negativeWork]를 실행합니다.<br>
 *
 * @param comparison Value to compare with the receiver.<br><br>
 *        수신 객체와 비교할 값입니다.<br>
 * @param positiveWork Action to run when the condition is true.<br><br>
 *        조건이 참일 때 실행할 동작입니다.<br>
 * @param negativeWork Action to run when the condition is false.<br><br>
 *        조건이 거짓일 때 실행할 동작입니다.<br>
 * @return Result from the branch that ran.<br><br>
 *         실행된 분기에서 반환된 값을 돌려줍니다.<br>
 */
public inline fun <T> Int.ifGreaterThanOrEqual(
    comparison: Int,
    positiveWork: () -> T,
    negativeWork: () -> T,
): T =
    if (this >= comparison) {
        positiveWork()
    } else {
        negativeWork()
    }

/**
 * Chooses between [positiveWork] and [negativeWork] depending on whether the receiver is greater than or equal to [comparison].<br><br>
 * 수신 객체가 [comparison]보다 크거나 같을 시, 여부에 따라 [positiveWork] 또는 [negativeWork]를 실행합니다.<br>
 *
 * @param comparison Value to compare with the receiver.<br><br>
 *        수신 객체와 비교할 값입니다.<br>
 * @param positiveWork Action to run when the condition is true.<br><br>
 *        조건이 참일 때 실행할 동작입니다.<br>
 * @param negativeWork Action to run when the condition is false.<br><br>
 *        조건이 거짓일 때 실행할 동작입니다.<br>
 * @return Result from the branch that ran.<br><br>
 *         실행된 분기에서 반환된 값을 돌려줍니다.<br>
 */
public inline fun <T> Float.ifGreaterThanOrEqual(
    comparison: Float,
    positiveWork: () -> T,
    negativeWork: () -> T,
): T =
    if (this >= comparison) {
        positiveWork()
    } else {
        negativeWork()
    }

/**
 * Chooses between [positiveWork] and [negativeWork] depending on whether the receiver is greater than or equal to [comparison].<br><br>
 * 수신 객체가 [comparison]보다 크거나 같을 시, 여부에 따라 [positiveWork] 또는 [negativeWork]를 실행합니다.<br>
 *
 * @param comparison Value to compare with the receiver.<br><br>
 *        수신 객체와 비교할 값입니다.<br>
 * @param positiveWork Action to run when the condition is true.<br><br>
 *        조건이 참일 때 실행할 동작입니다.<br>
 * @param negativeWork Action to run when the condition is false.<br><br>
 *        조건이 거짓일 때 실행할 동작입니다.<br>
 * @return Result from the branch that ran.<br><br>
 *         실행된 분기에서 반환된 값을 돌려줍니다.<br>
 */
public inline fun <T> Double.ifGreaterThanOrEqual(
    comparison: Double,
    positiveWork: () -> T,
    negativeWork: () -> T,
): T =
    if (this >= comparison) {
        positiveWork()
    } else {
        negativeWork()
    }

/**
 * Chooses between [positiveWork] and [negativeWork] depending on whether the receiver is greater than or equal to [comparison].<br><br>
 * 수신 객체가 [comparison]보다 크거나 같을 시, 여부에 따라 [positiveWork] 또는 [negativeWork]를 실행합니다.<br>
 *
 * @param comparison Value to compare with the receiver.<br><br>
 *        수신 객체와 비교할 값입니다.<br>
 * @param positiveWork Action to run when the condition is true.<br><br>
 *        조건이 참일 때 실행할 동작입니다.<br>
 * @param negativeWork Action to run when the condition is false.<br><br>
 *        조건이 거짓일 때 실행할 동작입니다.<br>
 * @return Result from the branch that ran.<br><br>
 *         실행된 분기에서 반환된 값을 돌려줍니다.<br>
 */
public inline fun <T> Long.ifGreaterThanOrEqual(
    comparison: Long,
    positiveWork: () -> T,
    negativeWork: () -> T,
): T =
    if (this >= comparison) {
        positiveWork()
    } else {
        negativeWork()
    }

/**
 * Chooses between [positiveWork] and [negativeWork] depending on whether the receiver is greater than or equal to [comparison].<br><br>
 * 수신 객체가 [comparison]보다 크거나 같을 시, 여부에 따라 [positiveWork] 또는 [negativeWork]를 실행합니다.<br>
 *
 * @param comparison Value to compare with the receiver.<br><br>
 *        수신 객체와 비교할 값입니다.<br>
 * @param positiveWork Action to run when the condition is true.<br><br>
 *        조건이 참일 때 실행할 동작입니다.<br>
 * @param negativeWork Action to run when the condition is false.<br><br>
 *        조건이 거짓일 때 실행할 동작입니다.<br>
 * @return Result from the branch that ran.<br><br>
 *         실행된 분기에서 반환된 값을 돌려줍니다.<br>
 */
public inline fun <T> Short.ifGreaterThanOrEqual(
    comparison: Short,
    positiveWork: () -> T,
    negativeWork: () -> T,
): T =
    if (this >= comparison) {
        positiveWork()
    } else {
        negativeWork()
    }

/**
 * Chooses between [positiveWork] and [negativeWork] depending on whether the receiver is exactly equal to [comparison].<br><br>
 * 수신 객체가 [comparison]와 같을 시, 여부에 따라 [positiveWork] 또는 [negativeWork]를 실행합니다.<br>
 *
 * @param comparison Value to compare with the receiver.<br><br>
 *        수신 객체와 비교할 값입니다.<br>
 * @param positiveWork Action to run when the condition is true.<br><br>
 *        조건이 참일 때 실행할 동작입니다.<br>
 * @param negativeWork Action to run when the condition is false.<br><br>
 *        조건이 거짓일 때 실행할 동작입니다.<br>
 * @return Result from the branch that ran.<br><br>
 *         실행된 분기에서 반환된 값을 돌려줍니다.<br>
 */
public inline fun <T> Int.ifEquals(
    comparison: Int,
    positiveWork: () -> T,
    negativeWork: () -> T,
): T =
    if (this == comparison) {
        positiveWork()
    } else {
        negativeWork()
    }

/**
 * Chooses between [positiveWork] and [negativeWork] depending on whether the receiver is exactly equal to [comparison].<br><br>
 * 수신 객체가 [comparison]와 같을 시, 여부에 따라 [positiveWork] 또는 [negativeWork]를 실행합니다.<br>
 *
 * @param comparison Value to compare with the receiver.<br><br>
 *        수신 객체와 비교할 값입니다.<br>
 * @param positiveWork Action to run when the condition is true.<br><br>
 *        조건이 참일 때 실행할 동작입니다.<br>
 * @param negativeWork Action to run when the condition is false.<br><br>
 *        조건이 거짓일 때 실행할 동작입니다.<br>
 * @return Result from the branch that ran.<br><br>
 *         실행된 분기에서 반환된 값을 돌려줍니다.<br>
 */
public inline fun <T> Float.ifEquals(
    comparison: Float,
    positiveWork: () -> T,
    negativeWork: () -> T,
): T =
    if (this == comparison) {
        positiveWork()
    } else {
        negativeWork()
    }

/**
 * Chooses between [positiveWork] and [negativeWork] depending on whether the receiver is exactly equal to [comparison].<br><br>
 * 수신 객체가 [comparison]와 같을 시, 여부에 따라 [positiveWork] 또는 [negativeWork]를 실행합니다.<br>
 *
 * @param comparison Value to compare with the receiver.<br><br>
 *        수신 객체와 비교할 값입니다.<br>
 * @param positiveWork Action to run when the condition is true.<br><br>
 *        조건이 참일 때 실행할 동작입니다.<br>
 * @param negativeWork Action to run when the condition is false.<br><br>
 *        조건이 거짓일 때 실행할 동작입니다.<br>
 * @return Result from the branch that ran.<br><br>
 *         실행된 분기에서 반환된 값을 돌려줍니다.<br>
 */
public inline fun <T> Double.ifEquals(
    comparison: Double,
    positiveWork: () -> T,
    negativeWork: () -> T,
): T =
    if (this == comparison) {
        positiveWork()
    } else {
        negativeWork()
    }

/**
 * Chooses between [positiveWork] and [negativeWork] depending on whether the receiver is exactly equal to [comparison].<br><br>
 * 수신 객체가 [comparison]와 같을 시, 여부에 따라 [positiveWork] 또는 [negativeWork]를 실행합니다.<br>
 *
 * @param comparison Value to compare with the receiver.<br><br>
 *        수신 객체와 비교할 값입니다.<br>
 * @param positiveWork Action to run when the condition is true.<br><br>
 *        조건이 참일 때 실행할 동작입니다.<br>
 * @param negativeWork Action to run when the condition is false.<br><br>
 *        조건이 거짓일 때 실행할 동작입니다.<br>
 * @return Result from the branch that ran.<br><br>
 *         실행된 분기에서 반환된 값을 돌려줍니다.<br>
 */
public inline fun <T> Long.ifEquals(
    comparison: Long,
    positiveWork: () -> T,
    negativeWork: () -> T,
): T =
    if (this == comparison) {
        positiveWork()
    } else {
        negativeWork()
    }

/**
 * Chooses between [positiveWork] and [negativeWork] depending on whether the receiver is exactly equal to [comparison].<br><br>
 * 수신 객체가 [comparison]와 같을 시, 여부에 따라 [positiveWork] 또는 [negativeWork]를 실행합니다.<br>
 *
 * @param comparison Value to compare with the receiver.<br><br>
 *        수신 객체와 비교할 값입니다.<br>
 * @param positiveWork Action to run when the condition is true.<br><br>
 *        조건이 참일 때 실행할 동작입니다.<br>
 * @param negativeWork Action to run when the condition is false.<br><br>
 *        조건이 거짓일 때 실행할 동작입니다.<br>
 * @return Result from the branch that ran.<br><br>
 *         실행된 분기에서 반환된 값을 돌려줍니다.<br>
 */
public inline fun <T> Short.ifEquals(
    comparison: Short,
    positiveWork: () -> T,
    negativeWork: () -> T,
): T =
    if (this == comparison) {
        positiveWork()
    } else {
        negativeWork()
    }

/**
 * Executes [doWork] when the Boolean receiver is true.<br><br>
 * 불리언 수신 객체가 true일 때 [doWork]를 실행합니다.<br>
 *
 * @param doWork Action invoked when the condition is satisfied.<br><br>
 *        조건이 충족될 때 실행할 동작입니다.<br>
 * @return Result of [doWork] when the branch runs, otherwise null.<br><br>
 *         분기가 실행되면 [doWork] 반환값을, 그렇지 않으면 null을 돌려줍니다.<br>
 */
public inline fun <T> Boolean.ifTrue(doWork: () -> T): T? =
    if (this) {
        doWork()
    } else {
        null
    }

/**
 * Executes [positiveWork] when the Boolean receiver is true, otherwise runs [negativeWork].<br><br>
 * 불리언 수신 객체가 true이면 [positiveWork], 아니면 [negativeWork]를 실행합니다.<br>
 *
 * @param positiveWork Action to run for a true receiver.<br><br>
 *        수신 객체가 true일 때 실행할 동작입니다.<br>
 * @param negativeWork Action to run for a false receiver.<br><br>
 *        수신 객체가 false일 때 실행할 동작입니다.<br>
 * @return Result from the executed branch.<br><br>
 *         실행된 분기에서 반환된 값을 돌려줍니다.<br>
 */
public inline fun <T> Boolean.ifTrue(
    positiveWork: () -> T,
    negativeWork: () -> T,
): T =
    if (this) {
        positiveWork()
    } else {
        negativeWork()
    }

/**
 * Executes [doWork] when the Boolean receiver is false.<br><br>
 * 불리언 수신 객체가 false일 때 [doWork]를 실행합니다.<br>
 *
 * @param doWork Action invoked when the condition is satisfied.<br><br>
 *        조건이 충족될 때 실행할 동작입니다.<br>
 * @return Result of [doWork] when the branch runs, otherwise null.<br><br>
 *         분기가 실행되면 [doWork] 반환값을, 그렇지 않으면 null을 돌려줍니다.<br>
 */
public inline fun <T> Boolean.ifFalse(doWork: () -> T): T? =
    if (!this) {
        doWork()
    } else {
        null
    }

/**
 * Returns the first non-null element from [values].<br><br>
 * 인자로 전달된 [values] 중 가장 먼저 등장하는 null이 아닌 값을 반환합니다.<br>
 *
 * @param values Candidate values that may contain null entries.<br><br>
 *        null일 수도 있는 후보 값 목록입니다.<br>
 * @return First non-null value or null when none exist.<br><br>
 *         null이 아닌 값이 없으면 null을 반환합니다.<br>
 */
public fun <T> firstNotNull(vararg values: T?): T? {
    for (value in values) {
        if (value != null) return value
    }
    return null
}
