package kr.open.library.simple_ui.compose.robolectric.state

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.onStart
import kr.open.library.simple_ui.compose.state.CollectAsEffect
import kr.open.library.simple_ui.compose.state.CollectVmEvent
import kr.open.library.simple_ui.core.viewmodel.BaseViewModelEvent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Robolectric + createComposeRule 기반 VmEventCollect Composable 통합 테스트.<br>
 * Integration tests for VmEventCollect Composables using Robolectric and createComposeRule.<br>
 *
 * 이벤트 수신, CollectAsEffect 동작, 단일 소비자 수집을 검증합니다.<br>
 * Verifies event reception, CollectAsEffect behavior, and single-consumer collection.<br>
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class VmEventCollectRobolectricTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    // -----------------------------------------------------------------------
    // 테스트용 ViewModel
    // Test ViewModel
    // -----------------------------------------------------------------------

    private class TestStringViewModel : BaseViewModelEvent<String>() {
        fun emit(value: String) = sendEventVm(value)
    }

    private class TestIntViewModel : BaseViewModelEvent<Int>() {
        fun emit(value: Int) = sendEventVm(value)
    }

    private class ManualLifecycleOwner : LifecycleOwner {
        private val registry = LifecycleRegistry(this)

        override val lifecycle: Lifecycle
            get() = registry

        fun moveTo(state: Lifecycle.State) {
            registry.currentState = state
        }
    }

    // -----------------------------------------------------------------------
    // CollectVmEvent — 이벤트 수신 검증
    // CollectVmEvent — event reception
    // -----------------------------------------------------------------------

    @Test
    fun `CollectVmEvent receives single emitted event`() {
        val vm = TestStringViewModel()
        val received = mutableStateOf<String?>(null)

        composeTestRule.setContent {
            vm.CollectVmEvent { event ->
                received.value = event
            }
        }

        composeTestRule.runOnIdle {
            vm.emit("hello")
        }

        composeTestRule.waitUntil(timeoutMillis = 3_000) { received.value != null }
        assertEquals("hello", received.value)
    }

    @Test
    fun `CollectVmEvent receives multiple events in order`() {
        val vm = TestIntViewModel()
        val received = mutableListOf<Int>()

        composeTestRule.setContent {
            vm.CollectVmEvent { value ->
                received.add(value)
            }
        }

        composeTestRule.runOnIdle {
            vm.emit(1)
            vm.emit(2)
            vm.emit(3)
        }

        composeTestRule.waitUntil(timeoutMillis = 3_000) { received.size == 3 }
        assertEquals(listOf(1, 2, 3), received)
    }

    // -----------------------------------------------------------------------
    // CollectAsEffect — Flow emit 수신 검증
    // CollectAsEffect — Flow emission reception
    // -----------------------------------------------------------------------

    @Test
    fun `CollectAsEffect receives single flow emission`() {
        val sharedFlow = MutableSharedFlow<String>(extraBufferCapacity = 10)
        val received = mutableStateOf<String?>(null)

        composeTestRule.setContent {
            sharedFlow.CollectAsEffect { value ->
                received.value = value
            }
        }

        composeTestRule.runOnIdle {
            sharedFlow.tryEmit("flow-event")
        }

        composeTestRule.waitUntil(timeoutMillis = 3_000) { received.value != null }
        assertEquals("flow-event", received.value)
    }

    @Test
    fun `CollectAsEffect accumulates multiple flow emissions in order`() {
        val sharedFlow = MutableSharedFlow<Int>(extraBufferCapacity = 10)
        val received = mutableListOf<Int>()

        composeTestRule.setContent {
            sharedFlow.CollectAsEffect { value ->
                received.add(value)
            }
        }

        composeTestRule.runOnIdle {
            sharedFlow.tryEmit(10)
            sharedFlow.tryEmit(20)
            sharedFlow.tryEmit(30)
        }

        composeTestRule.waitUntil(timeoutMillis = 3_000) { received.size == 3 }
        assertEquals(listOf(10, 20, 30), received)
    }

    // -----------------------------------------------------------------------
    // 라이프사이클 STARTED 미만 상태에서 수집 중단 검증
    // Lifecycle below STARTED — collection does not start
    // -----------------------------------------------------------------------

    @Test
    fun `CollectAsEffect does not collect before composition is active`() {
        // Compose UI 테스트에서 setContent 전에 emit하면 수집 불가
        // Emitting before setContent makes collection impossible
        val sharedFlow = MutableSharedFlow<String>(extraBufferCapacity = 0)
        val received = mutableStateOf<String?>(null)

        // emit이 먼저, 이후 setContent — 버퍼가 0이므로 수집 안 됨을 검증
        // Emit first, then setContent — buffer=0, so nothing is received
        sharedFlow.tryEmit("before-composition")

        composeTestRule.setContent {
            sharedFlow.CollectAsEffect { value ->
                received.value = value
            }
        }

        composeTestRule.waitForIdle()

        // 버퍼 없는 SharedFlow에서 수집 시작 전 emit은 미수신
        // Value emitted before collector was active is not received with no-buffer SharedFlow
        assertNull(received.value)
    }

    @Test
    fun `CollectAsEffect with RESUMED minActiveState collects after lifecycle reaches RESUMED`() {
        val sharedFlow = MutableSharedFlow<String>(extraBufferCapacity = 10)
        val received = mutableStateOf<String?>(null)

        // RESUMED 상태 요구 — 기본 createComposeRule 환경은 RESUMED까지 도달
        // Require RESUMED — default createComposeRule environment reaches RESUMED
        composeTestRule.setContent {
            sharedFlow.CollectAsEffect(
                minActiveState = Lifecycle.State.RESUMED,
            ) { value ->
                received.value = value
            }
        }

        composeTestRule.runOnIdle {
            sharedFlow.tryEmit("resumed-event")
        }

        composeTestRule.waitUntil(timeoutMillis = 3_000) { received.value != null }
        assertEquals("resumed-event", received.value)
    }

    @Test
    fun `CollectAsEffect stops below STARTED and restarts when lifecycle becomes active`() {
        val sharedFlow = MutableSharedFlow<String>(extraBufferCapacity = 1)
        val received = mutableListOf<String>()
        val lifecycleOwner = ManualLifecycleOwner()
        composeTestRule.runOnIdle {
            lifecycleOwner.moveTo(Lifecycle.State.CREATED)
        }

        composeTestRule.setContent {
            CompositionLocalProvider(LocalLifecycleOwner provides lifecycleOwner) {
                sharedFlow.CollectAsEffect { value ->
                    received += value
                }
            }
        }
        composeTestRule.waitForIdle()
        assertEquals(0, sharedFlow.subscriptionCount.value)

        composeTestRule.runOnIdle {
            lifecycleOwner.moveTo(Lifecycle.State.STARTED)
        }
        composeTestRule.waitUntil(timeoutMillis = 3_000) {
            sharedFlow.subscriptionCount.value == 1
        }
        composeTestRule.runOnIdle {
            sharedFlow.tryEmit("started")
        }
        composeTestRule.waitUntil(timeoutMillis = 3_000) {
            received == listOf("started")
        }

        composeTestRule.runOnIdle {
            lifecycleOwner.moveTo(Lifecycle.State.CREATED)
        }
        composeTestRule.waitUntil(timeoutMillis = 3_000) {
            sharedFlow.subscriptionCount.value == 0
        }
        composeTestRule.runOnIdle {
            sharedFlow.tryEmit("stopped")
        }
        composeTestRule.waitForIdle()
        assertEquals(listOf("started"), received)

        composeTestRule.runOnIdle {
            lifecycleOwner.moveTo(Lifecycle.State.STARTED)
        }
        composeTestRule.waitUntil(timeoutMillis = 3_000) {
            sharedFlow.subscriptionCount.value == 1
        }
        composeTestRule.runOnIdle {
            sharedFlow.tryEmit("restarted")
        }
        composeTestRule.waitUntil(timeoutMillis = 3_000) {
            received == listOf("started", "restarted")
        }
    }

    @Test
    fun `CollectAsEffect uses the latest callback without restarting collection`() {
        val sharedFlow = MutableSharedFlow<String>(extraBufferCapacity = 1)
        var collectionStarts = 0
        val trackedFlow = sharedFlow.onStart { collectionStarts++ }
        val firstCallbackValues = mutableListOf<String>()
        val secondCallbackValues = mutableListOf<String>()
        val useSecondCallback = mutableStateOf(false)

        composeTestRule.setContent {
            val useSecond = useSecondCallback.value
            trackedFlow.CollectAsEffect { value ->
                if (useSecond) {
                    secondCallbackValues += value
                } else {
                    firstCallbackValues += value
                }
            }
        }
        composeTestRule.waitUntil(timeoutMillis = 3_000) {
            sharedFlow.subscriptionCount.value == 1
        }

        composeTestRule.runOnIdle {
            useSecondCallback.value = true
        }
        composeTestRule.waitForIdle()
        composeTestRule.runOnIdle {
            sharedFlow.tryEmit("latest")
        }
        composeTestRule.waitUntil(timeoutMillis = 3_000) {
            secondCallbackValues == listOf("latest")
        }

        assertTrue(firstCallbackValues.isEmpty())
        assertEquals(1, collectionStarts)
    }
}
