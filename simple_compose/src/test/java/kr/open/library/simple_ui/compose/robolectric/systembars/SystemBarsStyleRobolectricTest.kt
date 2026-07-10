package kr.open.library.simple_ui.compose.robolectric.systembars

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.core.view.WindowCompat
import kr.open.library.simple_ui.compose.systembars.SystemBarsStyle
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Robolectric + createComposeRule 기반 SystemBarsStyle 통합 테스트.<br>
 * Integration tests for SystemBarsStyle using Robolectric and createComposeRule.<br>
 *
 * 아이콘 명암 설정·복원 및 조건부 패딩 Modifier 동작을 검증합니다.<br>
 * Verifies icon appearance setting/restoration and conditional padding Modifier behavior.<br>
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class SystemBarsStyleRobolectricTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<androidx.activity.ComponentActivity>()

    // -----------------------------------------------------------------------
    // SystemBarsStyle — isAppearanceLightStatusBars 설정 검증
    // SystemBarsStyle — isAppearanceLightStatusBars setting
    // -----------------------------------------------------------------------

    @Test
    fun `SystemBarsStyle sets isAppearanceLightStatusBars to true when statusBarDarkIcons is true`() {
        composeTestRule.setContent {
            SystemBarsStyle(statusBarDarkIcons = true)
        }

        composeTestRule.waitForIdle()

        val window = composeTestRule.activity.window
        val view = composeTestRule.activity.window.decorView
        val controller = WindowCompat.getInsetsController(window, view)
        assertTrue(
            "statusBarDarkIcons=true 이면 isAppearanceLightStatusBars가 true여야 합니다",
            controller.isAppearanceLightStatusBars,
        )
    }

    @Test
    fun `SystemBarsStyle sets isAppearanceLightStatusBars to false when statusBarDarkIcons is false`() {
        composeTestRule.setContent {
            SystemBarsStyle(statusBarDarkIcons = false)
        }

        composeTestRule.waitForIdle()

        val window = composeTestRule.activity.window
        val view = composeTestRule.activity.window.decorView
        val controller = WindowCompat.getInsetsController(window, view)
        assertFalse(
            "statusBarDarkIcons=false 이면 isAppearanceLightStatusBars가 false여야 합니다",
            controller.isAppearanceLightStatusBars,
        )
    }

    // -----------------------------------------------------------------------
    // SystemBarsStyle — isAppearanceLightNavigationBars 설정 검증
    // SystemBarsStyle — isAppearanceLightNavigationBars setting
    // -----------------------------------------------------------------------

    @Test
    fun `SystemBarsStyle sets navigationBarDarkIcons independently when provided`() {
        composeTestRule.setContent {
            SystemBarsStyle(statusBarDarkIcons = false, navigationBarDarkIcons = true)
        }

        composeTestRule.waitForIdle()

        val window = composeTestRule.activity.window
        val view = composeTestRule.activity.window.decorView
        val controller = WindowCompat.getInsetsController(window, view)
        assertFalse("statusBarDarkIcons=false → isAppearanceLightStatusBars false", controller.isAppearanceLightStatusBars)
        assertTrue("navigationBarDarkIcons=true → isAppearanceLightNavigationBars true", controller.isAppearanceLightNavigationBars)
    }

    @Test
    fun `SystemBarsStyle uses statusBarDarkIcons as default for navigationBarDarkIcons`() {
        composeTestRule.setContent {
            SystemBarsStyle(statusBarDarkIcons = true)
        }

        composeTestRule.waitForIdle()

        val window = composeTestRule.activity.window
        val view = composeTestRule.activity.window.decorView
        val controller = WindowCompat.getInsetsController(window, view)
        assertTrue("navigationBarDarkIcons 기본값은 statusBarDarkIcons와 동일해야 합니다", controller.isAppearanceLightNavigationBars)
    }

    // -----------------------------------------------------------------------
    // SystemBarsStyle — Composable 이탈 시 복원 검증
    // SystemBarsStyle — restoration on disposal
    // -----------------------------------------------------------------------

    @Test
    fun `SystemBarsStyle restores original isAppearanceLightStatusBars on disposal`() {
        val window = composeTestRule.activity.window
        val view = composeTestRule.activity.window.decorView
        val controller = WindowCompat.getInsetsController(window, view)

        // 복원 전 원래 값 설정 (false)
        // Set original value to false before composing
        composeTestRule.runOnUiThread {
            controller.isAppearanceLightStatusBars = false
        }

        var showStyle by mutableStateOf(true)

        composeTestRule.setContent {
            if (showStyle) {
                SystemBarsStyle(statusBarDarkIcons = true)
            }
        }

        composeTestRule.waitForIdle()
        // 설정 후 확인
        assertTrue("설정 중에는 true여야 합니다", controller.isAppearanceLightStatusBars)

        // Composable 제거 → onDispose 실행
        // Remove composable → onDispose fires
        composeTestRule.runOnUiThread { showStyle = false }
        composeTestRule.waitForIdle()

        assertFalse(
            "Composable 이탈 후 isAppearanceLightStatusBars가 원래 값(false)으로 복원되어야 합니다",
            controller.isAppearanceLightStatusBars,
        )
    }
}
