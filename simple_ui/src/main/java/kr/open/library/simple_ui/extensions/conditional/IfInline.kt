package kr.open.library.simple_ui.extensions.conditional

/**
 * @param comparison
 *
 * @return (center > comparison) -> doWork()
 */
public inline fun <T> Int.ifGreaterThan(comparison: Int, doWork: () -> T
): T? = if (this > comparison) { doWork() } else { null }


/**
 * @param comparison
 *
 * @return (center > comparison) -> positiveWork() else negativeWork()
 */
public inline fun <T> Int.ifGreaterThan(comparison: Int, positiveWork: () -> T, negativeWork: () -> T,
): T? = if (this > comparison) { positiveWork() } else { negativeWork() }


/**
 * @param comparison
 *
 * @return (center > comparison) -> doWork()
 */
public inline fun <T> Float.ifGreaterThan(comparison: Float, doWork: () -> T,
): T? = if (this > comparison) { doWork() } else { null }

/**
 * @param comparison
 *
 * @return (center > comparison) -> positiveWork() else negativeWork()
 */
public inline fun <T> Float.ifGreaterThan(comparison: Float, positiveWork: () -> T, negativeWork: () -> T,
): T? = if (this > comparison) { positiveWork() } else { negativeWork() }

/**
 * @param comparison
 *
 * @return (center > comparison) -> doWork()
 */
public inline fun <T> Double.ifGreaterThan(comparison: Double, doWork: () -> T,
): T? = if (this > comparison) { doWork() } else { null }

/**
 * @param comparison
 *
 * @return (center > comparison) -> positiveWork() else negativeWork()
 */
public inline fun <T> Double.ifGreaterThan(comparison: Double, positiveWork: () -> T, negativeWork: () -> T
): T? = if (this > comparison) { positiveWork() } else { negativeWork() }

/**
 * @param comparison
 *
 * @return (center > comparison) -> doWork()
 */
public inline fun <T> Long.ifGreaterThan(comparison: Long, doWork: () -> T,
): T? = if (this > comparison) { doWork() } else { null }

/**
 * @param comparison
 *
 * @return (center > comparison) -> positiveWork() else negativeWork()
 */
public inline fun <T> Long.ifGreaterThan(comparison: Long, positiveWork: () -> T, negativeWork: () -> T
): T? = if (this > comparison) { positiveWork() } else { negativeWork() }

/**
 * @param comparison
 *
 * @return (center > comparison) -> doWork()
 */
public inline fun <T> Short.ifGreaterThan(comparison: Short, doWork: () -> T
): T? = if (this > comparison) { doWork() } else { null }

/**
 * @param comparison
 *
 * @return (center > comparison) -> positiveWork() else negativeWork()
 */
public inline fun <T> Short.ifGreaterThan(comparison: Short, positiveWork: () -> T, negativeWork: () -> T
): T? = if (this > comparison) { positiveWork() } else { negativeWork() }

/**
 * @param comparison
 *
 * @return (center > comparison) -> doWork()
 */
public inline fun <T> Int.ifGreaterThanOrEqual(comparison: Int, doWork: () -> T
): T? = if (this >= comparison) { doWork() } else { null }

/**
 * @param comparison
 *
 * @return (this > comparison) -> positiveWork() else negativeWork()
 */
public inline fun <T> Int.ifGreaterThanOrEqual(comparison: Int, positiveWork: () -> T, negativeWork: () -> T
): T = if (this >= comparison) { positiveWork() } else { negativeWork() }

/**
 * @param comparison
 *
 * @return (center > comparison) -> doWork()
 */
public inline fun <T> Float.ifGreaterThanOrEqual(comparison: Float, doWork: () -> T
): T? = if (this >= comparison) { doWork() } else { null }

/**
 * @param comparison
 *
 * @return (this > comparison) -> positiveWork() else negativeWork()
 */
public inline fun <T> Float.ifGreaterThanOrEqual(comparison: Float, positiveWork: () -> T, negativeWork: () -> T
): T = if (this >= comparison) { positiveWork() } else { negativeWork() }

/**
 * @param comparison
 *
 * @return (center > comparison) -> doWork()
 */
public inline fun <T> Double.ifGreaterThanOrEqual(comparison: Double, doWork: () -> T,
): T? = if (this >= comparison) { doWork() } else { null }

/**
 * @param comparison
 *
 * @return (this > comparison) -> positiveWork() else negativeWork()
 */
public inline fun <T> Double.ifGreaterThanOrEqual(comparison: Double, positiveWork: () -> T, negativeWork: () -> T
): T = if (this >= comparison) { positiveWork() } else { negativeWork() }

/**
 * @param comparison
 *
 * @return (center > comparison) -> doWork()
 */
public inline fun <T> Short.ifGreaterThanOrEqual(comparison: Short, doWork: () -> T,
): T? = if (this >= comparison) { doWork() } else { null }

/**
 * @param comparison
 *
 * @return (this > comparison) -> positiveWork() else negativeWork()
 */
public inline fun <T> Short.ifGreaterThanOrEqual(comparison: Short, positiveWork: () -> T, negativeWork: () -> T
): T = if (this >= comparison) { positiveWork() } else { negativeWork() }

/**
 * @param comparison
 *
 * @return (center > comparison) -> doWork()
 */
