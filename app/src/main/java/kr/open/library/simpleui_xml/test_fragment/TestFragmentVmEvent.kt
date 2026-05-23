package kr.open.library.simpleui_xml.test_fragment

sealed interface TestFragmentVmEvent {
    data object Dump : TestFragmentVmEvent
}
