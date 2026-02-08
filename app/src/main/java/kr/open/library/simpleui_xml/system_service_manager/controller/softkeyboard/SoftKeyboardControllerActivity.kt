package kr.open.library.simpleui_xml.system_service_manager.controller.softkeyboard

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.launch
import kr.open.library.simple_ui.xml.extensions.view.toastShowShort
import kr.open.library.simple_ui.xml.system_manager.controller.softkeyboard.SoftKeyboardActionResult
import kr.open.library.simple_ui.xml.system_manager.controller.softkeyboard.SoftKeyboardResizePolicy
import kr.open.library.simple_ui.xml.system_manager.extensions.getSoftKeyboardController
import kr.open.library.simple_ui.xml.ui.components.activity.normal.BaseActivity
import kr.open.library.simpleui_xml.R
import kr.open.library.simpleui_xml.databinding.ActivitySoftkeyboardControllerBinding

/**
 * Sample activity demonstrating SoftKeyboardController usage.<br><br>
 * SoftKeyboardController 사용 예제를 보여주는 샘플 액티비티입니다.<br>
 */
class SoftKeyboardControllerActivity : BaseActivity(R.layout.activity_softkeyboard_controller) {
    private lateinit var binding: ActivitySoftkeyboardControllerBinding

    private val softKeyboardController by lazy { getSoftKeyboardController() }

    // Store Deferred references for cancellation and result observation
    // 취소와 결과 관찰을 위해 Deferred 참조를 보관합니다.
    private var showDelayDeferred: Deferred<SoftKeyboardActionResult>? = null
    private var hideDelayDeferred: Deferred<SoftKeyboardActionResult>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySoftkeyboardControllerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initListener()
    }

    private fun initListener() {
        binding.run {
            // Basic show - 기본 표시
            btnShow.setOnClickListener {
                edtTest.isFocusable = true
                val success = softKeyboardController.show(edtTest)
                toastShowShort(if (success) "키보드 표시 요청 성공" else "키보드 표시 요청 실패")
            }

            // Basic hide - 기본 숨김
            btnHide.setOnClickListener {
                val success = softKeyboardController.hide(edtTest)
                toastShowShort(if (success) "키보드 숨김 요청 성공" else "키보드 숨김 요청 실패")
            }

            // Delayed show with actual result - 실제 결과를 받는 지연 표시
            btnShowDelay.setOnClickListener {
                edtTest.isFocusable = true
                showDelayDeferred?.cancel()
                val deferred = softKeyboardController.showAwaitAsync(
                    v = edtTest,
                    coroutineScope = lifecycleScope,
                    delayMillis = 300,
                )
                showDelayDeferred = deferred

                lifecycleScope.launch {
                    val result = deferred.await()
                    toastShowShort("지연 표시 결과: ${result.toSummaryText()}")
                }
            }

            // Delayed hide with actual result - 실제 결과를 받는 지연 숨김
            btnHideDelay.setOnClickListener {
                hideDelayDeferred?.cancel()
                val deferred = softKeyboardController.hideAwaitAsync(
                    v = edtTest,
                    coroutineScope = lifecycleScope,
                    delayMillis = 300,
                )
                hideDelayDeferred = deferred

                lifecycleScope.launch {
                    val result = deferred?.await() ?: return@launch
                    toastShowShort("지연 숨김 결과: ${result.toSummaryText()}")
                }
            }

            // Window mode: Adjust Pan - 창 모드: Adjust Pan
            btnSetAdjustPan.setOnClickListener {
                softKeyboardController.setAdjustPan(window)
                toastShowShort("Set Adjust Pan Mode")
            }

            // Window mode: Adjust Resize - 창 모드: Adjust Resize
            btnSetAdjustResize.setOnClickListener {
                val success = softKeyboardController.configureImeResize(
                    window = window,
                    policy = SoftKeyboardResizePolicy.KEEP_CURRENT_WINDOW,
                )
                toastShowShort(if (success) "Resize 정책 적용 성공" else "Resize 정책 적용 실패")
            }
        }
    }

    override fun onDestroy() {
        // Cancel pending deferred tasks on destroy - onDestroy 시 대기 중인 작업 취소
        showDelayDeferred?.cancel()
        hideDelayDeferred?.cancel()
        super.onDestroy()
    }

    private fun SoftKeyboardActionResult.toSummaryText(): String = when (this) {
        SoftKeyboardActionResult.Success -> "성공"
        SoftKeyboardActionResult.Timeout -> "타임아웃"
        is SoftKeyboardActionResult.Failure -> "실패(${reason.name})"
    }
}
