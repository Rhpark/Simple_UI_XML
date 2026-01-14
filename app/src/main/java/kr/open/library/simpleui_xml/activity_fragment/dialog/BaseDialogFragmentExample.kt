package kr.open.library.simpleui_xml.activity_fragment.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import kr.open.library.simple_ui.core.logcat.Logx
import kr.open.library.simple_ui.xml.ui.components.dialog.normal.BaseDialogFragment
import kr.open.library.simpleui_xml.R

class BaseDialogFragmentExample : BaseDialogFragment(R.layout.dialog_base_dialog_fragment) {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        Logx.d("BaseDialogFragmentExample - onCreateView() called")
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        Logx.d("BaseDialogFragmentExample - onViewCreated() called")

        // resizeDialog 호출 - 화면의 80% 너비, 50% 높이
        resizeDialog(0.8f, null)
        Logx.d("BaseDialogFragmentExample - resizeDialog(0.8f, 0.5f) called")

        // UI 초기화
        view.findViewById<TextView>(R.id.tvDialogTitle).text = "BaseDialogFragment Example"
        view.findViewById<TextView>(R.id.tvDialogContent).text =
            "This dialog uses resizeDialog(0.8f, 0.5f)\n\n" +
            "Width: 80% of screen\n" +
            "Height: 50% of screen"

        // OK 버튼
        view.findViewById<Button>(R.id.btnOk).setOnClickListener {
            Logx.d("BaseDialogFragmentExample - OK button clicked")
            safeDismiss()
        }

        // Cancel 버튼
        view.findViewById<Button>(R.id.btnCancel).setOnClickListener {
            Logx.d("BaseDialogFragmentExample - Cancel button clicked")
            safeDismiss()
        }
    }

    override fun onDestroyView() {
        Logx.d("BaseDialogFragmentExample - onDestroyView() called")
        super.onDestroyView()
    }
}
