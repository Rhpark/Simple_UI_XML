package kr.open.library.simpleui_xml.temp.adapter.rcv.databind

import androidx.databinding.ViewDataBinding
import kr.open.library.simple_ui.xml.ui.temp.normal.binding.databind.BaseMultiDataBindingAdapter
import kr.open.library.simple_ui.xml.ui.temp.viewholder.binding.BaseBindingViewHolder
import kr.open.library.simpleui_xml.R
import kr.open.library.simpleui_xml.temp.data.TempItem
import kr.open.library.simpleui_xml.temp.data.TempItemType
import kr.open.library.simpleui_xml.temp.util.TempItemDataBindingBinder

class TempMultiDataBindingAdapter :
    BaseMultiDataBindingAdapter<TempItem>(
        layoutResProvider = { item, _ ->
            when (item.type) {
                TempItemType.PRIMARY -> R.layout.item_temp_multi_primary_databinding
                TempItemType.SECONDARY -> R.layout.item_temp_multi_secondary_databinding
            }
        },
    ) {
    override fun onBind(
        holder: BaseBindingViewHolder<ViewDataBinding>,
        item: TempItem,
        position: Int,
        viewType: Int,
    ) {
        TempItemDataBindingBinder.bind(holder.binding, item)
    }
}
