package kr.open.library.simpleui_xml.activity_fragment

import kr.open.library.simple_ui.presenter.viewmodel.BaseViewModelEvent

class ActivityFragmentActivityVm : BaseViewModelEvent<ActivityFragmentActivityVmEvent>() {

    fun onClickBaseActivity() = sendEventVm(ActivityFragmentActivityVmEvent.OnClickBaseActivity)

    fun onClickBaseBindingActivity() = sendEventVm(ActivityFragmentActivityVmEvent.OnClickBaseBindingActivity)

    fun onClickBaseFragment() = sendEventVm(ActivityFragmentActivityVmEvent.OnClickBaseFragment)

    fun onClickBaseBindingFragment() = sendEventVm(ActivityFragmentActivityVmEvent.OnClickBaseBindingFragment)

    fun onClickBaseDialogFragment() = sendEventVm(ActivityFragmentActivityVmEvent.OnClickBaseDialogFragment)

    fun onClickBaseBindingDialogFragment() = sendEventVm(ActivityFragmentActivityVmEvent.OnClickBaseBindingDialogFragment)
}
