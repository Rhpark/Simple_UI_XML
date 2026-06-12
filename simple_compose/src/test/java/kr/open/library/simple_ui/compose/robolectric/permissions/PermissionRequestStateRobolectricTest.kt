package kr.open.library.simple_ui.compose.robolectric.permissions

import android.Manifest
import android.app.Activity
import android.app.Application
import android.content.pm.PackageManager
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.core.app.ApplicationProvider
import kr.open.library.simple_ui.compose.permissions.PermissionRequestState
import kr.open.library.simple_ui.compose.permissions.rememberPermissionRequestState
import kr.open.library.simple_ui.core.permissions.model.PermissionDeniedItem
import kr.open.library.simple_ui.core.permissions.model.PermissionDeniedType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

/**
 * Robolectric + createAndroidComposeRule 기반 PermissionRequestState 통합 테스트.<br>
 * Integration tests for PermissionRequestState using Robolectric and createAndroidComposeRule.<br>
 *
 * granted/denied/빈 요청/manifest 미선언/특수 권한 인텐트/rationale/상태 복원 시나리오를 검증합니다.<br>
 * Verifies granted/denied/empty-request/manifest-undeclared/special-permission-intent/
 * rationale/state-restoration scenarios.<br>
 *
 * 테스트 매니페스트(src/test/AndroidManifest.xml)에 CAMERA, SYSTEM_ALERT_WINDOW가 선언되어 있어야 합니다.<br>
 * The test manifest (src/test/AndroidManifest.xml) must declare CAMERA and SYSTEM_ALERT_WINDOW.<br>
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class PermissionRequestStateRobolectricTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val application: Application
        get() = ApplicationProvider.getApplicationContext()

    // -----------------------------------------------------------------------
    // 빈 요청 / manifest 미선언
    // Empty request / manifest undeclared
    // -----------------------------------------------------------------------

    @Test
    fun `empty permission list completes immediately with EMPTY_REQUEST`() {
        lateinit var state: PermissionRequestState
        composeTestRule.setContent {
            state = rememberPermissionRequestState(emptyList())
        }

        var result: List<PermissionDeniedItem>? = null
        composeTestRule.runOnUiThread {
            state.request { result = it }
        }
        composeTestRule.waitForIdle()

        val expected = listOf(PermissionDeniedItem("", PermissionDeniedType.EMPTY_REQUEST))
        assertEquals(expected, result)
        assertEquals(expected, state.deniedItems)
        // 빈 목록은 전체 승인으로 간주 (vacuous truth)
        // An empty list is treated as all granted (vacuous truth)
        assertTrue(state.allGranted)
    }

    @Test
    fun `undeclared permission completes immediately with MANIFEST_UNDECLARED`() {
        val undeclared = "com.test.UNDECLARED_PERMISSION"
        lateinit var state: PermissionRequestState
        composeTestRule.setContent {
            state = rememberPermissionRequestState(listOf(undeclared))
        }

        var result: List<PermissionDeniedItem>? = null
        composeTestRule.runOnUiThread {
            state.request { result = it }
        }
        composeTestRule.waitForIdle()

        assertEquals(
            listOf(PermissionDeniedItem(undeclared, PermissionDeniedType.MANIFEST_UNDECLARED)),
            result,
        )
        // 시스템 다이얼로그가 실행되지 않아야 함
        // The system dialog must not be launched
        assertNull(shadowOf(composeTestRule.activity).lastRequestedPermission)
    }

    // -----------------------------------------------------------------------
    // granted 시나리오
    // Granted scenarios
    // -----------------------------------------------------------------------

    @Test
    fun `initial allGranted is true when permission is already granted`() {
        shadowOf(application).grantPermissions(Manifest.permission.CAMERA)

        lateinit var state: PermissionRequestState
        composeTestRule.setContent {
            state = rememberPermissionRequestState(listOf(Manifest.permission.CAMERA))
        }
        composeTestRule.waitForIdle()

        assertTrue(state.allGranted)
    }

    @Test
    fun `request with granted permission completes with empty denied list without launching dialog`() {
        shadowOf(application).grantPermissions(Manifest.permission.CAMERA)

        lateinit var state: PermissionRequestState
        composeTestRule.setContent {
            state = rememberPermissionRequestState(listOf(Manifest.permission.CAMERA))
        }

        var result: List<PermissionDeniedItem>? = null
        composeTestRule.runOnUiThread {
            state.request { result = it }
        }
        composeTestRule.waitForIdle()

        assertEquals(emptyList<PermissionDeniedItem>(), result)
        assertTrue(state.allGranted)
        assertNull(shadowOf(composeTestRule.activity).lastRequestedPermission)
    }

    // -----------------------------------------------------------------------
    // denied 시나리오 (런타임 다이얼로그 거부)
    // Denied scenario (runtime dialog denial)
    // -----------------------------------------------------------------------

    @Test
    fun `runtime dialog denial completes with DENIED and allGranted stays false`() {
        lateinit var state: PermissionRequestState
        composeTestRule.setContent {
            state = rememberPermissionRequestState(listOf(Manifest.permission.CAMERA))
        }
        composeTestRule.waitForIdle()
        assertFalse(state.allGranted)

        var result: List<PermissionDeniedItem>? = null
        composeTestRule.runOnUiThread {
            state.request { result = it }
        }
        composeTestRule.waitForIdle()

        deliverRuntimeResult(granted = false)
        composeTestRule.waitForIdle()

        assertEquals(
            listOf(PermissionDeniedItem(Manifest.permission.CAMERA, PermissionDeniedType.DENIED)),
            result,
        )
        assertEquals(result, state.deniedItems)
        assertFalse(state.allGranted)
    }

    // -----------------------------------------------------------------------
    // rationale 시나리오 (상태 노출 + 계속/취소)
    // Rationale scenario (state exposure + continue/cancel)
    // -----------------------------------------------------------------------

    @Test
    fun `rationale pauses the flow and continueRequest launches the dialog`() {
        shadowOf(application.packageManager)
            .setShouldShowRequestPermissionRationale(Manifest.permission.CAMERA, true)

        lateinit var state: PermissionRequestState
        composeTestRule.setContent {
            state = rememberPermissionRequestState(listOf(Manifest.permission.CAMERA))
        }

        var result: List<PermissionDeniedItem>? = null
        composeTestRule.runOnUiThread {
            state.request { result = it }
        }
        composeTestRule.waitForIdle()

        // 일시 정지: rationale 상태만 노출되고 다이얼로그는 실행되지 않음
        // Paused: only the rationale state is exposed; the dialog is not launched
        assertEquals(listOf(Manifest.permission.CAMERA), state.rationaleRequired)
        assertNull(shadowOf(composeTestRule.activity).lastRequestedPermission)
        assertNull(result)

        composeTestRule.runOnUiThread {
            state.continueRequest()
        }
        composeTestRule.waitForIdle()

        assertTrue(state.rationaleRequired.isEmpty())
        assertNotNull(shadowOf(composeTestRule.activity).lastRequestedPermission)

        // 승인 후 결과 전달 → 빈 거부 목록
        // Grant and deliver the result → empty denied list
        shadowOf(application).grantPermissions(Manifest.permission.CAMERA)
        deliverRuntimeResult(granted = true)
        composeTestRule.waitForIdle()

        assertEquals(emptyList<PermissionDeniedItem>(), result)
        assertTrue(state.allGranted)
    }

    @Test
    fun `cancelRequest at rationale stage completes pending permissions as DENIED`() {
        shadowOf(application.packageManager)
            .setShouldShowRequestPermissionRationale(Manifest.permission.CAMERA, true)

        lateinit var state: PermissionRequestState
        composeTestRule.setContent {
            state = rememberPermissionRequestState(listOf(Manifest.permission.CAMERA))
        }

        var result: List<PermissionDeniedItem>? = null
        composeTestRule.runOnUiThread {
            state.request { result = it }
        }
        composeTestRule.waitForIdle()
        assertEquals(listOf(Manifest.permission.CAMERA), state.rationaleRequired)

        composeTestRule.runOnUiThread {
            state.cancelRequest()
        }
        composeTestRule.waitForIdle()

        assertEquals(
            listOf(PermissionDeniedItem(Manifest.permission.CAMERA, PermissionDeniedType.DENIED)),
            result,
        )
        assertTrue(state.rationaleRequired.isEmpty())
        assertNull(shadowOf(composeTestRule.activity).lastRequestedPermission)
    }

    // -----------------------------------------------------------------------
    // 특수 권한 인텐트 시나리오
    // Special permission intent scenario
    // -----------------------------------------------------------------------

    @Test
    fun `special permission launches settings intent and re-checks grant status on return`() {
        lateinit var state: PermissionRequestState
        composeTestRule.setContent {
            state = rememberPermissionRequestState(listOf(Manifest.permission.SYSTEM_ALERT_WINDOW))
        }

        var result: List<PermissionDeniedItem>? = null
        composeTestRule.runOnUiThread {
            state.request { result = it }
        }
        composeTestRule.waitForIdle()

        val shadowActivity = shadowOf(composeTestRule.activity)
        val started = shadowActivity.nextStartedActivityForResult
        assertNotNull(started)
        assertEquals(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, started.intent.action)

        // 설정 화면 복귀 — resultCode와 무관하게 보유 상태 재확인으로 판정 (여전히 미허용 → DENIED)
        // Return from settings — grant status re-check regardless of resultCode (still off → DENIED)
        composeTestRule.runOnUiThread {
            shadowActivity.receiveResult(started.intent, Activity.RESULT_CANCELED, null)
        }
        composeTestRule.waitForIdle()

        assertEquals(
            listOf(
                PermissionDeniedItem(
                    Manifest.permission.SYSTEM_ALERT_WINDOW,
                    PermissionDeniedType.DENIED,
                ),
            ),
            result,
        )
        assertFalse(state.allGranted)
    }

    // -----------------------------------------------------------------------
    // 상태 복원 시나리오 (요청 이력 보존 → PERMANENTLY_DENIED 판정)
    // State restoration scenario (history preserved → PERMANENTLY_DENIED decision)
    // -----------------------------------------------------------------------

    @Test
    fun `request history survives state restoration and enables PERMANENTLY_DENIED decision`() {
        val restorationTester = StateRestorationTester(composeTestRule)
        var state: PermissionRequestState? = null
        restorationTester.setContent {
            state = rememberPermissionRequestState(listOf(Manifest.permission.CAMERA))
        }

        // 1차 요청 → 거부 (이력 없음 + rationale 불가 → DENIED)
        // First request → denial (no history + no rationale → DENIED)
        var firstResult: List<PermissionDeniedItem>? = null
        composeTestRule.runOnUiThread {
            state?.request { firstResult = it }
        }
        composeTestRule.waitForIdle()
        deliverRuntimeResult(granted = false)
        composeTestRule.waitForIdle()
        assertEquals(
            listOf(PermissionDeniedItem(Manifest.permission.CAMERA, PermissionDeniedType.DENIED)),
            firstResult,
        )

        // 구성 변경 에뮬레이션 — rememberSaveable 저장/복원
        // Emulate configuration change — rememberSaveable save/restore
        restorationTester.emulateSavedInstanceStateRestore()
        composeTestRule.waitForIdle()

        // 2차 요청 → 거부 (복원된 이력 있음 + rationale 불가 → PERMANENTLY_DENIED)
        // Second request → denial (restored history + no rationale → PERMANENTLY_DENIED)
        var secondResult: List<PermissionDeniedItem>? = null
        composeTestRule.runOnUiThread {
            state?.request { secondResult = it }
        }
        composeTestRule.waitForIdle()
        deliverRuntimeResult(granted = false)
        composeTestRule.waitForIdle()

        assertEquals(
            listOf(
                PermissionDeniedItem(
                    Manifest.permission.CAMERA,
                    PermissionDeniedType.PERMANENTLY_DENIED,
                ),
            ),
            secondResult,
        )
    }

    @Test
    fun `rationale pending state survives restoration and continueRequest launches the dialog`() {
        // MEDIUM 회귀 테스트: rationale 대기 중 구성 변경이 발생해도 흐름이 교착되지 않아야 한다
        // MEDIUM regression test: a configuration change while rationale is pending must not deadlock the flow
        shadowOf(application.packageManager)
            .setShouldShowRequestPermissionRationale(Manifest.permission.CAMERA, true)

        val restorationTester = StateRestorationTester(composeTestRule)
        var state: PermissionRequestState? = null
        restorationTester.setContent {
            state = rememberPermissionRequestState(listOf(Manifest.permission.CAMERA))
        }

        composeTestRule.runOnUiThread {
            state?.request { }
        }
        composeTestRule.waitForIdle()
        assertEquals(listOf(Manifest.permission.CAMERA), state?.rationaleRequired)

        // 구성 변경 에뮬레이션 — rationale 대기 상태와 대상 런타임 권한이 복원되어야 함
        // Emulate configuration change — the rationale pending state and target permissions must survive
        restorationTester.emulateSavedInstanceStateRestore()
        composeTestRule.waitForIdle()

        assertEquals(
            "복원 후에도 rationale 대기 상태가 유지되어야 합니다",
            listOf(Manifest.permission.CAMERA),
            state?.rationaleRequired,
        )

        // 복원된 상태에서 continueRequest → 시스템 다이얼로그 실행 (교착 없음)
        // continueRequest on the restored state → the system dialog launches (no deadlock)
        composeTestRule.runOnUiThread {
            state?.continueRequest()
        }
        composeTestRule.waitForIdle()

        assertTrue(state?.rationaleRequired.orEmpty().isEmpty())
        assertNotNull(shadowOf(composeTestRule.activity).lastRequestedPermission)
    }

    @Test
    fun `PERMANENTLY_DENIED decision survives continueRequest after restoration`() {
        // 적대적 리뷰 3-a 회귀 테스트: 복원 후 continueRequest로 새로 띄운 다이얼로그의 결과는
        // isRestored 보호 없이 판정되어야 한다 (보호 유지 시 PERMANENTLY_DENIED → DENIED 다운그레이드)
        // Adversarial-review 3-a regression: results of a dialog freshly launched via
        // continueRequest after restoration must be mapped without the isRestored guard
        val shadowPm = shadowOf(application.packageManager)
        shadowPm.setShouldShowRequestPermissionRationale(Manifest.permission.CAMERA, true)

        val restorationTester = StateRestorationTester(composeTestRule)
        var state: PermissionRequestState? = null
        restorationTester.setContent {
            state = rememberPermissionRequestState(listOf(Manifest.permission.CAMERA))
        }

        // 1차 요청: rationale → continue → 거부 (요청 이력 기록)
        // First request: rationale → continue → denial (records the request history)
        composeTestRule.runOnUiThread { state?.request { } }
        composeTestRule.waitForIdle()
        composeTestRule.runOnUiThread { state?.continueRequest() }
        composeTestRule.waitForIdle()
        deliverRuntimeResult(granted = false)
        composeTestRule.waitForIdle()

        // 2차 요청: rationale 대기 중 회전
        // Second request: configuration change while rationale is pending
        composeTestRule.runOnUiThread { state?.request { } }
        composeTestRule.waitForIdle()
        assertEquals(listOf(Manifest.permission.CAMERA), state?.rationaleRequired)
        restorationTester.emulateSavedInstanceStateRestore()
        composeTestRule.waitForIdle()

        // 복원된 상태에서 continue → "다시 묻지 않음" 거부 시뮬레이션 (rationale=false)
        // Continue on the restored state → simulate "don't ask again" denial (rationale=false)
        composeTestRule.runOnUiThread { state?.continueRequest() }
        composeTestRule.waitForIdle()
        shadowPm.setShouldShowRequestPermissionRationale(Manifest.permission.CAMERA, false)
        deliverRuntimeResult(granted = false)
        composeTestRule.waitForIdle()

        assertEquals(
            "복원 후 continueRequest의 새 결과는 PERMANENTLY_DENIED로 판정되어야 합니다",
            listOf(
                PermissionDeniedItem(
                    Manifest.permission.CAMERA,
                    PermissionDeniedType.PERMANENTLY_DENIED,
                ),
            ),
            state?.deniedItems,
        )
    }

    // -----------------------------------------------------------------------
    // 설정 화면 이동 동의 게이트 (gateSettingsNavigation = true)
    // Settings navigation consent gate (gateSettingsNavigation = true)
    // -----------------------------------------------------------------------

    @Test
    fun `settings navigation gate pauses before navigating and continueSettingsNavigation launches`() {
        lateinit var state: PermissionRequestState
        composeTestRule.setContent {
            state = rememberPermissionRequestState(
                listOf(Manifest.permission.SYSTEM_ALERT_WINDOW),
                gateSettingsNavigation = true,
            )
        }

        var result: List<PermissionDeniedItem>? = null
        composeTestRule.runOnUiThread {
            state.request { result = it }
        }
        composeTestRule.waitForIdle()

        // 일시 정지: 동의 게이트 상태만 노출되고 설정 화면은 실행되지 않음
        // Paused: only the consent gate state is exposed; the settings screen is not launched
        val shadowActivity = shadowOf(composeTestRule.activity)
        assertEquals(Manifest.permission.SYSTEM_ALERT_WINDOW, state.settingsNavigationRequired)
        assertNull(shadowActivity.nextStartedActivityForResult)
        assertNull(result)

        composeTestRule.runOnUiThread {
            state.continueSettingsNavigation()
        }
        composeTestRule.waitForIdle()

        assertNull(state.settingsNavigationRequired)
        val started = shadowActivity.nextStartedActivityForResult
        assertNotNull(started)
        assertEquals(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, started.intent.action)

        // 설정 복귀 — 여전히 미허용 → DENIED
        // Return from settings — still off → DENIED
        composeTestRule.runOnUiThread {
            shadowActivity.receiveResult(started.intent, Activity.RESULT_CANCELED, null)
        }
        composeTestRule.waitForIdle()
        assertEquals(
            listOf(
                PermissionDeniedItem(
                    Manifest.permission.SYSTEM_ALERT_WINDOW,
                    PermissionDeniedType.DENIED,
                ),
            ),
            result,
        )
    }

    @Test
    fun `cancelSettingsNavigation denies only that permission without navigating`() {
        lateinit var state: PermissionRequestState
        composeTestRule.setContent {
            state = rememberPermissionRequestState(
                listOf(Manifest.permission.SYSTEM_ALERT_WINDOW),
                gateSettingsNavigation = true,
            )
        }

        var result: List<PermissionDeniedItem>? = null
        composeTestRule.runOnUiThread {
            state.request { result = it }
        }
        composeTestRule.waitForIdle()
        assertEquals(Manifest.permission.SYSTEM_ALERT_WINDOW, state.settingsNavigationRequired)

        composeTestRule.runOnUiThread {
            state.cancelSettingsNavigation()
        }
        composeTestRule.waitForIdle()

        // 설정 화면 미실행 + 해당 권한만 DENIED (simple_xml의 onNavigateToSettings cancel과 동일 의미)
        // No settings launch + only that permission DENIED (same semantics as xml's cancel)
        assertNull(shadowOf(composeTestRule.activity).nextStartedActivityForResult)
        assertEquals(
            listOf(
                PermissionDeniedItem(
                    Manifest.permission.SYSTEM_ALERT_WINDOW,
                    PermissionDeniedType.DENIED,
                ),
            ),
            result,
        )
        assertNull(state.settingsNavigationRequired)
    }

    // -----------------------------------------------------------------------
    // 혼합(런타임 + 특수) 요청 / Role / launcher 실패 / 다이얼로그 표시 중 회전
    // Mixed (runtime + special) request / Role / launcher failure / rotation mid-dialog
    // -----------------------------------------------------------------------

    @Test
    fun `mixed runtime and special request processes sequentially and aggregates both denials`() {
        lateinit var state: PermissionRequestState
        composeTestRule.setContent {
            state = rememberPermissionRequestState(
                listOf(Manifest.permission.CAMERA, Manifest.permission.SYSTEM_ALERT_WINDOW),
            )
        }

        var result: List<PermissionDeniedItem>? = null
        composeTestRule.runOnUiThread {
            state.request { result = it }
        }
        composeTestRule.waitForIdle()

        // 1단계: 런타임 다이얼로그 먼저 — 특수 권한 설정 화면은 아직 미실행
        // (Robolectric에서는 런타임 요청 자체가 startActivityForResult로 기록되므로 먼저 소비)
        // Stage 1: the runtime dialog comes first — the settings screen is not launched yet
        // (On Robolectric the runtime request itself is recorded via startActivityForResult)
        val shadowActivity = shadowOf(composeTestRule.activity)
        assertNotNull(shadowActivity.lastRequestedPermission)
        val runtimeStarted = shadowActivity.nextStartedActivityForResult
        assertNotNull(runtimeStarted)
        assertNull(
            "런타임 결과 전에 설정 화면이 실행되면 안 됩니다",
            shadowActivity.nextStartedActivityForResult,
        )

        deliverRuntimeResult(granted = false)
        composeTestRule.waitForIdle()

        // 2단계: 런타임 결과 처리 후 특수 권한 설정 화면 실행
        // Stage 2: after the runtime result, the settings screen launches
        val started = shadowActivity.nextStartedActivityForResult
        assertNotNull(started)
        composeTestRule.runOnUiThread {
            shadowActivity.receiveResult(started.intent, Activity.RESULT_CANCELED, null)
        }
        composeTestRule.waitForIdle()

        assertEquals(
            listOf(
                PermissionDeniedItem(Manifest.permission.CAMERA, PermissionDeniedType.DENIED),
                PermissionDeniedItem(
                    Manifest.permission.SYSTEM_ALERT_WINDOW,
                    PermissionDeniedType.DENIED,
                ),
            ),
            result,
        )
    }

    @Test
    fun `unavailable role completes with NOT_SUPPORTED without any navigation`() {
        val role = "android.app.role.BROWSER"
        lateinit var state: PermissionRequestState
        composeTestRule.setContent {
            state = rememberPermissionRequestState(listOf(role))
        }

        var result: List<PermissionDeniedItem>? = null
        composeTestRule.runOnUiThread {
            state.request { result = it }
        }
        composeTestRule.waitForIdle()

        assertEquals(
            listOf(PermissionDeniedItem(role, PermissionDeniedType.NOT_SUPPORTED)),
            result,
        )
        val shadowActivity = shadowOf(composeTestRule.activity)
        assertNull(shadowActivity.lastRequestedPermission)
        assertNull(shadowActivity.nextStartedActivityForResult)
    }

    @Test
    fun `runtime request without a launcher completes with LIFECYCLE_NOT_READY`() {
        // launcher 미주입 상태를 직접 생성해 실행 실패 경로를 검증 (컴포지션 외부 사용 시나리오)
        // Directly construct a state without launchers to verify the launch-failure path
        val state = PermissionRequestState(
            context = application,
            activity = null,
            permissions = listOf(Manifest.permission.CAMERA),
        )

        var result: List<PermissionDeniedItem>? = null
        composeTestRule.runOnUiThread {
            state.request { result = it }
        }
        composeTestRule.waitForIdle()

        assertEquals(
            listOf(
                PermissionDeniedItem(
                    Manifest.permission.CAMERA,
                    PermissionDeniedType.LIFECYCLE_NOT_READY,
                ),
            ),
            result,
        )
    }

    @Test
    fun `settings request without a launcher completes with FAILED_TO_LAUNCH_SETTINGS`() {
        val state = PermissionRequestState(
            context = application,
            activity = null,
            permissions = listOf(Manifest.permission.SYSTEM_ALERT_WINDOW),
        )

        var result: List<PermissionDeniedItem>? = null
        composeTestRule.runOnUiThread {
            state.request { result = it }
        }
        composeTestRule.waitForIdle()

        assertEquals(
            listOf(
                PermissionDeniedItem(
                    Manifest.permission.SYSTEM_ALERT_WINDOW,
                    PermissionDeniedType.FAILED_TO_LAUNCH_SETTINGS,
                ),
            ),
            result,
        )
    }

    @Test
    fun `runtime result delivered after restoration mid-dialog updates state without deadlock`() {
        // 적대적 리뷰 테스트 공백 보강: 다이얼로그 표시 중 회전 → 복원 후 결과 재전달 →
        // 콜백 없이 State만 갱신(isRestored 보호로 DENIED)되고 흐름이 교착되지 않아야 한다
        // Coverage gap fix: rotation while the dialog is showing → result redelivered after
        // restoration → only the State updates (DENIED under the isRestored guard), no deadlock
        val restorationTester = StateRestorationTester(composeTestRule)
        var state: PermissionRequestState? = null
        restorationTester.setContent {
            state = rememberPermissionRequestState(listOf(Manifest.permission.CAMERA))
        }

        var callbackResult: List<PermissionDeniedItem>? = null
        composeTestRule.runOnUiThread {
            state?.request { callbackResult = it }
        }
        composeTestRule.waitForIdle()
        assertNotNull(shadowOf(composeTestRule.activity).lastRequestedPermission)

        // 다이얼로그 표시 중 구성 변경
        // Configuration change while the dialog is showing
        restorationTester.emulateSavedInstanceStateRestore()
        composeTestRule.waitForIdle()

        deliverRuntimeResult(granted = false)
        composeTestRule.waitForIdle()

        // 콜백은 소실되고 State만 갱신 — 복원 결과는 isRestored 보호로 DENIED (PERMANENTLY 아님)
        // The callback is lost; only the State updates — DENIED under the isRestored guard
        assertNull(callbackResult)
        assertEquals(
            listOf(PermissionDeniedItem(Manifest.permission.CAMERA, PermissionDeniedType.DENIED)),
            state?.deniedItems,
        )

        // 교착 없음 — 후속 요청이 정상 진행되고 복원 이력 기반으로 PERMANENTLY_DENIED 판정
        // No deadlock — a subsequent request proceeds and resolves to PERMANENTLY_DENIED
        var secondResult: List<PermissionDeniedItem>? = null
        composeTestRule.runOnUiThread {
            state?.request { secondResult = it }
        }
        composeTestRule.waitForIdle()
        deliverRuntimeResult(granted = false)
        composeTestRule.waitForIdle()
        assertEquals(
            listOf(
                PermissionDeniedItem(
                    Manifest.permission.CAMERA,
                    PermissionDeniedType.PERMANENTLY_DENIED,
                ),
            ),
            secondResult,
        )
    }

    // -----------------------------------------------------------------------
    // 테스트 헬퍼
    // Test helpers
    // -----------------------------------------------------------------------

    /**
     * 마지막 런타임 권한 요청에 대해 플랫폼 결과를 전달합니다.<br>
     * Delivers the platform result for the last runtime permission request.<br>
     *
     * 실제 결과 전달 경로(dispatchActivityResult)와 달리 onRequestPermissionsResult 직접 호출은
     * Activity 내부의 mHasCurrentPermissionsRequest 플래그를 리셋하지 않으므로,
     * 후속 requestPermissions가 빈 배열 취소 콜백을 받지 않도록 리플렉션으로 플래그를 리셋합니다.<br>
     * Unlike the real delivery path (dispatchActivityResult), calling onRequestPermissionsResult
     * directly does not reset Activity's internal mHasCurrentPermissionsRequest flag, so the flag
     * is reset via reflection to keep subsequent requestPermissions calls from receiving an
     * empty-array cancellation callback.<br>
     */
    private fun deliverRuntimeResult(granted: Boolean) {
        val shadowActivity = shadowOf(composeTestRule.activity)
        val request = shadowActivity.lastRequestedPermission
        assertNotNull("런타임 권한 요청이 실행되지 않았습니다 / runtime request was not launched", request)
        val grantResult =
            if (granted) PackageManager.PERMISSION_GRANTED else PackageManager.PERMISSION_DENIED
        composeTestRule.runOnUiThread {
            composeTestRule.activity.onRequestPermissionsResult(
                request.requestCode,
                request.requestedPermissions,
                IntArray(request.requestedPermissions.size) { grantResult },
            )
            Activity::class.java.getDeclaredField("mHasCurrentPermissionsRequest").apply {
                isAccessible = true
                setBoolean(composeTestRule.activity, false)
            }
        }
    }
}
