package kr.open.library.simpleui_xml.temp.adapter.rcv.normal

import android.view.View
import kr.open.library.simple_ui.xml.ui.temp.normal.normal.BaseSingleAdapter
import kr.open.library.simpleui_xml.R
import kr.open.library.simpleui_xml.temp.data.TempItem
import kr.open.library.simpleui_xml.temp.util.TempItemViewBinder

class TempSingleNormalAdapter : BaseSingleAdapter<TempItem>(R.layout.item_temp_single) {
    override fun onBind(view: View, item: TempItem, position: Int) {
        TempItemViewBinder.bind(view, item, position)
    }
}
