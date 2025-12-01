package kr.open.library.simpleui_xml.activity_fragment.fragment

sealed class BaseBindingFragmentExampleVmEvent {
    data class ShowMessage(
        val message: String,
    ) : BaseBindingFragmentExampleVmEvent()
}
