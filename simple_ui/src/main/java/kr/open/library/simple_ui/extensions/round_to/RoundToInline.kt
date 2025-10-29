package kr.open.library.simple_ui.extensions.round_to

import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.pow
import kotlin.math.round
import kotlin.math.roundToInt
import kotlin.math.roundToLong

/**
 * ex) 3.14159.roundTo(2) -> 3.14
 */
public inline fun Double.roundTo(decimals: Int): Double {
    val factor = 10.0.pow(decimals.toDouble())
    return round(this * factor) / factor
}

/**
 * ex) 3.14159.roundUp(2) -> 3.15
 */
public inline fun Double.roundUp(decimals: Int): Double {
    val factor = 10.0.pow(decimals.toDouble())
    return ceil(this * factor) / factor
}

/**
 * ex) 3.14159.roundDown(2) -> 3.14
 */
public inline fun Double.roundDown(decimals: Int): Double {
    val factor = 10.0.pow(decimals.toDouble())
    return floor(this * factor) / factor
}

/**
 * ex) 3.14159.roundTo(2) -> 3.14f
 */
public inline fun Float.roundTo(decimals: Int): Float {
    val factor = 10f.pow(decimals.toFloat())
    return round(this * factor) / factor
}

/**
 * 3.14159f.roundUp(2) -> 3.15f
 */
public inline fun Float.roundUp(decimals: Int): Float {
    val factor = 10f.pow(decimals.toFloat())
    return ceil(this * factor) / factor
}

/**
 * 3.14159f.roundDown(2) -> 3.14f
 */
public inline fun Float.roundDown(decimals: Int): Float {
    val factor = 10f.pow(decimals.toFloat())
    return floor(this * factor) / factor
}

public fun roundHalfUp(value: Double): Double =
    if (value >= 0.0) floor(value + 0.5) else ceil(value - 0.5)

/**
 * 1234.roundTo(2) -> 1200
 */
public inline fun Int.roundTo(place: Int): Int {
    val factor = 10.0.pow(place.toDouble())
    val scaled = this.toDouble() / factor
    val rounded = roundHalfUp(scaled)
    return (rounded * factor).toInt()
}

/**
 * 1234.roundUp(2) -> 1300
 */
public inline fun Int.roundUp(place: Int): Int {
    val factor = 10.0.pow(place.toDouble())
    return ceil(this.toDouble() / factor).toInt() * factor.toInt()
}

/**
 * 1234.roundDown(2) -> 1200
 */
public inline fun Int.roundDown(place: Int): Int {
    val factor = 10.0.pow(place.toDouble())
    return floor(this.toDouble() / factor).toInt() * factor.toInt()
}

/**
 * 1234.roundTo(2) -> 1200
 */
public inline fun Long.roundTo(place: Int): Long {
    val factor = 10.0.pow(place.toDouble())
    val scaled = this.toDouble() / factor
    val rounded = roundHalfUp(scaled)
    return (rounded * factor).roundToLong()
}

/**
 * 1234.roundUp(2) -> 1300
 */
public inline fun Long.roundUp(place: Int): Long {
    val factor = 10.0.pow(place.toDouble())
    return ceil(this.toDouble() / factor).toLong() * factor.toLong()
}

/**
 * 1234.roundDown(2) -> 1200
 */
public inline fun Long.roundDown(place: Int): Long {
    val factor = 10.0.pow(place.toDouble())
    return floor(this.toDouble() / factor).toLong() * factor.toLong()
}

/**
 * 1234.roundTo(2) -> 1200
 */
public inline fun Short.roundTo(place: Int): Short {
    val result = this.toInt().roundTo(place)
    return result.toShort()
}

/**
 * 1234.roundUp(2) -> 1300
 */
public inline fun Short.roundUp(place: Int): Short {
    val result = this.toInt().roundUp(place)
    return result.toShort()
}

/**
 * 1234.roundDown(2) -> 1200
 */
public inline fun Short.roundDown(place: Int): Short {
    val result = this.toInt().roundDown(place)
    return result.toShort()
}
