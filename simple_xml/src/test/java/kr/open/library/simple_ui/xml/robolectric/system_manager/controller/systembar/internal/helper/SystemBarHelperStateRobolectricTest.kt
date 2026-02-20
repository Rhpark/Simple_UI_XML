package kr.open.library.simple_ui.xml.robolectric.system_manager.controller.systembar.internal.helper

import android.app.Activity
import androidx.core.graphics.Insets
import androidx.core.view.WindowInsetsCompat
import kr.open.library.simple_ui.xml.system_manager.controller.systembar.internal.helper.NavigationBarHelper
import kr.open.library.simple_ui.xml.system_manager.controller.systembar.internal.helper.StatusBarHelper
import kr.open.library.simple_ui.xml.system_manager.controller.systembar.model.SystemBarStableState
import kr.open.library.simple_ui.xml.system_manager.controller.systembar.model.SystemBarVisibleState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner

/**
 * Robolectric tests for StatusBarHelper and NavigationBarHelper sealed state returns.<br><br>
 *
 * ## Known Robolectric limitation / 알려진 Robolectric 제한사항
 * `WindowInsetsCompat.Builder.setInsetsIgnoringVisibility()` does NOT work independently
 * from `setInsets()` in Robolectric — the builder internally clamps stable insets to match
 * visible insets, so `getInsetsIgnoringVisibility()` returns the same value as `getInsets()`.<br>
 * This means the **Hidden** state (stable > 0, visible == 0) and "stable remains Stable
 * when hidden" scenarios are **NOT testable** in Robolectric for both StatusBar and NavigationBar.<br>
 * For NavigationBar, `getInsetsIgnoringVisibility(navigationBars())` always returns `Insets.NONE`
 * regardless of input, making all NavigationBar StableState tests untestable.<br><br>
 *
 * `WindowInsetsCompat.Builder.setInsetsIgnoringVisibility()`는 Robolectric에서
 * `setInsets()`와 독립적으로 동작하지 않습니다 — 빌더가 내부적으로 stable insets를
 * visible insets에 맞춰 클램핑하여, `getInsetsIgnoringVisibility()`가 `getInsets()`와
 * 동일한 값을 반환합니다.<br>
 * 따라서 **Hidden** 상태(stable > 0, visible == 0)와 "숨김 시에도 Stable 유지" 시나리오는
 * StatusBar와 NavigationBar 모두에서 Robolectric에서 **테스트 불가**합니다.<br>
 * NavigationBar의 경우 `getInsetsIgnoringVisibility(navigationBars())`가 입력과 무관하게
 * 항상 `Insets.NONE`을 반환하여, 모든 NavigationBar StableState 테스트가 불가능합니다.<br>
 */
@RunWith(RobolectricTestRunner::class)
class SystemBarHelperStateRobolectricTest {
    private lateinit var activity: Activity

    @Before
    fun setUp() {
        activity = Robolectric.buildActivity(Activity::class.java).setup().get()
        activity.window.decorView.layout(0, 0, 1080, 1920)
        assertTrue(activity.window.decorView.isAttachedToWindow)
        assertTrue(activity.window.decorView.width > 0)
        assertTrue(activity.window.decorView.height > 0)
    }

    // ── StatusBar VisibleState ──────────────────────────────────────────

    @Test
    fun `status bar returns NotReady when insets are null`() {
        val helper = StatusBarHelper(activity.window.decorView)

        assertEquals(SystemBarVisibleState.NotReady, helper.getStatusBarVisibleState(null))
        assertEquals(SystemBarStableState.NotReady, helper.getStatusBarStableState(null))
    }

    @Test
    fun `status bar returns NotPresent when stable and visible are zero`() {
        val helper = StatusBarHelper(activity.window.decorView)
        val insets = createInsets()

        assertEquals(SystemBarVisibleState.NotPresent, helper.getStatusBarVisibleState(insets))
        assertEquals(SystemBarStableState.NotPresent, helper.getStatusBarStableState(insets))
    }

    // NOTE: StatusBar Hidden test is omitted — see class KDoc for Robolectric limitation.

