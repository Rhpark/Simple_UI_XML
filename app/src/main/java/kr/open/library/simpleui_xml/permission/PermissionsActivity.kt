package kr.open.library.simpleui_xml.permission

import android.Manifest
import android.os.Bundle
import android.widget.TextView
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kr.open.library.simple_ui.logcat.Logx
import kr.open.library.simple_ui.presenter.extensions.view.SnackBarOption
import kr.open.library.simple_ui.presenter.extensions.view.snackBarMakeShort
import kr.open.library.simple_ui.presenter.extensions.view.snackBarShowShort
import kr.open.library.simple_ui.presenter.ui.activity.BaseBindingActivity
import kr.open.library.simple_ui.presenter.ui.adapter.normal.simple.SimpleRcvAdapter
import kr.open.library.simpleui_xml.R
import kr.open.library.simpleui_xml.databinding.ActivityPermissionsBinding

class PermissionsActivity : BaseBindingActivity<ActivityPermissionsBinding>(R.layout.activity_permissions) {

    private val vm :PermissionsActivityVm by lazy { getViewModel<PermissionsActivityVm>() }

    private val adapter = SimpleRcvAdapter<String>(R.layout.item_rcv_textview) {
        holder, item, position -> holder.findViewById<TextView>(R.id.tvTitle).text = item
    }.apply {
        setOnItemClickListener { i, s, view -> view.snackBarShowShort("OnClick ${s}") }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.vm = vm
        lifecycle.addObserver(vm)
        binding.rcvPermission.adapter = adapter
        eventVmCollect()
    }

    override fun eventVmCollect() {
        lifecycleScope.launch {
            vm.mEventVm.collect{
                when (it) {
                    is PermissionsActivityVmEvent.OnClickPermissionsCamera -> {
                        permissions(listOf(Manifest.permission.CAMERA))
                    }
                    is PermissionsActivityVmEvent.OnClickPermissionsLocation -> {
                        permissions(listOf(Manifest.permission.ACCESS_FINE_LOCATION))
                    }
                    is PermissionsActivityVmEvent.OnClickPermissionsMulti -> {
                        permissions(
                            listOf(
                                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.SYSTEM_ALERT_WINDOW
                            )
                        )
                    }
                }
            }
        }
    }

    private fun permissions(permissions: List<String>) {
        onRequestPermissions(permissions) { deniedPermissions ->
            Logx.d("deniedPermissions $deniedPermissions")
            val msg = permissions.toString() + (if (deniedPermissions.isEmpty()) { "Permission is granted" }
                                                 else { "Permission denied $deniedPermissions" })

            binding.btnCameraPermission.snackBarMakeShort(msg,SnackBarOption(actionText = "Ok", action = {})).show()
            adapter.addItem(msg)
        }
    }
}
