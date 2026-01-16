package kr.open.library.simpleui_xml.temp.adapter.list.viewbind

import androidx.viewbinding.ViewBinding
import kr.open.library.simple_ui.xml.ui.temp.list.binding.viewbind.BaseMultiViewBindingListAdapter
import kr.open.library.simple_ui.xml.ui.temp.viewholder.binding.BaseBindingViewHolder
import kr.open.library.simpleui_xml.R
import kr.open.library.simpleui_xml.databinding.ItemTempMultiPrimaryBinding
import kr.open.library.simpleui_xml.databinding.ItemTempMultiSecondaryBinding
import kr.open.library.simpleui_xml.temp.data.TempItem
import kr.open.library.simpleui_xml.temp.data.TempItemType
import kr.open.library.simpleui_xml.temp.util.TempItemViewBindingBinder

class TempMultiViewBindingListAdapter :
    BaseMultiViewBindingListAdapter<TempItem>(
        viewTypeProvider = { item, _ ->
            when (item.type) {
                TempItemType.PRIMARY -> R.layout.item_temp_multi_primary
                TempItemType.SECONDARY -> R.layout.item_temp_multi_secondary
            }
        },
        inflateMap = mapOf(
            R.layout.item_temp_multi_primary to ItemTempMultiPrimaryBinding::inflate,
            R.layout.item_temp_multi_secondary to ItemTempMultiSecondaryBinding::inflate,
        ),
    ) {
    override fun onBind(
        holder: BaseBindingViewHolder<ViewBinding>,
        item: TempItem,
        position: Int,
        viewType: Int,
    ) {
        when (val binding = holder.binding) {
            is ItemTempMultiPrimaryBinding -> TempItemViewBindingBinder.bind(binding, item, position)
            is ItemTempMultiSecondaryBinding -> TempItemViewBindingBinder.bind(binding, item, position)
        }
    }
}