    @Test
    fun `status bar returns Visible when visible top is positive`() {
        val helper = StatusBarHelper(activity.window.decorView)
        val insets = createInsets(statusBarTop = 72)

        val visibleState = helper.getStatusBarVisibleState(insets)
        assertTrue(visibleState is SystemBarVisibleState.Visible)
        assertEquals(72, (visibleState as SystemBarVisibleState.Visible).rect.height())
    }

    // ── StatusBar StableState ───────────────────────────────────────────

    @Test
    fun `status bar returns Stable when stable top is positive`() {
        val helper = StatusBarHelper(activity.window.decorView)
        val insets = createInsets(statusBarTop = 96)

        val stableState = helper.getStatusBarStableState(insets)
        assertTrue("Expected Stable but got: $stableState", stableState is SystemBarStableState.Stable)
        assertEquals(96, (stableState as SystemBarStableState.Stable).rect.height())
    }

    // NOTE: "stable remains Stable even when hidden" test is omitted — see class KDoc.

    // ── StatusBar coordinate clamping ───────────────────────────────────

    @Test
    fun `status bar visible rect clamps to decorView height`() {
        val helper = StatusBarHelper(activity.window.decorView)
        val insets = createInsets(statusBarTop = 3000)

        val visibleState = helper.getStatusBarVisibleState(insets) as SystemBarVisibleState.Visible
        assertEquals(1920, visibleState.rect.height())
    }

    @Test
    fun `status bar stable rect clamps to decorView height`() {
        val helper = StatusBarHelper(activity.window.decorView)
        val insets = createInsets(statusBarTop = 3000)

        val stableState = helper.getStatusBarStableState(insets) as SystemBarStableState.Stable
        assertEquals(1920, stableState.rect.height())
    }

    @Test
    fun `status bar visible rect width matches decorView width`() {
        val helper = StatusBarHelper(activity.window.decorView)
        val insets = createInsets(statusBarTop = 96)

        val visibleState = helper.getStatusBarVisibleState(insets) as SystemBarVisibleState.Visible
        assertEquals(0, visibleState.rect.left)
        assertEquals(1080, visibleState.rect.right)
    }

    // ── NavigationBar VisibleState ──────────────────────────────────────

    @Test
    fun `navigation bar returns NotReady when insets are null`() {
        val helper = NavigationBarHelper(activity.window.decorView)

        assertEquals(SystemBarVisibleState.NotReady, helper.getNavigationBarVisibleState(null))
        assertEquals(SystemBarStableState.NotReady, helper.getNavigationBarStableState(null))
    }

    @Test
    fun `navigation bar returns NotPresent when stable and visible are zero`() {
        val helper = NavigationBarHelper(activity.window.decorView)
        val insets = createInsets()

        assertEquals(SystemBarVisibleState.NotPresent, helper.getNavigationBarVisibleState(insets))
        assertEquals(SystemBarStableState.NotPresent, helper.getNavigationBarStableState(insets))
    }

    // NOTE: NavigationBar Hidden / StableState tests are omitted.
    //       See class KDoc for the Robolectric limitation.

    @Test
    fun `navigation bar returns Visible with bottom position`() {
        val helper = NavigationBarHelper(activity.window.decorView)

        val visibleState = helper.getNavigationBarVisibleState(
            createInsets(navVisibleBottom = 80)
        ) as SystemBarVisibleState.Visible
        assertEquals(1840, visibleState.rect.top)
        assertEquals(1920, visibleState.rect.bottom)
    }

    @Test
    fun `navigation bar bottom rect spans full width`() {
        val helper = NavigationBarHelper(activity.window.decorView)

        val visibleState = helper.getNavigationBarVisibleState(
            createInsets(navVisibleBottom = 80)
        ) as SystemBarVisibleState.Visible
        assertEquals(0, visibleState.rect.left)
        assertEquals(1080, visibleState.rect.right)
    }

    @Test
    fun `navigation bar returns Visible with left position`() {
        val helper = NavigationBarHelper(activity.window.decorView)

        val visibleState = helper.getNavigationBarVisibleState(
            createInsets(navVisibleLeft = 64)
        ) as SystemBarVisibleState.Visible
        assertEquals(0, visibleState.rect.left)
        assertEquals(64, visibleState.rect.right)
    }

