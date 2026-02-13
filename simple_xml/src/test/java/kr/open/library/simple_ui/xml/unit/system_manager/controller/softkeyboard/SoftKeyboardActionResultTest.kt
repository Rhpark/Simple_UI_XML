package kr.open.library.simple_ui.xml.unit.system_manager.controller.softkeyboard

import kr.open.library.simple_ui.xml.system_manager.controller.softkeyboard.SoftKeyboardActionResult
import kr.open.library.simple_ui.xml.system_manager.controller.softkeyboard.SoftKeyboardFailureReason
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for SoftKeyboardActionResult and SoftKeyboardFailureReason.<br><br>
 * SoftKeyboardActionResult와 SoftKeyboardFailureReason에 대한 단위 테스트입니다.<br>
 */
class SoftKeyboardActionResultTest {
    // ==============================================
    // Success Tests
    // ==============================================

    @Test
    fun `Success is singleton data object`() {
        val a = SoftKeyboardActionResult.Success
        val b = SoftKeyboardActionResult.Success
        assertTrue(a === b)
    }

    @Test
    fun `Success equals itself`() {
        assertEquals(SoftKeyboardActionResult.Success, SoftKeyboardActionResult.Success)
    }

    @Test
    fun `Success implements SoftKeyboardActionResult`() {
        val result: SoftKeyboardActionResult = SoftKeyboardActionResult.Success
        assertTrue(result is SoftKeyboardActionResult.Success)
    }

    // ==============================================
    // Timeout Tests
    // ==============================================

    @Test
    fun `Timeout is singleton data object`() {
        val a = SoftKeyboardActionResult.Timeout
        val b = SoftKeyboardActionResult.Timeout
        assertTrue(a === b)
    }

    @Test
    fun `Timeout equals itself`() {
        assertEquals(SoftKeyboardActionResult.Timeout, SoftKeyboardActionResult.Timeout)
    }

    @Test
    fun `Timeout implements SoftKeyboardActionResult`() {
        val result: SoftKeyboardActionResult = SoftKeyboardActionResult.Timeout
        assertTrue(result is SoftKeyboardActionResult.Timeout)
    }

    // ==============================================
    // Failure Tests
    // ==============================================

    @Test
    fun `Failure stores reason and message`() {
        val failure = SoftKeyboardActionResult.Failure(
            reason = SoftKeyboardFailureReason.OFF_MAIN_THREAD,
            message = "test message",
        )
        assertEquals(SoftKeyboardFailureReason.OFF_MAIN_THREAD, failure.reason)
        assertEquals("test message", failure.message)
    }

    @Test
    fun `Failure message defaults to null`() {
        val failure = SoftKeyboardActionResult.Failure(
            reason = SoftKeyboardFailureReason.EXCEPTION_OCCURRED,
        )
        assertEquals(SoftKeyboardFailureReason.EXCEPTION_OCCURRED, failure.reason)
        assertNull(failure.message)
    }

    @Test
    fun `Failure with same reason and message are equal`() {
        val a = SoftKeyboardActionResult.Failure(
            reason = SoftKeyboardFailureReason.FOCUS_REQUEST_FAILED,
            message = "msg",
        )
        val b = SoftKeyboardActionResult.Failure(
            reason = SoftKeyboardFailureReason.FOCUS_REQUEST_FAILED,
            message = "msg",
        )
        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
    }

    @Test
    fun `Failure with different reason are not equal`() {
        val a = SoftKeyboardActionResult.Failure(
            reason = SoftKeyboardFailureReason.OFF_MAIN_THREAD,
            message = "msg",
        )
        val b = SoftKeyboardActionResult.Failure(
            reason = SoftKeyboardFailureReason.WINDOW_TOKEN_MISSING,
            message = "msg",
        )
        assertNotEquals(a, b)
    }

    @Test
    fun `Failure with different message are not equal`() {
        val a = SoftKeyboardActionResult.Failure(
            reason = SoftKeyboardFailureReason.EXCEPTION_OCCURRED,
            message = "msg1",
        )
        val b = SoftKeyboardActionResult.Failure(
            reason = SoftKeyboardFailureReason.EXCEPTION_OCCURRED,
            message = "msg2",
        )
        assertNotEquals(a, b)
    }

