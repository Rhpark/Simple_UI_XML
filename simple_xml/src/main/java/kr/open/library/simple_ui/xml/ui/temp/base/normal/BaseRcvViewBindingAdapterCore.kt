package kr.open.library.simple_ui.xml.ui.temp.base.normal

import androidx.viewbinding.ViewBinding
import kr.open.library.simple_ui.xml.ui.temp.viewholder.binding.BaseViewBindingViewHolder

abstract class BaseRcvViewBindingAdapterCore<ITEM : Any, BINDING : ViewBinding>(
    diffUtilEnabled: Boolean = false
) : RootRcvAdapterCore<ITEM, BaseViewBindingViewHolder<BINDING>>(diffUtilEnabled) {
    abstract fun getCreateViewHolder(binding: BINDING): BaseViewBindingViewHolder<BINDING>
}
