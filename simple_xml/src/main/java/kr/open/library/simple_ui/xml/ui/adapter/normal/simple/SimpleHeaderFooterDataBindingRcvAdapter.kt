package kr.open.library.simple_ui.xml.ui.adapter.normal.simple

import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.ViewDataBinding
import kr.open.library.simple_ui.xml.ui.adapter.normal.headerfooter.HeaderFooterRcvAdapter
import kr.open.library.simple_ui.xml.ui.adapter.viewholder.BaseRcvDataBindingViewHolder

/**
 * Simple RecyclerView adapter with DataBinding and Header/Content/Footer support.<br><br>
 * DataBinding과 Header/Content/Footer를 지원하는 간단한 RecyclerView 어댑터입니다.<br>
 *
 * @param ITEM Type of data items in the list.<br><br>
 *             리스트에서 사용하는 아이템 타입입니다.<br>
 * @param BINDING Generated ViewDataBinding type for the item view.<br><br>
 *                아이템 뷰에 대응하는 ViewDataBinding 타입입니다.<br>
 * @param layoutRes Layout resource ID for item views.<br><br>
 *                  아이템 뷰 레이아웃 리소스 ID입니다.<br>
 * @param onBind Function that binds data to the ViewHolder.<br><br>
 *               ViewHolder에 데이터를 바인딩하는 함수입니다.<br>
 */
public open class SimpleHeaderFooterDataBindingRcvAdapter<ITEM : Any, BINDING : ViewDataBinding>(
    @LayoutRes private val layoutRes: Int,
    private val onBind: (BaseRcvDataBindingViewHolder<BINDING>, ITEM, position: Int) -> Unit,
) : HeaderFooterRcvAdapter<ITEM, BaseRcvDataBindingViewHolder<BINDING>>() {
    /**
     * Creates a new ViewHolder instance.<br><br>
     * 새로운 ViewHolder 인스턴스를 생성합니다.<br>
     */
    override fun createViewHolderInternal(
        parent: ViewGroup,
        viewType: Int,
    ): BaseRcvDataBindingViewHolder<BINDING> = BaseRcvDataBindingViewHolder(layoutRes, parent)

    /**
     * Binds data to the ViewHolder.<br><br>
     * ViewHolder에 데이터를 바인딩합니다.<br>
     */
    override fun onBindViewHolder(
        holder: BaseRcvDataBindingViewHolder<BINDING>,
        item: ITEM,
        position: Int,
    ) {
        onBind(holder, item, position)
    }
}
