package kr.open.library.simpleui_xml.deviceverification.compose

import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
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
internal class ComposeLifecycleIntegrationTest {
    private val physicalDeviceRule = PhysicalDeviceRule()
    private val composeRule = createAndroidComposeRule<ComposeExamplesActivity>()

    @get:Rule
    val ruleChain: TestRule = RuleChain.outerRule(physicalDeviceRule).around(composeRule)

    @Test
    fun composeP0003_eventIsCollectedOnceAcrossActivityRecreation() {
        composeRule.onNodeWithTag(ComposeExampleSemantics.EVENT_SEND).performClick()
        composeRule
            .onNodeWithTag(ComposeExampleSemantics.EVENT_STATE)
            .assertTextEquals("최근 수신 이벤트: ViewModel 이벤트 #1")

        composeRule.activityRule.scenario.recreate()
        composeRule.waitForIdle()

        composeRule
            .onNodeWithTag(ComposeExampleSemantics.EVENT_STATE)
            .assertTextEquals("최근 수신 이벤트: ViewModel 이벤트 #1")
        composeRule.onNodeWithTag(ComposeExampleSemantics.EVENT_SEND).performClick()
        composeRule
            .onNodeWithTag(ComposeExampleSemantics.EVENT_STATE)
            .assertTextEquals("최근 수신 이벤트: ViewModel 이벤트 #2")
    }
}
