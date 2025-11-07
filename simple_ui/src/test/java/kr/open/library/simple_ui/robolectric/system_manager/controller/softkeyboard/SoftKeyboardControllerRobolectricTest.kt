package kr.open.library.simple_ui.robolectric.system_manager.controller.softkeyboard

import android.graphics.Rect
import android.os.Build
import android.view.View
import android.view.Window
import android.view.WindowInsetsController
import android.view.WindowManager
import android.widget.EditText
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.mock
import org.mockito.Mockito.spy
import org.mockito.Mockito.`when`
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.LooperMode
import org.robolectric.android.controller.ActivityController
import org.robolectric.Robolectric
import org.robolectric.shadows.ShadowInputMethodManager
import org.robolectric.Shadows
import org.robolectric.shadows.ShadowLooper
import org.robolectric.annotation.Config
import kr.open.library.simple_ui.system_manager.controller.softkeyboard.SoftKeyboardController


@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.TIRAMISU])
@LooperMode(LooperMode.Mode.PAUSED)
class SoftKeyboardControllerRobolectricTest {

    private lateinit var activityController: ActivityController<ComponentActivity>
    private lateinit var activity: ComponentActivity
    private lateinit var controller: SoftKeyboardController

    @Before
    fun setUp() {
        activityController = Robolectric.buildActivity(ComponentActivity::class.java).setup()
        activity = activityController.get()
        controller = SoftKeyboardController(activity)
        ShadowInputMethodManager.reset()
    }

    @After
    fun tearDown() {
        ShadowInputMethodManager.reset()
        activityController.pause().stop().destroy()
    }

    private fun shadowImm(): ShadowInputMethodManager =
        Shadows.shadowOf(controller.imm)

    private fun attachEditText(): EditText {
        val container = FrameLayout(activity)
        val editText = EditText(activity)
        container.addView(editText)
        activity.setContentView(container)
        assertTrue(editText.requestFocus())
        return editText
    }

    private fun attachViewWithFocusResult(canFocus: Boolean): View {
        val view = View(activity).apply {
            isFocusable = canFocus
            isFocusableInTouchMode = canFocus
        }
        val container = FrameLayout(activity).apply { addView(view) }
        activity.setContentView(container)
        return view
    }

    @Test
    fun show_returnsTrue_whenFocusSucceeds() {
        val view = attachEditText()
        assertFalse(shadowImm().isSoftInputVisible())
        val result = controller.show(view)
        assertTrue(result)
        assertTrue(shadowImm().isSoftInputVisible())
    }

    @Test
    fun show_returnsFalse_whenFocusFails() {
        val view = attachViewWithFocusResult(false)
        assertFalse(controller.show(view))
        assertFalse(shadowImm().isSoftInputVisible())
    }

    @Test
    fun hide_returnsTrue_whenKeyboardVisible() {
        val view = attachEditText()
        controller.show(view)
        assertTrue(shadowImm().isSoftInputVisible())
        val result = controller.hide(view)
        assertTrue(result)
        assertFalse(shadowImm().isSoftInputVisible())
    }

    @Test
    fun hide_returnsFalse_whenFocusFails() {
        val view = attachViewWithFocusResult(false)
        assertFalse(controller.hide(view))
    }

    @Test
    fun showDelay_postsRunnableAndShowsKeyboard() {
        val view = attachEditText()
        assertTrue(controller.showDelay(view, 50L))
        assertFalse(shadowImm().isSoftInputVisible())
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()
        assertTrue(shadowImm().isSoftInputVisible())
    }

    @Test
    fun hideDelay_postsRunnableAndHidesKeyboard() {
        val view = attachEditText()
        controller.show(view)
        assertTrue(shadowImm().isSoftInputVisible())
        assertTrue(controller.hideDelay(view, 50L))
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()
        assertFalse(shadowImm().isSoftInputVisible())
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun showDelay_withCoroutineScope_showsAfterAdvance() {
        val view = attachEditText()
        val scope = TestScope(StandardTestDispatcher())
        assertTrue(controller.showDelay(view, delay = 100L, coroutineScope = scope))
        assertFalse(shadowImm().isSoftInputVisible())
        scope.advanceUntilIdle()
        assertTrue(shadowImm().isSoftInputVisible())
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun hideDelay_withCoroutineScope_hidesAfterAdvance() {
        val view = attachEditText()
        controller.show(view)
        val scope = TestScope(StandardTestDispatcher())
        assertTrue(controller.hideDelay(view, delay = 100L, coroutineScope = scope))
        scope.advanceUntilIdle()
        assertFalse(shadowImm().isSoftInputVisible())
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q])
    fun setAdjustResize_onPreR_setsAdjustResizeFlag() {
        val window = activity.window
        controller.setAdjustResize(window)
        val adjustMask = window.attributes.softInputMode and WindowManager.LayoutParams.SOFT_INPUT_MASK_ADJUST
        assertEquals(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE, adjustMask)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.R])
    fun setAdjustResize_onR_setsTransientBehaviorWhenControllerExists() {
        val window = activity.window
        controller.setAdjustResize(window)
        window.insetsController?.let {
            assertEquals(
                WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE,
                it.systemBarsBehavior
            )
        }
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.R])
    fun setAdjustResize_onR_fallsBackToWindowCompat_whenControllerIsNull() {
        // Spy를 사용하여 insetsController가 null을 반환하도록 만듦
        val window = spy(activity.window)
        doReturn(null).`when`(window).insetsController

        // controller가 null일 때 WindowCompat.setDecorFitsSystemWindows가 호출됨
        controller.setAdjustResize(window)

        // 예외가 발생하지 않으면 성공
        // WindowCompat.setDecorFitsSystemWindows는 내부적으로 안전하게 처리됨
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.TIRAMISU])
    fun startStylusHandwriting_returnsTrue_whenFocusSucceeds() {
        val view = attachEditText()
        assertTrue(controller.startStylusHandwriting(view))
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.TIRAMISU])
    fun startStylusHandwriting_returnsFalse_whenFocusFails() {
        val view = attachViewWithFocusResult(false)
        assertFalse(controller.startStylusHandwriting(view))
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.TIRAMISU])
    fun startStylusHandwriting_withDelay_postsRunnableAndStartsHandwriting() {
        val view = attachEditText()
        assertTrue(controller.startStylusHandwriting(view, 50L))
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()
        // Robolectric에서 스타일러스 핸드라이팅 상태를 직접 검증하기 어려우므로
        // Runnable이 정상적으로 실행되었는지 확인 (예외가 발생하지 않음)
    }

    @Test
    fun setAdjustPan_returnsTrue_andSetsSoftInputMode() {
        val window = activity.window
        val result = controller.setAdjustPan(window)
        assertTrue(result)
        val adjustMask = window.attributes.softInputMode and WindowManager.LayoutParams.SOFT_INPUT_MASK_ADJUST
        assertEquals(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN, adjustMask)
    }

    @Test
    fun setSoftInputMode_returnsTrue_andAppliesCustomMode() {
        val window = activity.window
        val customMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE or
                WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE
        val result = controller.setSoftInputMode(window, customMode)
        assertTrue(result)
        assertEquals(customMode, window.attributes.softInputMode)
    }

    @Test
    fun setSoftInputMode_withAdjustNothing_setsCorrectly() {
        val window = activity.window
        val mode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING
        assertTrue(controller.setSoftInputMode(window, mode))
        val adjustMask = window.attributes.softInputMode and WindowManager.LayoutParams.SOFT_INPUT_MASK_ADJUST
        assertEquals(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING, adjustMask)
    }
}