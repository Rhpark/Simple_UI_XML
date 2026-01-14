package kr.open.library.simpleui_xml.activity_fragment.dialog

import android.os.Bundle
import android.view.View
import kr.open.library.simple_ui.core.logcat.Logx
import kr.open.library.simple_ui.xml.ui.components.dialog.binding.BaseDataBindingDialogFragment
import kr.open.library.simpleui_xml.R
import kr.open.library.simpleui_xml.databinding.DialogBaseBindingDialogFragmentBinding

class BaseBindingDialogFragmentExample :
    BaseDataBindingDialogFragment<DialogBaseBindingDialogFragmentBinding>(R.layout.dialog_base_binding_dialog_fragment) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Logx.d("BaseBindingDialogFragmentExample - onViewCreated() called")

        // resizeDialog 호출 - 화면의 85% 너비, 60% 높이
        resizeDialog(0.85f, 0.6f)
        Logx.d("BaseBindingDialogFragmentExample - resizeDialog(0.85f, 0.6f) called")
    }

    override fun onCreateView(binding: DialogBaseBindingDialogFragmentBinding, savedInstanceState: Bundle?) {
        // DataBinding으로 데이터 설정
        binding.tvDialogTitle.text = "BaseBindingDialogFragment Example"
        binding.tvDialogContent.text =
            "This dialog uses:\n\n" +
            "✓ BaseBindingDialogFragment\n" +
            "✓ DataBinding\n" +
            "✓ resizeDialog(0.85f, 0.6f)\n\n" +
            "Width: 85% of screen\n" +
            "Height: 60% of screen"

        // OK 버튼
        binding.btnOk.setOnClickListener {
            Logx.d("BaseBindingDialogFragmentExample - OK button clicked")
            safeDismiss()
        }

        // Cancel 버튼
        binding.btnCancel.setOnClickListener {
            Logx.d("BaseBindingDialogFragmentExample - Cancel button clicked")
            safeDismiss()
        }
    }
}
