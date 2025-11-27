/**
 * String validation and cleanup helpers that keep pattern checks consistent.<br><br>
 * 문자열 패턴 검증과 정리를 일관되게 처리하기 위한 확장 함수 모음입니다.<br>
 */
package kr.open.library.simple_ui.core.extensions.string

import androidx.annotation.CheckResult

// Precompiled regex patterns for better performance
private val WHITESPACE_REGEX = "[\\s\\p{Z}]".toRegex()
private val HTML_TAG_REGEX = "<[^>]*>".toRegex()
private val NUMERIC_REGEX = "^[0-9]*$".toRegex()
private val ALPHANUMERIC_REGEX = "^[a-zA-Z0-9]*$".toRegex()

/**
 * Validates the string against Android's built-in email address pattern.<br><br>
 * Android 기본 이메일 패턴에 부합하는지 검사합니다.<br>
 *
 * @return true when the format is a valid email, otherwise false.<br><br>
 *         유효한 이메일이면 true, 아니면 false를 반환합니다.<br>
 *
 * Example:
 * ```
 * "user@example.com".isEmailValid() // returns true
 * "invalid-email".isEmailValid()    // returns false
 * ```
 */
@CheckResult
public fun String.isEmailValid(): Boolean = android.util.Patterns.EMAIL_ADDRESS.matcher(this).matches()

/**
 * Validates the string against Android's built-in phone number pattern.<br><br>
 * Android 기본 전화번호 패턴에 부합하는지 검사합니다.<br>
 *
 * @return true when the format is a valid phone number, otherwise false.<br><br>
 *         유효한 전화번호면 true, 아니면 false를 반환합니다.<br>
 *
 * Example:
 * ```
 * "+1-555-123-4567".isPhoneNumberValid() // returns true
 * "abc123".isPhoneNumberValid()          // returns false
 * ```
 */
@CheckResult
public fun String.isPhoneNumberValid(): Boolean = android.util.Patterns.PHONE.matcher(this).matches()

/**
 * Validates the string against Android's built-in web URL pattern.<br><br>
 * Android 기본 웹 URL 패턴에 부합하는지 검사합니다.<br>
 *
 * @return true when the format is a valid URL, otherwise false.<br><br>
 *         유효한 URL이면 true, 아니면 false를 반환합니다.<br>
 *
 * Example:
 * ```
 * "https://www.example.com".isUrlValid() // returns true
 * "not-a-url".isUrlValid()               // returns false
 * ```
 */
@CheckResult
public fun String.isUrlValid(): Boolean = android.util.Patterns.WEB_URL.matcher(this).matches()

/**
 * Checks whether the string contains only digits (0-9); empty strings count as numeric.<br><br>
 * 문자열이 숫자(0-9)만 포함하는지 검사하며, 빈 문자열도 숫자로 취급합니다.<br>
 *
 * @return true when every character is numeric, otherwise false.<br><br>
 *         모든 문자가 숫자면 true, 아니면 false를 반환합니다.<br>
 *
 * Example:
 * ```
 * "12345".isNumeric()   // returns true
 * "123a45".isNumeric()  // returns false
 * "".isNumeric()        // returns true
 * ```
 */
@CheckResult
public fun String.isNumeric(): Boolean = matches(NUMERIC_REGEX)

/**
 * Checks whether the string contains only letters and digits; empty strings count as alphanumeric.<br><br>
 * 문자열이 영문자와 숫자만 포함하는지 검사하며, 빈 문자열도 허용합니다.<br>
 *
 * @return true when every character is alphanumeric, otherwise false.<br><br>
 *         모든 문자가 영숫자면 true, 아니면 false를 반환합니다.<br>
 *
 * Example:
 * ```
 * "abc123".isAlphaNumeric()  // returns true
 * "abc-123".isAlphaNumeric() // returns false
 * "".isAlphaNumeric()        // returns true
 * ```
 */
@CheckResult
public fun String.isAlphaNumeric(): Boolean = matches(ALPHANUMERIC_REGEX)

/**
 * Removes every whitespace character such as spaces, tabs, and line breaks from the string.<br><br>
 * 공백, 탭, 줄바꿈 등 모든 공백 문자를 제거합니다.<br>
 *
 * @return A new string with whitespace removed.<br><br>
 *         공백이 제거된 새 문자열을 반환합니다.<br>
 *
 * Example:
 * ```
 * "Hello World\n\t".removeWhitespace() // returns "HelloWorld"
 * "  a b c  ".removeWhitespace()       // returns "abc"
 * ```
 */
@CheckResult
public fun String.removeWhitespace(): String = replace(WHITESPACE_REGEX, "")

/**
 * Strips simple HTML tags using a lightweight regex, leaving only text content.<br><br>
 * 가벼운 정규식을 사용해 단순 HTML 태그를 제거하고 텍스트만 남깁니다.<br>
 *
 * @return A new string with tags removed.<br><br>
 *         태그가 제거된 새 문자열을 반환합니다.<br>
 *
 * Example:
 * ```
 * "<p>Hello <b>World</b></p>".removeHtmlTags()  // returns "Hello World"
 * "<div>Text</div>".removeHtmlTags()            // returns "Text"
 * ```
 *
 * Note: This implementation targets basic markup; use a dedicated parser for complex HTML.<br><br>
 *       단순 마크업에 적합한 구현이므로, 복잡한 HTML은 전용 파서를 사용하는 것이 좋습니다.<br>
 */
@CheckResult
public fun String.removeHtmlTags(): String = replace(HTML_TAG_REGEX, "")
