package kr.open.library.simple_ui.extensions

import kr.open.library.simple_ui.extensions.trycatch.*
import kotlinx.coroutines.CancellationException
import org.junit.Test
import org.junit.Assert.*

/**
 * Try-Catch Extensions에 대한 단위 테스트
 *
 * 테스트 대상:
 * - safeCatch(block) - 예외를 잡아서 출력
 * - safeCatch(defaultValue, block) - 예외 발생 시 기본값 반환
 * - safeCatch(block, onCatch) - 예외 발생 시 커스텀 핸들러 실행
 */
class TryCatchExtensionsTest {

    // ========== 1. safeCatch(block) 테스트 ==========

    /**
     * 정상 실행되면 예외가 발생하지 않는다
     */
    @Test
    fun testSafeCatchExecutesNormallyWithoutException() {
        // Given
        var executed = false

        // When
        safeCatch {
            executed = true
        }

        // Then
        assertTrue("코드가 실행되어야 합니다", executed)
    }

    /**
     * 예외가 발생해도 프로그램이 멈추지 않는다
     */
    @Test
    fun testSafeCatchContinuesExecutionAfterException() {
        // Given
        var continueExecution = false

        // When
        safeCatch {
            throw RuntimeException("테스트 예외")
        }
        continueExecution = true

        // Then
        assertTrue("예외 후에도 코드가 계속 실행되어야 합니다", continueExecution)
    }

    /**
     * CancellationException은 다시 던져진다
     */
    @Test(expected = CancellationException::class)
    fun testSafeCatchRethrowsCancellationException() {
        // Given & When & Then
        // CancellationException은 코루틴 취소를 위해 반드시 전파되어야 함
        safeCatch {
            throw CancellationException("코루틴 취소")
        }
    }

    /**
     * Error는 다시 던져진다
     */
    @Test(expected = OutOfMemoryError::class)
    fun testSafeCatchRethrowsError() {
        // Given & When & Then
        // OOM 같은 심각한 에러는 반드시 전파되어야 함
        safeCatch {
            throw OutOfMemoryError("메모리 부족")
        }
    }

    // ========== 2. safeCatch(defaultValue, block) 테스트 ==========

    /**
     * 정상 실행되면 결과값을 반환한다
     */
    @Test
    fun testSafeCatchWithDefaultValueReturnsResultOnSuccess() {
        // Given
        val expectedValue = 42

        // When
        val result = safeCatch(defaultValue = 0) {
            expectedValue
        }

        // Then
        assertEquals("정상 실행 시 결과값을 반환해야 합니다", expectedValue, result)
    }

    /**
     * 예외가 발생하면 기본값을 반환한다
     */
    @Test
    fun testSafeCatchReturnsDefaultValueOnException() {
        // Given
        val defaultValue = "기본값"

        // When - 일부러 예외를 발생시킴
        val result = safeCatch(defaultValue) {
            throw RuntimeException("에러 발생!")
        }

        // Then - 에러가 나도 기본값을 받아야 함
        assertEquals("예외 발생 시 기본값을 반환해야 합니다", defaultValue, result)
    }

    /**
     * null 기본값도 정상 처리된다
     */
    @Test
    fun testSafeCatchHandlesNullDefaultValue() {
        // Given
        val defaultValue: String? = null

        // When
        val result = safeCatch(defaultValue) {
            throw RuntimeException("에러")
        }

        // Then
        assertNull("null 기본값이 반환되어야 합니다", result)
    }

    /**
     * 숫자 계산 예외 시 기본값을 반환한다
     */
    @Test
    fun testSafeCatchReturnsDefaultValueOnNumberFormatException() {
        // Given
        val defaultValue = -1

        // When
        val result = safeCatch(defaultValue) {
            "abc".toInt()  // NumberFormatException 발생
        }

        // Then
        assertEquals("NumberFormatException 발생 시 기본값 반환", defaultValue, result)
    }

    /**
     * 기본값이 있어도 CancellationException은 전파된다
     */
    @Test(expected = CancellationException::class)
    fun testSafeCatchWithDefaultValueRethrowsCancellationException() {
        // Given & When & Then
        safeCatch(defaultValue = "기본값") {
            throw CancellationException("코루틴 취소")
        }
    }

    // ========== 3. safeCatch(block, onCatch) 테스트 ==========

    /**
     * 정상 실행되면 결과값을 반환하고 onCatch는 실행안됨
     */
    @Test
    fun testSafeCatchWithOnCatchReturnsResultAndSkipsHandler() {
        // Given
        var catchCalled = false
        val expectedValue = "성공"

        // When
        val result = safeCatch(
            block = { expectedValue },
            onCatch = {
                catchCalled = true
                "실패"
            }
        )

        // Then
        assertEquals("정상 실행 시 결과값을 반환해야 합니다", expectedValue, result)
        assertFalse("onCatch가 호출되지 않아야 합니다", catchCalled)
    }

