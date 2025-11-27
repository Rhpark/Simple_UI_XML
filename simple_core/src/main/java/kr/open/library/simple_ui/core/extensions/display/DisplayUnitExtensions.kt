package kr.open.library.simple_ui.core.extensions.display

import android.content.Context
import android.util.TypedValue

/**
 * Display unit conversion extension functions for Number types.<br>
 * Provides convenient methods to convert between DP, PX, and SP units.<br><br>
 * Number 타입을 위한 디스플레이 단위 변환 확장 함수입니다.<br>
 * DP, PX, SP 단위 간 변환을 위한 편리한 메서드를 제공합니다.<br>
 *
 * Example usage:<br>
 * ```kotlin
 * val pixels = 16.dpToPx(context)
 * val dp = 48.pxToDp(context)
 * val sp = 14.dpToSp(context)
 * ```
 *
 * 사용 예시:<br>
 * ```kotlin
 * val pixels = 16.dpToPx(context)
 * val dp = 48.pxToDp(context)
 * val sp = 14.dpToSp(context)
 * ```
 */

/****************
 * DP To PX, SP *
 ****************/

/**
 * Converts dp value to pixels.<br><br>
 * dp 값을 픽셀로 변환합니다.<br>
 *
 * @param context The Android context for accessing display metrics.<br><br>
 *                디스플레이 메트릭에 접근하기 위한 Android 컨텍스트.<br>
 *
 * @return The converted pixel value as Float.<br><br>
 *         Float로 변환된 픽셀 값.<br>
 */
public fun Number.dpToPx(context: Context): Float =
    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this.toFloat(), context.resources.displayMetrics)

/**
 * Converts dp value to sp.<br><br>
 * dp 값을 sp로 변환합니다.<br>
 *
 * @param context The Android context for accessing display metrics.<br><br>
 *                디스플레이 메트릭에 접근하기 위한 Android 컨텍스트.<br>
 *
 * @return The converted sp value as Float.<br><br>
 *         Float로 변환된 sp 값.<br>
 */
public fun Number.dpToSp(context: Context): Float =
    TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, this.toFloat(), context.resources.displayMetrics,
    ) / (context.resources.displayMetrics.density * context.resources.configuration.fontScale)

/****************
 * PX To DP, SP *
 ****************/

/**
 * Converts pixel value to dp.<br><br>
 * 픽셀 값을 dp로 변환합니다.<br>
 *
 * @param context The Android context for accessing display metrics.<br><br>
 *                디스플레이 메트릭에 접근하기 위한 Android 컨텍스트.<br>
 *
 * @return The converted dp value as Float.<br><br>
 *         Float로 변환된 dp 값.<br>
 */
public fun Number.pxToDp(context: Context): Float = this.toFloat() / context.resources.displayMetrics.density

/**
 * Converts pixel value to sp.<br><br>
 * 픽셀 값을 sp로 변환합니다.<br>
 *
 * @param context The Android context for accessing display metrics.<br><br>
 *                디스플레이 메트릭에 접근하기 위한 Android 컨텍스트.<br>
 *
 * @return The converted sp value as Float.<br><br>
 *         Float로 변환된 sp 값.<br>
 */
public fun Number.pxToSp(context: Context): Float =
    (this.toFloat() / context.resources.displayMetrics.density / context.resources.configuration.fontScale)

/****************
 * SP To DP, PX *
 ****************/

/**
 * Converts sp value to pixels.<br><br>
 * sp 값을 픽셀로 변환합니다.<br>
 *
 * @param context The Android context for accessing display metrics.<br><br>
 *                디스플레이 메트릭에 접근하기 위한 Android 컨텍스트.<br>
 *
 * @return The converted pixel value as Float.<br><br>
 *         Float로 변환된 픽셀 값.<br>
 */
public fun Number.spToPx(context: Context): Float =
    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, this.toFloat(), context.resources.displayMetrics)

/**
 * Converts sp value to dp.<br><br>
 * sp 값을 dp로 변환합니다.<br>
 *
 * @param context The Android context for accessing display metrics.<br><br>
 *                디스플레이 메트릭에 접근하기 위한 Android 컨텍스트.<br>
 *
 * @return The converted dp value as Float.<br><br>
 *         Float로 변환된 dp 값.<br>
 */
public fun Number.spToDp(context: Context): Float = (this.toFloat() * context.resources.configuration.fontScale)
