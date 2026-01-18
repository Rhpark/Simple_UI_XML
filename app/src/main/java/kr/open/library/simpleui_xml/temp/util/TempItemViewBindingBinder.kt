package kr.open.library.simpleui_xml.temp.util

import kr.open.library.simpleui_xml.databinding.ItemTempMultiPrimaryBinding
import kr.open.library.simpleui_xml.databinding.ItemTempMultiSecondaryBinding
import kr.open.library.simpleui_xml.databinding.ItemTempSingleBinding
import kr.open.library.simpleui_xml.temp.data.TempItem

/**
 * Binder for ViewBinding-based item layouts.<br><br>
 * ViewBinding 기반 아이템 레이아웃을 바인딩하는 유틸입니다.<br>
 */
object TempItemViewBindingBinder {
    /**
     * Binds single-type ViewBinding item view.<br><br>
     * 단일 타입 ViewBinding 아이템 뷰를 바인딩합니다.<br>
     */
    fun bind(binding: ItemTempSingleBinding, item: TempItem, position: Int) {
        binding.tvTitle.text = item.title
        binding.tvDescription.text = item.description
        binding.tvType.text = "Id: ${item.id} | Type: ${item.type} | Pos: $position"
    }

    /**
     * Binds primary multi-type ViewBinding item view.<br><br>
     * 다중 타입의 기본(ViewType) ViewBinding 아이템 뷰를 바인딩합니다.<br>
     */
    fun bind(binding: ItemTempMultiPrimaryBinding, item: TempItem, position: Int) {
        binding.tvTitle.text = item.title
        binding.tvDescription.text = item.description
        binding.tvType.text = "Id: ${item.id} | Type: ${item.type} | Pos: $position"
    }

    /**
     * Binds secondary multi-type ViewBinding item view.<br><br>
     * 다중 타입의 보조(ViewType) ViewBinding 아이템 뷰를 바인딩합니다.<br>
     */
    fun bind(binding: ItemTempMultiSecondaryBinding, item: TempItem, position: Int) {
        binding.tvTitle.text = item.title
        binding.tvDescription.text = item.description
        binding.tvType.text = "Id: ${item.id} | Type: ${item.type} | Pos: $position"
    }
}
