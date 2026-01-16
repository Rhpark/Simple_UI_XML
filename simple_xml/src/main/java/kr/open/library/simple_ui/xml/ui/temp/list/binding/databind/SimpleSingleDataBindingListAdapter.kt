package kr.open.library.simple_ui.xml.ui.temp.list.binding.databind

import androidx.annotation.LayoutRes
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.DiffUtil
import kr.open.library.simple_ui.xml.ui.temp.base.list.diffcallback.DefaultDiffCallback
import kr.open.library.simple_ui.xml.ui.temp.viewholder.binding.BaseBindingViewHolder
import kr.open.library.simple_ui.xml.ui.temp.viewholder.binding.BaseDataBindingViewHolder

open class SimpleSingleDataBindingListAdapter<ITEM : Any, BINDING : ViewDataBinding>(
    @LayoutRes layoutRes: Int,
    diffCallback: DiffUtil.ItemCallback<ITEM> = DefaultDiffCallback(),
    private val onBindItem: (holder: BaseBindingViewHolder<BINDING>, item: ITEM, position: Int) -> Unit,
) : BaseSingleDataBindingListAdapter<ITEM, BINDING>(layoutRes, diffCallback) {
    override fun getCreateViewHolder(binding: BINDING): BaseDataBindingViewHolder<BINDING> = BaseDataBindingViewHolder(binding)

    override fun onBind(holder: BaseDataBindingViewHolder<BINDING>, item: ITEM, position: Int) {
        onBindItem(holder, item, position)
    }
}
