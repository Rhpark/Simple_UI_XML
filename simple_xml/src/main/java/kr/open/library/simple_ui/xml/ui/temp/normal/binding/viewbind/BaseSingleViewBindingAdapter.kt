package kr.open.library.simple_ui.xml.ui.temp.normal.binding.viewbind

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import kr.open.library.simple_ui.xml.ui.temp.base.normal.BaseRcvViewBindingAdapterCore
import kr.open.library.simple_ui.xml.ui.temp.viewholder.binding.BaseViewBindingViewHolder

abstract class BaseSingleViewBindingAdapter<ITEM : Any, BINDING : ViewBinding>(
    private val inflate: (LayoutInflater, ViewGroup, Boolean) -> BINDING,
    diffUtilEnabled: Boolean = false,
) : BaseRcvViewBindingAdapterCore<ITEM, BINDING>(diffUtilEnabled) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewBindingViewHolder<BINDING> {
        val binding = inflate(LayoutInflater.from(parent.context), parent, false)
        return getCreateViewHolder(binding)
    }

    final override fun onBindItem(holder: BaseViewBindingViewHolder<BINDING>, position: Int, item: ITEM) {
        onBind(holder, item, position)
    }

    protected abstract fun onBind(holder: BaseViewBindingViewHolder<BINDING>, item: ITEM, position: Int)
}
