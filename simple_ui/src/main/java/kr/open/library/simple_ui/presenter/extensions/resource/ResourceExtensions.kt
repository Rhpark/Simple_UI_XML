package kr.open.library.simple_ui.presenter.extensions.resource

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import kr.open.library.simple_ui.extensions.trycatch.safeCatch

/**
 * Resource access extension functions for Context.<br>
 * Provides convenient methods to safely access Android resources with backward compatibility.<br><br>
 * Context를 위한 리소스 접근 확장 함수입니다.<br>
 * 역호환성과 함께 Android 리소스에 안전하게 접근하는 편리한 메서드를 제공합니다.<br>
 *
 * Example usage:<br>
 * ```kotlin
 * val icon = context.getDrawableCompat(R.drawable.ic_favorite)
 * val color = context.getColorCompat(R.color.primary_blue)
 * val margin = context.getDimensionPixelSize(R.dimen.margin_large)
 * val message = context.getStringFormatted(R.string.welcome_user, userName)
 *
 * // Safe variants with default values
 * val safeIcon = context.getDrawableSafe(userProvidedResourceId)
 * val safeColor = context.getColorSafe(R.color.theme_color, Color.BLACK)
 * ```
 */

/**
 * Gets a drawable using ContextCompat for backward compatibility.<br><br>
 * 역호환성을 위해 ContextCompat을 사용하여 drawable을 가져옵니다.<br>
 *
 * @param drawableRes The drawable resource ID.<br><br>
 *                    drawable 리소스 ID.<br>
 *
 * @return The drawable or null if not found.<br><br>
 *         drawable 또는 찾지 못한 경우 null.<br>
 */
public fun Context.getDrawableCompat(@DrawableRes drawableRes: Int,
): Drawable? = ContextCompat.getDrawable(this, drawableRes)

/**
 * Gets a color using ContextCompat for backward compatibility.<br><br>
 * 역호환성을 위해 ContextCompat을 사용하여 색상을 가져옵니다.<br>
 *
 * @param colorRes The color resource ID.<br><br>
 *                 색상 리소스 ID.<br>
 *
 * @return The color as an integer.<br><br>
 *         정수값으로 반환된 색상.<br>
 */
public fun Context.getColorCompat(@ColorRes colorRes: Int,
): Int = ContextCompat.getColor(this, colorRes)

/**
 * Gets dimension pixel size for the given dimension resource.<br><br>
 * 주어진 dimension 리소스의 픽셀 크기를 가져옵니다.<br>
 *
 * @param dimenRes The dimension resource ID.<br><br>
 *                 dimension 리소스 ID.<br>
 *
 * @return The dimension in pixels.<br><br>
 *         픽셀 단위의 dimension 값.<br>
 */
public fun Context.getDimensionPixelSize(@DimenRes dimenRes: Int,
): Int = resources.getDimensionPixelSize(dimenRes)

/**
 * Gets dimension pixel offset for the given dimension resource.<br><br>
 * 주어진 dimension 리소스의 픽셀 오프셋을 가져옵니다.<br>
 *
 * @param dimenRes The dimension resource ID.<br><br>
 *                 dimension 리소스 ID.<br>
 *
 * @return The dimension offset in pixels.<br><br>
 *         픽셀 단위의 dimension 오프셋 값.<br>
 */
public fun Context.getDimensionPixelOffset(@DimenRes dimenRes: Int,
): Int = resources.getDimensionPixelOffset(dimenRes)

/**
 * Gets formatted string with arguments.<br><br>
 * 인자를 사용하여 포맷된 문자열을 가져옵니다.<br>
 *
 * @param stringRes The string resource ID.<br><br>
 *                  문자열 리소스 ID.<br>
 *
 * @param args The formatting arguments.<br><br>
 *             포맷팅 인자.<br>
 *
 * @return The formatted string.<br><br>
 *         포맷된 문자열.<br>
 */
public fun Context.getStringFormatted(@StringRes stringRes: Int, vararg args: Any,
): String = getString(stringRes, *args)

/**
 * Gets string array from resources.<br><br>
 * 리소스에서 문자열 배열을 가져옵니다.<br>
 *
 * @param arrayRes The string array resource ID.<br><br>
 *                 문자열 배열 리소스 ID.<br>
 *
 * @return Array of strings.<br><br>
 *         문자열 배열.<br>
 */
public fun Context.getStringArray(arrayRes: Int): Array<String> = resources.getStringArray(arrayRes)

/**
 * Gets integer value from resources.<br><br>
 * 리소스에서 정수 값을 가져옵니다.<br>
 *
 * @param intRes The integer resource ID.<br><br>
 *               정수 리소스 ID.<br>
 *
 * @return The integer value.<br><br>
 *         정수 값.<br>
 */
public fun Context.getInteger(intRes: Int): Int = resources.getInteger(intRes)

/**
 * Safely gets a drawable, returning null if resource is not found or invalid.<br><br>
 * 리소스를 찾을 수 없거나 유효하지 않은 경우 null을 반환하여 안전하게 drawable을 가져옵니다.<br>
 *
 * @param drawableRes The drawable resource ID.<br><br>
 *                    drawable 리소스 ID.<br>
 *
 * @return The drawable or null if not found/invalid.<br><br>
 *         drawable 또는 찾지 못했거나 유효하지 않은 경우 null.<br>
 */
public fun Context.getDrawableSafe(@DrawableRes drawableRes: Int,
): Drawable? = safeCatch(defaultValue = null) { ContextCompat.getDrawable(this, drawableRes) }

/**
 * Safely gets a color, returning a default color if resource is not found or invalid.<br><br>
 * 리소스를 찾을 수 없거나 유효하지 않은 경우 기본 색상을 반환하여 안전하게 색상을 가져옵니다.<br>
 *
 * @param colorRes The color resource ID.<br><br>
 *                 색상 리소스 ID.<br>
 *
 * @param defaultColor The default color to return if resource is invalid.<br><br>
 *                     리소스가 유효하지 않을 때 반환할 기본 색상.<br>
 *
 * @return The color or default color.<br><br>
 *         색상 또는 기본 색상.<br>
 */
public fun Context.getColorSafe(
    @ColorRes colorRes: Int, defaultColor: Int,
): Int = safeCatch(defaultValue = defaultColor) { ContextCompat.getColor(this, colorRes) }

/**
 * Safely gets a string, returning empty string if resource is not found or invalid.<br><br>
 * 리소스를 찾을 수 없거나 유효하지 않은 경우 빈 문자열을 반환하여 안전하게 문자열을 가져옵니다.<br>
 *
 * @param stringRes The string resource ID.<br><br>
 *                  문자열 리소스 ID.<br>
 *
 * @return The string or empty string if not found.<br><br>
 *         문자열 또는 찾지 못한 경우 빈 문자열.<br>
 */
public fun Context.getStringSafe(@StringRes stringRes: Int, ): String = safeCatch(defaultValue = "") { getString(stringRes) }
