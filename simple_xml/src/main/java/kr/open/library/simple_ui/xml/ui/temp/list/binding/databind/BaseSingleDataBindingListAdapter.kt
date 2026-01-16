package kr.open.library.simple_ui.xml.ui.temp.list.binding.databind

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.DiffUtil
import kr.open.library.simple_ui.xml.ui.temp.base.list.BaseListDataBindingAdapterCore
import kr.open.library.simple_ui.xml.ui.temp.base.list.diffcallback.DefaultDiffCallback
import kr.open.library.simple_ui.xml.ui.temp.viewholder.binding.BaseDataBindingViewHolder

abstract class BaseSingleDataBindingListAdapter<ITEM : Any, BINDING : ViewDataBinding>(
    @LayoutRes private val layoutRes: Int,
    diffCallback: DiffUtil.ItemCallback<ITEM> = DefaultDiffCallback(),
) : BaseListDataBindingAdapterCore<ITEM, BINDING>(diffCallback) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseDataBindingViewHolder<BINDING> {
        val inflater = LayoutInflater.from(parent.context)
        val binding: BINDING = DataBindingUtil.inflate(inflater, layoutRes, parent, false)
        return getCreateViewHolder(binding)
    }

    final override fun onBindItem(holder: BaseDataBindingViewHolder<BINDING>, position: Int, item: ITEM) {
        onBind(holder, item, position)
    }

    protected abstract fun onBind(holder: BaseDataBindingViewHolder<BINDING>, item: ITEM, position: Int)
}
