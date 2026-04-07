package kr.open.library.simple_ui.xml.ui.adapter.normal.simple

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import kr.open.library.simple_ui.xml.ui.adapter.normal.base.BaseRcvAdapter
import kr.open.library.simple_ui.xml.ui.adapter.viewholder.BaseRcvViewBindingViewHolder

/**
 * Simple RecyclerView adapter using ViewBinding-enabled ViewHolders.<br><br>
 * ViewBinding을 사용하는 단순 RecyclerView 어댑터입니다.<br>
 *
 * @param ITEM Type of data items in the list.<br><br>
 *             리스트에 담길 아이템 타입입니다.<br>
 *
 * @param BINDING Generated ViewBinding type for the item view.<br><br>
 *                아이템 뷰에 대응하는 ViewBinding 타입입니다.<br>
 *
 * @param inflate ViewBinding inflate function for item views.<br><br>
 *                아이템 뷰용 ViewBinding inflate 함수입니다.<br>
 *
 * @param onBind Function to bind data to the ViewHolder.<br><br>
 *               뷰홀더에 데이터를 바인딩하는 함수입니다.<br>
 */
public open class SimpleRcvViewBindingAdapter<ITEM : Any, BINDING : ViewBinding>(
    private val inflate: (LayoutInflater, ViewGroup, Boolean) -> BINDING,
    private val onBind: (BaseRcvViewBindingViewHolder<BINDING>, ITEM, position: Int) -> Unit,
) : BaseRcvAdapter<ITEM, BaseRcvViewBindingViewHolder<BINDING>>() {
    /**
     * Creates a new ViewHolder instance.<br><br>
     * 새로운 ViewHolder 인스턴스를 생성합니다.<br>
     *
     * @return A new BaseRcvViewBindingViewHolder instance.<br><br>
     *         새로운 BaseRcvViewBindingViewHolder 인스턴스.<br>
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
