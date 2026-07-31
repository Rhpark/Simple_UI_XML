package kr.open.library.simpleui.consumer

import kotlinx.coroutines.flow.Flow
import kr.open.library.simple_ui.core.viewmodel.BaseViewModelEvent

public class CoreConsumerViewModel : BaseViewModelEvent<String>() {
    public fun send(value: String) {
        sendEventVm(value)
    }
}

public fun observeCoreEvents(viewModel: CoreConsumerViewModel): Flow<String> = viewModel.eventVmFlow
