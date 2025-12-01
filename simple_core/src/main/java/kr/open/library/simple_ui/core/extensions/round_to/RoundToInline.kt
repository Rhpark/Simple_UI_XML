/**
 * Numeric rounding helpers that keep precision adjustments consistent across types.<br><br>
 * 다양한 숫자 타입에서 동일한 방식으로 자릿수를 조정할 수 있게 도와주는 반올림 도우미 모음입니다.<br>
 */
package kr.open.library.simple_ui.core.extensions.round_to

import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.pow
import kotlin.math.round
import kotlin.math.roundToLong

/**
 * Rounds the double to [decimals] digits using half-up semantics (0.5 goes up).<br><br>
 * [decimals] 자리까지 반올림(0.5 이상 올림)하여 `Double` 값을 반환합니다.<br>
 *
 * @param decimals Number of fractional digits to preserve.<br><br>
 *        유지할 소수 자릿수입니다.
 *
 * @return Rounded value with the requested precision.<br><br>
 *         지정한 자릿수로 반올림된 값을 돌려줍니다.<br>
 */
public inline fun Double.roundTo(decimals: Int): Double {
    val factor = 10.0.pow(decimals.toDouble())
    return round(this * factor) / factor
}

/**
 * Rounds the double upward to [decimals] digits (ceiling for the fractional part).<br><br>
 * 소수점 이하를 올림하여 [decimals] 자리까지 보존한 `Double` 값을 반환합니다.<br>
 *
 * @param decimals Number of fractional digits to preserve.<br><br>
 *        유지할 소수 자릿수입니다.
 *
 * @return Value rounded upward at the requested precision.<br><br>
 *         지정한 자릿수에서 올림 처리된 값을 돌려줍니다.<br>
 */
public inline fun Double.roundUp(decimals: Int): Double {
    val factor = 10.0.pow(decimals.toDouble())
    return ceil(this * factor) / factor
}

/**
 * Rounds the double downward to [decimals] digits (floor for the fractional part).<br><br>
 * 소수점 이하를 내림하여 [decimals] 자리까지 보존한 `Double` 값을 반환합니다.<br>
 *
 * @param decimals Number of fractional digits to preserve.<br><br>
 *        유지할 소수 자릿수입니다.
 *
 * @return Value rounded downward at the requested precision.<br><br>
 *         지정한 자릿수에서 내림 처리된 값을 돌려줍니다.<br>
 */
public inline fun Double.roundDown(decimals: Int): Double {
    val factor = 10.0.pow(decimals.toDouble())
    return floor(this * factor) / factor
}

/**
 * Rounds the float to [decimals] digits using half-up semantics (0.5 goes up).<br><br>
 * [decimals] 자리까지 반올림(0.5 이상 올림)하여 `Float` 값을 반환합니다.<br>
 *
 * @param decimals Number of fractional digits to preserve.<br><br>
 *        유지할 소수 자릿수입니다.
 *
 * @return Rounded value with the requested precision.<br><br>
 *         지정한 자릿수로 반올림된 값을 돌려줍니다.<br>
 */
public inline fun Float.roundTo(decimals: Int): Float {
    val factor = 10f.pow(decimals.toFloat())
    return round(this * factor) / factor
}

/**
 * Rounds the float upward to [decimals] digits (ceiling for the fractional part).<br><br>
 * 소수점 이하를 올림하여 [decimals] 자리까지 보존한 `Float` 값을 반환합니다.<br>
 *
 * @param decimals Number of fractional digits to preserve.<br><br>
 *        유지할 소수 자릿수입니다.
 *
 * @return Value rounded upward at the requested precision.<br><br>
 *         지정한 자릿수에서 올림 처리된 값을 돌려줍니다.<br>
 */
public inline fun Float.roundUp(decimals: Int): Float {
    val factor = 10f.pow(decimals.toFloat())
    return ceil(this * factor) / factor
}

/**
 * Rounds the float downward to [decimals] digits (floor for the fractional part).<br><br>
 * 소수점 이하를 내림하여 [decimals] 자리까지 보존한 `Float` 값을 반환합니다.<br>
 *
 * @param decimals Number of fractional digits to preserve.<br><br>
 *        유지할 소수 자릿수입니다.
 *
 * @return Value rounded downward at the requested precision.<br><br>
 *         지정한 자릿수에서 내림 처리된 값을 돌려줍니다.<br>
 */
public inline fun Float.roundDown(decimals: Int): Float {
    val factor = 10f.pow(decimals.toFloat())
    return floor(this * factor) / factor
}

/**
 * Rounds halves away from zero (0.5 up for positive, -0.5 down for negative).<br><br>
 * 양수는 0.5 이상 올림, 음수는 -0.5 이하 내림 방식으로 반올림합니다.<br>
 *
 * @param value Number to round using half-up semantics.<br><br>
 *        반올림할 값입니다.
 *
 * @return Half-up rounded result as `Double`.<br><br>
 *         반올림 결과를 `Double`로 반환합니다.<br>
 */
public fun roundHalfUp(value: Double): Double = if (value >= 0.0) floor(value + 0.5) else ceil(value - 0.5)

/**
 * Rounds the integer to the specified decimal [place] using half-up semantics.<br><br>
 * 정수를 [place] 자리까지 반올림(0.5 이상 올림) 처리합니다.<br>
 *
 * @param place Position (10^place) to keep, counting from the least significant digit.<br><br>
 *        유지할 자릿수를 나타내는 값(10^[place] 단위)입니다.
 *
 * @return Integer rounded to the requested place.<br><br>
 *         지정한 자릿수로 반올림된 정수입니다.<br>
 */
