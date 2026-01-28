package kr.open.library.simpleui_xml.temp.normal.adapter.listadapter

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import kr.open.library.simple_ui.xml.ui.temp.base.list.diffcallback.DefaultDiffCallback
import kr.open.library.simple_ui.xml.ui.temp.list.normal.SimpleSingleListAdapter
import kr.open.library.simpleui_xml.R
import kr.open.library.simpleui_xml.temp.data.TempItem
import java.util.concurrent.Executor

/**
 * ListAdapter normal simple single-type adapter example.<br><br>
 * ListAdapter 일반 간단 단일 타입 어댑터 예제입니다.<br>
 */
class TempSimpleSingleNormalListAdapter(
    /**
     * DiffUtil callback for item comparison.<br><br>
     * 아이템 비교를 위한 DiffUtil 콜백입니다.<br>
     */
    diffCallback: DiffUtil.ItemCallback<TempItem> = DefaultDiffCallback(),
    /**
     * Executor used for background diff computation.<br><br>
     * 백그라운드 diff 계산에 사용하는 Executor입니다.<br>
     */
    diffExecutor: Executor? = null,
) : SimpleSingleListAdapter<TempItem>(
        layoutRes = R.layout.item_temp_single,
        diffCallback = diffCallback,
        diffExecutor = diffExecutor,
        onBindItem = { view: View, item: TempItem, position: Int ->
            val tvTitle = view.findViewById<TextView>(R.id.tvTitle)
            val tvDescription = view.findViewById<TextView>(R.id.tvDescription)
            val tvType = view.findViewById<TextView>(R.id.tvType)

            tvTitle.text = item.title
            tvDescription.text = item.description
            tvType.text = "Id: ${item.id} | Type: ${item.type} | Pos: $position"
        },
    )
