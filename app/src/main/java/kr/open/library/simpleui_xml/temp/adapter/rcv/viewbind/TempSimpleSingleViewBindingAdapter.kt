package kr.open.library.simpleui_xml.temp.adapter.rcv.viewbind

import kr.open.library.simple_ui.xml.ui.temp.normal.binding.viewbind.SimpleSingleViewBindingAdapter
import kr.open.library.simple_ui.xml.ui.temp.viewholder.binding.BaseBindingViewHolder
import kr.open.library.simpleui_xml.databinding.ItemTempSingleBinding
import kr.open.library.simpleui_xml.temp.data.TempItem
import kr.open.library.simpleui_xml.temp.util.TempItemViewBindingBinder

class TempSimpleSingleViewBindingAdapter :
    SimpleSingleViewBindingAdapter<TempItem, ItemTempSingleBinding>(
        inflate = ItemTempSingleBinding::inflate,
        onBindItem = { holder: BaseBindingViewHolder<ItemTempSingleBinding>, item, position ->
            TempItemViewBindingBinder.bind(holder.binding, item, position)
        },
    )
