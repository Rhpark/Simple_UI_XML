package kr.open.library.simpleui_xml.activity_fragment.activity

import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kr.open.library.simple_ui.core.logcat.Logx
import kr.open.library.simple_ui.xml.extensions.view.snackBarShowShort
import kr.open.library.simple_ui.xml.ui.components.activity.binding.BaseDataBindingActivity
import kr.open.library.simpleui_xml.R
import kr.open.library.simpleui_xml.databinding.ActivityBaseBindingActivityExampleBinding

class BaseBindingActivityExample :
    BaseDataBindingActivity<ActivityBaseBindingActivityExampleBinding>(R.layout.activity_base_binding_activity_example) {
    private val vm: BaseBindingActivityExampleVm by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        getBinding().vm = vm
        lifecycle.addObserver(vm)
    }

    override fun onEventVmCollect(binding: ActivityBaseBindingActivityExampleBinding) {
        lifecycleScope.launch {
            vm.mEventVm.collect { event ->
                when (event) {
                    is BaseBindingActivityExampleVmEvent.ShowMessage -> {
                        getBinding().root.snackBarShowShort(event.message)
                        Logx.d("Event: ${event.message}")
                    }
                }
            }
        }

        // Counter 값을 UI에 업데이트
        lifecycleScope.launch {
            vm.counter.collect { count ->
                getBinding().tvCounter.text = "Count: $count"
            }
        }
    }
}
