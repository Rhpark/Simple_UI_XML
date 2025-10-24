package kr.open.library.simpleui_xml.activity_fragment

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kr.open.library.simple_ui.presenter.ui.activity.BaseBindingActivity
import kr.open.library.simpleui_xml.R
import kr.open.library.simpleui_xml.activity_fragment.activity.BaseActivityExample
import kr.open.library.simpleui_xml.activity_fragment.activity.BaseBindingActivityExample
import kr.open.library.simpleui_xml.activity_fragment.dialog.BaseBindingDialogFragmentExample
import kr.open.library.simpleui_xml.activity_fragment.dialog.BaseDialogFragmentExample
import kr.open.library.simpleui_xml.activity_fragment.fragment.FragmentContainerActivity
import kr.open.library.simpleui_xml.databinding.ActivityActivityFragmentBinding

class ActivityFragmentActivity : BaseBindingActivity<ActivityActivityFragmentBinding>(R.layout.activity_activity_fragment) {

    private val vm: ActivityFragmentActivityVm by viewModels()

    companion object {
        const val BASE_FRAGMENT = 1
        const val BASE_BINDING_FRAGMENT = 2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.vm = vm
        lifecycle.addObserver(vm)
        eventVmCollect()
    }

    override fun eventVmCollect() {
        lifecycleScope.launch {
            vm.mEventVm.collect { event ->
                when (event) {
                    is ActivityFragmentActivityVmEvent.OnClickBaseActivity -> {
                        startActivity(Intent(this@ActivityFragmentActivity, BaseActivityExample::class.java))
                    }
                    is ActivityFragmentActivityVmEvent.OnClickBaseBindingActivity -> {
                        startActivity(Intent(this@ActivityFragmentActivity, BaseBindingActivityExample::class.java))
                    }
                    is ActivityFragmentActivityVmEvent.OnClickBaseFragment -> {
                        val intent = Intent(this@ActivityFragmentActivity, FragmentContainerActivity::class.java)
                        intent.putExtra("FRAGMENT_TYPE", BASE_FRAGMENT)
                        startActivity(intent)
                    }
                    is ActivityFragmentActivityVmEvent.OnClickBaseBindingFragment -> {
                        val intent = Intent(this@ActivityFragmentActivity, FragmentContainerActivity::class.java)
                        intent.putExtra("FRAGMENT_TYPE", BASE_BINDING_FRAGMENT)
                        startActivity(intent)
                    }
                    is ActivityFragmentActivityVmEvent.OnClickBaseDialogFragment -> {
                        BaseDialogFragmentExample().safeShow(supportFragmentManager, "BaseDialogFragment")
                    }
                    is ActivityFragmentActivityVmEvent.OnClickBaseBindingDialogFragment -> {
                        BaseBindingDialogFragmentExample().safeShow(supportFragmentManager, "BaseBindingDialogFragment")
                    }
                }
            }
        }
    }
}
