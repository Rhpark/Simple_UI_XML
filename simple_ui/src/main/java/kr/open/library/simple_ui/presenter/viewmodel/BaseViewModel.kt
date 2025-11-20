package kr.open.library.simple_ui.presenter.viewmodel

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.ViewModel

/**
 * Base ViewModel class that integrates Android Lifecycle observation capabilities.<br>
 * Extends ViewModel and implements DefaultLifecycleObserver to handle lifecycle events directly within the ViewModel.<br><br>
 * Android Lifecycle 관찰 기능을 통합한 기본 ViewModel 클래스입니다.<br>
 * ViewModel을 확장하고 DefaultLifecycleObserver를 구현하여 ViewModel 내에서 직접 생명주기 이벤트를 처리합니다.<br>
 *
 * Features:<br>
 * - Lifecycle-aware ViewModel base class<br>
 * - Direct lifecycle event handling without separate observer registration<br>
 * - Integration with Android Architecture Components<br><br>
 * 기능:<br>
 * - 생명주기 인식 ViewModel 기본 클래스<br>
 * - 별도의 옵저버 등록 없이 직접 생명주기 이벤트 처리<br>
 * - Android Architecture Components와의 통합<br>
 *
 * Usage example:<br>
 * ```kotlin
 * class MyViewModel : BaseViewModel() {
 *     override fun onCreate(owner: LifecycleOwner) {
 *         super.onCreate(owner)
 *         // Initialize resources when lifecycle owner is created
 *     }
 *
 *     override fun onResume(owner: LifecycleOwner) {
 *         super.onResume(owner)
 *         // Resume operations
 *     }
 *
 *     override fun onPause(owner: LifecycleOwner) {
 *         super.onPause(owner)
 *         // Pause operations
 *     }
 * }
 *
 * // In Activity or Fragment
 * class MyActivity : AppCompatActivity() {
 *     private val viewModel: MyViewModel by viewModels()
 *
 *     override fun onCreate(savedInstanceState: Bundle?) {
 *         super.onCreate(savedInstanceState)
 *         lifecycle.addObserver(viewModel)
 *     }
 * }
 * ```
 *
 * @see BaseViewModelEvent For ViewModel with event emission capability.<br><br>
 *      이벤트 발행 기능이 있는 ViewModel은 BaseViewModelEvent를 참조하세요.<br>
 */
public abstract class BaseViewModel : ViewModel(), DefaultLifecycleObserver {
    // Override lifecycle methods as needed
}
