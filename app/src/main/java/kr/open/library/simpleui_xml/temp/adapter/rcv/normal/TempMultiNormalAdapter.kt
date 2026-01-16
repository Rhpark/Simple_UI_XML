package kr.open.library.simpleui_xml.temp.adapter.rcv.normal

import android.view.View
import kr.open.library.simple_ui.xml.ui.temp.normal.normal.BaseMultiAdapter
import kr.open.library.simpleui_xml.R
import kr.open.library.simpleui_xml.temp.data.TempItem
import kr.open.library.simpleui_xml.temp.data.TempItemType
import kr.open.library.simpleui_xml.temp.util.TempItemViewBinder

class TempMultiNormalAdapter :
    BaseMultiAdapter<TempItem>(
        layoutResProvider = { item, _ ->
            when (item.type) {
                TempItemType.PRIMARY -> R.layout.item_temp_multi_primary
                TempItemType.SECONDARY -> R.layout.item_temp_multi_secondary
            }
        },
    ) {
    override fun onBind(view: View, item: TempItem, position: Int, viewType: Int) {
        TempItemViewBinder.bind(view, item, position)
    }
}
