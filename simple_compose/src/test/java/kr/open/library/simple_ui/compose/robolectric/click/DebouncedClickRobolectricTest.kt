package kr.open.library.simple_ui.compose.robolectric.click

import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import kr.open.library.simple_ui.compose.click.debouncedClickable
import kr.open.library.simple_ui.compose.click.rememberDebouncedClick
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Robolectric + createComposeRule 기반 디바운스 클릭 통합 테스트.<br>
 * Integration tests for debounced click APIs using Robolectric and createComposeRule.<br>
 *
 * 연속 클릭 차단과 활성화 상태 동작을 실제 컴포지션에서 검증합니다.<br>
 * Verifies repeated-click suppression and enabled-state behavior in a real composition.<br>
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class DebouncedClickRobolectricTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<androidx.activity.ComponentActivity>()

    private companion object {
        const val TAG_TARGET = "debounced_target"
    }

    // -----------------------------------------------------------------------
    // Modifier.debouncedClickable
    // -----------------------------------------------------------------------

    @Test
    fun `debouncedClickable 는 차단 간격 이내의 연속 클릭을 한 번만 전달한다`() {
        var clickCount = 0
        composeTestRule.setContent {
            Box(
                modifier = Modifier
                    .testTag(TAG_TARGET)
                    .debouncedClickable { clickCount++ },
            )
        }

        repeat(5) { composeTestRule.onNodeWithTag(TAG_TARGET).performClick() }
        composeTestRule.waitForIdle()

        assertEquals("연타는 첫 클릭만 전달되어야 합니다", 1, clickCount)
    }

    @Test
    fun `debouncedClickable 는 차단 간격이 0이면 모든 클릭을 전달한다`() {
        var clickCount = 0
        composeTestRule.setContent {
            Box(
                modifier = Modifier
                    .testTag(TAG_TARGET)
                    .debouncedClickable(debounceMs = 0L) { clickCount++ },
            )
        }

        repeat(3) { composeTestRule.onNodeWithTag(TAG_TARGET).performClick() }
        composeTestRule.waitForIdle()

        assertEquals("차단 간격 0이면 모든 클릭이 전달되어야 합니다", 3, clickCount)
    }

    @Test
    fun `debouncedClickable 는 enabled 가 false 이면 클릭을 전달하지 않는다`() {
        var clickCount = 0
        composeTestRule.setContent {
            Box(
                modifier = Modifier
                    .testTag(TAG_TARGET)
                    .debouncedClickable(enabled = false) { clickCount++ },
            )
        }

        composeTestRule.onNodeWithTag(TAG_TARGET).performClick()
        composeTestRule.waitForIdle()

        assertEquals("비활성 상태에서는 클릭이 전달되지 않아야 합니다", 0, clickCount)
    }

    // -----------------------------------------------------------------------
    // rememberDebouncedClick
    // -----------------------------------------------------------------------

    @Test
    fun `rememberDebouncedClick 는 차단 간격 이내의 연속 호출을 한 번만 전달한다`() {
        var clickCount = 0
        lateinit var onClick: () -> Unit
        composeTestRule.setContent {
            onClick = rememberDebouncedClick { clickCount++ }
        }

        composeTestRule.waitForIdle()
        repeat(5) { onClick() }

        assertEquals("연타는 첫 호출만 전달되어야 합니다", 1, clickCount)
    }

    @Test
    fun `rememberDebouncedClick 는 재구성 후에도 동일한 차단 상태를 유지한다`() {
        var clickCount = 0
        lateinit var onClick: () -> Unit
        composeTestRule.setContent {
            onClick = rememberDebouncedClick { clickCount++ }
        }

        composeTestRule.waitForIdle()
        onClick()
        composeTestRule.waitForIdle()
        onClick()

        assertEquals("재구성 이후에도 차단 상태가 유지되어야 합니다", 1, clickCount)
    }
}
