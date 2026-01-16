package kr.open.library.simpleui_xml.temp.adapter.rcv.databind

import kr.open.library.simple_ui.xml.ui.temp.normal.binding.databind.BaseSingleDataBindingAdapter
import kr.open.library.simple_ui.xml.ui.temp.viewholder.binding.BaseBindingViewHolder
import kr.open.library.simpleui_xml.R
import kr.open.library.simpleui_xml.databinding.ItemTempSingleDatabindingBinding
import kr.open.library.simpleui_xml.temp.data.TempItem
import kr.open.library.simpleui_xml.temp.util.TempItemDataBindingBinder

class TempSingleDataBindingAdapter :
    BaseSingleDataBindingAdapter<TempItem, ItemTempSingleDatabindingBinding>(
        R.layout.item_temp_single_databinding,
    ) {
    override fun onBind(
        holder: BaseBindingViewHolder<ItemTempSingleDatabindingBinding>,
        item: TempItem,
        position: Int,
    ) {
        TempItemDataBindingBinder.bind(holder.binding, item)
    }
}
