package kr.open.library.simpleui_xml.activity_fragment.activity

sealed class BaseBindingActivityExampleVmEvent {
    data class ShowMessage(
        val message: String,
    ) : BaseBindingActivityExampleVmEvent()
}
