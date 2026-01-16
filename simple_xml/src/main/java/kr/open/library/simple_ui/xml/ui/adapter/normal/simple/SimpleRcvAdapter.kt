package kr.open.library.simple_ui.xml.ui.adapter.normal.simple

import android.view.ViewGroup
import androidx.annotation.LayoutRes
import kr.open.library.simple_ui.xml.ui.adapter.normal.base.BaseRcvAdapter
import kr.open.library.simple_ui.xml.ui.adapter.viewholder.BaseRcvViewHolder

/**
 * Simple RecyclerView adapter without DataBinding support.<br><br>
 * DataBinding을 사용하지 않는 단순 RecyclerView 어댑터입니다.<br>
 *
 * @param ITEM Type of data items in the list.<br><br>
 *             리스트에 담길 아이템 타입입니다.
 *
 * @param layoutRes Layout resource ID for item views.<br><br>
 *                  아이템 레이아웃 리소스 ID입니다.
 *
 * @param onBind Function to bind data to the ViewHolder.<br><br>
 *               뷰홀더에 데이터를 바인딩하는 함수입니다.
 */
public open class SimpleRcvAdapter<ITEM : Any>(
    @LayoutRes private val layoutRes: Int,
    private val onBind: (BaseRcvViewHolder, ITEM, position: Int) -> Unit,
) : BaseRcvAdapter<ITEM, BaseRcvViewHolder>() {
    /**
     * Creates a new ViewHolder instance.<br><br>
     * 새로운 ViewHolder 인스턴스를 생성합니다.<br>
     *
     * @return A new BaseRcvViewHolder instance.<br><br>
     *         새로운 BaseRcvViewHolder 인스턴스.<br>
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseRcvViewHolder = BaseRcvViewHolder(layoutRes, parent)

    /**
     * Binds data to the ViewHolder.<br><br>
     * ViewHolder에 데이터를 바인딩합니다.<br>
     */
    override fun onBindViewHolder(holder: BaseRcvViewHolder, position: Int, item: ITEM) {
        onBind(holder, item, position)
    }
}
