package kr.open.library.simpleui_xml.temp.multi.normal.adapter.listadapter

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import kr.open.library.simple_ui.xml.ui.temp.base.list.diffcallback.DefaultDiffCallback
import kr.open.library.simple_ui.xml.ui.temp.list.normal.BaseMultiListAdapter
import kr.open.library.simple_ui.xml.ui.temp.viewholder.normal.BaseRcvViewHolder
import kr.open.library.simpleui_xml.R
import kr.open.library.simpleui_xml.temp.data.TempItem
import kr.open.library.simpleui_xml.temp.multi.data.TempItemType
import java.util.concurrent.Executor

/**
 * ListAdapter normal multi-type adapter example.<br><br>
 * ListAdapter 일반 다중 타입 어댑터 예제입니다.<br>
 */
class TempMultiNormalListAdapter(
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
) : BaseMultiListAdapter<TempItem>(
        layoutResProvider = { item: TempItem, _: Int ->
            // Layout resolved by item type.<br><br>아이템 타입 기준으로 레이아웃을 결정합니다.<br>
            when (item.type) {
                TempItemType.PRIMARY -> R.layout.item_temp_multi_primary
                TempItemType.SECONDARY -> R.layout.item_temp_multi_secondary
            }
        },
        diffCallback = diffCallback,
        diffExecutor = diffExecutor,
    ) {
    /**
     * Creates the default ViewHolder for normal items.<br><br>
     * 일반 아이템용 기본 ViewHolder를 생성합니다.<br>
     */
    override fun getCreateViewHolder(view: View): BaseRcvViewHolder = BaseRcvViewHolder(view)

    /**
     * Binds item data to the provided view with view type info.<br><br>
     * 뷰 타입 정보를 포함해 뷰에 아이템 데이터를 바인딩합니다.<br>
     */
    override fun onBind(view: View, item: TempItem, position: Int, viewType: Int) {
        val tvTitle = view.findViewById<TextView>(R.id.tvTitle)
        val tvDescription = view.findViewById<TextView>(R.id.tvDescription)
        val tvType = view.findViewById<TextView>(R.id.tvType)

        tvTitle.text = item.title
        tvDescription.text = item.description
        tvType.text = "Id: ${item.id} | Type: ${item.type} | Pos: $position"
    }
}