package kr.open.library.simple_ui.compose.robolectric.toast

import android.widget.Toast
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.core.app.ApplicationProvider
import kr.open.library.simple_ui.compose.toast.rememberToast
import kr.open.library.simple_ui.compose.toast.showToast
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowToast

/**
 * Robolectric 기반 Toast 헬퍼 통합 테스트.<br>
 * Integration tests for the Toast helpers using Robolectric.<br>
 *
 * 표시 메시지와 표시 시간(duration) 전달을 검증합니다.<br>
 * Verifies the displayed message and the duration passed to the platform Toast.<br>
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class ToastHelpersRobolectricTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<androidx.activity.ComponentActivity>()

    // -----------------------------------------------------------------------
    // rememberToast
    // -----------------------------------------------------------------------

    @Test
    fun `rememberToast 는 전달한 메시지를 표시한다`() {
        lateinit var showToast: (CharSequence) -> Unit
        composeTestRule.setContent {
            showToast = rememberToast()
        }

        composeTestRule.waitForIdle()
        showToast("저장했습니다")

        assertEquals("전달한 메시지가 표시되어야 합니다", "저장했습니다", ShadowToast.getTextOfLatestToast())
    }

    @Test
    fun `rememberToast 는 기본값에서 LENGTH_SHORT 를 사용한다`() {
        lateinit var showToast: (CharSequence) -> Unit
        composeTestRule.setContent {
            showToast = rememberToast()
        }

        composeTestRule.waitForIdle()
        showToast("짧은 메시지")

        assertEquals(
            "기본값은 LENGTH_SHORT 여야 합니다",
            Toast.LENGTH_SHORT,
            ShadowToast.getLatestToast().duration,
        )
    }

    @Test
    fun `rememberToast 는 durationLong 이 true 이면 LENGTH_LONG 을 사용한다`() {
        lateinit var showToast: (CharSequence) -> Unit
        composeTestRule.setContent {
            showToast = rememberToast(durationLong = true)
        }

        composeTestRule.waitForIdle()
        showToast("긴 메시지")

        assertEquals(
            "durationLong=true 이면 LENGTH_LONG 이어야 합니다",
            Toast.LENGTH_LONG,
            ShadowToast.getLatestToast().duration,
        )
    }

    // -----------------------------------------------------------------------
    // Context.showToast
    // -----------------------------------------------------------------------

    @Test
    fun `Context showToast 는 Composable 외부에서도 메시지를 표시한다`() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()

        context.showToast("외부 호출")

        assertEquals("Composable 외부 호출도 표시되어야 합니다", "외부 호출", ShadowToast.getTextOfLatestToast())
    }

    @Test
    fun `Context showToast 는 durationLong 을 반영한다`() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()

        context.showToast("외부 긴 메시지", durationLong = true)

        assertEquals(
            "durationLong=true 이면 LENGTH_LONG 이어야 합니다",
            Toast.LENGTH_LONG,
            ShadowToast.getLatestToast().duration,
        )
    }
}
