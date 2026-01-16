package kr.open.library.simpleui_xml.temp.adapter.list.normal

import kr.open.library.simple_ui.xml.ui.temp.list.normal.SimpleSingleListAdapter
import kr.open.library.simpleui_xml.R
import kr.open.library.simpleui_xml.temp.data.TempItem
import kr.open.library.simpleui_xml.temp.util.TempItemViewBinder

class TempSimpleSingleNormalListAdapter :
    SimpleSingleListAdapter<TempItem>(
        layoutRes = R.layout.item_temp_single,
        onBindItem = { view, item, position ->
            TempItemViewBinder.bind(view, item, position)
        },
    )