public inline fun Int.roundTo(place: Int): Int {
    val factor = 10.0.pow(place.toDouble())
    val scaled = this.toDouble() / factor
    val rounded = roundHalfUp(scaled)
    return (rounded * factor).toInt()
}

/**
 * Rounds the integer upward to the specified decimal [place] (ceiling behavior).<br><br>
 * 정수를 [place] 자리에서 올림 처리합니다.<br>
 *
 * @param place Position (10^place) to keep, counting from the least significant digit.<br><br>
 *        유지할 자릿수를 나타내는 값(10^[place] 단위)입니다.
 *
 * @return Integer rounded upward at the requested place.<br><br>
 *         지정한 자릿수에서 올림 처리된 정수입니다.<br>
 */
public inline fun Int.roundUp(place: Int): Int {
    val factor = 10.0.pow(place.toDouble())
    return ceil(this.toDouble() / factor).toInt() * factor.toInt()
}

/**
 * Rounds the integer downward to the specified decimal [place] (floor behavior).<br><br>
 * 정수를 [place] 자리에서 내림 처리합니다.<br>
 *
 * @param place Position (10^place) to keep, counting from the least significant digit.<br><br>
 *        유지할 자릿수를 나타내는 값(10^[place] 단위)입니다.
 *
 * @return Integer rounded downward at the requested place.<br><br>
 *         지정한 자릿수에서 내림 처리된 정수입니다.<br>
 */
public inline fun Int.roundDown(place: Int): Int {
    val factor = 10.0.pow(place.toDouble())
    return floor(this.toDouble() / factor).toInt() * factor.toInt()
}

/**
 * Rounds the long to the specified decimal [place] using half-up semantics.<br><br>
 * `Long` 값을 [place] 자리까지 반올림(0.5 이상 올림) 처리합니다.<br>
 *
 * @param place Position (10^place) to keep, counting from the least significant digit.<br><br>
 *        유지할 자릿수를 나타내는 값(10^[place] 단위)입니다.
 *
 * @return Long rounded to the requested place.<br><br>
 *         지정한 자릿수로 반올림된 `Long` 값입니다.<br>
 */
public inline fun Long.roundTo(place: Int): Long {
    val factor = 10.0.pow(place.toDouble())
    val scaled = this.toDouble() / factor
    val rounded = roundHalfUp(scaled)
    return (rounded * factor).roundToLong()
}

/**
 * Rounds the long upward to the specified decimal [place] (ceiling behavior).<br><br>
 * `Long` 값을 [place] 자리에서 올림 처리합니다.<br>
 *
 * @param place Position (10^place) to keep, counting from the least significant digit.<br><br>
 *        유지할 자릿수를 나타내는 값(10^[place] 단위)입니다.
 *
 * @return Long rounded upward at the requested place.<br><br>
 *         지정한 자릿수에서 올림 처리된 `Long` 값입니다.<br>
 */
public inline fun Long.roundUp(place: Int): Long {
    val factor = 10.0.pow(place.toDouble())
    return ceil(this.toDouble() / factor).toLong() * factor.toLong()
}

/**
 * Rounds the long downward to the specified decimal [place] (floor behavior).<br><br>
 * `Long` 값을 [place] 자리에서 내림 처리합니다.<br>
 *
 * @param place Position (10^place) to keep, counting from the least significant digit.<br><br>
 *        유지할 자릿수를 나타내는 값(10^[place] 단위)입니다.
 *
 * @return Long rounded downward at the requested place.<br><br>
 *         지정한 자릿수에서 내림 처리된 `Long` 값입니다.<br>
 */
public inline fun Long.roundDown(place: Int): Long {
    val factor = 10.0.pow(place.toDouble())
    return floor(this.toDouble() / factor).toLong() * factor.toLong()
}

/**
 * Rounds the short to the specified decimal [place] using half-up semantics by delegating to [Int].<br><br>
 * `Short` 값을 [Int] 연산을 통해 [place] 자리까지 반올림(0.5 이상 올림) 처리합니다.<br>
 *
 * @param place Position (10^place) to keep, counting from the least significant digit.<br><br>
 *        유지할 자릿수를 나타내는 값(10^[place] 단위)입니다.
 *
 * @return Short rounded to the requested place.<br><br>
 *         지정한 자릿수로 반올림된 `Short` 값입니다.<br>
 */
public inline fun Short.roundTo(place: Int): Short {
    val result = this.toInt().roundTo(place)
    return result.toShort()
}

/**
 * Rounds the short upward to the specified decimal [place] via the [Int] helpers.<br><br>
 * `Short` 값을 [Int] 올림 연산으로 변환해 [place] 자리에서 올림 처리합니다.<br>
 *
 * @param place Position (10^place) to keep, counting from the least significant digit.<br><br>
 *        유지할 자릿수를 나타내는 값(10^[place] 단위)입니다.
 *
 * @return Short rounded upward at the requested place.<br><br>
 *         지정한 자릿수에서 올림 처리된 `Short` 값입니다.<br>
 */
public inline fun Short.roundUp(place: Int): Short {
    val result = this.toInt().roundUp(place)
    return result.toShort()
}

/**
 * Rounds the short downward to the specified decimal [place] via the [Int] helpers.<br><br>
 * `Short` 값을 [Int] 내림 연산으로 변환해 [place] 자리에서 내림 처리합니다.<br>
 *
 * @param place Position (10^place) to keep, counting from the least significant digit.<br><br>
 *        유지할 자릿수를 나타내는 값(10^[place] 단위)입니다.
 *
 * @return Short rounded downward at the requested place.<br><br>
 *         지정한 자릿수에서 내림 처리된 `Short` 값입니다.<br>
 */
public inline fun Short.roundDown(place: Int): Short {
    val result = this.toInt().roundDown(place)
    return result.toShort()
}
