package kr.open.library.simpleui_xml.deviceverification.compose

import android.Manifest
import android.provider.Settings
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kr.open.library.simple_ui.compose.permissions.PermissionRequestPhase
import kr.open.library.simple_ui.compose.permissions.PermissionRequestState
import kr.open.library.simple_ui.compose.permissions.rememberPermissionRequestState
import kr.open.library.simpleui_xml.deviceverification.harness.PhysicalDeviceRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.rules.TestRule
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class ComposePermissionIntegrationTest {
    private val physicalDeviceRule = PhysicalDeviceRule()
    private val composeRule = createComposeRule()

    @get:Rule
    val ruleChain: TestRule = RuleChain.outerRule(physicalDeviceRule).around(composeRule)

    private val context
        get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun composeP0002_overlayPermissionGateTransitionsWithoutDuplicateRequest() {
        requireOverlayPermissionDenied("COMPOSE-P0-002")

        lateinit var state: PermissionRequestState
        composeRule.setContent {
            state = rememberPermissionRequestState(
                permissions = listOf(Manifest.permission.SYSTEM_ALERT_WINDOW),
                gateSettingsNavigation = true,
            )
            Box(modifier = Modifier.testTag("compose_permission_host"))
        }
        composeRule.onNodeWithTag("compose_permission_host").fetchSemanticsNode()

        composeRule.runOnIdle {
            state.request { }
        }
        composeRule.waitForIdle()

        assertEquals(PermissionRequestPhase.SETTINGS_NAVIGATION_REQUIRED, state.phase)
        assertEquals(Manifest.permission.SYSTEM_ALERT_WINDOW, state.settingsNavigationRequired)
        assertTrue(state.isRequesting)

        composeRule.runOnIdle {
            state.request { }
            state.cancelSettingsNavigation()
        }
        composeRule.waitForIdle()

        assertEquals(PermissionRequestPhase.COMPLETED, state.phase)
        assertFalse(state.isRequesting)
    }

    @Test
    fun composeP1001_permissionGateStateSurvivesSavedStateRestoration() {
        requireOverlayPermissionDenied("COMPOSE-P1-001")

        val restorationTester = StateRestorationTester(composeRule)
        var state: PermissionRequestState? = null
        restorationTester.setContent {
            state = rememberPermissionRequestState(
                permissions = listOf(Manifest.permission.SYSTEM_ALERT_WINDOW),
                gateSettingsNavigation = true,
            )
        }

        composeRule.runOnIdle {
            state?.request { }
        }
        composeRule.waitForIdle()
        assertEquals(PermissionRequestPhase.SETTINGS_NAVIGATION_REQUIRED, state?.phase)

        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        assertEquals(PermissionRequestPhase.SETTINGS_NAVIGATION_REQUIRED, state?.phase)
        assertEquals(Manifest.permission.SYSTEM_ALERT_WINDOW, state?.settingsNavigationRequired)
        assertTrue(state?.isRequesting == true)

        composeRule.runOnIdle {
            state?.cancelSettingsNavigation()
        }
    }

    private fun requireOverlayPermissionDenied(scenarioId: String) {
        val isGranted = Settings.canDrawOverlays(context)
        physicalDeviceRule.requirePrecondition(
            scenarioId = scenarioId,
            expected = "overlayPermission=false",
            actual = "overlayPermission=$isGranted",
            isSatisfied = !isGranted,
        )
    }
}
