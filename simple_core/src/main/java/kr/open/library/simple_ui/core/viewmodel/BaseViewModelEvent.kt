package kr.open.library.simple_ui.core.viewmodel

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/**
 * Base ViewModel class with event handling capability using Kotlin Flows.<br>
 * Provides a unidirectional communication channel from ViewModel to View (Activity, Fragment, or CustomView).<br><br>
 * Kotlin Flow를 사용한 이벤트 처리 기능을 갖춘 기본 ViewModel 클래스입니다.<br>
 * ViewModel에서 View(Activity, Fragment 또는 CustomView)로의 단방향 통신 채널을 제공합니다.<br>
 *
 * Features:<br>
 * - Type-safe event emission using generics<br>
 * - Buffered channel for reliable event delivery<br>
 * - Automatic channel cleanup on ViewModel destruction<br>
 * - Lifecycle-aware event observation<br><br>
 * 기능:<br>
 * - 제네릭을 사용한 타입 안전 이벤트 발행<br>
 * - 안정적인 이벤트 전달을 위한 버퍼링된 채널<br>
 * - ViewModel 소멸 시 자동 채널 정리<br>
 * - 생명주기 인식 이벤트 관찰<br>
 *
 * Usage example:<br>
 * ```kotlin
 * // Define event types
 * sealed class MyEvent {
 *     data class ShowToast(val message: String) : MyEvent()
 *     object NavigateToHome : MyEvent()
 *     data class ShowError(val error: Throwable) : MyEvent()
 * }
 *
 * // Create ViewModel
 * class MyViewModel : BaseViewModelEvent<MyEvent>() {
 *     fun onButtonClick() {
 *         sendEventVm(MyEvent.ShowToast("Button clicked!"))
 *     }
 *
 *     fun onLoginSuccess() {
 *         sendEventVm(MyEvent.NavigateToHome)
 *     }
 * }
 *
 * // Observe events in Activity/Fragment
 * class MyActivity : AppCompatActivity() {
 *     private val viewModel: MyViewModel by viewModels()
 *
 *     override fun onCreate(savedInstanceState: Bundle?) {
 *         super.onCreate(savedInstanceState)
 *
 *         lifecycleScope.launch {
 *             repeatOnLifecycle(Lifecycle.State.STARTED) {
 *                 viewModel.mEventVm.collect { event ->
 *                     when (event) {
 *                         is MyEvent.ShowToast -> showToast(event.message)
 *                         is MyEvent.NavigateToHome -> navigateToHome()
 *                         is MyEvent.ShowError -> showError(event.error)
 *                     }
 *                 }
 *             }
 *         }
 *     }
 * }
 * ```
 * @param EVENT_TYPE The type of events this ViewModel can emit. Sealed classes are recommended for type safety.<br><br>
 *                   이 ViewModel이 발행할 수 있는 이벤트 타입. 타입 안전성을 위해 sealed class를 권장합니다.<br>
 *
 * @see BaseViewModel For ViewModel without event handling capability.<br><br>
 *      이벤트 처리 기능이 없는 ViewModel은 BaseViewModel을 참조하세요.<br>
 */
public abstract class BaseViewModelEvent<EVENT_TYPE> : BaseViewModel() {
    /**
     * Private channel for sending events from ViewModel to View.<br>
     * Uses buffered channel to prevent event loss when collector is not ready.<br><br>
     * ViewModel에서 View로 이벤트를 보내기 위한 private 채널입니다.<br>
     * 수집기가 준비되지 않았을 때 이벤트 손실을 방지하기 위해 버퍼링된 채널을 사용합니다.<br>
     */
    private val eventVm = Channel<EVENT_TYPE>(Channel.BUFFERED)

    /**
     * Public Flow for observing events in View layer.<br>
     * Collect this flow in Activity, Fragment, or CustomView to receive events from ViewModel.<br><br>
     * View 레이어에서 이벤트를 관찰하기 위한 public Flow입니다.<br>
     * ViewModel로부터 이벤트를 받으려면 Activity, Fragment 또는 CustomView에서 이 flow를 수집하세요.<br>
     */
    public val mEventVm: Flow<EVENT_TYPE> = eventVm.receiveAsFlow()

    /**
     * Sends an event to the View layer through the event channel.<br>
     * Uses viewModelScope to launch a coroutine for sending the event.<br><br>
     * 이벤트 채널을 통해 View 레이어로 이벤트를 보냅니다.<br>
     * viewModelScope를 사용하여 이벤트 전송을 위한 코루틴을 실행합니다.<br>
     *
     * @param event The event to send to the View.<br><br>
     *              View로 보낼 이벤트.<br>
     */
    protected fun sendEventVm(event: EVENT_TYPE) {
        viewModelScope.launch { eventVm.send(event) }
    }

    /**
     * Called when this ViewModel is no longer used and will be destroyed.<br>
     * Closes the event channel to prevent memory leaks and ensure proper resource cleanup.<br><br>
     * 이 ViewModel이 더 이상 사용되지 않고 소멸될 때 호출됩니다.<br>
     * 메모리 누수를 방지하고 적절한 리소스 정리를 보장하기 위해 이벤트 채널을 닫습니다.<br>
     */
    override fun onCleared() {
        super.onCleared()
        eventVm.close()
    }
}
