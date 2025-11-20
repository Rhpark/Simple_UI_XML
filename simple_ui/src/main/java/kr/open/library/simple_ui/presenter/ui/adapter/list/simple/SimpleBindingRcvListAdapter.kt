package kr.open.library.simple_ui.presenter.ui.adapter.list.simple

import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.ViewDataBinding
import kr.open.library.simple_ui.presenter.ui.adapter.list.diffutil.RcvListDiffUtilCallBack
import kr.open.library.simple_ui.presenter.ui.adapter.list.base.BaseRcvListAdapter
import kr.open.library.simple_ui.presenter.ui.adapter.viewholder.BaseBindingRcvViewHolder

/**
 * A simple RecyclerView ListAdapter implementation with DataBinding support.<br>
 * Provides a convenient way to create adapters without subclassing.<br><br>
 * DataBinding을 지원하는 간단한 RecyclerView ListAdapter 구현체입니다.<br>
 * 서브클래싱 없이 어댑터를 생성하는 편리한 방법을 제공합니다.<br>
 *
 * Features:<br>
 * - DataBinding support for item views<br>
 * - Lambda-based binding without subclassing<br>
 * - Inherits all BaseRcvListAdapter functionality<br>
 * - DiffUtil-based efficient list updates<br><br>
 * 기능:<br>
 * - 아이템 뷰를 위한 DataBinding 지원<br>
 * - 서브클래싱 없이 람다 기반 바인딩<br>
 * - BaseRcvListAdapter의 모든 기능 상속<br>
 * - DiffUtil 기반 효율적인 리스트 업데이트<br>
 *
 * Usage example:<br>
 * ```kotlin
 * val adapter = SimpleBindingRcvListAdapter<User, ItemUserBinding>(
 *     layoutRes = R.layout.item_user,
 *     listDiffUtil = RcvListDiffUtilCallBack(
 *         itemsTheSame = { old, new -> old.id == new.id },
 *         contentsTheSame = { old, new -> old == new }
 *     )
 * ) { holder, item, position ->
 *     holder.binding.apply {
 *         tvName.text = item.name
 *         tvEmail.text = item.email
 *     }
 * }
 * ```
 * <br>
 *
 * @param ITEM The type of data items in the list.<br><br>
 *             리스트의 데이터 아이템 타입.<br>
 *
 * @param BINDING The type of ViewDataBinding for item views.<br><br>
 *                아이템 뷰를 위한 ViewDataBinding 타입.<br>
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
 * @see SimpleRcvListAdapter For adapter without DataBinding support.<br><br>
 *      DataBinding이 없는 어댑터는 SimpleRcvListAdapter를 참조하세요.<br>
 *
 * @see BaseRcvListAdapter For the base ListAdapter implementation.<br><br>
 *      기본 ListAdapter 구현은 BaseRcvListAdapter를 참조하세요.<br>
 */
public open class SimpleBindingRcvListAdapter<ITEM : Any, BINDING : ViewDataBinding>(
    @LayoutRes private val layoutRes: Int,
    listDiffUtil: RcvListDiffUtilCallBack<ITEM>,
    private val onBind: (BaseBindingRcvViewHolder<BINDING>, ITEM, position: Int) -> Unit
) : BaseRcvListAdapter<ITEM, BaseBindingRcvViewHolder<BINDING>>(listDiffUtil) {

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
     * @return A new BaseBindingRcvViewHolder that holds a view with DataBinding.<br><br>
     *         DataBinding이 있는 뷰를 보유하는 새로운 BaseBindingRcvViewHolder.<br>
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseBindingRcvViewHolder<BINDING> =
        BaseBindingRcvViewHolder(layoutRes, parent)

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
    override fun onBindViewHolder(holder: BaseBindingRcvViewHolder<BINDING>, position: Int, item: ITEM) {
        onBind(holder, item, position)
    }
}