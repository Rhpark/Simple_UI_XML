package kr.open.library.simple_ui.xml.ui.temp.base.list

import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.DiffUtil
import kr.open.library.simple_ui.xml.ui.temp.base.list.diffcallback.DefaultDiffCallback
import kr.open.library.simple_ui.xml.ui.temp.viewholder.binding.BaseDataBindingViewHolder

abstract class BaseListDataBindingAdapterCore<ITEM : Any, BINDING : ViewDataBinding>(
    diffCallback: DiffUtil.ItemCallback<ITEM> = DefaultDiffCallback(),
) : RootListAdapterCore<ITEM, BaseDataBindingViewHolder<BINDING>>(diffCallback) {
    abstract fun getCreateViewHolder(binding: BINDING): BaseDataBindingViewHolder<BINDING>
}
