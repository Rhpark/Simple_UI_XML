package kr.open.library.simple_ui.extensions

import kr.open.library.simple_ui.extensions.string.*
import org.junit.Test
import org.junit.Assert.*
import org.junit.Ignore

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
//@Ignore("임시로 비활성화")
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

    // ========== 5. 추가 엣지 케이스 테스트 ==========

    /**
     * 음수는 숫자가 아닌 것으로 판정된다
     */
    @Test
    fun testIsNumericWithNegativeNumberReturnsFalse() {
        // Given
        val negativeNumbers = listOf("-123", "-1", "-0")

        // When & Then
        negativeNumbers.forEach { str ->
            assertFalse("'$str'은 숫자가 아니어야 합니다 (음수 기호 포함)", str.isNumeric())
        }
    }

    /**
     * 선행 0이 있는 숫자도 정상 처리된다
     */
    @Test
    fun testIsNumericWithLeadingZeros() {
        // Given
        val numbersWithLeadingZeros = listOf("00123", "0000", "01")

        // When & Then
        numbersWithLeadingZeros.forEach { str ->
            assertTrue("'$str'은 숫자여야 합니다 (선행 0 포함)", str.isNumeric())
        }
    }

    /**
     * 공백이 포함된 숫자는 숫자가 아닌 것으로 판정된다
     */
    @Test
    fun testIsNumericWithSpacesReturnsFalse() {
        // Given
        val numbersWithSpaces = listOf("123 456", " 123", "123 ", "1 2 3")

        // When & Then
        numbersWithSpaces.forEach { str ->
            assertFalse("'$str'은 숫자가 아니어야 합니다 (공백 포함)", str.isNumeric())
        }
    }

    /**
     * 매우 긴 숫자 문자열도 정상 처리된다
     */
    @Test
    fun testIsNumericWithVeryLongString() {
        // Given
        val veryLongNumber = "1".repeat(10000)

        // When
        val result = veryLongNumber.isNumeric()

        // Then
        assertTrue("매우 긴 숫자 문자열도 처리 가능해야 합니다", result)
    }

    /**
     * 유니코드 전각 숫자는 숫자가 아닌 것으로 판정된다
     */
    @Test
    fun testIsNumericWithFullWidthDigitsReturnsFalse() {
        // Given
        val fullWidthDigits = "１２３４５" // 전각 숫자

        // When
        val result = fullWidthDigits.isNumeric()

        // Then
        assertFalse("전각 숫자는 숫자가 아니어야 합니다", result)
    }

    /**
     * 특수문자와 영문자가 섞인 복잡한 문자열 테스트
     */
    @Test
    fun testIsAlphaNumericWithMixedSpecialChars() {
        // Given
        val complexStrings = listOf(
            "abc123!@#",
            "test_123",
            "hello-world123",
            "user@domain.com"
        )

        // When & Then
        complexStrings.forEach { str ->
            assertFalse("'$str'은 영숫자가 아니어야 합니다 (특수문자 포함)", str.isAlphaNumeric())
        }
    }

    /**
     * 대소문자 혼합 영숫자는 정상 처리된다
     */
    @Test
    fun testIsAlphaNumericWithMixedCase() {
        // Given
        val mixedCaseStrings = listOf(
            "AbC123",
            "TeSt456XyZ",
            "MiXeD"
        )

        // When & Then
        mixedCaseStrings.forEach { str ->
            assertTrue("'$str'은 영숫자여야 합니다", str.isAlphaNumeric())
        }
    }

    /**
     * 다양한 종류의 공백을 모두 제거한다
     */
    @Test
    fun testRemoveWhitespaceWithVariousWhitespaceTypes() {
        // Given
        val input = "Hello\u0020World\u0009Test\u000A\u000D\u00A0End"
        // \u0020: 일반 공백, \u0009: 탭, \u000A: LF, \u000D: CR, \u00A0: Non-breaking space

        // When
        val result = input.removeWhitespace()

        // Then
        assertEquals("모든 공백이 제거되어 연결되어야 합니다", "HelloWorldTestEnd", result)
        assertFalse("일반 공백이 제거되어야 합니다", result.contains("\u0020"))
        assertFalse("탭이 제거되어야 합니다", result.contains("\u0009"))
        assertFalse("개행이 제거되어야 합니다", result.contains("\u000A"))
        assertFalse("캐리지 리턴이 제거되어야 합니다", result.contains("\u000D"))
    }

    /**
     * 중첩된 HTML 태그를 제거한다
     */
    @Test
    fun testStripHtmlTagsWithNestedTags() {
        // Given
        val input = "<div><div><div>Nested</div></div></div>"

        // When
        val result = input.stripHtmlTags()

        // Then
        assertEquals("Nested", result)
    }

    /**
     * 자체 닫힘 태그를 제거한다
     */
    @Test
    fun testStripHtmlTagsWithSelfClosingTags() {
        // Given
        val input = "<img src='test.jpg'/><br/>Text<hr/>"

        // When
        val result = input.stripHtmlTags()

        // Then
        assertEquals("Text", result)
    }

    /**
     * 속성이 있는 HTML 태그를 제거한다
     */
    @Test
    fun testStripHtmlTagsWithAttributes() {
        // Given
        val input = "<div class='container' id='main'>Content</div>"

        // When
        val result = input.stripHtmlTags()

        // Then
        assertEquals("Content", result)
    }

    /**
     * HTML 엔티티는 그대로 남는다 (이 함수는 엔티티 변환을 하지 않음)
     */
    @Test
    fun testStripHtmlTagsDoesNotDecodeEntities() {
        // Given
        val input = "<p>&lt;Hello&gt; &amp; &quot;World&quot;</p>"

        // When
        val result = input.stripHtmlTags()

        // Then
        assertEquals("&lt;Hello&gt; &amp; &quot;World&quot;", result)
    }

    /**
     * 잘못된 형식의 HTML도 처리된다
     */
    @Test
    fun testStripHtmlTagsWithMalformedHtml() {
        // Given
        val malformedHtml = listOf(
            "<div>Unclosed",
            "No tags here",
            "<>Empty tag<>",
            "<<<Multiple brackets>>>"
        )

        // When & Then
        malformedHtml.forEach { html ->
            val result = html.stripHtmlTags()
            assertNotNull("잘못된 HTML도 예외 없이 처리되어야 합니다", result)
        }
    }
}
