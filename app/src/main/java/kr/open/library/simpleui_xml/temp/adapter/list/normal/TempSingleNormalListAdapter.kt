package kr.open.library.simpleui_xml.temp.adapter.list.normal

import android.view.View
import androidx.recyclerview.widget.DiffUtil
import kr.open.library.simple_ui.xml.ui.temp.base.list.diffcallback.DefaultDiffCallback
import kr.open.library.simple_ui.xml.ui.temp.list.normal.BaseSingleListAdapter
import kr.open.library.simple_ui.xml.ui.temp.viewholder.normal.BaseRcvViewHolder
import kr.open.library.simpleui_xml.R
import kr.open.library.simpleui_xml.temp.data.TempItem
import kr.open.library.simpleui_xml.temp.util.TempItemViewBinder
import java.util.concurrent.Executor

/**
 * ListAdapter normal single-type adapter example.<br><br>
 * ListAdapter 일반 단일 타입 어댑터 예제입니다.<br>
 */
class TempSingleNormalListAdapter(
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
) : BaseSingleListAdapter<TempItem>(
        R.layout.item_temp_single,
        diffCallback,
        diffExecutor,
    ) {
    /**
     * Creates the default ViewHolder for normal items.<br><br>
     * 일반 아이템용 기본 ViewHolder를 생성합니다.<br>
     */
    override fun getCreateViewHolder(view: View): BaseRcvViewHolder = BaseRcvViewHolder(view)

    /**
     * Binds item data to the provided view.<br><br>
     * 제공된 View에 아이템 데이터를 바인딩합니다.<br>
     */
    override fun onBind(view: View, item: TempItem, position: Int) {
        TempItemViewBinder.bind(view, item, position)
    }
}
