package kr.open.library.simple_ui.extensions

import kr.open.library.simple_ui.extensions.string.*
import org.junit.Test
import org.junit.Assert.*

/**
 * String Extensions에 대한 단위 테스트
 *
 * 테스트 대상:
 * - 숫자 검증 (isNumeric)
 * - 영숫자 검증 (isAlphaNumeric)
 * - 공백 제거 (removeWhitespace)
 * - HTML 태그 제거 (stripHtmlTags)
 *
 * Note: 이메일/전화번호/URL 검증 테스트는 android.util.Patterns 의존성으로 인해
 *       JVM Unit Test에서 실행 불가능하므로 제외되었습니다.
 *       해당 기능은 androidTest/ 폴더에서 테스트 가능합니다.
 */
class StringExtensionsTest {

    // ========== 1. 숫자 검증 테스트 ==========

    /**
     * 숫자로만 구성되면 true를 반환한다
     */
    @Test
    fun testIsNumericWithOnlyDigitsReturnsTrue() {
        // Given
        val numericStrings = listOf(
            "12345",
            "0",
            "999",
            ""  // 빈 문자열은 숫자로 간주
        )

        // When & Then
        numericStrings.forEach { str ->
            assertTrue("'$str'은 숫자여야 합니다", str.isNumeric())
        }
    }

    /**
     * 숫자가 아닌 문자가 포함되면 false를 반환한다
     */
    @Test
    fun testIsNumericWithNonDigitsReturnsFalse() {
        // Given
        val nonNumericStrings = listOf(
            "123a45",
            "abc",
            "12.34",  // 소수점 포함
            "12-34"   // 하이픈 포함
        )

        // When & Then
        nonNumericStrings.forEach { str ->
            assertFalse("'$str'은 숫자가 아니어야 합니다", str.isNumeric())
        }
    }

    // ========== 2. 영숫자 검증 테스트 ==========

    /**
     * 영숫자로만 구성되면 true를 반환한다
     */
    @Test
    fun testIsAlphaNumericWithValidCharsReturnsTrue() {
        // Given
        val alphanumericStrings = listOf(
            "abc123",
            "ABC123",
            "test",
            "123",
            ""  // 빈 문자열은 영숫자로 간주
        )

        // When & Then
        alphanumericStrings.forEach { str ->
            assertTrue("'$str'은 영숫자여야 합니다", str.isAlphaNumeric())
        }
    }

    /**
     * 특수문자가 포함되면 false를 반환한다
     */
    @Test
    fun testIsAlphaNumericWithSpecialCharsReturnsFalse() {
        // Given
        val nonAlphanumericStrings = listOf(
            "abc-123",
            "test@123",
            "hello world",  // 공백 포함
            "test!"
        )

        // When & Then
        nonAlphanumericStrings.forEach { str ->
            assertFalse("'$str'은 영숫자가 아니어야 합니다", str.isAlphaNumeric())
        }
    }

    // ========== 3. 공백 제거 테스트 ==========

    /**
     * 모든 공백을 제거한다
     */
    @Test
    fun testRemoveWhitespaceRemovesAllWhitespace() {
        // Given
        val input = "Hello World\n\t"

        // When
        val result = input.removeWhitespace()

        // Then
        assertEquals("HelloWorld", result)
    }

    /**
     * 여러 종류의 공백을 모두 제거한다
     */
    @Test
    fun testRemoveWhitespaceRemovesMultipleTypes() {
        // Given
        val input = "  a b c  "

        // When
        val result = input.removeWhitespace()

        // Then
        assertEquals("abc", result)
    }

    /**
     * 공백이 없으면 원본을 반환한다
     */
    @Test
    fun testRemoveWhitespaceWithNoWhitespaceReturnsOriginal() {
        // Given
        val input = "HelloWorld"

        // When
        val result = input.removeWhitespace()

        // Then
        assertEquals("HelloWorld", result)
    }

    // ========== 4. HTML 태그 제거 테스트 ==========

    /**
     * HTML 태그를 제거하고 텍스트만 반환한다
     */
    @Test
    fun testStripHtmlTagsRemovesTagsAndReturnsText() {
        // Given
        val input = "<p>Hello <b>World</b></p>"

        // When
        val result = input.stripHtmlTags()

        // Then
        assertEquals("Hello World", result)
    }

    /**
     * div 태그를 제거한다
     */
    @Test
    fun testStripHtmlTagsRemovesDivTag() {
        // Given
        val input = "<div>Text</div>"

        // When
        val result = input.stripHtmlTags()

        // Then
        assertEquals("Text", result)
    }

    /**
     * HTML 태그가 없으면 원본을 반환한다
     */
    @Test
    fun testStripHtmlTagsWithNoTagsReturnsOriginal() {
        // Given
        val input = "Plain text"

        // When
        val result = input.stripHtmlTags()

        // Then
        assertEquals("Plain text", result)
    }

    /**
     * 여러 HTML 태그를 한번에 제거한다
     */
    @Test
    fun testStripHtmlTagsRemovesMultipleTags() {
        // Given
        val input = "<div><p>Hello</p><span>World</span></div>"

        // When
        val result = input.stripHtmlTags()

        // Then
        assertEquals("HelloWorld", result)
    }
}
