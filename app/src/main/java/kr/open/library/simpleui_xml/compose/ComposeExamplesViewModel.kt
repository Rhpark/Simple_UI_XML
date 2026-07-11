package kr.open.library.simpleui_xml.compose

import kr.open.library.simple_ui.core.viewmodel.BaseViewModelEvent

class ComposeExamplesViewModel : BaseViewModelEvent<Int>() {
    private var nextSequence = 0

    fun sendNextEvent() {
        nextSequence += 1
        sendEventVm(nextSequence)
    }
}
