package kr.open.library.simple_ui.xml.ui.temp.normal.binding.viewbind

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import kr.open.library.simple_ui.xml.ui.temp.viewholder.binding.BaseViewBindingViewHolder

open class SimpleSingleViewBindingAdapter<ITEM : Any, BINDING : ViewBinding>(
    inflate: (LayoutInflater, ViewGroup, Boolean) -> BINDING,
    diffUtilEnabled: Boolean = false,
    private val onBindItem: (holder: BaseViewBindingViewHolder<BINDING>, item: ITEM, position: Int) -> Unit,
) : BaseSingleViewBindingAdapter<ITEM, BINDING>(inflate, diffUtilEnabled) {
    override fun onBind(holder: BaseViewBindingViewHolder<BINDING>, item: ITEM, position: Int) {
        onBindItem(holder, item, position)
    }

    override fun getCreateViewHolder(binding: BINDING): BaseViewBindingViewHolder<BINDING> = BaseViewBindingViewHolder(binding)
}
