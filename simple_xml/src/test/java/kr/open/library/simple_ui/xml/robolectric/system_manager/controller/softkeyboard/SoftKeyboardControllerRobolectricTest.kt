package kr.open.library.simple_ui.xml.robolectric.system_manager.controller.softkeyboard

import android.app.Application
import android.os.Build
import android.view.View
import android.view.ViewTreeObserver
import android.view.Window
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.runCurrent
import kr.open.library.simple_ui.xml.system_manager.controller.softkeyboard.SoftKeyboardActionResult
import kr.open.library.simple_ui.xml.system_manager.controller.softkeyboard.SoftKeyboardFailureReason
import kr.open.library.simple_ui.xml.system_manager.controller.softkeyboard.SoftKeyboardController
import kr.open.library.simple_ui.xml.system_manager.controller.softkeyboard.SoftKeyboardResizePolicy
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Robolectric test for SoftKeyboardController.<br><br>
 * SoftKeyboardController의 Robolectric 테스트입니다.<br>
 *
 * Tests all keyboard control methods including show/hide, delay operations, and window configuration.<br><br>
 * 키보드 표시/숨김, 지연 작업, 윈도우 설정 등 모든 키보드 제어 메서드를 테스트합니다.<br>
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.TIRAMISU]) // API 33 for stylus handwriting tests
class SoftKeyboardControllerRobolectricTest {
    private lateinit var application: Application
    private lateinit var controller: SoftKeyboardController

    @Mock
    private lateinit var mockImm: InputMethodManager

    @Mock
    private lateinit var mockWindow: Window

    @Mock
    private lateinit var mockView: View

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        application = ApplicationProvider.getApplicationContext()

        // Inject mock InputMethodManager
        Shadows.shadowOf(application).setSystemService(
            android.content.Context.INPUT_METHOD_SERVICE,
            mockImm,
        )

