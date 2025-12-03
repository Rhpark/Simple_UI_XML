package kr.open.library.simple_ui.xml.robolectric.system_manager.controller.softkeyboard

import android.app.Application
import android.os.Build
import android.view.View
import android.view.Window
import android.view.WindowInsetsController
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kr.open.library.simple_ui.xml.system_manager.controller.softkeyboard.SoftKeyboardController
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
        assertNotNull(controller.imm)
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
    fun `setAdjustResize uses WindowInsetsController on API 30+`() {
        val mockInsetsController = mock(WindowInsetsController::class.java)
        `when`(mockWindow.insetsController).thenReturn(mockInsetsController)

        controller.setAdjustResize(mockWindow)

        verify(mockInsetsController).systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q]) // API 29 (Android 10)
    fun `setAdjustResize uses legacy setSoftInputMode on API below 30`() {
        controller.setAdjustResize(mockWindow)

        verify(mockWindow).setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
    }

    // Note: WindowCompat.setDecorFitsSystemWindows requires a real Window with decorView
    // This test is skipped as it's difficult to mock properly in unit tests

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
    fun `showDelay with Runnable posts delayed action`() {
        `when`(mockView.postDelayed(any(Runnable::class.java), anyLong())).thenReturn(true)

        val result = controller.showDelay(mockView, 100L)

        assertTrue(result)
        verify(mockView).postDelayed(any(Runnable::class.java), eq(100L))
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `showDelay with coroutine launches delayed task`() =
        runTest {
            val testDispatcher = StandardTestDispatcher(testScheduler)
            val testScope = TestScope(testDispatcher)

            `when`(mockView.requestFocus()).thenReturn(true)
            `when`(mockImm.showSoftInput(mockView, InputMethodManager.SHOW_IMPLICIT)).thenReturn(true)

            val result = controller.showDelay(mockView, 100L, InputMethodManager.SHOW_IMPLICIT, testScope)

            assertTrue(result)

            // Advance time to trigger the delayed coroutine
            testScope.testScheduler.advanceTimeBy(100L)
            testScope.testScheduler.runCurrent()

            verify(mockView).requestFocus()
            verify(mockImm).showSoftInput(mockView, InputMethodManager.SHOW_IMPLICIT)
        }

    // ========================================
    // 4. Keyboard Hide Tests
    // ========================================

    @Test
    fun `hide returns true when view requests focus successfully`() {
        val mockWindowToken = mock(android.os.IBinder::class.java)
        `when`(mockView.requestFocus()).thenReturn(true)
        `when`(mockView.windowToken).thenReturn(mockWindowToken)
        `when`(mockImm.hideSoftInputFromWindow(mockWindowToken, 0)).thenReturn(true)

        val result = controller.hide(mockView)

        assertTrue(result)
        verify(mockView).requestFocus()
        verify(mockImm).hideSoftInputFromWindow(mockWindowToken, 0)
    }

    @Test
    fun `hide returns false when view fails to request focus`() {
        `when`(mockView.requestFocus()).thenReturn(false)

        val result = controller.hide(mockView)

        assertFalse(result)
        verify(mockView).requestFocus()
        verify(mockImm, never()).hideSoftInputFromWindow(any(), anyInt())
    }

    @Test
    fun `hide uses custom flag when provided`() {
        val customFlag = InputMethodManager.HIDE_IMPLICIT_ONLY
        val mockWindowToken = mock(android.os.IBinder::class.java)
        `when`(mockView.requestFocus()).thenReturn(true)
        `when`(mockView.windowToken).thenReturn(mockWindowToken)
        `when`(mockImm.hideSoftInputFromWindow(mockWindowToken, customFlag)).thenReturn(true)

        val result = controller.hide(mockView, customFlag)

        assertTrue(result)
        verify(mockImm).hideSoftInputFromWindow(mockWindowToken, customFlag)
    }

    @Test
    fun `hideDelay with Runnable posts delayed action`() {
        `when`(mockView.postDelayed(any(Runnable::class.java), anyLong())).thenReturn(true)

        val result = controller.hideDelay(mockView, 100L)

        assertTrue(result)
        verify(mockView).postDelayed(any(Runnable::class.java), eq(100L))
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `hideDelay with coroutine launches delayed task`() =
        runTest {
            val testDispatcher = StandardTestDispatcher(testScheduler)
            val testScope = TestScope(testDispatcher)

            val mockWindowToken = mock(android.os.IBinder::class.java)
            `when`(mockView.requestFocus()).thenReturn(true)
            `when`(mockView.windowToken).thenReturn(mockWindowToken)
            `when`(mockImm.hideSoftInputFromWindow(mockWindowToken, 0)).thenReturn(true)

            val result = controller.hideDelay(mockView, 100L, 0, testScope)

            assertTrue(result)

            // Advance time to trigger the delayed coroutine
            testScope.testScheduler.advanceTimeBy(100L)
            testScope.testScheduler.runCurrent()

            verify(mockView).requestFocus()
            verify(mockImm).hideSoftInputFromWindow(mockWindowToken, 0)
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

        val result = controller.setSoftInputMode(mockWindowWithException, WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)

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
        `when`(mockView.requestFocus()).thenReturn(true)
        `when`(mockView.windowToken).thenReturn(mockWindowToken)
        `when`(mockImm.hideSoftInputFromWindow(mockWindowToken, 0))
            .thenThrow(RuntimeException("Test exception"))

        val result = controller.hide(mockView)

        assertFalse(result)
    }
}