public inline fun <T> Long.ifGreaterThanOrEqual(comparison: Long, doWork: () -> T,
): T? = if (this >= comparison) { doWork() } else { null }

/**
 * @param comparison
 *
 * @return (this > comparison) -> positiveWork() else negativeWork()
 */
public inline fun <T> Long.ifGreaterThanOrEqual(comparison: Long, positiveWork: () -> T, negativeWork: () -> T
): T = if (this >= comparison) { positiveWork() } else { negativeWork() }

/**
 * @param comparison
 *
 * @return (center > comparison) -> doWork()
 */
public inline fun <T> Int.ifEquals(comparison: Int, doWork: () -> T
): T? = if (this == comparison) { doWork() } else { null }

/**
 * @param comparison
 *
 * @return (this > comparison) -> positiveWork() else negativeWork()
 */
public inline fun <T> Int.ifEquals(comparison: Int, positiveWork: () -> T, negativeWork: () -> T
): T = if (this == comparison) { positiveWork() } else { negativeWork() }

/**
 * @param comparison
 *
 * @return (center > comparison) -> doWork()
 */
public inline fun <T> Float.ifEquals(comparison: Float, doWork: () -> T
): T? = if (this == comparison) { doWork() } else { null }

/**
 * @param comparison
 *
 * @return (this > comparison) -> positiveWork() else negativeWork()
 */
public inline fun <T> Float.ifEquals(comparison: Float, positiveWork: () -> T, negativeWork: () -> T
): T = if (this == comparison) { positiveWork() } else { negativeWork() }

/**
 * @param comparison
 *
 * @return (center > comparison) -> doWork()
 */
public inline fun <T> Double.ifEquals(comparison: Double, doWork: () -> T,
): T? = if (this == comparison) { doWork() } else { null }

/**
 * @param comparison
 *
 * @return (this > comparison) -> positiveWork() else negativeWork()
 */
public inline fun <T> Double.ifEquals(comparison: Double, positiveWork: () -> T, negativeWork: () -> T
): T = if (this == comparison) { positiveWork() } else { negativeWork() }

/**
 * @param comparison
 *
 * @return (center > comparison) -> doWork()
 */
public inline fun <T> Long.ifEquals(comparison: Long, doWork: () -> T
): T? = if (this == comparison) { doWork() } else { null }

/**
 * @param comparison
 *
 * @return (this > comparison) -> positiveWork() else negativeWork()
 */
public inline fun <T> Long.ifEquals(comparison: Long, positiveWork: () -> T, negativeWork: () -> T
): T = if (this == comparison) { positiveWork() } else { negativeWork() }

/**
 * @param comparison
 *
 * @return (center > comparison) -> doWork()
 */
public inline fun <T> Short.ifEquals(comparison: Short, doWork: () -> T
): T? = if (this == comparison) { doWork() } else { null }

/**
 * @param comparison
 *
 * @return (this > comparison) -> positiveWork() else negativeWork()
 */
public inline fun <T> Short.ifEquals(comparison: Short, positiveWork: () -> T, negativeWork: () -> T,
): T = if (this == comparison) { positiveWork() } else { negativeWork() }

/**
 * @param comparison
 *
 * @return (center > comparison) -> doWork()
 */
public inline fun <T> Int.ifNotEquals(comparison: Int, doWork: () -> T,
): T? = if (this != comparison) { doWork() } else { null }

/**
 * @param comparison
 *
 * @return (center > comparison) -> doWork()
 */
public inline fun <T> Float.ifNotEquals(comparison: Float, doWork: () -> T,
): T? = if (this != comparison) { doWork() } else { null }

/**
 * @param comparison
 *
 * @return (center > comparison) -> doWork()
 */
public inline fun <T> Short.ifNotEquals(comparison: Short, doWork: () -> T,
): T? = if (this != comparison) { doWork() } else { null }

/**
 * @param comparison
 *
 * @return (center > comparison) -> doWork()
 */
public inline fun <T> Double.ifNotEquals(comparison: Double, doWork: () -> T,
): T? = if (this != comparison) { doWork() } else { null }

/**
 * @param comparison
 *
 * @return (center > comparison) -> doWork()
 */
public inline fun <T> Long.ifNotEquals(comparison: Long, doWork: () -> T,
): T? = if (this != comparison) { doWork() } else { null }

/**
 * @return (center == true) -> doWork()
 */
public inline fun <T> Boolean.ifTrue(doWork: () -> T): T? = if (this) { doWork() } else { null }

/**
 * @return (center == true) -> positiveWork() else negativeWork()
 */
public inline fun <T> Boolean.ifTrue(positiveWork: () -> T, negativeWork: () -> T
): T = if (this) { positiveWork() } else { negativeWork() }

/**
 *
 * @return (center == false) -> doWork()
 */
public inline fun <T> Boolean.ifFalse(doWork: () -> T): T? = if (!this) { doWork() } else { null }


/**
 * Returns the first non-null value from the provided values
 * More efficient implementation that stops at first non-null
 *
 * @param values Variable number of nullable values to check
 * @return The first non-null value, or null if all are null
 *
 * Example:
 * val result = firstNotNull(userInput, cachedValue, defaultValue)
 */
public fun <T> firstNotNull(vararg values: T?): T? {
    for (value in values) { if (value != null) return value }
    return null
}
