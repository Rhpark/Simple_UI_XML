package kr.open.library.simple_ui.xml.ui.temp.list.binding.databind

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.DiffUtil
import kr.open.library.simple_ui.xml.ui.temp.base.list.BaseListDataBindingAdapterCore
import kr.open.library.simple_ui.xml.ui.temp.base.list.diffcallback.DefaultDiffCallback
import kr.open.library.simple_ui.xml.ui.temp.viewholder.binding.BaseDataBindingViewHolder

abstract class BaseMultiDataBindingListAdapter<ITEM : Any, BINDING : ViewDataBinding>(
    private val layoutResProvider: (item: ITEM, position: Int) -> Int,
    diffCallback: DiffUtil.ItemCallback<ITEM> = DefaultDiffCallback(),
) : BaseListDataBindingAdapterCore<ITEM, BINDING>(diffCallback) {
    override fun getItemViewType(position: Int): Int =
        layoutResProvider(getItem(position), position)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseDataBindingViewHolder<BINDING> {
        val inflater = LayoutInflater.from(parent.context)
        val binding: BINDING = DataBindingUtil.inflate(inflater, viewType, parent, false)
        return getCreateViewHolder(binding)
    }

    final override fun onBindItem(holder: BaseDataBindingViewHolder<BINDING>, position: Int, item: ITEM) {
        onBind(holder, item, position, holder.itemViewType)
    }

    protected abstract fun onBind(holder: BaseDataBindingViewHolder<BINDING>, item: ITEM, position: Int, viewType: Int)
}
