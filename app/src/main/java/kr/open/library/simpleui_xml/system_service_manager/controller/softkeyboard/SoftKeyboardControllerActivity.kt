package kr.open.library.simpleui_xml.system_service_manager.controller.softkeyboard

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Job
import kr.open.library.simple_ui.xml.extensions.view.toastShowShort
import kr.open.library.simple_ui.xml.system_manager.extensions.getSoftKeyboardController
import kr.open.library.simple_ui.xml.ui.activity.BaseActivity
import kr.open.library.simpleui_xml.R
import kr.open.library.simpleui_xml.databinding.ActivitySoftkeyboardControllerBinding

/**
 * Sample activity demonstrating SoftKeyboardController usage.<br><br>
 * SoftKeyboardController 사용법을 보여주는 샘플 액티비티입니다.<br>
 */
class SoftKeyboardControllerActivity : BaseActivity(R.layout.activity_softkeyboard_controller) {
    private lateinit var binding: ActivitySoftkeyboardControllerBinding

    private val softKeyboardController by lazy { getSoftKeyboardController() }

    // Store Job references for potential cancellation
    // 취소 가능성을 위한 Job 참조 저장
    private var showDelayJob: Job? = null
    private var hideDelayJob: Job? = null

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
                softKeyboardController.show(edtTest)
                toastShowShort("Show keyboard")
            }

            // Basic hide - 기본 숨김
            btnHide.setOnClickListener {
                softKeyboardController.hide(edtTest)
                toastShowShort("Hide keyboard")
            }

            // Delayed show with Job (cancellable) - Job으로 지연 표시 (취소 가능)
            btnShowDelay.setOnClickListener {
                edtTest.isFocusable = true
                // Cancel previous job if exists - 이전 작업이 있으면 취소
                showDelayJob?.cancel()
                // Launch new delayed show - 새로운 지연 표시 실행
                showDelayJob = softKeyboardController.showDelay(edtTest, 300, coroutineScope = lifecycleScope)
                toastShowShort("Show keyboard with 300ms delay")
            }

            // Delayed hide with Job (cancellable) - Job으로 지연 숨김 (취소 가능)
            btnHideDelay.setOnClickListener {
                // Cancel previous job if exists - 이전 작업이 있으면 취소
                hideDelayJob?.cancel()
                // Launch new delayed hide - 새로운 지연 숨김 실행
                hideDelayJob = softKeyboardController.hideDelay(edtTest, 300, coroutineScope = lifecycleScope)
                toastShowShort("Hide keyboard with 300ms delay")
            }

            // Window mode: Adjust Pan - 윈도우 모드: Adjust Pan
            btnSetAdjustPan.setOnClickListener {
                softKeyboardController.setAdjustPan(window)
                toastShowShort("Set Adjust Pan Mode")
            }

            // Window mode: Adjust Resize - 윈도우 모드: Adjust Resize
            btnSetAdjustResize.setOnClickListener {
                softKeyboardController.setAdjustResize(window)
                toastShowShort("Set Adjust Resize Mode")
            }
        }
    }

    override fun onDestroy() {
        // Cancel pending jobs on destroy - 종료 시 대기 중인 작업 취소
        showDelayJob?.cancel()
        hideDelayJob?.cancel()
        super.onDestroy()
    }
}
