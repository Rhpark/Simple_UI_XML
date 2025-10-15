package kr.open.library.simple_ui.presenter.ui.fragment.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes

public abstract class BaseDialogFragment(
    @LayoutRes private val layoutRes: Int,
    private val isAttachToParent: Boolean = false
) : RootDialogFragment() {

    protected lateinit var rootView: View
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(layoutRes, container, isAttachToParent)
        return rootView
    }
}