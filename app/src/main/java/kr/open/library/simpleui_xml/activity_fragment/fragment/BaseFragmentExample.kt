package kr.open.library.simpleui_xml.activity_fragment.fragment

import android.graphics.Color
import android.os.Bundle
import android.view.View
import kr.open.library.simple_ui.core.logcat.Logx
import kr.open.library.simple_ui.xml.extensions.view.snackBarShowShort
import kr.open.library.simple_ui.xml.ui.components.fragment.normal.BaseFragment
import kr.open.library.simpleui_xml.R
import kotlin.random.Random

class BaseFragmentExample : BaseFragment(R.layout.fragment_base_example) {
    override fun onCreateView(rootView: View, savedInstanceState: Bundle?) {
        Logx.d("BaseFragmentExample - onCreateView() called")
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        Logx.d("BaseFragmentExample - onViewCreated() called")

        // rootView 클릭 리스너 - 배경색 랜덤 변경
        getRootView().setOnClickListener {
            val randomColor =
                Color.rgb(
                    Random.nextInt(256),
                    Random.nextInt(256),
                    Random.nextInt(256),
                )
            getRootView().setBackgroundColor(randomColor)
            getRootView().snackBarShowShort("Background color changed!")
            Logx.d(
                "BaseFragmentExample - rootView clicked, color changed to RGB(${
                    Color.red(
                        randomColor,
                    )
                }, ${Color.green(randomColor)}, ${Color.blue(randomColor)})",
            )
        }
    }

    override fun onDestroyView() {
        Logx.d("BaseFragmentExample - onDestroyView() called")
        super.onDestroyView()
    }
}
