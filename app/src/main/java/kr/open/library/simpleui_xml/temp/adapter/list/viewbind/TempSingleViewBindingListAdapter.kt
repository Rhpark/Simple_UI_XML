package kr.open.library.simpleui_xml.temp.adapter.list.viewbind

import kr.open.library.simple_ui.xml.ui.temp.list.binding.viewbind.BaseSingleViewBindingListAdapter
import kr.open.library.simple_ui.xml.ui.temp.viewholder.binding.BaseBindingViewHolder
import kr.open.library.simpleui_xml.databinding.ItemTempSingleBinding
import kr.open.library.simpleui_xml.temp.data.TempItem
import kr.open.library.simpleui_xml.temp.util.TempItemViewBindingBinder

class TempSingleViewBindingListAdapter :
    BaseSingleViewBindingListAdapter<TempItem, ItemTempSingleBinding>(
        ItemTempSingleBinding::inflate,
    ) {
    override fun onBind(
        holder: BaseBindingViewHolder<ItemTempSingleBinding>,
        item: TempItem,
        position: Int,
    ) {
        TempItemViewBindingBinder.bind(holder.binding, item, position)
    }
}
