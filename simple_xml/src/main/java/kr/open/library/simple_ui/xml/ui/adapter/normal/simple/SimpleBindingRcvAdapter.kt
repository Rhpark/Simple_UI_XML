package kr.open.library.simple_ui.xml.ui.adapter.normal.simple

import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.ViewDataBinding
import kr.open.library.simple_ui.xml.ui.adapter.normal.base.BaseRcvAdapter
import kr.open.library.simple_ui.xml.ui.adapter.viewholder.BaseBindingRcvViewHolder

/**
 * Simple RecyclerView adapter using DataBinding-enabled ViewHolders.<br><br>
 * DataBinding을 사용하는 단순 RecyclerView 어댑터입니다.<br>
 *
 * @param ITEM Type of data items in the list.<br><br>
 *             리스트에 담길 아이템 타입입니다.
 *
 * @param BINDING Generated ViewDataBinding type for the item view.<br><br>
 *                아이템 뷰에 대응하는 ViewDataBinding 타입입니다.
 *
 * @param layoutRes Layout resource ID for item views.<br><br>
 *                  아이템 레이아웃 리소스 ID입니다.
 *
 * @param onBind Function to bind data to the ViewHolder.<br><br>
 *               뷰홀더에 데이터를 바인딩하는 함수입니다.
 */
public open class SimpleBindingRcvAdapter<ITEM : Any, BINDING : ViewDataBinding>(
    @LayoutRes private val layoutRes: Int,
    private val onBind: (BaseBindingRcvViewHolder<BINDING>, ITEM, position: Int) -> Unit,
) : BaseRcvAdapter<ITEM, BaseBindingRcvViewHolder<BINDING>>() {
    /**
     * Creates a new ViewHolder instance.<br><br>
     * 새로운 ViewHolder 인스턴스를 생성합니다.<br>
     *
     * @return A new BaseBindingRcvViewHolder instance.<br><br>
     *         새로운 BaseBindingRcvViewHolder 인스턴스.<br>
     */
    override fun createViewHolderInternal(
        parent: ViewGroup,
        viewType: Int,
    ): BaseBindingRcvViewHolder<BINDING> = BaseBindingRcvViewHolder(layoutRes, parent)

    /**
     * Binds data to the ViewHolder.<br><br>
     * ViewHolder에 데이터를 바인딩합니다.<br>
     */
    override fun onBindViewHolder(
        holder: BaseBindingRcvViewHolder<BINDING>,
        position: Int,
        item: ITEM,
    ) {
        onBind(holder, item, position)
    }
}
