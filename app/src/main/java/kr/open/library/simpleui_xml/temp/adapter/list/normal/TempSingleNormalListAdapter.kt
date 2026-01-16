package kr.open.library.simpleui_xml.temp.adapter.list.normal

import android.view.View
import kr.open.library.simple_ui.xml.ui.temp.list.normal.BaseSingleListAdapter
import kr.open.library.simpleui_xml.R
import kr.open.library.simpleui_xml.temp.data.TempItem
import kr.open.library.simpleui_xml.temp.util.TempItemViewBinder

class TempSingleNormalListAdapter : BaseSingleListAdapter<TempItem>(R.layout.item_temp_single) {
    override fun onBind(view: View, item: TempItem, position: Int) {
        TempItemViewBinder.bind(view, item, position)
    }
}
