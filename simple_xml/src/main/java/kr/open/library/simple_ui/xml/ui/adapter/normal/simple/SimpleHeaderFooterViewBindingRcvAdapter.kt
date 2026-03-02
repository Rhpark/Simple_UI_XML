package kr.open.library.simple_ui.xml.ui.adapter.normal.simple

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import kr.open.library.simple_ui.xml.ui.adapter.normal.headerfooter.HeaderFooterRcvAdapter
import kr.open.library.simple_ui.xml.ui.adapter.viewholder.BaseRcvViewBindingViewHolder

/**
 * Simple RecyclerView adapter with ViewBinding and Header/Content/Footer support.<br><br>
 * ViewBinding과 Header/Content/Footer를 지원하는 간단한 RecyclerView 어댑터입니다.<br>
 *
 * @param ITEM Type of data items in the list.<br><br>
 *             리스트에서 사용하는 아이템 타입입니다.<br>
 * @param BINDING Generated ViewBinding type for the item view.<br><br>
 *                아이템 뷰에 대응하는 ViewBinding 타입입니다.<br>
 * @param inflate ViewBinding inflate function for item views.<br><br>
 *                아이템 뷰용 ViewBinding inflate 함수입니다.<br>
 * @param onBind Function that binds data to the ViewHolder.<br><br>
 *               ViewHolder에 데이터를 바인딩하는 함수입니다.<br>
 */
public open class SimpleHeaderFooterViewBindingRcvAdapter<ITEM : Any, BINDING : ViewBinding>(
    private val inflate: (LayoutInflater, ViewGroup, Boolean) -> BINDING,
    private val onBind: (BaseRcvViewBindingViewHolder<BINDING>, ITEM, position: Int) -> Unit,
) : HeaderFooterRcvAdapter<ITEM, BaseRcvViewBindingViewHolder<BINDING>>() {
    /**
     * Creates a new ViewHolder instance.<br><br>
     * 새로운 ViewHolder 인스턴스를 생성합니다.<br>
     */
    override fun createViewHolderInternal(
        parent: ViewGroup,
        viewType: Int,
    ): BaseRcvViewBindingViewHolder<BINDING> {
        val binding = inflate(LayoutInflater.from(parent.context), parent, false)
        return BaseRcvViewBindingViewHolder(binding)
    }

    /**
     * Binds data to the ViewHolder.<br><br>
     * ViewHolder에 데이터를 바인딩합니다.<br>
     */
    override fun onBindViewHolder(
        holder: BaseRcvViewBindingViewHolder<BINDING>,
        item: ITEM,
        position: Int,
    ) {
        onBind(holder, item, position)
    }
}
