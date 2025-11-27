package kr.open.library.simpleui_xml.activity_fragment

import androidx.lifecycle.LifecycleOwner
import kr.open.library.simple_ui.core.logcat.Logx
import kr.open.library.simple_ui.core.viewmodel.BaseViewModelEvent

class ActivityFragmentActivityVm : BaseViewModelEvent<ActivityFragmentActivityVmEvent>() {

    fun onClickBaseActivity() = sendEventVm(ActivityFragmentActivityVmEvent.OnClickBaseActivity)

    fun onClickBaseBindingActivity() = sendEventVm(ActivityFragmentActivityVmEvent.OnClickBaseBindingActivity)

    fun onClickBaseFragment() = sendEventVm(ActivityFragmentActivityVmEvent.OnClickBaseFragment)

    fun onClickBaseBindingFragment() = sendEventVm(ActivityFragmentActivityVmEvent.OnClickBaseBindingFragment)

    fun onClickBaseDialogFragment() = sendEventVm(ActivityFragmentActivityVmEvent.OnClickBaseDialogFragment)

    fun onClickBaseBindingDialogFragment() = sendEventVm(ActivityFragmentActivityVmEvent.OnClickBaseBindingDialogFragment)

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        Logx.d()
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        Logx.d()
    }

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
        Logx.d()
    }

    override fun onPause(owner: LifecycleOwner) {
        super.onPause(owner)
        Logx.d()
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        Logx.d()
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        Logx.d()
    }
}
