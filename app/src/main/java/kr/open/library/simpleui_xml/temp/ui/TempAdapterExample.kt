package kr.open.library.simpleui_xml.temp.ui

import androidx.recyclerview.widget.RecyclerView
import kr.open.library.simpleui_xml.temp.data.TempItem

data class TempAdapterExample(
    val title: String,
    val createAdapter: () -> RecyclerView.Adapter<*>,
    val createItems: () -> List<TempItem>,
)
