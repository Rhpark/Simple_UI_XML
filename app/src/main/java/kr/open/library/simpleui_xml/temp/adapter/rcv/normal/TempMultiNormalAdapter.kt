package kr.open.library.simpleui_xml.temp.adapter.rcv.normal

import android.view.View
import kr.open.library.simple_ui.xml.ui.temp.normal.normal.BaseMultiAdapter
import kr.open.library.simple_ui.xml.ui.temp.viewholder.normal.BaseRcvViewHolder
import kr.open.library.simpleui_xml.R
import kr.open.library.simpleui_xml.temp.data.TempItem
import kr.open.library.simpleui_xml.temp.data.TempItemType
import kr.open.library.simpleui_xml.temp.util.TempItemViewBinder
import java.util.concurrent.Executor

/**
 * RecyclerView normal multi-type adapter example.<br><br>
 * RecyclerView 일반 다중 타입 어댑터 예제입니다.<br>
 */
class TempMultiNormalAdapter(
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
) : BaseMultiAdapter<TempItem, BaseRcvViewHolder>(
        layoutResProvider = { item: TempItem, _: Int ->
            // Layout resolved by item type.<br><br>아이템 타입 기준으로 결정된 레이아웃입니다.<br>
            when (item.type) {
                TempItemType.PRIMARY -> R.layout.item_temp_multi_primary
                TempItemType.SECONDARY -> R.layout.item_temp_multi_secondary
            }
        },
        diffUtilEnabled = diffUtilEnabled,
        diffExecutor = diffExecutor,
    ) {
    /**
     * Creates the default ViewHolder for normal items.<br><br>
     * 일반 아이템용 기본 ViewHolder를 생성합니다.<br>
     */
    override fun getCreateViewHolder(view: View): BaseRcvViewHolder = BaseRcvViewHolder(view)

    /**
     * Binds item data to the provided view with view type info.<br><br>
     * 뷰 타입 정보를 포함해 제공된 View에 아이템 데이터를 바인딩합니다.<br>
     */
    override fun onBind(view: View, item: TempItem, position: Int, viewType: Int) {
        TempItemViewBinder.bind(view, item, position)
    }
}
