package kr.open.library.simpleui_xml.temp.util

import android.view.View
import android.widget.TextView
import kr.open.library.simpleui_xml.R
import kr.open.library.simpleui_xml.temp.data.TempItem

object TempItemViewBinder {
    fun bind(view: View, item: TempItem, position: Int) {
        view.findViewById<TextView>(R.id.tvTitle).text = item.title
        view.findViewById<TextView>(R.id.tvDescription).text = item.description
        view.findViewById<TextView>(R.id.tvType).text = "Type: ${item.type} | Pos: $position"
    }
}
