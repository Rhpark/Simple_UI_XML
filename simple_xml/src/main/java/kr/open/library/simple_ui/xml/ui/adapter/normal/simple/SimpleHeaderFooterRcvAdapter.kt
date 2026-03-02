package kr.open.library.simple_ui.xml.ui.adapter.normal.simple

import android.view.ViewGroup
import androidx.annotation.LayoutRes
import kr.open.library.simple_ui.xml.ui.adapter.normal.headerfooter.HeaderFooterRcvAdapter
import kr.open.library.simple_ui.xml.ui.adapter.viewholder.BaseRcvViewHolder

/**
 * Simple RecyclerView adapter with Header/Content/Footer section support.<br><br>
 * Header/Content/Footer 섹션을 지원하는 간단한 RecyclerView 어댑터입니다.<br>
 *
 * @param ITEM Type of data items in the list.<br><br>
 *             리스트에서 사용하는 아이템 타입입니다.<br>
 * @param layoutRes Layout resource ID for item views.<br><br>
 *                  아이템 뷰 레이아웃 리소스 ID입니다.<br>
 * @param onBind Function that binds data to the ViewHolder.<br><br>
 *               ViewHolder에 데이터를 바인딩하는 함수입니다.<br>
 */
public open class SimpleHeaderFooterRcvAdapter<ITEM : Any>(
    @LayoutRes private val layoutRes: Int,
    private val onBind: (BaseRcvViewHolder, ITEM, position: Int) -> Unit,
) : HeaderFooterRcvAdapter<ITEM, BaseRcvViewHolder>() {
    /**
     * Creates a new ViewHolder instance.<br><br>
     * 새로운 ViewHolder 인스턴스를 생성합니다.<br>
     */
    override fun createViewHolderInternal(parent: ViewGroup, viewType: Int): BaseRcvViewHolder =
        BaseRcvViewHolder(layoutRes, parent)

    /**
     * Binds data to the ViewHolder.<br><br>
     * ViewHolder에 데이터를 바인딩합니다.<br>
     */
    override fun onBindViewHolder(holder: BaseRcvViewHolder, item: ITEM, position: Int) {
        onBind(holder, item, position)
    }
}
