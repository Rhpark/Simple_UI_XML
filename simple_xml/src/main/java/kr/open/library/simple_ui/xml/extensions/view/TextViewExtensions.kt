package kr.open.library.simple_ui.xml.extensions.view

import android.graphics.Paint
import android.graphics.Typeface
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat

/**
 * TextView extension functions for text manipulation and styling.<br>
 * Provides convenient methods for text extraction, type conversion, and text style formatting.<br><br>
 * 텍스트 조작 및 스타일링을 위한 TextView 확장 함수입니다.<br>
 * 텍스트 추출, 타입 변환 및 텍스트 스타일 포맷팅을 위한 편리한 메서드를 제공합니다.<br>
 *
 * Example usage:<br>
 * ```kotlin
 * // Text extraction and conversion
 * val text = textView.getString()
 * val isEmpty = textView.isTextEmpty()
 * val number = textView.textToInt() ?: 0
 *
 * // Text styling
 * textView.bold()
 * textView.italic()
 * textView.underline()
 * textView.strikeThrough()
 * textView.setTextColorRes(R.color.primary)
 *
 * // Chained styling
 * textView.style {
 *     bold()
 *     underline()
 *     setTextColorRes(R.color.red)
 * }
 * ```
 */

/**
 * Converts TextView content to String.<br><br>
 * TextView 내용을 String으로 변환합니다.<br>
 *
 * @return The text content as a String.<br><br>
 *         String으로 변환된 텍스트 내용.<br>
 */
public fun TextView.getString(): String = this.text.toString()

/**
 * Checks if the TextView content is empty.<br><br>
 * TextView 내용이 비어있는지 확인합니다.<br>
 *
 * @return `true` if the text is empty, `false` otherwise.<br><br>
 *         텍스트가 비어있으면 `true`, 그 외는 `false`.<br>
 */
public fun TextView.isTextEmpty(): Boolean = this.getString().isEmpty()

/**
 * Checks if the TextView content is null or empty.<br><br>
 * TextView 내용이 null이거나 비어있는지 확인합니다.<br>
 *
 * @return `true` if the text is null or empty, `false` otherwise.<br><br>
 *         텍스트가 null이거나 비어있으면 `true`, 그 외는 `false`.<br>
 */
public fun TextView.isTextNullOrEmpty(): Boolean = this.getString().isEmpty()

/**
 * Safely converts TextView content to Int.<br>
 * Returns null if the conversion fails.<br><br>
 * TextView 내용을 안전하게 Int로 변환합니다.<br>
 * 변환에 실패하면 null을 반환합니다.<br>
 *
 * @return The text content as Int, or null if conversion fails.<br><br>
 *         Int로 변환된 텍스트 내용, 변환 실패 시 null.<br>
 */
public fun TextView.textToInt(): Int? = this.text.toString().toIntOrNull()

/**
 * Safely converts TextView content to Float.<br>
 * Returns null if the conversion fails.<br><br>
 * TextView 내용을 안전하게 Float로 변환합니다.<br>
 * 변환에 실패하면 null을 반환합니다.<br>
 *
 * @return The text content as Float, or null if conversion fails.<br><br>
 *         Float로 변환된 텍스트 내용, 변환 실패 시 null.<br>
 */
public fun TextView.textToFloat(): Float? = this.text.toString().toFloatOrNull()

/**
 * Safely converts TextView content to Double.<br>
 * Returns null if the conversion fails.<br><br>
 * TextView 내용을 안전하게 Double로 변환합니다.<br>
 * 변환에 실패하면 null을 반환합니다.<br>
 *
 * @return The text content as Double, or null if conversion fails.<br><br>
 *         Double로 변환된 텍스트 내용, 변환 실패 시 null.<br>
 */
public fun TextView.textToDouble(): Double? = this.text.toString().toDoubleOrNull()

/**
 * Makes the TextView text bold.<br><br>
 * TextView 텍스트를 굵게 만듭니다.<br>
 *
 * @return The TextView instance for method chaining.<br><br>
 *         메서드 체이닝을 위한 TextView 인스턴스.<br>
 */
