package kr.open.library.simpleui_xml.deviceverification.harness

import android.app.KeyguardManager
import android.os.PowerManager
import android.os.SystemClock
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.AssumptionViolatedException
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import org.junit.runners.model.MultipleFailureException

/**
 * Prepares an interactive physical device before each test and restores shared state afterward.<br><br>
 * 각 테스트 전에 실기기를 조작 가능한 상태로 준비하고 종료 후 공통 상태를 복원합니다.<br>
 */
internal class PhysicalDeviceRule : TestWatcher() {
    private val targetContext
        get() = InstrumentationRegistry.getInstrumentation().targetContext

    lateinit var environment: DeviceEnvironmentSnapshot
        private set

    val cleanup: DeviceStateCleanup by lazy {
        DeviceStateCleanup(targetContext)
    }

    override fun starting(description: Description) {
        ensureInteractiveDevice()
        environment = DeviceEnvironment.capture(targetContext)
        if (environment.apiLevel < 28) {
            throw AssumptionViolatedException(
                "minSdk 28 미만 단말에서는 실행할 수 없습니다: ${environment.asDiagnosticText()}",
            )
        }
    }

    override fun finished(description: Description) {
        MultipleFailureException.assertEmpty(cleanup.runAll())
    }

    /**
     * Fails with device diagnostics when a scenario precondition is not satisfied.<br><br>
     * 시나리오 사전 조건이 충족되지 않으면 단말 진단 정보를 포함해 실패 처리합니다.<br>
     *
     * @param scenarioId Scenario ID being verified.<br><br>
     *                   검증 시나리오 ID입니다.<br>
     * @param expected Expected precondition state.<br><br>
     *                 기대한 사전 상태입니다.<br>
     * @param actual Actual precondition state.<br><br>
     *               실제 사전 상태입니다.<br>
     * @param isSatisfied Whether the precondition is satisfied.<br><br>
     *                    사전 조건 충족 여부입니다.<br>
     */
    fun requirePrecondition(
        scenarioId: String,
        expected: String,
        actual: String,
        isSatisfied: Boolean,
    ) {
        if (!isSatisfied) {
            throw AssertionError(
                "$scenarioId 사전 조건 불일치: expected=$expected, actual=$actual, " +
                    environment.asDiagnosticText(),
            )
        }
    }

    private fun ensureInteractiveDevice() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val automation = instrumentation.uiAutomation
        val powerManager = targetContext.getSystemService(PowerManager::class.java)
        val keyguardManager = targetContext.getSystemService(KeyguardManager::class.java)
        val displayMetrics = targetContext.resources.displayMetrics
        val centerX = displayMetrics.widthPixels / 2
        val swipeStartY = displayMetrics.heightPixels * 3 / 4
        val swipeEndY = displayMetrics.heightPixels / 4
        val swipeCommand =
            "input swipe $centerX $swipeStartY $centerX $swipeEndY $SWIPE_DURATION_MILLIS"

        repeat(INTERACTIVE_MAX_RETRIES) {
            automation.executeShellCommand("input keyevent KEYCODE_WAKEUP").close()
            automation.executeShellCommand("wm dismiss-keyguard").close()
            automation.executeShellCommand(swipeCommand).close()
            SystemClock.sleep(INTERACTIVE_RETRY_INTERVAL_MILLIS)

            if (powerManager.isInteractive && !keyguardManager.isKeyguardLocked) return
        }

        error("실기기 화면이 꺼져 있거나 잠겨 있습니다. 화면을 켜고 잠금을 해제해 주세요.")
    }

    private companion object {
        const val INTERACTIVE_MAX_RETRIES = 10
        const val INTERACTIVE_RETRY_INTERVAL_MILLIS = 500L
        const val SWIPE_DURATION_MILLIS = 300L
    }
}
