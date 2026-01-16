package kr.open.library.simple_ui.xml.ui.temp.list.binding.viewbind

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.viewbinding.ViewBinding
import kr.open.library.simple_ui.xml.ui.temp.base.list.diffcallback.DefaultDiffCallback
import kr.open.library.simple_ui.xml.ui.temp.viewholder.binding.BaseViewBindingViewHolder

open class SimpleSingleViewBindingListAdapter<ITEM : Any, BINDING : ViewBinding>(
    inflate: (LayoutInflater, ViewGroup, Boolean) -> BINDING,
    diffCallback: DiffUtil.ItemCallback<ITEM> = DefaultDiffCallback(),
    private val onBindItem: (holder: BaseViewBindingViewHolder<BINDING>, item: ITEM, position: Int) -> Unit,
) : BaseSingleViewBindingListAdapter<ITEM, BINDING>(inflate, diffCallback) {
    override fun onBind(holder: BaseViewBindingViewHolder<BINDING>, item: ITEM, position: Int) {
        onBindItem(holder, item, position)
    }

    override fun getCreateViewHolder(binding: BINDING): BaseViewBindingViewHolder<BINDING> = BaseViewBindingViewHolder(binding)
}