public fun TextView.bold(): TextView = apply {
    setTypeface(typeface, Typeface.BOLD)
}

/**
 * Makes the TextView text italic.<br><br>
 * TextView 텍스트를 기울임꼴로 만듭니다.<br>
 *
 * @return The TextView instance for method chaining.<br><br>
 *         메서드 체이닝을 위한 TextView 인스턴스.<br>
 */
public fun TextView.italic(): TextView = apply {
    setTypeface(typeface, Typeface.ITALIC)
}

/**
 * Makes the TextView text bold and italic.<br><br>
 * TextView 텍스트를 굵은 기울임꼴로 만듭니다.<br>
 *
 * @return The TextView instance for method chaining.<br><br>
 *         메서드 체이닝을 위한 TextView 인스턴스.<br>
 */
public fun TextView.boldItalic(): TextView = apply {
    setTypeface(typeface, Typeface.BOLD_ITALIC)
}

/**
 * Makes the TextView text normal (removes bold/italic).<br><br>
 * TextView 텍스트를 보통으로 만듭니다 (굵게/기울임꼴 제거).<br>
 *
 * @return The TextView instance for method chaining.<br><br>
 *         메서드 체이닝을 위한 TextView 인스턴스.<br>
 */
public fun TextView.normal(): TextView = apply {
    setTypeface(typeface, Typeface.NORMAL)
}

/**
 * Adds underline to the TextView text.<br><br>
 * TextView 텍스트에 밑줄을 추가합니다.<br>
 *
 * @return The TextView instance for method chaining.<br><br>
 *         메서드 체이닝을 위한 TextView 인스턴스.<br>
 */
public fun TextView.underline(): TextView = apply {
    paintFlags = paintFlags or Paint.UNDERLINE_TEXT_FLAG
}

/**
 * Removes underline from the TextView text.<br><br>
 * TextView 텍스트에서 밑줄을 제거합니다.<br>
 *
 * @return The TextView instance for method chaining.<br><br>
 *         메서드 체이닝을 위한 TextView 인스턴스.<br>
 */
public fun TextView.removeUnderline(): TextView = apply {
    paintFlags = paintFlags and Paint.UNDERLINE_TEXT_FLAG.inv()
}

/**
 * Adds strikethrough to the TextView text.<br><br>
 * TextView 텍스트에 취소선을 추가합니다.<br>
 *
 * @return The TextView instance for method chaining.<br><br>
 *         메서드 체이닝을 위한 TextView 인스턴스.<br>
 */
public fun TextView.strikeThrough(): TextView = apply {
    paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
}

/**
 * Removes strikethrough from the TextView text.<br><br>
 * TextView 텍스트에서 취소선을 제거합니다.<br>
 *
 * @return The TextView instance for method chaining.<br><br>
 *         메서드 체이닝을 위한 TextView 인스턴스.<br>
 */
public fun TextView.removeStrikeThrough(): TextView = apply {
    paintFlags = paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
}

/**
 * Sets text color using color resource.<br><br>
 * 색상 리소스를 사용하여 텍스트 색상을 설정합니다.<br>
 *
 * @param colorRes Color resource ID.<br><br>
 *                 색상 리소스 ID.<br>
 *
 * @return The TextView instance for method chaining.<br><br>
 *         메서드 체이닝을 위한 TextView 인스턴스.<br>
 */
public fun TextView.setTextColorRes(@ColorRes colorRes: Int): TextView = apply {
    setTextColor(ContextCompat.getColor(context, colorRes))
}

/**
 * Chains multiple styling operations using a DSL-style block.<br><br>
 * DSL 스타일 블록을 사용하여 여러 스타일링 작업을 연쇄합니다.<br>
 *
 * @param block Configuration block for styling operations.<br><br>
 *              스타일링 작업을 위한 설정 블록.<br>
 *
 * @return The TextView instance for method chaining.<br><br>
 *         메서드 체이닝을 위한 TextView 인스턴스.<br>
 */
public fun TextView.style(block: TextView.() -> Unit): TextView = apply(block)