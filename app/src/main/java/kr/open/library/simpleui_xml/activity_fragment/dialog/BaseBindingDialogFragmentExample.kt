package kr.open.library.simpleui_xml.activity_fragment.dialog

import android.os.Bundle
import android.view.View
import kr.open.library.simple_ui.logcat.Logx
import kr.open.library.simple_ui.presenter.ui.fragment.dialog.BaseBindingDialogFragment
import kr.open.library.simpleui_xml.R
import kr.open.library.simpleui_xml.databinding.DialogBaseBindingDialogFragmentBinding

class BaseBindingDialogFragmentExample : BaseBindingDialogFragment<DialogBaseBindingDialogFragmentBinding>(R.layout.dialog_base_binding_dialog_fragment) {

    override fun afterOnCreateView(rootView: View, savedInstanceState: Bundle?) {
        super.afterOnCreateView(rootView, savedInstanceState)
        // 주의: 메서드명 오타 (afterOnCreateView - Crate not Create)
        Logx.d("BaseBindingDialogFragmentExample - afterOnCreateView() called (typo: Crate not Create)")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Logx.d("BaseBindingDialogFragmentExample - onViewCreated() called")

        // resizeDialog 호출 - 화면의 85% 너비, 60% 높이
        resizeDialog(0.85f, 0.6f)
        Logx.d("BaseBindingDialogFragmentExample - resizeDialog(0.85f, 0.6f) called")

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

    override fun onDestroyView() {
        Logx.d("BaseBindingDialogFragmentExample - onDestroyView() called")
        super.onDestroyView()
    }
}
