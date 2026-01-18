package kr.open.library.simple_ui.xml.ui.adapter.list.simple

import android.view.ViewGroup
import androidx.annotation.LayoutRes
import kr.open.library.simple_ui.xml.ui.adapter.list.base.BaseRcvListAdapter
import kr.open.library.simple_ui.xml.ui.adapter.list.diffutil.RcvListDiffUtilCallBack
import kr.open.library.simple_ui.xml.ui.adapter.viewholder.BaseRcvViewHolder

/**
 * A simple RecyclerView ListAdapter implementation without DataBinding support.<br>
 * Provides a convenient way to create adapters without subclassing.<br><br>
 * DataBinding 없이 사용하는 간단한 RecyclerView ListAdapter 구현체입니다.<br>
 * 서브클래싱 없이 어댑터를 생성하는 편리한 방법을 제공합니다.<br>
 *
 * Features:<br>
 * - View-based binding using findViewById<br>
 * - Lambda-based binding without subclassing<br>
 * - Inherits all BaseRcvListAdapter functionality<br>
 * - DiffUtil-based efficient list updates<br><br>
 * 기능:<br>
 * - findViewById를 사용한 뷰 기반 바인딩<br>
 * - 서브클래싱 없이 람다 기반 바인딩<br>
 * - BaseRcvListAdapter의 모든 기능 상속<br>
 * - DiffUtil 기반 효율적인 리스트 업데이트<br>
 *
 * Usage example:<br>
 * ```kotlin
 * val adapter = SimpleRcvListAdapter<User>(
 *     layoutRes = R.layout.item_user,
 *     listDiffUtil = RcvListDiffUtilCallBack(
 *         itemsTheSame = { old, new -> old.id == new.id },
 *         contentsTheSame = { old, new -> old == new }
 *     )
 * ) { holder, item, position ->
 *     holder.getView<TextView>(R.id.tvName).text = item.name
 *     holder.getView<TextView>(R.id.tvEmail).text = item.email
 * }
 * ```
 * <br>
 *
 * @param ITEM The type of data items in the list.<br><br>
 *             리스트의 데이터 아이템 타입.<br>
 *
 * @param layoutRes Layout resource ID for item views.<br><br>
 *                  아이템 뷰를 위한 레이아웃 리소스 ID.<br>
 *
 * @param listDiffUtil DiffUtil callback for comparing items.<br><br>
 *                     아이템 비교를 위한 DiffUtil 콜백.<br>
 *
 * @param onBind Lambda function to bind data to the ViewHolder.<br><br>
 *               ViewHolder에 데이터를 바인딩하는 람다 함수.<br>
 *
 * @see SimpleBindingRcvListAdapter For adapter with DataBinding support.<br><br>
 *      DataBinding을 사용하는 어댑터는 SimpleBindingRcvListAdapter를 참조하세요.<br>
 *
 * @see BaseRcvListAdapter For the base ListAdapter implementation.<br><br>
 *      기본 ListAdapter 구현은 BaseRcvListAdapter를 참조하세요.<br>
 */
public open class SimpleRcvListAdapter<ITEM : Any>(
    @LayoutRes private val layoutRes: Int,
    listDiffUtil: RcvListDiffUtilCallBack<ITEM>,
    private val onBind: (BaseRcvViewHolder, ITEM, position: Int) -> Unit,
) : BaseRcvListAdapter<ITEM, BaseRcvViewHolder>(listDiffUtil) {
    /**
     * Creates a new ViewHolder for the given view type.<br><br>
     * 주어진 뷰 타입에 대한 새로운 ViewHolder를 생성합니다.<br>
     *
     * @param parent The ViewGroup into which the new View will be added.<br><br>
     *               새로운 View가 추가될 ViewGroup.<br>
     *
     * @param viewType The view type of the new View.<br><br>
     *                 새로운 View의 뷰 타입.<br>
     *
     * @return A new BaseRcvViewHolder that holds a view.<br><br>
     *         뷰를 보유하는 새로운 BaseRcvViewHolder.<br>
     */
    override fun createViewHolderInternal(
        parent: ViewGroup,
        viewType: Int,
    ): BaseRcvViewHolder = BaseRcvViewHolder(layoutRes, parent)

    /**
     * Binds data to the ViewHolder at the specified position.<br>
     * Delegates to the onBind lambda provided in the constructor.<br><br>
     * 지정된 위치의 ViewHolder에 데이터를 바인딩합니다.<br>
     * 생성자에서 제공된 onBind 람다에 위임합니다.<br>
     *
     * @param holder The ViewHolder to bind data to.<br><br>
     *               데이터를 바인딩할 ViewHolder.<br>
     *
     * @param position The position of the item in the list.<br><br>
     *                 리스트에서 아이템의 위치.<br>
     *
     * @param item The item to bind.<br><br>
     *             바인딩할 아이템.<br>
     */
    override fun onBindViewHolder(
        holder: BaseRcvViewHolder,
        position: Int,
        item: ITEM,
    ) {
        onBind(holder, item, position)
    }
}
