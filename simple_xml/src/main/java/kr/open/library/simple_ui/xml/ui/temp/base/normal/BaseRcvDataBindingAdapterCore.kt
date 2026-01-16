package kr.open.library.simple_ui.xml.ui.temp.base.normal

import androidx.databinding.ViewDataBinding
import kr.open.library.simple_ui.xml.ui.temp.viewholder.binding.BaseDataBindingViewHolder

abstract class BaseRcvDataBindingAdapterCore<ITEM : Any, BINDING : ViewDataBinding>(
    diffUtilEnabled: Boolean = false
) : RootRcvAdapterCore<ITEM, BaseDataBindingViewHolder<BINDING>>(diffUtilEnabled) {
    abstract fun getCreateViewHolder(binding: BINDING): BaseDataBindingViewHolder<BINDING>
}
