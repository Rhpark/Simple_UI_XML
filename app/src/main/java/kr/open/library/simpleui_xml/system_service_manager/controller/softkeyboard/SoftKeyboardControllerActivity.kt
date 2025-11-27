package kr.open.library.simpleui_xml.system_service_manager.controller.softkeyboard

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import kr.open.library.simple_ui.xml.extensions.view.toastShort
import kr.open.library.simple_ui.xml.ui.activity.BaseActivity
import kr.open.library.simple_ui.xml.system_manager.extensions.getSoftKeyboardController
import kr.open.library.simpleui_xml.R
import kr.open.library.simpleui_xml.databinding.ActivitySoftkeyboardControllerBinding

class SoftKeyboardControllerActivity : BaseActivity(R.layout.activity_softkeyboard_controller) {

    private lateinit var binding: ActivitySoftkeyboardControllerBinding

    private val softKeyboardController by lazy { getSoftKeyboardController() }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySoftkeyboardControllerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initListener()
    }

    private fun initListener() {
        binding.run {
            btnShow.setOnClickListener {
                edtTest.isFocusable = true
                softKeyboardController.show(edtTest)
                toastShort("Show keyboard")
            }

            btnHide.setOnClickListener {
                softKeyboardController.hide(edtTest)
                toastShort("Hide keyboard")
            }

            btnShowDelay.setOnClickListener {
                edtTest.isFocusable = true
                softKeyboardController.showDelay(edtTest, 300, coroutineScope = lifecycleScope)
                toastShort("Show keyboard with 300ms delay")
            }

            btnHideDelay.setOnClickListener {
                softKeyboardController.hideDelay(edtTest, 300, coroutineScope = lifecycleScope)
                toastShort("Hide keyboard with 300ms delay")
            }

            btnSetAdjustPan.setOnClickListener {
                softKeyboardController.setAdjustPan(window)
                toastShort("Set Adjust Pan Mode")
            }

            btnSetAdjustResize.setOnClickListener {
                softKeyboardController.setAdjustResize(window)
                toastShort("Set Adjust Resize Mode")
            }
        }
    }
}
