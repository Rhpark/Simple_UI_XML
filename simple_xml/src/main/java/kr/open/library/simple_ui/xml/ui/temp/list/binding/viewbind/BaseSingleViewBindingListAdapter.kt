package kr.open.library.simple_ui.xml.ui.temp.list.binding.viewbind

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.viewbinding.ViewBinding
import kr.open.library.simple_ui.xml.ui.temp.base.list.BaseListViewBindingAdapterCore
import kr.open.library.simple_ui.xml.ui.temp.base.list.diffcallback.DefaultDiffCallback
import kr.open.library.simple_ui.xml.ui.temp.viewholder.binding.BaseViewBindingViewHolder

abstract class BaseSingleViewBindingListAdapter<ITEM : Any, BINDING : ViewBinding>(
    private val inflate: (LayoutInflater, ViewGroup, Boolean) -> BINDING,
    diffCallback: DiffUtil.ItemCallback<ITEM> = DefaultDiffCallback(),
) : BaseListViewBindingAdapterCore<ITEM, BINDING>(diffCallback) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewBindingViewHolder<BINDING> {
        val binding = inflate(LayoutInflater.from(parent.context), parent, false)
        return getCreateViewHolder(binding)
    }

    final override fun onBindItem(holder: BaseViewBindingViewHolder<BINDING>, position: Int, item: ITEM) {
        onBind(holder, item, position)
    }

    protected abstract fun onBind(holder: BaseViewBindingViewHolder<BINDING>, item: ITEM, position: Int)
}
