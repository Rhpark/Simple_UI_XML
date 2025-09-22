package kr.open.library.simple_ui.presenter.extensions.resource

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat

/**
 * Resource access extensions for cleaner resource handling
 */

/**
 * Gets a drawable using ContextCompat for backward compatibility
 *
 * @param drawableRes The drawable resource ID
 * @return The drawable or null if not found
 *
 * Example:
 * val icon = getDrawableCompat(R.drawable.ic_favorite)
 */
public fun Context.getDrawableCompat(@DrawableRes drawableRes: Int,
): Drawable? = ContextCompat.getDrawable(this, drawableRes)

/**
 * Gets a color using ContextCompat for backward compatibility
 *
 * @param colorRes The color resource ID
 * @return The color as an integer
 *
 * Example:
 * val primaryColor = getColorCompat(R.color.primary_blue)
 */
public fun Context.getColorCompat(@ColorRes colorRes: Int,
): Int = ContextCompat.getColor(this, colorRes)

/**
 * Gets dimension pixel size for the given dimension resource
 *
 * @param dimenRes The dimension resource ID
 * @return The dimension in pixels
 *
 * Example:
 * val margin = getDimensionPixelSize(R.dimen.margin_large)
 */
public fun Context.getDimensionPixelSize(@DimenRes dimenRes: Int,
): Int = resources.getDimensionPixelSize(dimenRes)

/**
 * Gets dimension pixel offset for the given dimension resource
 *
 * @param dimenRes The dimension resource ID
 * @return The dimension offset in pixels
 *
 * Example:
 * val offset = getDimensionPixelOffset(R.dimen.shadow_offset)
 */
public fun Context.getDimensionPixelOffset(@DimenRes dimenRes: Int,
): Int = resources.getDimensionPixelOffset(dimenRes)

/**
 * Gets formatted string with arguments
 *
 * @param stringRes The string resource ID
 * @param args The formatting arguments
 * @return The formatted string
 *
 * Example:
 * val message = getStringFormatted(R.string.welcome_user, userName)
 */
public fun Context.getStringFormatted(@StringRes stringRes: Int, vararg args: Any,
): String = getString(stringRes, *args)

/**
 * Gets string array from resources
 *
 * @param arrayRes The string array resource ID
 * @return Array of strings
 *
 * Example:
 * val categories = getStringArray(R.array.product_categories)
 */
public fun Context.getStringArray(arrayRes: Int): Array<String> = resources.getStringArray(arrayRes)

/**
 * Gets integer value from resources
 *
 * @param intRes The integer resource ID
 * @return The integer value
 *
 * Example:
 * val maxItems = getInteger(R.integer.max_grid_items)
 */
public fun Context.getInteger(intRes: Int): Int = resources.getInteger(intRes)

/**
 * Safely gets a drawable, returning null if resource is not found or invalid
 *
 * @param drawableRes The drawable resource ID
 * @return The drawable or null if not found/invalid
 *
 * Example:
 * val safeIcon = getDrawableSafe(userProvidedResourceId)
 */
public fun Context.getDrawableSafe(@DrawableRes drawableRes: Int,
): Drawable? = try {
    ContextCompat.getDrawable(this, drawableRes)
} catch (e: android.content.res.Resources.NotFoundException) {
    null
} catch (e: OutOfMemoryError) {
    null
}

/**
 * Safely gets a color, returning a default color if resource is not found or invalid
 *
 * @param colorRes The color resource ID
 * @param defaultColor The default color to return if resource is invalid
 * @return The color or default color
 *
 * Example:
 * val color = getColorSafe(R.color.theme_color, Color.BLACK)
 */
public fun Context.getColorSafe(
    @ColorRes colorRes: Int, defaultColor: Int,
): Int = try {
    ContextCompat.getColor(this, colorRes)
} catch (e: android.content.res.Resources.NotFoundException) {
    defaultColor
}

/**
 * Safely gets a string, returning empty string if resource is not found or invalid
 *
 * @param stringRes The string resource ID
 * @return The string or empty string if not found
 *
 * Example:
 * val text = getStringSafe(R.string.optional_text)
 */
public fun Context.getStringSafe(@StringRes stringRes: Int,
): String = try {
    getString(stringRes)
} catch (e: android.content.res.Resources.NotFoundException) {
    ""
}
