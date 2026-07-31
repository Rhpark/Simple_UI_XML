package kr.open.library.simpleui_xml.deviceverification.compose

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import kr.open.library.simpleui_xml.compose.ComposeExampleSemantics
import kr.open.library.simpleui_xml.compose.ComposeExamplesActivity
import kr.open.library.simpleui_xml.deviceverification.harness.PhysicalDeviceRule
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.rules.TestRule
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class ComposeRenderingIntegrationTest {
    private val physicalDeviceRule = PhysicalDeviceRule()
    private val composeRule = createAndroidComposeRule<ComposeExamplesActivity>()

    @get:Rule
    val ruleChain: TestRule = RuleChain.outerRule(physicalDeviceRule).around(composeRule)

    @Test
    fun composeP0001_exampleScreenRendersCoreSemantics() {
        composeRule.onNodeWithTag(ComposeExampleSemantics.SCREEN).assertIsDisplayed()
        composeRule.onNodeWithText("Simple Compose Examples").assertIsDisplayed()
        composeRule.onNodeWithTag(ComposeExampleSemantics.PERMISSION_STATE).assertIsDisplayed()
        composeRule.onNodeWithTag(ComposeExampleSemantics.EVENT_STATE).assertIsDisplayed()
        composeRule.onNodeWithTag(ComposeExampleSemantics.SYSTEM_BAR_STATE).assertIsDisplayed()
        composeRule.onNodeWithTag(ComposeExampleSemantics.SCROLL_STATE).assertIsDisplayed()
    }
}
