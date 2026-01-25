package kr.open.library.simpleui_xml.system_service_manager.controller.softkeyboard

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Job
import kr.open.library.simple_ui.xml.extensions.view.toastShowShort
import kr.open.library.simple_ui.xml.system_manager.extensions.getSoftKeyboardController
import kr.open.library.simple_ui.xml.ui.components.activity.normal.BaseActivity
import kr.open.library.simpleui_xml.R
import kr.open.library.simpleui_xml.databinding.ActivitySoftkeyboardControllerBinding

/**
 * Sample activity demonstrating SoftKeyboardController usage.<br><br>
 * SoftKeyboardController ?ъ슜踰뺤쓣 蹂댁뿬二쇰뒗 ?섑뵆 ?≫떚鍮꾪떚?낅땲??<br>
 */
class SoftKeyboardControllerActivity : BaseActivity(R.layout.activity_softkeyboard_controller) {
    private lateinit var binding: ActivitySoftkeyboardControllerBinding

    private val softKeyboardController by lazy { getSoftKeyboardController() }

    // Store Job references for potential cancellation
    // 痍⑥냼 媛?μ꽦???꾪븳 Job 李몄“ ???
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
            // Basic show - 湲곕낯 ?쒖떆
            btnShow.setOnClickListener {
                edtTest.isFocusable = true
                softKeyboardController.show(edtTest)
                toastShowShort("Show keyboard")
            }

            // Basic hide - 湲곕낯 ?④?
            btnHide.setOnClickListener {
                softKeyboardController.hide(edtTest)
                toastShowShort("Hide keyboard")
            }

            // Delayed show with Job (cancellable) - Job?쇰줈 吏???쒖떆 (痍⑥냼 媛??
            btnShowDelay.setOnClickListener {
                edtTest.isFocusable = true
                // Cancel previous job if exists - ?댁쟾 ?묒뾽???덉쑝硫?痍⑥냼
                showDelayJob?.cancel()
                // Launch new delayed show - ?덈줈??吏???쒖떆 ?ㅽ뻾
                showDelayJob = softKeyboardController.showDelay(edtTest, 300, coroutineScope = lifecycleScope)
                toastShowShort("Show keyboard with 300ms delay")
            }

            // Delayed hide with Job (cancellable) - Job?쇰줈 吏???④? (痍⑥냼 媛??
            btnHideDelay.setOnClickListener {
                // Cancel previous job if exists - ?댁쟾 ?묒뾽???덉쑝硫?痍⑥냼
                hideDelayJob?.cancel()
                // Launch new delayed hide - ?덈줈??吏???④? ?ㅽ뻾
                hideDelayJob = softKeyboardController.hideDelay(edtTest, 300, coroutineScope = lifecycleScope)
                toastShowShort("Hide keyboard with 300ms delay")
            }

            // Window mode: Adjust Pan - ?덈룄??紐⑤뱶: Adjust Pan
            btnSetAdjustPan.setOnClickListener {
                softKeyboardController.setAdjustPan(window)
                toastShowShort("Set Adjust Pan Mode")
            }

            // Window mode: Adjust Resize - ?덈룄??紐⑤뱶: Adjust Resize
            btnSetAdjustResize.setOnClickListener {
                softKeyboardController.setAdjustResize(window)
                toastShowShort("Set Adjust Resize Mode")
            }
        }
    }

    override fun onDestroy() {
        // Cancel pending jobs on destroy - 醫낅즺 ???湲?以묒씤 ?묒뾽 痍⑥냼
        showDelayJob?.cancel()
        hideDelayJob?.cancel()
        super.onDestroy()
    }
}
