package kr.open.library.simpleui_xml.activity_fragment.activity

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kr.open.library.simple_ui.core.viewmodel.BaseViewModelEvent

class BaseBindingActivityExampleVm : BaseViewModelEvent<BaseBindingActivityExampleVmEvent>() {
    private val _counter = MutableStateFlow(0)
    val counter: StateFlow<Int> = _counter.asStateFlow()

    fun increment() {
        viewModelScope.launch {
            _counter.value++
            sendEventVm(BaseBindingActivityExampleVmEvent.ShowMessage("Count: ${_counter.value}"))
        }
    }

    fun decrement() {
        viewModelScope.launch {
            _counter.value--
            sendEventVm(BaseBindingActivityExampleVmEvent.ShowMessage("Count: ${_counter.value}"))
        }
    }

    fun reset() {
        viewModelScope.launch {
            _counter.value = 0
            sendEventVm(BaseBindingActivityExampleVmEvent.ShowMessage("Counter Reset!"))
        }
    }
}
