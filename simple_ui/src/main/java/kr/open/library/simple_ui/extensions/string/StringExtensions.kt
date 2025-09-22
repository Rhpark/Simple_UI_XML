package kr.open.library.simple_ui.extensions.string

import androidx.annotation.CheckResult

// Precompiled regex patterns for better performance
private val WHITESPACE_REGEX = "\\s".toRegex()
private val HTML_TAG_REGEX = "<[^>]*>".toRegex()
private val NUMERIC_REGEX = "^[0-9]*$".toRegex()
private val ALPHANUMERIC_REGEX = "^[a-zA-Z0-9]*$".toRegex()

/**
 * Validates if the string is a valid email address using Android's built-in email pattern
 *
 * @return true if the string matches a valid email format, false otherwise
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
 * Validates if the string is a valid phone number using Android's built-in phone pattern
 *
 * @return true if the string matches a valid phone number format, false otherwise
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
 * Validates if the string is a valid URL using Android's built-in web URL pattern
 *
 * @return true if the string matches a valid URL format, false otherwise
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
 * Checks if the string contains only numeric characters (0-9)
 * Empty strings are considered numeric
 *
 * @return true if the string contains only digits, false otherwise
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
 * Checks if the string contains only alphanumeric characters (a-z, A-Z, 0-9)
 * Empty strings are considered alphanumeric
 *
 * @return true if the string contains only letters and digits, false otherwise
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
 * Removes all whitespace characters from the string
 * This includes spaces, tabs, newlines, and other whitespace characters
 *
 * @return a new string with all whitespace characters removed
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
 * Removes all HTML tags from the string, leaving only the text content
 * This uses a simple regex pattern that matches basic HTML tags
 *
 * @return a new string with HTML tags removed
 *
 * Example:
 * ```
 * "<p>Hello <b>World</b></p>".stripHtmlTags()  // returns "Hello World"
 * "<div>Text</div>".stripHtmlTags()            // returns "Text"
 * ```
 *
 * Note: This is a basic implementation suitable for simple HTML.
 * For complex HTML parsing, consider using a proper HTML parser.
 */
@CheckResult
public fun String.stripHtmlTags(): String = replace(HTML_TAG_REGEX, "")
