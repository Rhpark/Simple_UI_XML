package kr.open.library.simpleui_xml.temp.adapter.rcv.viewbind

import kr.open.library.simple_ui.xml.ui.temp.normal.binding.viewbind.BaseSingleViewBindingAdapter
import kr.open.library.simple_ui.xml.ui.temp.viewholder.binding.BaseBindingViewHolder
import kr.open.library.simpleui_xml.databinding.ItemTempSingleBinding
import kr.open.library.simpleui_xml.temp.data.TempItem
import kr.open.library.simpleui_xml.temp.util.TempItemViewBindingBinder

class TempSingleViewBindingAdapter : BaseSingleViewBindingAdapter<TempItem, ItemTempSingleBinding>(ItemTempSingleBinding::inflate) {
    override fun onBind(
        holder: BaseBindingViewHolder<ItemTempSingleBinding>,
        item: TempItem,
        position: Int,
    ) {
        TempItemViewBindingBinder.bind(holder.binding, item, position)
    }
}