        controller = SoftKeyboardController(application)
    }

    // ========================================
    // 1. Basic Functionality Tests
    // ========================================

    @Test
    fun `controller is created successfully`() {
        assertNotNull(controller)
    }

    // ========================================
    // 2. Window Configuration Tests
    // ========================================

    @Test
    fun `setAdjustPan sets window soft input mode to SOFT_INPUT_ADJUST_PAN`() {
        val result = controller.setAdjustPan(mockWindow)

        assertTrue(result)
        verify(mockWindow).setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
    }

    @Test
    fun `setSoftInputMode sets custom soft input mode`() {
        val customMode =
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE or
                WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE

        val result = controller.setSoftInputMode(mockWindow, customMode)

        assertTrue(result)
        verify(mockWindow).setSoftInputMode(customMode)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.R]) // API 30 (Android 11+)
    fun `setAdjustResize keeps current window policy on API 30+`() {
        val result = controller.setAdjustResize(mockWindow)

        assertTrue(result)
        verify(mockWindow, never()).setSoftInputMode(anyInt())
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q]) // API 29 (Android 10)
    fun `setAdjustResize uses legacy setSoftInputMode on API below 30`() {
        val result = controller.setAdjustResize(mockWindow)

        assertTrue(result)
        verify(mockWindow).setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.R])
    fun `configureImeResize with LEGACY_ADJUST_RESIZE calls setSoftInputMode on API 30+`() {
        val result = controller.configureImeResize(mockWindow, SoftKeyboardResizePolicy.LEGACY_ADJUST_RESIZE)

        assertTrue(result)
        verify(mockWindow).setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.R])
    fun `configureImeResize with FORCE_DECOR_FITS_TRUE returns true when decorView exists`() {
        val mockDecorView = mock(View::class.java)
        `when`(mockWindow.decorView).thenReturn(mockDecorView)

        val result = controller.configureImeResize(mockWindow, SoftKeyboardResizePolicy.FORCE_DECOR_FITS_TRUE)

        assertTrue(result)
    }

    // ========================================
    // 3. Keyboard Show Tests
    // ========================================

    @Test
    fun `show returns true when view requests focus successfully`() {
        `when`(mockView.requestFocus()).thenReturn(true)
        `when`(mockImm.showSoftInput(mockView, InputMethodManager.SHOW_IMPLICIT)).thenReturn(true)

        val result = controller.show(mockView)

        assertTrue(result)
        verify(mockView).requestFocus()
        verify(mockImm).showSoftInput(mockView, InputMethodManager.SHOW_IMPLICIT)
    }

    @Test
    fun `show returns false when view fails to request focus`() {
        `when`(mockView.requestFocus()).thenReturn(false)

        val result = controller.show(mockView)

        assertFalse(result)
        verify(mockView).requestFocus()
        // Don't verify imm.showSoftInput as it's not called when requestFocus fails
    }

    @Test
    fun `show uses custom flag when provided`() {
        val customFlag = InputMethodManager.SHOW_FORCED
        `when`(mockView.requestFocus()).thenReturn(true)
        `when`(mockImm.showSoftInput(mockView, customFlag)).thenReturn(true)

        val result = controller.show(mockView, customFlag)

        assertTrue(result)
        verify(mockImm).showSoftInput(mockView, customFlag)
    }

    @Test
    fun `scheduleShow posts delayed action`() {
        `when`(mockView.postDelayed(any(Runnable::class.java), anyLong())).thenReturn(true)

        val result = controller.showDelay(mockView, 100L)

        assertTrue(result)
        verify(mockView).postDelayed(any(Runnable::class.java), eq(100L))
    }

    @Test
    fun `showDelay returns false when delay is negative`() {
        val result = controller.showDelay(mockView, -1L)
        assertFalse(result)
        verify(mockView, never()).postDelayed(any(Runnable::class.java), anyLong())
    }

    // ========================================
    // 4. Keyboard Hide Tests
    // ========================================

    @Test
    fun `hide returns true when windowToken is available`() {
        val mockWindowToken = mock(android.os.IBinder::class.java)
        `when`(mockView.windowToken).thenReturn(mockWindowToken)
        `when`(mockImm.hideSoftInputFromWindow(mockWindowToken, 0)).thenReturn(true)

        val result = controller.hide(mockView)

        assertTrue(result)
        verify(mockImm).hideSoftInputFromWindow(mockWindowToken, 0)
    }

    @Test
    fun `hide returns true when windowToken is null but applicationWindowToken is available`() {
        val mockApplicationWindowToken = mock(android.os.IBinder::class.java)
        `when`(mockView.windowToken).thenReturn(null)
        `when`(mockView.applicationWindowToken).thenReturn(mockApplicationWindowToken)
        `when`(mockImm.hideSoftInputFromWindow(mockApplicationWindowToken, 0)).thenReturn(true)

        val result = controller.hide(mockView)

        assertTrue(result)
        verify(mockImm).hideSoftInputFromWindow(mockApplicationWindowToken, 0)
    }

    @Test
    fun `hide returns false when both windowToken and applicationWindowToken are null`() {
        `when`(mockView.windowToken).thenReturn(null)
        `when`(mockView.applicationWindowToken).thenReturn(null)

        val result = controller.hide(mockView)

        assertFalse(result)
        verify(mockImm, never()).hideSoftInputFromWindow(any(), anyInt())
    }

    @Test
    fun `hide uses custom flag when provided`() {
        val customFlag = InputMethodManager.HIDE_IMPLICIT_ONLY
        val mockWindowToken = mock(android.os.IBinder::class.java)
        `when`(mockView.windowToken).thenReturn(mockWindowToken)
        `when`(mockImm.hideSoftInputFromWindow(mockWindowToken, customFlag)).thenReturn(true)

        val result = controller.hide(mockView, customFlag)

        assertTrue(result)
        verify(mockImm).hideSoftInputFromWindow(mockWindowToken, customFlag)
    }

    @Test
    fun `scheduleHide posts delayed action`() {
        `when`(mockView.postDelayed(any(Runnable::class.java), anyLong())).thenReturn(true)

        val result = controller.hideDelay(mockView, 100L)

        assertTrue(result)
        verify(mockView).postDelayed(any(Runnable::class.java), eq(100L))
    }

    @Test
    fun `hideDelay returns false when delay is negative`() {
        val result = controller.hideDelay(mockView, -1L)
        assertFalse(result)
        verify(mockView, never()).postDelayed(any(Runnable::class.java), anyLong())
    }

    // ========================================
    // 5. Stylus Handwriting Tests (API 33+)
    // ========================================

    @Test
    @Config(sdk = [Build.VERSION_CODES.TIRAMISU]) // API 33
    fun `startStylusHandwriting returns true when view requests focus successfully`() {
        `when`(mockView.requestFocus()).thenReturn(true)

        val result = controller.startStylusHandwriting(mockView)

        assertTrue(result)
        verify(mockView).requestFocus()
        verify(mockImm).startStylusHandwriting(mockView)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.TIRAMISU])
    fun `startStylusHandwriting returns false when view fails to request focus`() {
        `when`(mockView.requestFocus()).thenReturn(false)

        val result = controller.startStylusHandwriting(mockView)

        assertFalse(result)
        verify(mockView).requestFocus()
        verify(mockImm, never()).startStylusHandwriting(mockView)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.TIRAMISU])
    fun `startStylusHandwriting with delay posts delayed action`() {
        `when`(mockView.postDelayed(any(Runnable::class.java), anyLong())).thenReturn(true)

        val result = controller.startStylusHandwriting(mockView, 100L)

        assertTrue(result)
        verify(mockView).postDelayed(any(Runnable::class.java), eq(100L))
    }

    // ========================================
    // 6. Error Handling Tests
    // ========================================

    @Test
    fun `setAdjustPan returns false when exception occurs`() {
        val mockWindowWithException = mock(Window::class.java)
        `when`(mockWindowWithException.setSoftInputMode(anyInt())).thenThrow(RuntimeException("Test exception"))

        val result = controller.setAdjustPan(mockWindowWithException)

        assertFalse(result)
    }

    @Test
    fun `setSoftInputMode returns false when exception occurs`() {
        val mockWindowWithException = mock(Window::class.java)
        `when`(mockWindowWithException.setSoftInputMode(anyInt())).thenThrow(RuntimeException("Test exception"))

        val result = controller.setSoftInputMode(
            mockWindowWithException,
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN,
        )

        assertFalse(result)
    }

    @Test
    fun `show returns false when exception occurs during showSoftInput`() {
        `when`(mockView.requestFocus()).thenReturn(true)
        `when`(mockImm.showSoftInput(mockView, InputMethodManager.SHOW_IMPLICIT))
            .thenThrow(RuntimeException("Test exception"))

        val result = controller.show(mockView)

        assertFalse(result)
    }

    @Test
    fun `hide returns false when exception occurs during hideSoftInputFromWindow`() {
        val mockWindowToken = mock(android.os.IBinder::class.java)
        `when`(mockView.windowToken).thenReturn(mockWindowToken)
        `when`(mockImm.hideSoftInputFromWindow(mockWindowToken, 0))
            .thenThrow(RuntimeException("Test exception"))

        val result = controller.hide(mockView)

        assertFalse(result)
    }

    // ========================================
    // 7. New API Contract Tests
    // ========================================

    @Test
    fun `show returns false when called off main thread`() {
        val latch = CountDownLatch(1)
        val result = AtomicBoolean(true)

        Thread {
            result.set(controller.show(mockView))
            latch.countDown()
        }.start()

        assertTrue(latch.await(2, TimeUnit.SECONDS))
        assertFalse(result.get())
    }

    @Test
    fun `showAwait returns INVALID_ARGUMENT when delay is negative`() =
        runTest {
            val result = controller.showAwait(mockView, delayMillis = -1L)
            assertTrue(result is SoftKeyboardActionResult.Failure)
            assertEquals(
                SoftKeyboardFailureReason.INVALID_ARGUMENT,
                (result as SoftKeyboardActionResult.Failure).reason,
            )
        }

    @Test
    fun `hideAwait returns INVALID_ARGUMENT when timeout is zero`() =
        runTest {
            val result = controller.hideAwait(mockView, timeoutMillis = 0L)
            assertTrue(result is SoftKeyboardActionResult.Failure)
            assertEquals(
                SoftKeyboardFailureReason.INVALID_ARGUMENT,
                (result as SoftKeyboardActionResult.Failure).reason,
            )
        }

    @Test
    fun `hideAwait returns IME_REQUEST_REJECTED when hide request is rejected`() =
        runTest {
            val mockWindowToken = mock(android.os.IBinder::class.java)
            `when`(mockView.windowToken).thenReturn(mockWindowToken)
            `when`(mockImm.hideSoftInputFromWindow(mockWindowToken, 0)).thenReturn(false)

            val result = controller.hideAwait(mockView, timeoutMillis = 100L)
            assertTrue(result is SoftKeyboardActionResult.Failure)
            assertEquals(
                SoftKeyboardFailureReason.IME_REQUEST_REJECTED,
                (result as SoftKeyboardActionResult.Failure).reason,
            )
        }

    @Test
    fun `showAwaitAsync returns Failure result when focus cannot be obtained`() =
        runTest {
            `when`(mockView.requestFocus()).thenReturn(false)
            val deferred = controller.showAwaitAsync(v = mockView, coroutineScope = this)
            val result = deferred.await()
            assertTrue(result is SoftKeyboardActionResult.Failure)
            assertEquals(
                SoftKeyboardFailureReason.FOCUS_REQUEST_FAILED,
                (result as SoftKeyboardActionResult.Failure).reason,
            )
        }

    @Test
    fun `showAwaitAsync returns OFF_MAIN_THREAD failure when called off main thread`() =
        runTest {
            val latch = CountDownLatch(1)
            val resultRef = java.util.concurrent.atomic.AtomicReference<SoftKeyboardActionResult?>(null)

            Thread {
                val deferred = controller.showAwaitAsync(v = mockView, coroutineScope = this)
                resultRef.set(kotlinx.coroutines.runBlocking { deferred.await() })
                latch.countDown()
            }.start()

            assertTrue(latch.await(2, TimeUnit.SECONDS))
            val result = resultRef.get()
            assertTrue(result is SoftKeyboardActionResult.Failure)
            assertEquals(
                SoftKeyboardFailureReason.OFF_MAIN_THREAD,
                (result as SoftKeyboardActionResult.Failure).reason,
            )
        }

    @Test
    fun `hideAwaitAsync returns Failure result when no window token is available`() =
        runTest {
            `when`(mockView.windowToken).thenReturn(null)
            `when`(mockView.applicationWindowToken).thenReturn(null)
            val deferred = controller.hideAwaitAsync(v = mockView, coroutineScope = this)
            assertNotNull(deferred)
            val result = deferred?.await()
            assertTrue(result is SoftKeyboardActionResult.Failure)
            assertEquals(
                SoftKeyboardFailureReason.WINDOW_TOKEN_MISSING,
                (result as SoftKeyboardActionResult.Failure).reason,
            )
        }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun `showAwait cancellation is propagated as cancellation`() =
        runTest {
            `when`(mockView.requestFocus()).thenReturn(true)
            `when`(mockImm.showSoftInput(mockView, InputMethodManager.SHOW_IMPLICIT)).thenReturn(true)

            val deferred = async { controller.showAwait(mockView, delayMillis = 5_000L, timeoutMillis = 6_000L) }
            runCurrent()
            deferred.cancel()

            assertTrue(deferred.isCancelled)
        }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun `hideAwait cancellation is propagated as cancellation`() =
        runTest {
            val deferred = async { controller.hideAwait(mockView, delayMillis = 5_000L, timeoutMillis = 6_000L) }
            runCurrent()
            deferred.cancel()

            assertTrue(deferred.isCancelled)
        }

    // ========================================
    // 8. showAwait / hideAwait Timeout Tests
    // ========================================

    @Test
    fun `showAwait reaches awaitImeVisibility when request succeeds and returns non-Success`() =
        runTest {
            `when`(mockView.requestFocus()).thenReturn(true)
            `when`(mockImm.showSoftInput(mockView, InputMethodManager.SHOW_IMPLICIT)).thenReturn(true)

            val result = controller.showAwait(mockView, timeoutMillis = 50L)

            // awaitImeVisibility가 viewTreeObserver null로 EXCEPTION_OCCURRED 반환
            // 또는 Timeout 반환 — 중요한 건 Success가 아님 (IME 미가시)
            assertTrue(
                "showAwait should not return Success without real IME, got: $result",
                result !is SoftKeyboardActionResult.Success,
            )
        }

    @Test
    fun `hideAwait returns Success when IME already hidden`() =
        runTest {
            // Robolectric에서 WindowInsets가 null → isImeVisible = false
            // expectedVisible = false와 일치하므로 즉시 Success 반환
            val mockWindowToken = mock(android.os.IBinder::class.java)
            `when`(mockView.windowToken).thenReturn(mockWindowToken)
            `when`(mockImm.hideSoftInputFromWindow(mockWindowToken, 0)).thenReturn(true)

            val result = controller.hideAwait(mockView, timeoutMillis = 100L)

            assertTrue(
                "hideAwait should return Success when IME is already hidden, got: $result",
                result is SoftKeyboardActionResult.Success,
            )
        }

    // ========================================
    // 9. showAwait / hideAwait EXCEPTION_OCCURRED Tests
    // ========================================

    @Test
    fun `showAwait returns EXCEPTION_OCCURRED when requestShowInternal throws`() =
        runTest {
            `when`(mockView.requestFocus()).thenReturn(true)
            `when`(mockImm.showSoftInput(mockView, InputMethodManager.SHOW_IMPLICIT))
                .thenThrow(RuntimeException("IMM failure"))

            val result = controller.showAwait(mockView, timeoutMillis = 100L)

            assertTrue(result is SoftKeyboardActionResult.Failure)
            assertEquals(
                SoftKeyboardFailureReason.EXCEPTION_OCCURRED,
                (result as SoftKeyboardActionResult.Failure).reason,
            )
        }

    // ========================================
    // 10. Delay Boundary Tests
    // ========================================

    @Test
    fun `showDelay with zero delay posts immediately`() {
        `when`(mockView.postDelayed(any(Runnable::class.java), anyLong())).thenReturn(true)

        val result = controller.showDelay(mockView, 0L)

        assertTrue(result)
        verify(mockView).postDelayed(any(Runnable::class.java), eq(0L))
    }

    @Test
    fun `hideDelay with zero delay posts immediately`() {
        `when`(mockView.postDelayed(any(Runnable::class.java), anyLong())).thenReturn(true)

        val result = controller.hideDelay(mockView, 0L)

        assertTrue(result)
        verify(mockView).postDelayed(any(Runnable::class.java), eq(0L))
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.TIRAMISU])
    fun `startStylusHandwriting with negative delay returns false`() {
        val result = controller.startStylusHandwriting(mockView, -1L)

        assertFalse(result)
        verify(mockView, never()).postDelayed(any(Runnable::class.java), anyLong())
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.TIRAMISU])
    fun `startStylusHandwriting with zero delay posts immediately`() {
        `when`(mockView.postDelayed(any(Runnable::class.java), anyLong())).thenReturn(true)

        val result = controller.startStylusHandwriting(mockView, 0L)

        assertTrue(result)
        verify(mockView).postDelayed(any(Runnable::class.java), eq(0L))
    }

    // ========================================
    // 11. showAwait / hideAwait Parameter Boundary Tests
    // ========================================

    @Test
    fun `showAwait returns INVALID_ARGUMENT when timeout is zero`() =
        runTest {
            val result = controller.showAwait(mockView, timeoutMillis = 0L)
            assertTrue(result is SoftKeyboardActionResult.Failure)
            assertEquals(
                SoftKeyboardFailureReason.INVALID_ARGUMENT,
                (result as SoftKeyboardActionResult.Failure).reason,
            )
        }

    @Test
    fun `showAwait returns INVALID_ARGUMENT when timeout is negative`() =
        runTest {
            val result = controller.showAwait(mockView, timeoutMillis = -1L)
            assertTrue(result is SoftKeyboardActionResult.Failure)
            assertEquals(
                SoftKeyboardFailureReason.INVALID_ARGUMENT,
                (result as SoftKeyboardActionResult.Failure).reason,
            )
        }

    @Test
    fun `hideAwait returns INVALID_ARGUMENT when delay is negative`() =
        runTest {
            val result = controller.hideAwait(mockView, delayMillis = -1L)
            assertTrue(result is SoftKeyboardActionResult.Failure)
            assertEquals(
                SoftKeyboardFailureReason.INVALID_ARGUMENT,
                (result as SoftKeyboardActionResult.Failure).reason,
            )
        }

    @Test
    fun `showAwait Failure message describes invalid argument`() =
        runTest {
            val result = controller.showAwait(mockView, delayMillis = -1L)
            assertTrue(result is SoftKeyboardActionResult.Failure)
            val failure = result as SoftKeyboardActionResult.Failure
            assertNotNull(failure.message)
            assertTrue(failure.message!!.isNotEmpty())
        }

    // ========================================
    // 12. Off-Main Thread Tests for Await APIs
    // ========================================

    @Test
    fun `hide returns false when called off main thread`() {
        val latch = CountDownLatch(1)
        val result = AtomicBoolean(true)

        Thread {
            result.set(controller.hide(mockView))
            latch.countDown()
        }.start()

        assertTrue(latch.await(2, TimeUnit.SECONDS))
        assertFalse(result.get())
    }

    @Test
    fun `setAdjustPan returns false when called off main thread`() {
        val latch = CountDownLatch(1)
        val result = AtomicBoolean(true)

        Thread {
            result.set(controller.setAdjustPan(mockWindow))
            latch.countDown()
        }.start()

        assertTrue(latch.await(2, TimeUnit.SECONDS))
        assertFalse(result.get())
    }

    // ========================================
    // 13. configureImeResize API Level Branch Tests
    // ========================================

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q]) // API 29
    fun `configureImeResize with KEEP_CURRENT_WINDOW uses legacy on API 29`() {
        val result = controller.configureImeResize(mockWindow, SoftKeyboardResizePolicy.KEEP_CURRENT_WINDOW)

        assertTrue(result)
        verify(mockWindow).setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q]) // API 29
    fun `configureImeResize with FORCE_DECOR_FITS_TRUE falls back to legacy on API 29`() {
        val result = controller.configureImeResize(mockWindow, SoftKeyboardResizePolicy.FORCE_DECOR_FITS_TRUE)

        assertTrue(result)
        verify(mockWindow).setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q]) // API 29
    fun `configureImeResize with LEGACY_ADJUST_RESIZE works on API 29`() {
        val result = controller.configureImeResize(mockWindow, SoftKeyboardResizePolicy.LEGACY_ADJUST_RESIZE)

        assertTrue(result)
        verify(mockWindow).setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
    }

    @Test
    fun `configureImeResize default policy is KEEP_CURRENT_WINDOW`() {
        val result = controller.configureImeResize(mockWindow)

        assertTrue(result)
    }

    // ========================================
    // 14. hideAwait WINDOW_TOKEN_MISSING Tests
    // ========================================

    @Test
    fun `hideAwait returns WINDOW_TOKEN_MISSING when both tokens null`() =
        runTest {
            `when`(mockView.windowToken).thenReturn(null)
            `when`(mockView.applicationWindowToken).thenReturn(null)

            val result = controller.hideAwait(mockView, timeoutMillis = 100L)

            assertTrue(result is SoftKeyboardActionResult.Failure)
            assertEquals(
                SoftKeyboardFailureReason.WINDOW_TOKEN_MISSING,
                (result as SoftKeyboardActionResult.Failure).reason,
            )
            assertNotNull(result.message)
        }

    // ========================================
    // 15. showAwait FOCUS_REQUEST_FAILED Tests
    // ========================================

    @Test
    fun `showAwait returns FOCUS_REQUEST_FAILED when view cannot get focus`() =
        runTest {
            `when`(mockView.requestFocus()).thenReturn(false)

            val result = controller.showAwait(mockView, timeoutMillis = 100L)

            assertTrue(result is SoftKeyboardActionResult.Failure)
            assertEquals(
                SoftKeyboardFailureReason.FOCUS_REQUEST_FAILED,
                (result as SoftKeyboardActionResult.Failure).reason,
            )
        }

    @Test
    fun `showAwait returns IME_REQUEST_REJECTED when show request fails`() =
        runTest {
            `when`(mockView.requestFocus()).thenReturn(true)
            `when`(mockImm.showSoftInput(mockView, InputMethodManager.SHOW_IMPLICIT)).thenReturn(false)

            val result = controller.showAwait(mockView, timeoutMillis = 100L)

            assertTrue(result is SoftKeyboardActionResult.Failure)
            assertEquals(
                SoftKeyboardFailureReason.IME_REQUEST_REJECTED,
                (result as SoftKeyboardActionResult.Failure).reason,
            )
        }
}
