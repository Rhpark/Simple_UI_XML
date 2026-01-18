package kr.open.library.simpleui_xml.temp.util

import android.view.View
import android.widget.TextView
import kr.open.library.simpleui_xml.R
import kr.open.library.simpleui_xml.temp.data.TempItem

/**
 * Binder for normal View-based item layouts.<br><br>
 * 일반 View 기반 아이템 레이아웃을 바인딩하는 유틸입니다.<br>
 */
object TempItemViewBinder {
    /**
     * Binds item data to the provided view.<br><br>
     * 제공된 View에 아이템 데이터를 바인딩합니다.<br>
     */
    fun bind(view: View, item: TempItem, position: Int) {
        // Title view reference.<br><br>제목 텍스트 뷰 참조입니다.<br>
        val tvTitle = view.findViewById<TextView>(R.id.tvTitle)
        // Description view reference.<br><br>설명 텍스트 뷰 참조입니다.<br>
        val tvDescription = view.findViewById<TextView>(R.id.tvDescription)
        // Type view reference.<br><br>타입 텍스트 뷰 참조입니다.<br>
        val tvType = view.findViewById<TextView>(R.id.tvType)

        tvTitle.text = item.title
        tvDescription.text = item.description
        tvType.text = "Id: ${item.id} | Type: ${item.type} | Pos: $position"
    }
}
