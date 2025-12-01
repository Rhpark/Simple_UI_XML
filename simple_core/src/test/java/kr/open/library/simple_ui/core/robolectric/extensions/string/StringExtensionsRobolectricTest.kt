package kr.open.library.simple_ui.core.robolectric.extensions.string

import kr.open.library.simple_ui.core.extensions.string.isAlphaNumeric
import kr.open.library.simple_ui.core.extensions.string.isEmailValid
import kr.open.library.simple_ui.core.extensions.string.isNumeric
import kr.open.library.simple_ui.core.extensions.string.isPhoneNumberValid
import kr.open.library.simple_ui.core.extensions.string.isUrlValid
import kr.open.library.simple_ui.core.extensions.string.removeHtmlTags
import kr.open.library.simple_ui.core.extensions.string.removeWhitespace
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Robolectric tests for String extension functions
 * Requires Robolectric for android.util.Patterns
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [android.os.Build.VERSION_CODES.TIRAMISU])
class StringExtensionsRobolectricTest {
    // ==============================================
    // isEmailValid() Tests
    // ==============================================

    @Test
    fun isEmailValid_validEmails_returnTrue() {
        assertTrue("user@example.com".isEmailValid())
        assertTrue("test.user@example.com".isEmailValid())
        assertTrue("user+tag@example.co.uk".isEmailValid())
        assertTrue("user_name@test-domain.com".isEmailValid())
        assertTrue("123@example.com".isEmailValid())
    }

    @Test
    fun isEmailValid_invalidEmails_returnFalse() {
        assertFalse("".isEmailValid())
        assertFalse("invalid-email".isEmailValid())
        assertFalse("@example.com".isEmailValid())
        assertFalse("user@".isEmailValid())
        assertFalse("user @example.com".isEmailValid())
        assertFalse("user@example".isEmailValid())
    }

    // ==============================================
    // isPhoneNumberValid() Tests
    // ==============================================

    @Test
    fun isPhoneNumberValid_validPhoneNumbers_returnTrue() {
        assertTrue("+1-555-123-4567".isPhoneNumberValid())
        assertTrue("555-123-4567".isPhoneNumberValid())
        assertTrue("(555) 123-4567".isPhoneNumberValid())
        assertTrue("5551234567".isPhoneNumberValid())
        assertTrue("+821012345678".isPhoneNumberValid())
    }

    @Test
    fun isPhoneNumberValid_invalidPhoneNumbers_returnFalse() {
        assertFalse("".isPhoneNumberValid())
        assertFalse("abc123".isPhoneNumberValid())
        // Note: "123" might be considered valid by some phone patterns, so we test clearly invalid cases
    }

    // ==============================================
    // isUrlValid() Tests
    // ==============================================

    @Test
    fun isUrlValid_validUrls_returnTrue() {
        assertTrue("https://www.example.com".isUrlValid())
        assertTrue("http://example.com".isUrlValid())
        assertTrue("https://example.com/path/to/page".isUrlValid())
        assertTrue("http://example.com:8080".isUrlValid())
        assertTrue("https://sub.domain.example.com".isUrlValid())
    }

    @Test
    fun isUrlValid_invalidUrls_returnFalse() {
        assertFalse("".isUrlValid())
        assertFalse("not-a-url".isUrlValid())
        // Note: "example.com" behavior varies in Robolectric vs real Android
        // Robolectric's WEB_URL pattern may accept domains without protocols
    }

    // ==============================================
    // isNumeric() Tests
    // ==============================================

    @Test
    fun isNumeric_numericStrings_returnTrue() {
        assertTrue("12345".isNumeric())
        assertTrue("0".isNumeric())
        assertTrue("999999999".isNumeric())
        assertTrue("".isNumeric()) // Empty string is considered numeric
    }

    @Test
    fun isNumeric_nonNumericStrings_returnFalse() {
        assertFalse("123a45".isNumeric())
        assertFalse("abc".isNumeric())
        assertFalse("12.34".isNumeric())
        assertFalse("12-34".isNumeric())
        assertFalse(" 123 ".isNumeric())
        assertFalse("+123".isNumeric())
    }

    // ==============================================
    // isAlphaNumeric() Tests
    // ==============================================

    @Test
    fun isAlphaNumeric_alphanumericStrings_returnTrue() {
        assertTrue("abc123".isAlphaNumeric())
        assertTrue("ABC123".isAlphaNumeric())
        assertTrue("abcABC123".isAlphaNumeric())
        assertTrue("test".isAlphaNumeric())
        assertTrue("123".isAlphaNumeric())
        assertTrue("".isAlphaNumeric()) // Empty string is considered alphanumeric
    }

