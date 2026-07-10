package kr.open.library.simple_ui.compose.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.Flow
import kr.open.library.simple_ui.core.viewmodel.BaseViewModelEvent

// ---------------------------------------------------------------------------
// 공개 Composable API
// Public Composable API
// ---------------------------------------------------------------------------

/**
 * [BaseViewModelEvent]의 [eventVmFlow][BaseViewModelEvent.eventVmFlow]를 Compose 생명주기 인식 방식으로 수집합니다.<br>
 * Collects [BaseViewModelEvent.eventVmFlow] in a lifecycle-aware manner within Compose.<br>
 *
 * 내부적으로 [LaunchedEffect] + [repeatOnLifecycle]을 사용하여 [minActiveState] 이상일 때만 수집합니다.<br>
 * Internally uses [LaunchedEffect] + [repeatOnLifecycle] to collect only when the lifecycle is at least [minActiveState].<br>
 * [minActiveState]에는 [Lifecycle.State.CREATED], [Lifecycle.State.STARTED], [Lifecycle.State.RESUMED]만 사용합니다.<br>
 * Use only [Lifecycle.State.CREATED], [Lifecycle.State.STARTED], or [Lifecycle.State.RESUMED]
 * for [minActiveState].<br>
 *
 * **단일 소비자 제약 / Single-consumer constraint**:<br>
 * [BaseViewModelEvent.eventVmFlow]는 내부적으로 [Channel][kotlinx.coroutines.channels.Channel] 기반
 * (`receiveAsFlow`)이므로 **단일 소비자만 허용**됩니다.
 * 동일한 ViewModel 인스턴스에서 이 함수를 두 곳 이상에서 호출하면 이벤트가 누락될 수 있습니다.
 * 멀티캐스트가 필요하면 ViewModel 내부에서 `SharedFlow`로 전환하세요.<br>
 * [BaseViewModelEvent.eventVmFlow] is backed by a [Channel][kotlinx.coroutines.channels.Channel]
 * (`receiveAsFlow`), so it supports **only a single consumer**.
 * Calling this function from more than one location for the same ViewModel instance may cause events to be missed.
 * Switch to `SharedFlow` inside the ViewModel if multicast is needed.<br>
 *
 * @param minActiveState 수집을 시작·재개할 최소 생명주기 상태. 기본값은 [Lifecycle.State.STARTED].<br><br>
 *                       Minimum lifecycle state at which collection starts or resumes. Defaults to [Lifecycle.State.STARTED].<br>
 * @param onEvent 수집된 이벤트를 처리하는 suspend 람다.<br><br>
 *                Suspend lambda that handles each collected event.<br>
 */
@Composable
public fun <T> BaseViewModelEvent<T>.CollectVmEvent(
    minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
    onEvent: suspend (T) -> Unit,
) {
    eventVmFlow.CollectAsEffect(
        minActiveState = minActiveState,
        onEach = onEvent,
    )
}

/**
 * 임의의 [Flow]를 Compose 생명주기 인식 방식으로 수집합니다.<br>
 * Collects an arbitrary [Flow] in a lifecycle-aware manner within Compose.<br>
 *
 * 내부적으로 [LaunchedEffect] + [repeatOnLifecycle]을 사용하여 [minActiveState] 이상일 때만 수집합니다.<br>
 * Internally uses [LaunchedEffect] + [repeatOnLifecycle] to collect only when the lifecycle is at least [minActiveState].<br>
 *
 * 상태 바인딩용 `collectAsStateWithLifecycle`은 [androidx.lifecycle.compose] 패키지에서 이미 제공하므로
 * 이 함수는 화면 상태가 아닌 effect/event Flow 수집에 사용합니다.
 * [repeatOnLifecycle]은 라이프사이클 재진입 시 Flow를 다시 수집하므로, 단발 부수효과
 * (네비게이션·토스트·스낵바 등)에 사용할 때는 재수집 시 값을 다시 방출하지 않는 이벤트 Flow를
 * 전달해야 합니다. Cold Flow나 replay가 있는 SharedFlow는 효과를 다시 실행할 수 있습니다.<br>
 * Use this function for effect/event flows rather than screen-state binding, which is already
 * covered by `collectAsStateWithLifecycle` in [androidx.lifecycle.compose]. Because
 * [repeatOnLifecycle] re-collects when the lifecycle becomes active again, one-shot side effects
 * require an event flow that does not re-emit on collection. A cold Flow or a SharedFlow with
 * replay may execute the effect again.<br>
 *
 * [minActiveState]에는 [Lifecycle.State.CREATED], [Lifecycle.State.STARTED], [Lifecycle.State.RESUMED]만 사용합니다.<br>
 * Use only [Lifecycle.State.CREATED], [Lifecycle.State.STARTED], or [Lifecycle.State.RESUMED]
 * for [minActiveState].<br>
 *
 * @param minActiveState 수집을 시작·재개할 최소 생명주기 상태. 기본값은 [Lifecycle.State.STARTED].<br><br>
 *                       Minimum lifecycle state at which collection starts or resumes. Defaults to [Lifecycle.State.STARTED].<br>
 * @param onEach 각 방출 값을 처리하는 suspend 람다.<br><br>
 *               Suspend lambda invoked for each emitted value.<br>
 */
@Composable
public fun <T> Flow<T>.CollectAsEffect(
    minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
    onEach: suspend (T) -> Unit,
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    // 재구성으로 람다가 교체돼도 수집 재시작 없이 최신 람다를 사용하도록 참조만 갱신
    // Keep the latest lambda without restarting collection when recomposition replaces it
    val currentOnEach by rememberUpdatedState(onEach)
    LaunchedEffect(this, lifecycleOwner, minActiveState) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(minActiveState) {
            collect { currentOnEach(it) }
        }
    }
}
