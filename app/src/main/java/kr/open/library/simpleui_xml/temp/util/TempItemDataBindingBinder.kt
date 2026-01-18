package kr.open.library.simpleui_xml.temp.util

import androidx.databinding.ViewDataBinding
import kr.open.library.simpleui_xml.databinding.ItemTempMultiPrimaryDatabindingBinding
import kr.open.library.simpleui_xml.databinding.ItemTempMultiSecondaryDatabindingBinding
import kr.open.library.simpleui_xml.databinding.ItemTempSingleDatabindingBinding
import kr.open.library.simpleui_xml.temp.data.TempItem

/**
 * Binder for DataBinding-based item layouts.<br><br>
 * DataBinding 기반 아이템 레이아웃을 바인딩하는 유틸입니다.<br>
 */
object TempItemDataBindingBinder {
    /**
     * Binds item data to a DataBinding instance with position.<br><br>
     * 포지션 정보를 포함해 DataBinding 인스턴스에 아이템 데이터를 바인딩합니다.<br>
     */
    fun bind(binding: ViewDataBinding, item: TempItem, position: Int) {
        when (binding) {
            is ItemTempSingleDatabindingBinding -> {
                binding.item = item
                binding.position = position
            }
            is ItemTempMultiPrimaryDatabindingBinding -> {
                binding.item = item
                binding.position = position
            }
            is ItemTempMultiSecondaryDatabindingBinding -> {
                binding.item = item
                binding.position = position
            }
        }
        binding.executePendingBindings()
    }
}