    @Test
    fun `Failure implements SoftKeyboardActionResult`() {
        val result: SoftKeyboardActionResult = SoftKeyboardActionResult.Failure(
            reason = SoftKeyboardFailureReason.INVALID_ARGUMENT,
        )
        assertTrue(result is SoftKeyboardActionResult.Failure)
    }

    // ==============================================
    // Mutual Exclusivity Tests
    // ==============================================

    @Test
    fun `Success is not Timeout`() {
        val result: SoftKeyboardActionResult = SoftKeyboardActionResult.Success
        assertTrue(result !is SoftKeyboardActionResult.Timeout)
    }

    @Test
    fun `Success is not Failure`() {
        val result: SoftKeyboardActionResult = SoftKeyboardActionResult.Success
        assertTrue(result !is SoftKeyboardActionResult.Failure)
    }

    @Test
    fun `Timeout is not Failure`() {
        val result: SoftKeyboardActionResult = SoftKeyboardActionResult.Timeout
        assertTrue(result !is SoftKeyboardActionResult.Failure)
    }

    @Test
    fun `when expression covers all subtypes`() {
        val results = listOf(
            SoftKeyboardActionResult.Success,
            SoftKeyboardActionResult.Timeout,
            SoftKeyboardActionResult.Failure(reason = SoftKeyboardFailureReason.EXCEPTION_OCCURRED),
        )

        results.forEach { result ->
            val label = when (result) {
                SoftKeyboardActionResult.Success -> "success"
                SoftKeyboardActionResult.Timeout -> "timeout"
                is SoftKeyboardActionResult.Failure -> "failure"
            }
            assertTrue(label.isNotEmpty())
        }
    }

    // ==============================================
    // SoftKeyboardFailureReason Tests
    // ==============================================

    @Test
    fun `all FailureReason values are accessible`() {
        val reasons = SoftKeyboardFailureReason.entries
        assertEquals(6, reasons.size)
    }

    @Test
    fun `all FailureReason values are distinct`() {
        val reasons = SoftKeyboardFailureReason.entries.toSet()
        assertEquals(6, reasons.size)
    }

    @Test
    fun `FailureReason contains expected values`() {
        val expectedNames = setOf(
            "OFF_MAIN_THREAD",
            "INVALID_ARGUMENT",
            "FOCUS_REQUEST_FAILED",
            "WINDOW_TOKEN_MISSING",
            "IME_REQUEST_REJECTED",
            "EXCEPTION_OCCURRED",
        )
        val actualNames = SoftKeyboardFailureReason.entries.map { it.name }.toSet()
        assertEquals(expectedNames, actualNames)
    }

    @Test
    fun `FailureReason valueOf works for all values`() {
        SoftKeyboardFailureReason.entries.forEach { reason ->
            assertEquals(reason, SoftKeyboardFailureReason.valueOf(reason.name))
        }
    }

    @Test
    fun `Failure toString contains reason and message`() {
        val failure = SoftKeyboardActionResult.Failure(
            reason = SoftKeyboardFailureReason.OFF_MAIN_THREAD,
            message = "detail",
        )
        val str = failure.toString()
        assertNotNull(str)
        assertTrue(str.contains("OFF_MAIN_THREAD"))
        assertTrue(str.contains("detail"))
    }

    @Test
    fun `Failure copy with different reason`() {
        val original = SoftKeyboardActionResult.Failure(
            reason = SoftKeyboardFailureReason.INVALID_ARGUMENT,
            message = "msg",
        )
        val copied = original.copy(reason = SoftKeyboardFailureReason.EXCEPTION_OCCURRED)
        assertEquals(SoftKeyboardFailureReason.EXCEPTION_OCCURRED, copied.reason)
        assertEquals("msg", copied.message)
    }

    @Test
    fun `Failure copy with different message`() {
        val original = SoftKeyboardActionResult.Failure(
            reason = SoftKeyboardFailureReason.INVALID_ARGUMENT,
            message = "msg",
        )
        val copied = original.copy(message = "new msg")
        assertEquals(SoftKeyboardFailureReason.INVALID_ARGUMENT, copied.reason)
        assertEquals("new msg", copied.message)
    }
}
