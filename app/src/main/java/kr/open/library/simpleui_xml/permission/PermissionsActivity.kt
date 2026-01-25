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
 * ???붾㈃??沅뚰븳 ?섎룞 ?뚯뒪??耳?댁뒪?낅땲??<br>
 *
 * **Manual test cases / ?섎룞 ?뚯뒪??耳?댁뒪:**<br>
 * - Special-only request: turn off "Draw over other apps", request SYSTEM_ALERT_WINDOW, and verify onDeniedResult is called after returning from Settings.<br>
 * - Runtime + special: keep WRITE_EXTERNAL_STORAGE granted, turn off SYSTEM_ALERT_WINDOW, request both, and verify the callback runs only after returning.<br>
 * - Home exit: move to Settings, press Home, return to the app, and verify the result reflects the state at return time.<br>
 * - Allow then return: enable SYSTEM_ALERT_WINDOW in Settings and verify deniedResults is empty on return.<br><br>
 * - ?뱀닔 沅뚰븳 ?⑥씪 ?붿껌: "?ㅻⅨ ???꾩뿉 ?쒖떆"瑜?OFF濡?留뚮뱾怨?SYSTEM_ALERT_WINDOW ?붿껌 ???ㅼ젙 ?붾㈃ 蹂듦? ?쒖젏??onDeniedResult媛 ?몄텧?섎뒗吏 ?뺤씤?쒕떎.<br>
 * - ?고????뱀닔 議고빀: WRITE_EXTERNAL_STORAGE媛 ?뱀씤???곹깭?먯꽌 SYSTEM_ALERT_WINDOW瑜?OFF濡?留뚮뱺 ???④퍡 ?붿껌?섍퀬, 蹂듦? ?댄썑?먮쭔 肄쒕갚???몄텧?섎뒗吏 ?뺤씤?쒕떎.<br>
 * - ??踰꾪듉 ?댄깉: ?ㅼ젙 ?붾㈃ 吏꾩엯 ???덉쑝濡??섍컮?ㅺ? 蹂듦??덉쓣 ?? 蹂듦? ?쒖젏 ?곹깭媛 寃곌낵??諛섏쁺?섎뒗吏 ?뺤씤?쒕떎.<br>
 * - ?덉슜 ??蹂듦?: ?ㅼ젙?먯꽌 SYSTEM_ALERT_WINDOW瑜??덉슜????蹂듦??섎㈃ deniedResults媛 鍮꾩뼱?덈뒗吏 ?뺤씤?쒕떎.<br>
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
