package kr.open.library.simpleui_xml.activity_fragment.activity

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kr.open.library.simple_ui.core.logcat.Logx
import kr.open.library.simple_ui.xml.extensions.view.snackBarShowShort
import kr.open.library.simple_ui.xml.ui.activity.BaseBindingActivity
import kr.open.library.simpleui_xml.R
import kr.open.library.simpleui_xml.databinding.ActivityBaseBindingActivityExampleBinding

class BaseBindingActivityExample :
    BaseBindingActivity<ActivityBaseBindingActivityExampleBinding>(R.layout.activity_base_binding_activity_example) {
    private val vm: BaseBindingActivityExampleVm by viewModels()

    override fun onCreateView(
        rootView: View,
        savedInstanceState: Bundle?,
    ) {
        super.onCreateView(rootView, savedInstanceState)
        Logx.d("BaseBindingActivityExample - onCreateView() called")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.vm = vm
        lifecycle.addObserver(vm)

        // Counter 값을 UI에 업데이트
        lifecycleScope.launch {
            vm.counter.collect { count ->
                binding.tvCounter.text = "Count: $count"
            }
        }

        // ViewModel 이벤트 수집 시작 (필요 시 직접 호출)
        // BaseBindingActivity는 eventVmCollect()를 자동 호출하지 않습니다.
        eventVmCollect()

        Logx.d("BaseBindingActivityExample - onCreate() completed")
    }

    override fun eventVmCollect() {
        lifecycleScope.launch {
            vm.mEventVm.collect { event ->
                when (event) {
                    is BaseBindingActivityExampleVmEvent.ShowMessage -> {
                        binding.root.snackBarShowShort(event.message)
                        Logx.d("Event: ${event.message}")
                    }
                }
            }
        }
    }
}
