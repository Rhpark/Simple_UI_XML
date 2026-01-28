package kr.open.library.simpleui_xml.temp.normal.adapter.recyclerview

import android.view.View
import android.widget.TextView
import kr.open.library.simple_ui.xml.ui.temp.normal.normal.BaseSingleAdapter
import kr.open.library.simple_ui.xml.ui.temp.viewholder.normal.BaseRcvViewHolder
import kr.open.library.simpleui_xml.R
import kr.open.library.simpleui_xml.temp.data.TempItem
import java.util.concurrent.Executor

/**
 * RecyclerView normal single-type adapter example.<br><br>
 * RecyclerView 일반 단일 타입 어댑터 예제입니다.<br>
 */
class TempSingleNormalAdapter(
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
) : BaseSingleAdapter<TempItem, BaseRcvViewHolder>(
        R.layout.item_temp_single,
        diffUtilEnabled,
        diffExecutor,
    ) {
    /**
     * Creates the default ViewHolder for normal items.<br><br>
     * 일반 아이템용 기본 ViewHolder를 생성합니다.<br>
     */
    override fun getCreateViewHolder(view: View): BaseRcvViewHolder = BaseRcvViewHolder(view)

    /**
     * Binds item data to the provided view.<br><br>
     * 제공된 뷰에 아이템 데이터를 바인딩합니다.<br>
     */
    override fun onBind(view: View, item: TempItem, position: Int) {
        val tvTitle = view.findViewById<TextView>(R.id.tvTitle)
        val tvDescription = view.findViewById<TextView>(R.id.tvDescription)
        val tvType = view.findViewById<TextView>(R.id.tvType)

        tvTitle.text = item.title
        tvDescription.text = item.description
        tvType.text = "Id: ${item.id} | Type: ${item.type} | Pos: $position"
    }
}
