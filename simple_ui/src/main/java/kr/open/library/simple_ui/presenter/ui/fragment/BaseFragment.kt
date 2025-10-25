package kr.open.library.simple_ui.presenter.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes

abstract class BaseFragment(
    @LayoutRes private val layoutRes: Int,
    private val isAttachToParent: Boolean = false
) : RootFragment() {

    private var _rootView: View? = null
    protected val rootView: View
        get() = _rootView
            ?: throw IllegalStateException("View accessed after onDestroyView()")

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _rootView = inflater.inflate(layoutRes, container, isAttachToParent)
        return rootView
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _rootView = null
    }
}