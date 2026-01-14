package kr.open.library.simpleui_xml.activity_fragment.fragment

import android.os.Bundle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch
import kr.open.library.simple_ui.core.logcat.Logx
import kr.open.library.simple_ui.xml.extensions.view.snackBarShowShort
import kr.open.library.simple_ui.xml.ui.components.fragment.binding.BaseDataBindingFragment
import kr.open.library.simpleui_xml.R
import kr.open.library.simpleui_xml.databinding.FragmentBaseBindingExampleBinding

class BaseBindingFragmentExample : BaseDataBindingFragment<FragmentBaseBindingExampleBinding>(R.layout.fragment_base_binding_example) {
    private val vm: BaseBindingFragmentExampleVm by lazy { getViewModel() }

    override fun onCreateView(binding: FragmentBaseBindingExampleBinding, savedInstanceState: Bundle?) {
        super.onCreateView(binding, savedInstanceState)
        Logx.d("BaseBindingFragmentExample - onViewCreated() called")

        binding.vm = vm
        lifecycle.addObserver(vm)

        // Counter 값을 UI에 업데이트
        viewLifecycleOwner.lifecycleScope.launch {
            vm.counter.collect { count ->
                binding.tvCounter.text = "Count: $count"
            }
        }
    }

    override fun onEventVmCollect(binding: FragmentBaseBindingExampleBinding) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.mEventVm.collect { event ->
                    when (event) {
                        is BaseBindingFragmentExampleVmEvent.ShowMessage -> {
                            getBinding().root.snackBarShowShort(event.message)
                            Logx.d("Fragment Event: ${event.message}")
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        Logx.d("BaseBindingFragmentExample - onDestroyView() called")
        super.onDestroyView()
    }
}