    /**
     * 예외가 발생하면 onCatch 핸들러가 실행된다
     */
    @Test
    fun testSafeCatchExecutesOnCatchHandlerOnException() {
        // Given
        var catchCalled = false
        val exceptionMessage = "테스트 에러"

        // When
        val result = safeCatch(
            block = {
                throw RuntimeException(exceptionMessage)
            },
            onCatch = { exception ->
                catchCalled = true
                "에러 처리됨: ${exception.message}"
            }
        )

        // Then
        assertTrue("onCatch가 호출되어야 합니다", catchCalled)
        assertEquals("onCatch 결과를 반환해야 합니다", "에러 처리됨: $exceptionMessage", result)
    }

    /**
     * onCatch에서 예외 정보를 받을 수 있다
     */
    @Test
    fun testOnCatchReceivesExceptionInformation() {
        // Given
        var caughtException: Exception? = null

        // When
        safeCatch(
            block = {
                throw IllegalArgumentException("잘못된 인자")
            },
            onCatch = { exception ->
                caughtException = exception
                "처리됨"
            }
        )

        // Then
        assertNotNull("예외가 전달되어야 합니다", caughtException)
        assertTrue("IllegalArgumentException 타입이어야 합니다",
            caughtException is IllegalArgumentException)
        assertEquals("예외 메시지가 일치해야 합니다",
            "잘못된 인자", caughtException?.message)
    }

    /**
     * onCatch에서 다른 타입을 반환할 수 있다
     */
    @Test
    fun testOnCatchCanReturnDifferentType() {
        // Given & When
        val result = safeCatch(
            block = {
                throw RuntimeException("에러")
            },
            onCatch = {
                999  // 예외 발생 시 숫자 반환
            }
        )

        // Then
        assertEquals("onCatch에서 반환한 값이 결과가 되어야 합니다", 999, result)
    }

    /**
     * onCatch가 있어도 CancellationException은 전파된다
     */
    @Test(expected = CancellationException::class)
    fun testSafeCatchWithOnCatchRethrowsCancellationException() {
        // Given & When & Then
        safeCatch(
            block = {
                throw CancellationException("코루틴 취소")
            },
            onCatch = { "처리 시도" }
        )
    }

    // ========== 4. 실제 사용 시나리오 테스트 ==========

    /**
     * 파일 읽기 실패 시 빈 문자열 반환
     */
    @Test
    fun testFileReadReturnsEmptyStringOnFailure() {
        // 실제 사용 예시: 파일 읽기 실패 시 빈 문자열 반환
        val fileContent = safeCatch(defaultValue = "") {
            // 실제로는 파일을 읽지만, 테스트에서는 예외 발생
            throw java.io.FileNotFoundException("파일 없음")
        }

        assertEquals("파일 읽기 실패 시 빈 문자열", "", fileContent)
    }

    /**
     * JSON 파싱 실패 시 로그 남기고 기본 객체 반환
     */
    @Test
    fun testJsonParsingReturnsDefaultObjectAndLogsError() {
        // 실제 사용 예시: JSON 파싱 실패 시 기본 객체 반환
        data class User(val name: String, val age: Int)

        val defaultUser = User("Unknown", 0)
        var errorLogged = false

        val user = safeCatch(
            block = {
                // JSON 파싱 시도
                throw Exception("JSON 파싱 실패")
            },
            onCatch = { exception ->
                errorLogged = true
                println("JSON 파싱 에러: ${exception.message}")
                defaultUser
            }
        )

        assertEquals("기본 사용자 객체가 반환되어야 합니다", defaultUser, user)
        assertTrue("에러가 로깅되어야 합니다", errorLogged)
    }

    /**
     * 네트워크 요청 실패 시 재시도 로직
     */
    @Test
    fun testNetworkRequestFailureWithRetryLogic() {
        // 실제 사용 예시: 네트워크 요청 실패 시 재시도
        var attemptCount = 0

        val result = safeCatch(
            block = {
                attemptCount++
                throw java.net.SocketTimeoutException("타임아웃")
            },
            onCatch = { exception ->
                "네트워크 에러: 재시도 필요 (시도 횟수: $attemptCount)"
            }
        )

        assertEquals("시도 횟수가 1이어야 합니다", 1, attemptCount)
        assertTrue("재시도 메시지를 포함해야 합니다", result.contains("재시도 필요"))
    }
}
