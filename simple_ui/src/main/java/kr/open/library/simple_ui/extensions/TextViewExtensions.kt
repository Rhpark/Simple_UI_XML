package kr.open.library.simple_ui.extensions

import android.graphics.Paint
import android.graphics.Typeface
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat

public fun TextView.getString(): String = this.text.toString()

public fun TextView.isTextEmpty(): Boolean = this.getString().isEmpty()

public fun TextView.isTextNullOrEmpty(): Boolean = this.getString().isNullOrEmpty()

public fun TextView.textToInt(): Int? = this.text.toString().toIntOrNull()

public fun TextView.textToFloat(): Float? = this.text.toString().toFloatOrNull()

public fun TextView.textToDouble(): Double? = this.text.toString().toDoubleOrNull()

/**
 * Makes the TextView text bold
 *
 * Example:
 * ```
 * textView.bold()
 * ```
 */
public fun TextView.bold(): TextView = apply {
    setTypeface(typeface, Typeface.BOLD)
}

/**
 * Makes the TextView text italic
 *
 * Example:
 * ```
 * textView.italic()
 * ```
 */
public fun TextView.italic(): TextView = apply {
    setTypeface(typeface, Typeface.ITALIC)
}

/**
 * Makes the TextView text bold and italic
 *
 * Example:
 * ```
 * textView.boldItalic()
 * ```
 */
public fun TextView.boldItalic(): TextView = apply {
    setTypeface(typeface, Typeface.BOLD_ITALIC)
}

/**
 * Makes the TextView text normal (removes bold/italic)
 *
 * Example:
 * ```
 * textView.normal()
 * ```
 */
public fun TextView.normal(): TextView = apply {
    setTypeface(typeface, Typeface.NORMAL)
}

/**
 * Adds underline to the TextView text
 *
 * Example:
 * ```
 * textView.underline()
 * ```
 */
public fun TextView.underline(): TextView = apply {
    paintFlags = paintFlags or Paint.UNDERLINE_TEXT_FLAG
}

/**
 * Removes underline from the TextView text
 *
 * Example:
 * ```
 * textView.removeUnderline()
 * ```
 */
public fun TextView.removeUnderline(): TextView = apply {
    paintFlags = paintFlags and Paint.UNDERLINE_TEXT_FLAG.inv()
}

/**
 * Adds strikethrough to the TextView text
 *
 * Example:
 * ```
 * textView.strikeThrough()
 * ```
 */
public fun TextView.strikeThrough(): TextView = apply {
    paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
}

/**
 * Removes strikethrough from the TextView text
 *
 * Example:
 * ```
 * textView.removeStrikeThrough()
 * ```
 */
public fun TextView.removeStrikeThrough(): TextView = apply {
    paintFlags = paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
}

/**
 * Sets text color using color resource
 *
 * @param colorRes Color resource ID
 *
 * Example:
 * ```
 * textView.setTextColorRes(R.color.primary)
 * ```
 */
public fun TextView.setTextColorRes(@ColorRes colorRes: Int): TextView = apply {
    setTextColor(ContextCompat.getColor(context, colorRes))
}

/**
 * Chains multiple styling operations
 *
 * Example:
 * ```
 * textView.bold().underline().setTextColorRes(R.color.red)
 * ```
 */
public fun TextView.style(block: TextView.() -> Unit): TextView = apply(block)