package kr.open.library.simple_ui.presenter.extensions.view

import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.PorterDuff
import android.widget.ImageView
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import kr.open.library.simple_ui.presenter.extensions.resource.getDrawableSafe

/**
 * Sets drawable resource to ImageView
 *
 * @param drawableRes Drawable resource ID
 *
 * Example:
 * ```
 * imageView.setImageDrawableRes(R.drawable.icon)
 * ```
 */
public fun ImageView.setImageDrawableRes(@DrawableRes drawableRes: Int): ImageView = apply {
    setImageDrawable(context.getDrawableSafe(drawableRes))
}

/**
 * Sets tint color to ImageView using color resource
 *
 * @param colorRes Color resource ID
 * @param mode Tint mode (default: SRC_IN)
 *
 * Example:
 * ```
 * imageView.setTint(R.color.primary)
 * ```
 */
public fun ImageView.setTint(@ColorRes colorRes: Int, mode: PorterDuff.Mode = PorterDuff.Mode.SRC_IN): ImageView = apply {
    val color = ContextCompat.getColor(context, colorRes)
    setColorFilter(color, mode)
}

/**
 * Clears tint from ImageView
 *
 * Example:
 * ```
 * imageView.clearTint()
 * ```
 */
public fun ImageView.clearTint(): ImageView = apply {
    clearColorFilter()
}

/**
 * Makes ImageView grayscale
 *
 * Example:
 * ```
 * imageView.makeGrayscale()
 * ```
 */
public fun ImageView.makeGrayscale(): ImageView = apply {
    val matrix = ColorMatrix()
    matrix.setSaturation(0f)
    colorFilter = ColorMatrixColorFilter(matrix)
}

/**
 * Removes grayscale filter from ImageView
 *
 * Example:
 * ```
 * imageView.removeGrayscale()
 * ```
 */
public fun ImageView.removeGrayscale(): ImageView = apply {
    colorFilter = null
}

/**
 * Sets alpha transparency to ImageView
 *
 * @param alpha Alpha value (0.0f - 1.0f)
 *
 * Example:
 * ```
 * imageView.setAlpha(0.5f)
 * ```
 */
public fun ImageView.setAlpha(alpha: Float): ImageView = apply {
    imageAlpha = (alpha * 255).toInt().coerceIn(0, 255)
}

/**
 * Sets ScaleType to CENTER_CROP
 *
 * Example:
 * ```
 * imageView.centerCrop()
 * ```
 */
public fun ImageView.centerCrop(): ImageView = apply {
    scaleType = ImageView.ScaleType.CENTER_CROP
}

/**
 * Sets ScaleType to CENTER_INSIDE
 *
 * Example:
 * ```
 * imageView.centerInside()
 * ```
 */
public fun ImageView.centerInside(): ImageView = apply {
    scaleType = ImageView.ScaleType.CENTER_INSIDE
}

/**
 * Sets ScaleType to FIT_CENTER
 *
 * Example:
 * ```
 * imageView.fitCenter()
 * ```
 */
public fun ImageView.fitCenter(): ImageView = apply {
    scaleType = ImageView.ScaleType.FIT_CENTER
}

/**
 * Sets ScaleType to FIT_XY (stretches to fill)
 *
 * Example:
 * ```
 * imageView.fitXY()
 * ```
 */
public fun ImageView.fitXY(): ImageView = apply {
    scaleType = ImageView.ScaleType.FIT_XY
}

/**
 * Chains multiple ImageView operations
 *
 * Example:
 * ```
 * imageView.style {
 *     setImageDrawableRes(R.drawable.icon)
 *     setTint(R.color.primary)
 *     centerCrop()
 * }
 * ```
 */
public fun ImageView.style(block: ImageView.() -> Unit): ImageView = apply(block)

/**
 * Loads drawable and applies transformations
 *
 * @param drawableRes Drawable resource ID
 * @param block Configuration block for additional styling
 *
 * Example:
 * ```
 * imageView.load(R.drawable.icon) {
 *     setTint(R.color.primary)
 *     centerCrop()
 * }
 * ```
 */
public fun ImageView.load(@DrawableRes drawableRes: Int, block: (ImageView.() -> Unit)? = null): ImageView = apply {
    setImageDrawableRes(drawableRes)
    block?.invoke(this)
}