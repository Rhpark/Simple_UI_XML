/**
 * ImageView extension functions for image manipulation and styling.<br>
 * Provides convenient methods for setting images, tints, filters, and scale types.<br><br>
 * 이미지 조작 및 스타일링을 위한 ImageView 확장 함수입니다.<br>
 * 이미지 설정, 색조, 필터 및 스케일 타입 설정을 위한 편리한 메서드를 제공합니다.<br>
 *
 * Example usage:<br>
 * ```kotlin
 * // Basic usage
 * imageView.setImageDrawableRes(R.drawable.icon)
 * imageView.setTint(R.color.primary)
 * imageView.centerCrop()
 *
 * // Chaining operations
 * imageView.style {
 *     setImageDrawableRes(R.drawable.icon)
 *     setTint(R.color.primary)
 *     centerCrop()
 * }
 *
 * // Load with transformations
 * imageView.load(R.drawable.icon) {
 *     setTint(R.color.accent)
 *     makeGrayscale()
 * }
 * ```
 */
package kr.open.library.simple_ui.xml.extensions.view

import android.content.res.ColorStateList
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.PorterDuff
import android.widget.ImageView
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat
import kr.open.library.simple_ui.xml.extensions.resource.getDrawableSafe

/**
 * Sets drawable resource to ImageView.<br><br>
 * ImageView에 drawable 리소스를 설정합니다.<br>
 *
 * @param drawableRes Drawable resource ID.<br><br>
 *                    Drawable 리소스 ID.<br>
 *
 * @return The ImageView instance for method chaining.<br><br>
 *         메서드 체이닝을 위한 ImageView 인스턴스.<br>
 */
public fun ImageView.setImageDrawableRes(
    @DrawableRes drawableRes: Int,
): ImageView =
    apply {
        setImageDrawable(context.getDrawableSafe(drawableRes))
    }

/**
 * Sets tint color to ImageView using color resource.<br><br>
 * 색상 리소스를 사용하여 ImageView에 색조 색상을 설정합니다.<br>
 *
 * @param colorRes Color resource ID.<br><br>
 *                 색상 리소스 ID.<br>
 *
 * @param mode Tint mode (default: SRC_IN).<br><br>
 *             색조 모드 (기본값: SRC_IN).<br>
 *
 * @return The ImageView instance for method chaining.<br><br>
 *         메서드 체이닝을 위한 ImageView 인스턴스.<br>
 */
public fun ImageView.setTint(
    @ColorRes colorRes: Int,
    mode: PorterDuff.Mode = PorterDuff.Mode.SRC_IN,
): ImageView =
    apply {
        val color = ContextCompat.getColor(context, colorRes)
        ImageViewCompat.setImageTintList(this, ColorStateList.valueOf(color))
        ImageViewCompat.setImageTintMode(this, mode)
    }

/**
 * Clears tint from ImageView.<br><br>
 * ImageView에서 색조를 제거합니다.<br>
 *
 * @return The ImageView instance for method chaining.<br><br>
 *         메서드 체이닝을 위한 ImageView 인스턴스.<br>
 */
public fun ImageView.clearTint(): ImageView =
    apply {
        ImageViewCompat.setImageTintList(this, null)
        ImageViewCompat.setImageTintMode(this, null)
    }

/**
 * Makes ImageView grayscale by setting saturation to 0.<br><br>
 * 채도를 0으로 설정하여 ImageView를 흑백으로 만듭니다.<br>
 *
 * @return The ImageView instance for method chaining.<br><br>
 *         메서드 체이닝을 위한 ImageView 인스턴스.<br>
 */
public fun ImageView.makeGrayscale(): ImageView =
    apply {
        val matrix = ColorMatrix()
        matrix.setSaturation(0f)
        colorFilter = ColorMatrixColorFilter(matrix)
    }

/**
 * Removes grayscale filter from ImageView.<br><br>
 * ImageView에서 흑백 필터를 제거합니다.<br>
 *
 * @return The ImageView instance for method chaining.<br><br>
 *         메서드 체이닝을 위한 ImageView 인스턴스.<br>
 */
public fun ImageView.removeGrayscale(): ImageView =
    apply {
        colorFilter = null
    }

/**
 * Sets ScaleType to CENTER_CROP.<br><br>
 * ScaleType을 CENTER_CROP으로 설정합니다.<br>
 *
 * @return The ImageView instance for method chaining.<br><br>
 *         메서드 체이닝을 위한 ImageView 인스턴스.<br>
 */
public fun ImageView.centerCrop(): ImageView =
    apply {
        scaleType = ImageView.ScaleType.CENTER_CROP
    }

/**
 * Sets ScaleType to CENTER_INSIDE.<br><br>
 * ScaleType을 CENTER_INSIDE로 설정합니다.<br>
 *
 * @return The ImageView instance for method chaining.<br><br>
 *         메서드 체이닝을 위한 ImageView 인스턴스.<br>
 */
public fun ImageView.centerInside(): ImageView =
    apply {
        scaleType = ImageView.ScaleType.CENTER_INSIDE
    }

/**
 * Sets ScaleType to FIT_CENTER.<br><br>
 * ScaleType을 FIT_CENTER로 설정합니다.<br>
 *
 * @return The ImageView instance for method chaining.<br><br>
 *         메서드 체이닝을 위한 ImageView 인스턴스.<br>
 */
public fun ImageView.fitCenter(): ImageView =
    apply {
        scaleType = ImageView.ScaleType.FIT_CENTER
    }

/**
 * Sets ScaleType to FIT_XY (stretches to fill).<br><br>
 * ScaleType을 FIT_XY로 설정합니다 (늘려서 채움).<br>
 *
 * @return The ImageView instance for method chaining.<br><br>
 *         메서드 체이닝을 위한 ImageView 인스턴스.<br>
 */
public fun ImageView.fitXY(): ImageView =
    apply {
        scaleType = ImageView.ScaleType.FIT_XY
    }

/**
 * Chains multiple ImageView operations using a DSL-style block.<br><br>
 * DSL 스타일 블록을 사용하여 여러 ImageView 작업을 연쇄합니다.<br>
 *
 * @param block Configuration block for styling operations.<br><br>
 *              스타일링 작업을 위한 설정 블록.<br>
 *
 * @return The ImageView instance for method chaining.<br><br>
 *         메서드 체이닝을 위한 ImageView 인스턴스.<br>
 */
public fun ImageView.style(block: ImageView.() -> Unit): ImageView = apply(block)

/**
 * Loads drawable and applies transformations.<br><br>
 * drawable을 로드하고 변환을 적용합니다.<br>
 *
 * @param drawableRes Drawable resource ID.<br><br>
 *                    Drawable 리소스 ID.<br>
 *
 * @param block Configuration block for additional styling.<br><br>
 *              추가 스타일링을 위한 설정 블록.<br>
 *
 * @return The ImageView instance for method chaining.<br><br>
 *         메서드 체이닝을 위한 ImageView 인스턴스.<br>
 */
public fun ImageView.load(
    @DrawableRes drawableRes: Int,
    block: (ImageView.() -> Unit)? = null,
): ImageView =
    apply {
        setImageDrawableRes(drawableRes)
        block?.invoke(this)
    }
