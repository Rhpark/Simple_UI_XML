package kr.open.library.simpleui_xml.permission

import android.Manifest
import android.os.Bundle
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kr.open.library.simple_ui.core.logcat.Logx
import kr.open.library.simple_ui.xml.extensions.view.SnackBarOption
import kr.open.library.simple_ui.xml.extensions.view.snackBarMakeShort
import kr.open.library.simple_ui.xml.extensions.view.snackBarShowShort
import kr.open.library.simple_ui.xml.ui.adapter.normal.simple.SimpleRcvAdapter
import kr.open.library.simple_ui.xml.ui.components.activity.binding.BaseDataBindingActivity
import kr.open.library.simpleui_xml.R
import kr.open.library.simpleui_xml.databinding.ActivityPermissionsBinding

/**
 * Permission manual test cases for this screen.<br><br>
 * 이 화면의 권한 수동 테스트 케이스입니다.<br>
 *
 * **Manual test cases / 수동 테스트 케이스:**<br>
 * - Special-only request: turn off "Draw over other apps", request SYSTEM_ALERT_WINDOW, and verify onDeniedResult is called after returning from Settings.<br>
 * - Runtime + special: keep WRITE_EXTERNAL_STORAGE granted, turn off SYSTEM_ALERT_WINDOW, request both, and verify the callback runs only after returning.<br>
 * - Home exit: move to Settings, press Home, return to the app, and verify the result reflects the state at return time.<br>
 * - Allow then return: enable SYSTEM_ALERT_WINDOW in Settings and verify deniedResults is empty on return.<br><br>
 * - 특수 권한 단일 요청: "다른 앱 위에 표시"를 OFF로 만들고 SYSTEM_ALERT_WINDOW 요청 후 설정 화면 복귀 시점에 onDeniedResult가 호출되는지 확인한다.<br>
 * - 런타임+특수 조합: WRITE_EXTERNAL_STORAGE가 승인된 상태에서 SYSTEM_ALERT_WINDOW를 OFF로 만든 뒤 함께 요청하고, 복귀 이후에만 콜백이 호출되는지 확인한다.<br>
 * - 홈 버튼 이탈: 설정 화면 진입 후 홈으로 나갔다가 복귀했을 때, 복귀 시점 상태가 결과에 반영되는지 확인한다.<br>
 * - 허용 후 복귀: 설정에서 SYSTEM_ALERT_WINDOW를 허용한 뒤 복귀하면 deniedResults가 비어있는지 확인한다.<br>
 */
class PermissionsActivity : BaseDataBindingActivity<ActivityPermissionsBinding>(R.layout.activity_permissions) {
    private val vm: PermissionsActivityVm by lazy { getViewModel<PermissionsActivityVm>() }

    private val adapter = SimpleRcvAdapter<String>(R.layout.item_rcv_textview) { holder, item, position ->
        holder.findViewById<TextView>(R.id.tvTitle).text = item
    }.apply {
        setOnItemClickListener { i, s, view -> view.snackBarShowShort("OnClick $s") }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        getBinding().vm = vm
        lifecycle.addObserver(vm)
        getBinding().rcvPermission.adapter = adapter
    }

    override fun onEventVmCollect(binding: ActivityPermissionsBinding) {
        lifecycleScope.launch {
            vm.mEventVm.collect {
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
                                Manifest.permission.SYSTEM_ALERT_WINDOW,
                            ),
                        )
                    }
                    is PermissionsActivityVmEvent.OnClickPermissionsSpecialOnly -> {
                        permissions(listOf(Manifest.permission.SYSTEM_ALERT_WINDOW))
                    }
                    is PermissionsActivityVmEvent.OnClickPermissionsSpecialMulti -> {
                        permissions(
                            listOf(
                                Manifest.permission.SYSTEM_ALERT_WINDOW,
                                Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                            ),
                        )
                    }
                }
            }
        }
    }

    private fun permissions(permissions: List<String>) {
        requestPermissions(permissions) { deniedPermissions ->
            Logx.d("deniedPermissions $deniedPermissions")
            val msg = permissions.toString() +
                if (deniedPermissions.isEmpty()) {
                    "Permission is granted"
                } else {
                    "Permission denied $deniedPermissions"
                }

            getBinding().btnCameraPermission.snackBarMakeShort(msg, SnackBarOption(actionText = "Ok", action = {})).show()
            adapter.addItem(msg)
        }
    }
}
