package kr.open.library.simple_ui.xml.extensions.view

import android.widget.EditText

/**
 * EditText extension functions for convenient text extraction and type conversion.<br>
 * Provides methods to safely convert EditText content to various data types.<br><br>
 * 편리한 텍스트 추출 및 타입 변환을 위한 EditText 확장 함수입니다.<br>
 * EditText 내용을 다양한 데이터 타입으로 안전하게 변환하는 메서드를 제공합니다.<br>
 *
 * Example usage:<br>
 * ```kotlin
 * val text = editText.getTextToString()
 * val isEmpty = editText.isTextEmpty()
 * val age = editText.textToInt() ?: 0
 * val price = editText.textToDouble() ?: 0.0
 * ```
 *
 * 사용 예시:<br>
 * ```kotlin
 * val text = editText.getTextToString()
 * val isEmpty = editText.isTextEmpty()
 * val age = editText.textToInt() ?: 0
 * val price = editText.textToDouble() ?: 0.0
 * ```
 */

/**
 * Converts EditText content to String.<br><br>
 * EditText 내용을 String으로 변환합니다.<br>
 *
 * @return The text content as a String.<br><br>
 *         String으로 변환된 텍스트 내용.<br>
 */
public fun EditText.getTextToString(): String = this.text.toString()

/**
 * Checks if the EditText content is empty.<br><br>
 * EditText 내용이 비어있는지 확인합니다.<br>
 *
 * @return `true` if the text is empty, `false` otherwise.<br><br>
 *         텍스트가 비어있으면 `true`, 그 외는 `false`.<br>
 */
public fun EditText.isTextEmpty(): Boolean = this.getTextToString().isEmpty()

/**
 * Safely converts EditText content to Int.<br>
 * Returns null if the conversion fails.<br><br>
 * EditText 내용을 안전하게 Int로 변환합니다.<br>
 * 변환에 실패하면 null을 반환합니다.<br>
 *
 * @return The text content as Int, or null if conversion fails.<br><br>
 *         Int로 변환된 텍스트 내용, 변환 실패 시 null.<br>
 */
public fun EditText.textToInt(): Int? = this.text.toString().toIntOrNull()

/**
 * Safely converts EditText content to Float.<br>
 * Returns null if the conversion fails.<br><br>
 * EditText 내용을 안전하게 Float로 변환합니다.<br>
 * 변환에 실패하면 null을 반환합니다.<br>
 *
 * @return The text content as Float, or null if conversion fails.<br><br>
 *         Float로 변환된 텍스트 내용, 변환 실패 시 null.<br>
 */
public fun EditText.textToFloat(): Float? = this.text.toString().toFloatOrNull()

/**
 * Safely converts EditText content to Double.<br>
 * Returns null if the conversion fails.<br><br>
 * EditText 내용을 안전하게 Double로 변환합니다.<br>
 * 변환에 실패하면 null을 반환합니다.<br>
 *
 * @return The text content as Double, or null if conversion fails.<br><br>
 *         Double로 변환된 텍스트 내용, 변환 실패 시 null.<br>
 */
public fun EditText.textToDouble(): Double? = this.text.toString().toDoubleOrNull()
