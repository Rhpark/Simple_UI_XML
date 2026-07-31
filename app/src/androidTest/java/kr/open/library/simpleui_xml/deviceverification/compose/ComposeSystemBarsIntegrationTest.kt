package kr.open.library.simpleui_xml.deviceverification.compose

import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.core.view.WindowCompat
import androidx.test.ext.junit.runners.AndroidJUnit4
import kr.open.library.simpleui_xml.compose.ComposeExampleSemantics
import kr.open.library.simpleui_xml.compose.ComposeExamplesActivity
import kr.open.library.simpleui_xml.deviceverification.harness.PhysicalDeviceRule
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.rules.TestRule
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class ComposeSystemBarsIntegrationTest {
    private val physicalDeviceRule = PhysicalDeviceRule()
    private val composeRule = createAndroidComposeRule<ComposeExamplesActivity>()

    @get:Rule
    val ruleChain: TestRule = RuleChain.outerRule(physicalDeviceRule).around(composeRule)

    @Test
    fun composeP0004_systemBarIconAppearanceCanBeAppliedAndRestored() {
        composeRule.waitForIdle()
        assertTrue(currentLightStatusBarAppearance())

        composeRule.onNodeWithTag(ComposeExampleSemantics.SYSTEM_BAR_TOGGLE).performClick()
        composeRule
            .onNodeWithTag(ComposeExampleSemantics.SYSTEM_BAR_STATE)
            .assertTextEquals("현재: 밝은 아이콘")
        assertFalse(currentLightStatusBarAppearance())

        composeRule.onNodeWithTag(ComposeExampleSemantics.SYSTEM_BAR_TOGGLE).performClick()
        composeRule
            .onNodeWithTag(ComposeExampleSemantics.SYSTEM_BAR_STATE)
            .assertTextEquals("현재: 어두운 아이콘")
        assertTrue(currentLightStatusBarAppearance())
    }

    private fun currentLightStatusBarAppearance(): Boolean {
        val window = composeRule.activity.window
        return WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars
    }
}
