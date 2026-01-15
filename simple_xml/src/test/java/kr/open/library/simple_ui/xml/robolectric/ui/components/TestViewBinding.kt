package kr.open.library.simple_ui.xml.robolectric.ui.components

import android.view.View
import androidx.viewbinding.ViewBinding

class TestViewBinding(
    private val rootView: View
) : ViewBinding {
    override fun getRoot(): View = rootView
}
