package kr.open.library.simpleui_xml.temp.util

import kr.open.library.simpleui_xml.databinding.ItemTempMultiPrimaryBinding
import kr.open.library.simpleui_xml.databinding.ItemTempMultiSecondaryBinding
import kr.open.library.simpleui_xml.databinding.ItemTempSingleBinding
import kr.open.library.simpleui_xml.temp.data.TempItem

object TempItemViewBindingBinder {
    fun bind(binding: ItemTempSingleBinding, item: TempItem, position: Int) {
        binding.tvTitle.text = item.title
        binding.tvDescription.text = item.description
        binding.tvType.text = "Type: ${item.type} | Pos: $position"
    }

    fun bind(binding: ItemTempMultiPrimaryBinding, item: TempItem, position: Int) {
        binding.tvTitle.text = item.title
        binding.tvDescription.text = item.description
        binding.tvType.text = "Type: ${item.type} | Pos: $position"
    }

    fun bind(binding: ItemTempMultiSecondaryBinding, item: TempItem, position: Int) {
        binding.tvTitle.text = item.title
        binding.tvDescription.text = item.description
        binding.tvType.text = "Type: ${item.type} | Pos: $position"
    }
}
