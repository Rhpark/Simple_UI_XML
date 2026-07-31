package kr.open.library.simpleui_xml.deviceverification.compose

import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performScrollToIndex
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
internal class ComposeScrollIntegrationTest {
    private val physicalDeviceRule = PhysicalDeviceRule()
    private val composeRule = createAndroidComposeRule<ComposeExamplesActivity>()

    @get:Rule
    val ruleChain: TestRule = RuleChain.outerRule(physicalDeviceRule).around(composeRule)

    @Test
    fun composeP0005_lazyListReportsStartAndEndEdges() {
        composeRule
            .onNodeWithTag(ComposeExampleSemantics.SCROLL_STATE)
            .assertTextContains("위: true", substring = true)
            .assertTextContains("아래: false", substring = true)

        composeRule.onNodeWithTag(ComposeExampleSemantics.SCROLL_LIST).performScrollToIndex(29)
        composeRule.waitForIdle()
        composeRule
            .onNodeWithTag(ComposeExampleSemantics.SCROLL_STATE)
            .assertTextContains("위: false", substring = true)
            .assertTextContains("아래: true", substring = true)

        composeRule.onNodeWithTag(ComposeExampleSemantics.SCROLL_LIST).performScrollToIndex(0)
        composeRule.waitForIdle()
        composeRule
            .onNodeWithTag(ComposeExampleSemantics.SCROLL_STATE)
            .assertTextContains("위: true", substring = true)
            .assertTextContains("아래: false", substring = true)
    }
}
