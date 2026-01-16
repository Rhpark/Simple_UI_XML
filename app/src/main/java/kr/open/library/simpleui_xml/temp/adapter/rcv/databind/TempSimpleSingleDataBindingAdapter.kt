package kr.open.library.simpleui_xml.temp.adapter.rcv.databind

import kr.open.library.simple_ui.xml.ui.temp.normal.binding.databind.SimpleSingleDataBindingAdapter
import kr.open.library.simple_ui.xml.ui.temp.viewholder.binding.BaseBindingViewHolder
import kr.open.library.simpleui_xml.R
import kr.open.library.simpleui_xml.databinding.ItemTempSingleDatabindingBinding
import kr.open.library.simpleui_xml.temp.data.TempItem
import kr.open.library.simpleui_xml.temp.util.TempItemDataBindingBinder

class TempSimpleSingleDataBindingAdapter :
    SimpleSingleDataBindingAdapter<TempItem, ItemTempSingleDatabindingBinding>(
        layoutRes = R.layout.item_temp_single_databinding,
        onBindItem = { holder: BaseBindingViewHolder<ItemTempSingleDatabindingBinding>, item, _ ->
            TempItemDataBindingBinder.bind(holder.binding, item)
        },
    )