    @Test
    fun `navigation bar left rect spans full height`() {
        val helper = NavigationBarHelper(activity.window.decorView)

        val visibleState = helper.getNavigationBarVisibleState(
            createInsets(navVisibleLeft = 64)
        ) as SystemBarVisibleState.Visible
        assertEquals(0, visibleState.rect.top)
        assertEquals(1920, visibleState.rect.bottom)
    }

    @Test
    fun `navigation bar returns Visible with right position`() {
        val helper = NavigationBarHelper(activity.window.decorView)

        val visibleState = helper.getNavigationBarVisibleState(
            createInsets(navVisibleRight = 48)
        ) as SystemBarVisibleState.Visible
        assertEquals(1032, visibleState.rect.left)
        assertEquals(1080, visibleState.rect.right)
    }

    @Test
    fun `navigation bar right rect spans full height`() {
        val helper = NavigationBarHelper(activity.window.decorView)

        val visibleState = helper.getNavigationBarVisibleState(
            createInsets(navVisibleRight = 48)
        ) as SystemBarVisibleState.Visible
        assertEquals(0, visibleState.rect.top)
        assertEquals(1920, visibleState.rect.bottom)
    }

    @Test
    fun `navigation bar bottom clamps to decorView height`() {
        val helper = NavigationBarHelper(activity.window.decorView)

        val visibleState = helper.getNavigationBarVisibleState(
            createInsets(navVisibleBottom = 3000)
        ) as SystemBarVisibleState.Visible
        assertEquals(0, visibleState.rect.top)
        assertEquals(1920, visibleState.rect.bottom)
    }

    @Test
    fun `navigation bar falls back to NotPresent when only top inset is set`() {
        val helper = NavigationBarHelper(activity.window.decorView)

        val visibleState = helper.getNavigationBarVisibleState(
            createInsets(navVisibleTop = 42)
        )

        assertEquals(SystemBarVisibleState.NotPresent, visibleState)
    }

    // ── decorView not ready ──────────────────────────────────────

    @Test
    fun `status bar returns NotReady when decorView has zero dimensions`() {
        val zeroView = activity.window.decorView
        zeroView.layout(0, 0, 0, 0)
        val helper = StatusBarHelper(zeroView)
        val insets = createInsets(statusBarTop = 96)

        assertEquals(SystemBarVisibleState.NotReady, helper.getStatusBarVisibleState(insets))
        assertEquals(SystemBarStableState.NotReady, helper.getStatusBarStableState(insets))
    }

    @Test
    fun `navigation bar returns NotReady when decorView has zero dimensions`() {
        val zeroView = activity.window.decorView
        zeroView.layout(0, 0, 0, 0)
        val helper = NavigationBarHelper(zeroView)
        val insets = createInsets(navVisibleBottom = 80)

        assertEquals(SystemBarVisibleState.NotReady, helper.getNavigationBarVisibleState(insets))
        assertEquals(SystemBarStableState.NotReady, helper.getNavigationBarStableState(insets))
    }

    // ── Helper ──────────────────────────────────────────────────────────

    /**
     * Creates WindowInsetsCompat for testing.<br>
     * Sets both visible and stable insets to the same value because Robolectric's
     * Builder clamps stable insets to match visible insets.<br><br>
     *
     * Robolectric에서 Builder가 stable insets를 visible insets에 맞춰 클램핑하므로,
     * visible과 stable 모두 동일한 값으로 설정합니다.<br>
     */
    private fun createInsets(
        statusBarTop: Int = 0,
        navVisibleLeft: Int = 0,
        navVisibleTop: Int = 0,
        navVisibleRight: Int = 0,
        navVisibleBottom: Int = 0,
    ): WindowInsetsCompat {
        val builder = WindowInsetsCompat.Builder()

        builder.setInsets(
            WindowInsetsCompat.Type.statusBars(),
            Insets.of(0, statusBarTop, 0, 0),
        )
        builder.setInsetsIgnoringVisibility(
            WindowInsetsCompat.Type.statusBars(),
            Insets.of(0, statusBarTop, 0, 0),
        )

        builder.setInsets(
            WindowInsetsCompat.Type.navigationBars(),
            Insets.of(navVisibleLeft, navVisibleTop, navVisibleRight, navVisibleBottom),
        )

        return builder.build()
    }
}
