package kr.open.library.simple_ui.xml.ui.temp.base.list

import androidx.recyclerview.widget.DiffUtil
import androidx.viewbinding.ViewBinding
import kr.open.library.simple_ui.xml.ui.temp.base.list.diffcallback.DefaultDiffCallback
import kr.open.library.simple_ui.xml.ui.temp.viewholder.binding.BaseViewBindingViewHolder

abstract class BaseListViewBindingAdapterCore<ITEM : Any, BINDING : ViewBinding>(
    diffCallback: DiffUtil.ItemCallback<ITEM> = DefaultDiffCallback(),
) : RootListAdapterCore<ITEM, BaseViewBindingViewHolder<BINDING>>(diffCallback) {
    abstract fun getCreateViewHolder(binding: BINDING): BaseViewBindingViewHolder<BINDING>
}
