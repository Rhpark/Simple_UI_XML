package kr.open.library.simple_ui.presenter.ui.adapter.normal.simple

import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.ViewDataBinding
import kr.open.library.simple_ui.presenter.ui.adapter.normal.base.BaseRcvAdapter
import kr.open.library.simple_ui.presenter.ui.adapter.viewholder.BaseBindingRcvViewHolder

public open class SimpleBindingRcvAdapter<ITEM : Any, BINDING : ViewDataBinding>(
    @LayoutRes private val layoutRes: Int,
    private val onBind: (BaseBindingRcvViewHolder<BINDING>, ITEM, position: Int) -> Unit
) : BaseRcvAdapter<ITEM, BaseBindingRcvViewHolder<BINDING>>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int)
            : BaseBindingRcvViewHolder<BINDING> = BaseBindingRcvViewHolder(layoutRes, parent)

    override fun onBindViewHolder(holder: BaseBindingRcvViewHolder<BINDING>, position: Int, item: ITEM) {
        onBind(holder, item, position)
    }
}