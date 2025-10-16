package kr.open.library.simple_ui.extensions

import kr.open.library.simple_ui.extensions.string.*
import org.junit.Test
import org.junit.Assert.*

/**
 * String Extensions에 대한 단위 테스트
 *
 * 테스트 대상:
 * - 이메일 검증 (isEmailValid)
 * - 전화번호 검증 (isPhoneNumberValid)
 * - URL 검증 (isUrlValid)
 * - 숫자 검증 (isNumeric)
 * - 영숫자 검증 (isAlphaNumeric)
 * - 공백 제거 (removeWhitespace)
 * - HTML 태그 제거 (stripHtmlTags)
 */
class StringExtensionsTest {

    // ========== 1. 이메일 검증 테스트 ==========

    @Test
    fun `이메일_형식이_올바르면_true를_반환한다`() {
        // Given (준비)
        val validEmails = listOf(
            "test@example.com",
            "user.name@example.com",
            "user+tag@example.co.kr",
            "test123@test-domain.com"
        )

        // When & Then (실행 & 확인)
        validEmails.forEach { email ->
            assertTrue("'$email'은 유효한 이메일이어야 합니다", email.isEmailValid())
        }
    }

    @Test
    fun `이메일_형식이_잘못되면_false를_반환한다`() {
        // Given
        val invalidEmails = listOf(
            "test@",
            "@example.com",
            "test",
            "test@",
            "test @example.com",  // 공백 포함
            ""
        )

        // When & Then
        invalidEmails.forEach { email ->
            assertFalse("'$email'은 잘못된 이메일이어야 합니다", email.isEmailValid())
        }
    }

    // ========== 2. 전화번호 검증 테스트 ==========

    @Test
    fun `전화번호_형식이_올바르면_true를_반환한다`() {
        // Given
        val validPhones = listOf(
            "010-1234-5678",
            "01012345678",
            "+82-10-1234-5678",
            "02-123-4567"
        )

        // When & Then
        validPhones.forEach { phone ->
            assertTrue("'$phone'은 유효한 전화번호여야 합니다", phone.isPhoneNumberValid())
        }
    }

    @Test
    fun `전화번호_형식이_잘못되면_false를_반환한다`() {
        // Given
        val invalidPhones = listOf(
            "abc",
            "123",
            ""
        )

        // When & Then
        invalidPhones.forEach { phone ->
            assertFalse("'$phone'은 잘못된 전화번호여야 합니다", phone.isPhoneNumberValid())
        }
    }

    // ========== 3. URL 검증 테스트 ==========

    @Test
    fun `URL_형식이_올바르면_true를_반환한다`() {
        // Given
        val validUrls = listOf(
            "https://www.example.com",
            "http://example.com",
            "https://example.com/path/to/page",
            "http://sub.example.com:8080/path"
        )

        // When & Then
        validUrls.forEach { url ->
            assertTrue("'$url'은 유효한 URL이어야 합니다", url.isUrlValid())
        }
    }

    @Test
    fun `URL_형식이_잘못되면_false를_반환한다`() {
        // Given
        val invalidUrls = listOf(
            "not-a-url",
            "htp://wrong",
            ""
        )

        // When & Then
        invalidUrls.forEach { url ->
            assertFalse("'$url'은 잘못된 URL이어야 합니다", url.isUrlValid())
        }
    }

    // ========== 4. 숫자 검증 테스트 ==========

    @Test
    fun `숫자로만_구성되면_true를_반환한다`() {
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

    @Test
    fun `숫자가_아닌_문자가_포함되면_false를_반환한다`() {
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

    // ========== 5. 영숫자 검증 테스트 ==========

    @Test
    fun `영숫자로만_구성되면_true를_반환한다`() {
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

    @Test
    fun `특수문자가_포함되면_false를_반환한다`() {
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

    // ========== 6. 공백 제거 테스트 ==========

    @Test
    fun `모든_공백을_제거한다`() {
        // Given
        val input = "Hello World\n\t"

        // When
        val result = input.removeWhitespace()

        // Then
        assertEquals("HelloWorld", result)
    }

    @Test
    fun `여러_종류의_공백을_모두_제거한다`() {
        // Given
        val input = "  a b c  "

        // When
        val result = input.removeWhitespace()

        // Then
        assertEquals("abc", result)
    }

    @Test
    fun `공백이_없으면_원본을_반환한다`() {
        // Given
        val input = "HelloWorld"

        // When
        val result = input.removeWhitespace()

        // Then
        assertEquals("HelloWorld", result)
    }

    // ========== 7. HTML 태그 제거 테스트 ==========

    @Test
    fun `HTML_태그를_제거하고_텍스트만_반환한다`() {
        // Given
        val input = "<p>Hello <b>World</b></p>"

        // When
        val result = input.stripHtmlTags()

        // Then
        assertEquals("Hello World", result)
    }

    @Test
    fun `div_태그를_제거한다`() {
        // Given
        val input = "<div>Text</div>"

        // When
        val result = input.stripHtmlTags()

        // Then
        assertEquals("Text", result)
    }

    @Test
    fun `HTML_태그가_없으면_원본을_반환한다`() {
        // Given
        val input = "Plain text"

        // When
        val result = input.stripHtmlTags()

        // Then
        assertEquals("Plain text", result)
    }

    @Test
    fun `여러_HTML_태그를_한번에_제거한다`() {
        // Given
        val input = "<div><p>Hello</p><span>World</span></div>"

        // When
        val result = input.stripHtmlTags()

        // Then
        assertEquals("HelloWorld", result)
    }
}
