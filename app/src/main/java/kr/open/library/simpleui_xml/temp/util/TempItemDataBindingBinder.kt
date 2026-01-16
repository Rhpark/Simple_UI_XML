package kr.open.library.simpleui_xml.temp.util

import androidx.databinding.ViewDataBinding
import kr.open.library.simpleui_xml.databinding.ItemTempMultiPrimaryDatabindingBinding
import kr.open.library.simpleui_xml.databinding.ItemTempMultiSecondaryDatabindingBinding
import kr.open.library.simpleui_xml.databinding.ItemTempSingleDatabindingBinding
import kr.open.library.simpleui_xml.temp.data.TempItem

object TempItemDataBindingBinder {
    fun bind(binding: ViewDataBinding, item: TempItem) {
        when (binding) {
            is ItemTempSingleDatabindingBinding -> binding.item = item
            is ItemTempMultiPrimaryDatabindingBinding -> binding.item = item
            is ItemTempMultiSecondaryDatabindingBinding -> binding.item = item
        }
        binding.executePendingBindings()
    }
}
