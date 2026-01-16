package kr.open.library.simple_ui.xml.ui.temp.normal.binding.databind

import androidx.annotation.LayoutRes
import androidx.databinding.ViewDataBinding
import kr.open.library.simple_ui.xml.ui.temp.viewholder.binding.BaseBindingViewHolder
import kr.open.library.simple_ui.xml.ui.temp.viewholder.binding.BaseDataBindingViewHolder

open class SimpleSingleDataBindingAdapter<ITEM : Any, BINDING : ViewDataBinding>(
    @LayoutRes layoutRes: Int,
    diffUtilEnabled: Boolean = false,
    private val onBindItem: (holder: BaseBindingViewHolder<BINDING>, item: ITEM, position: Int) -> Unit,
) : BaseSingleDataBindingAdapter<ITEM, BINDING>(layoutRes, diffUtilEnabled) {
    override fun getCreateViewHolder(binding: BINDING): BaseDataBindingViewHolder<BINDING> = BaseDataBindingViewHolder(binding)

    override fun onBind(holder: BaseDataBindingViewHolder<BINDING>, item: ITEM, position: Int) {
        onBindItem(holder, item, position)
    }
}
