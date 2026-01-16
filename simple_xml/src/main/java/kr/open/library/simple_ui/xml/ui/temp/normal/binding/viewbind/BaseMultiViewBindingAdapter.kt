package kr.open.library.simple_ui.xml.ui.temp.normal.binding.viewbind

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import kr.open.library.simple_ui.core.logcat.Logx
import kr.open.library.simple_ui.xml.ui.temp.base.normal.BaseRcvViewBindingAdapterCore
import kr.open.library.simple_ui.xml.ui.temp.viewholder.binding.BaseViewBindingViewHolder

abstract class BaseMultiViewBindingAdapter<ITEM : Any, BINDING : ViewBinding>(
    private val viewTypeProvider: (item: ITEM, position: Int) -> Int,
    private val inflateMap: Map<Int, (LayoutInflater, ViewGroup, Boolean) -> BINDING>,
    diffUtilEnabled: Boolean = false,
) : BaseRcvViewBindingAdapterCore<ITEM, BINDING>(diffUtilEnabled) {
    override fun getItemViewType(position: Int): Int =
        viewTypeProvider(getItem(position), position)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewBindingViewHolder<BINDING> {
        val inflate = inflateMap[viewType]
        if (inflate == null) {
            Logx.e("Missing inflate for viewType: $viewType")
            throw IllegalArgumentException("Missing inflate for viewType: $viewType")
        }
        val binding = inflate(LayoutInflater.from(parent.context), parent, false)
        return getCreateViewHolder(binding)
    }

    override fun onBindItem(holder: BaseViewBindingViewHolder<BINDING>, position: Int, item: ITEM) {
        onBind(holder, item, position, holder.itemViewType)
    }

    protected abstract fun onBind(holder: BaseViewBindingViewHolder<BINDING>, item: ITEM, position: Int, viewType: Int)
}
