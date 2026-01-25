package kr.open.library.simpleui_xml.temp.normal.adapter.recyclerview

import android.view.View
import android.widget.TextView
import kr.open.library.simple_ui.xml.ui.temp.normal.normal.SimpleSingleAdapter
import kr.open.library.simpleui_xml.R
import kr.open.library.simpleui_xml.temp.data.TempItem
import java.util.concurrent.Executor

/**
 * RecyclerView normal simple single-type adapter example.<br><br>
 * RecyclerView 일반 간단 단일 타입 어댑터 예제입니다.<br>
 */
class TempSimpleSingleNormalAdapter(
    /**
     * Enables DiffUtil for normal adapter updates.<br><br>
     * 일반 어댑터 업데이트에서 DiffUtil 사용 여부입니다.<br>
     */
    diffUtilEnabled: Boolean = false,
    /**
     * Executor used for background diff computation.<br><br>
     * 백그라운드 diff 계산에 사용하는 Executor입니다.<br>
     */
    diffExecutor: Executor? = null,
) : SimpleSingleAdapter<TempItem>(
        layoutRes = R.layout.item_temp_single,
        diffUtilEnabled = diffUtilEnabled,
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