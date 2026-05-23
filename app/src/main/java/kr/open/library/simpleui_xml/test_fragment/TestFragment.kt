package kr.open.library.simpleui_xml.test_fragment

import android.os.Bundle
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch
import kr.open.library.simple_ui.xml.ui.components.fragment.binding.BaseDataBindingFragment
import kr.open.library.simpleui_xml.R
import kr.open.library.simpleui_xml.databinding.FragmentTestBinding

class TestFragment : BaseDataBindingFragment<FragmentTestBinding>(R.layout.fragment_test) {
    private val vm: TestFragmentVm by viewModels()

    override fun onBindingCreated(binding: FragmentTestBinding, savedInstanceState: Bundle?) {
        binding.vm = vm
        lifecycle.addObserver(vm)
    }

    override fun onEventVmCollect(binding: FragmentTestBinding) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.eventVmFlow.collect { event ->
                    when (event) {
                        is TestFragmentVmEvent.Dump -> { }
                    }
                }
            }
        }
    }
}
