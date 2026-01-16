package kr.open.library.simple_ui.xml.ui.temp.normal.binding.databind

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import kr.open.library.simple_ui.xml.ui.temp.base.normal.BaseRcvDataBindingAdapterCore
import kr.open.library.simple_ui.xml.ui.temp.viewholder.binding.BaseDataBindingViewHolder

abstract class BaseMultiDataBindingAdapter<ITEM : Any, BINDING : ViewDataBinding>(
    private val layoutResProvider: (item: ITEM, position: Int) -> Int,
    diffUtilEnabled: Boolean = false,
) : BaseRcvDataBindingAdapterCore<ITEM, BINDING>(diffUtilEnabled) {
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
