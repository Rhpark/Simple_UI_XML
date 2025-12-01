package kr.open.library.simpleui_xml.activity_fragment

sealed class ActivityFragmentActivityVmEvent {
    data object OnClickBaseActivity : ActivityFragmentActivityVmEvent()

    data object OnClickBaseBindingActivity : ActivityFragmentActivityVmEvent()

    data object OnClickBaseFragment : ActivityFragmentActivityVmEvent()

    data object OnClickBaseBindingFragment : ActivityFragmentActivityVmEvent()

    data object OnClickBaseDialogFragment : ActivityFragmentActivityVmEvent()

    data object OnClickBaseBindingDialogFragment : ActivityFragmentActivityVmEvent()
}