    @Test
    fun isAlphaNumeric_nonAlphanumericStrings_returnFalse() {
        assertFalse("abc-123".isAlphaNumeric())
        assertFalse("abc 123".isAlphaNumeric())
        assertFalse("abc_123".isAlphaNumeric())
        assertFalse("abc.123".isAlphaNumeric())
        assertFalse("abc@123".isAlphaNumeric())
        assertFalse("한글123".isAlphaNumeric())
    }

    // ==============================================
    // removeWhitespace() Tests
    // ==============================================

    @Test
    fun removeWhitespace_removesAllWhitespace() {
        assertEquals("HelloWorld", "Hello World".removeWhitespace())
        assertEquals("HelloWorld", "Hello World\n\t".removeWhitespace())
        assertEquals("abc", "  a b c  ".removeWhitespace())
        assertEquals("test", "  test  ".removeWhitespace())
        assertEquals("", "   ".removeWhitespace())
        assertEquals("", "".removeWhitespace())
    }

    @Test
    fun removeWhitespace_preservesNonWhitespaceCharacters() {
        assertEquals("Hello-World_123", "Hello-World_123".removeWhitespace())
        assertEquals("test@example.com", "test@example.com".removeWhitespace())
    }

    @Test
    fun removeWhitespace_handlesMultipleWhitespaceTypes() {
        assertEquals("abc", "a b\tc\n".removeWhitespace())
        assertEquals("text", "t e x t".removeWhitespace())
    }

    // ==============================================
    // stripHtmlTags() Tests
    // ==============================================

    @Test
    fun stripHtmlTags_removesSimpleHtmlTags() {
        assertEquals("Hello World", "<p>Hello World</p>".removeHtmlTags())
        assertEquals("Hello World", "<p>Hello <b>World</b></p>".removeHtmlTags())
        assertEquals("Text", "<div>Text</div>".removeHtmlTags())
    }

    @Test
    fun stripHtmlTags_removesMultipleTags() {
        assertEquals(
            "TitleContentFooter",
            "<h1>Title</h1><p>Content</p><footer>Footer</footer>".removeHtmlTags(),
        )
    }

    @Test
    fun stripHtmlTags_removesSelfClosingTags() {
        assertEquals("Text ", "<p>Text <br/></p>".removeHtmlTags())
        assertEquals("Image: ", "Image: <img src='test.jpg'/>".removeHtmlTags())
    }

    @Test
    fun stripHtmlTags_handlesEmptyAndNoTags() {
        assertEquals("", "".removeHtmlTags())
        assertEquals("Plain text", "Plain text".removeHtmlTags())
    }

    @Test
    fun stripHtmlTags_handlesTagsWithAttributes() {
        assertEquals("Link", "<a href='http://example.com'>Link</a>".removeHtmlTags())
        assertEquals(
            "Styled Text",
            "<span style='color: red;'>Styled Text</span>".removeHtmlTags(),
        )
    }

    @Test
    fun stripHtmlTags_preservesSpacesBetweenContent() {
        assertEquals("Hello World", "<span>Hello</span> <span>World</span>".removeHtmlTags())
    }

    // ==============================================
    // Edge Cases and Combined Tests
    // ==============================================

    @Test
    fun stringExtensions_handlesEmptyStrings() {
        assertFalse("".isEmailValid())
        assertFalse("".isPhoneNumberValid())
        assertFalse("".isUrlValid())
        assertTrue("".isNumeric())
        assertTrue("".isAlphaNumeric())
        assertEquals("", "".removeWhitespace())
        assertEquals("", "".removeHtmlTags())
    }

    @Test
    fun stringExtensions_handlesUnicodeCharacters() {
        assertEquals("한글테스트", "한글 테스트".removeWhitespace())
        assertEquals("日本語", "<p>日本語</p>".removeHtmlTags())
    }

    @Test
    fun removeWhitespace_combinedWithOtherOperations() {
        val input = "  Hello World  "
        val cleaned = input.removeWhitespace()
        assertEquals("HelloWorld", cleaned)
        assertTrue(cleaned.isAlphaNumeric())
    }

    @Test
    fun stripHtmlTags_combinedWithWhitespaceRemoval() {
        val html = "<p>Hello World</p>"
        val stripped = html.removeHtmlTags()
        assertEquals("Hello World", stripped)
        assertEquals("HelloWorld", stripped.removeWhitespace())
    }
}
