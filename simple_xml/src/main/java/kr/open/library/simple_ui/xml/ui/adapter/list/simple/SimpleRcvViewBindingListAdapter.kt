package kr.open.library.simple_ui.xml.ui.adapter.list.simple

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import kr.open.library.simple_ui.xml.ui.adapter.list.base.BaseRcvListAdapter
import kr.open.library.simple_ui.xml.ui.adapter.list.base.diffutil.RcvListDiffUtilCallBack
import kr.open.library.simple_ui.xml.ui.adapter.viewholder.BaseRcvViewBindingViewHolder

/**
 * A simple RecyclerView ListAdapter implementation with ViewBinding support.<br>
 * Provides a convenient way to create adapters without subclassing.<br><br>
 * ViewBinding을 지원하는 간단한 RecyclerView ListAdapter 구현체입니다.<br>
 * 서브클래싱 없이 어댑터를 생성하는 편리한 방법을 제공합니다.<br>
 *
 * Features:<br>
 * - ViewBinding support for item views<br>
 * - Lambda-based binding without subclassing<br>
 * - Inherits all BaseRcvListAdapter functionality<br>
 * - DiffUtil-based efficient list updates<br><br>
 * 기능:<br>
 * - 아이템 뷰를 위한 ViewBinding 지원<br>
 * - 서브클래싱 없이 람다 기반 바인딩<br>
 * - BaseRcvListAdapter의 모든 기능 상속<br>
 * - DiffUtil 기반 효율적인 리스트 업데이트<br>
 *
 * @param ITEM The type of data items in the list.<br><br>
 *             리스트의 데이터 아이템 타입.<br>
 *
 * @param BINDING The type of ViewBinding for item views.<br><br>
 *                아이템 뷰를 위한 ViewBinding 타입.<br>
 *
 * @param inflate ViewBinding inflate function for item views.<br><br>
 *                아이템 뷰용 ViewBinding inflate 함수.<br>
 *
 * @param listDiffUtil DiffUtil callback for comparing items.<br><br>
 *                     아이템 비교를 위한 DiffUtil 콜백.<br>
 *
 * @param onBind Lambda function to bind data to the ViewHolder.<br><br>
 *               ViewHolder에 데이터를 바인딩하는 람다 함수.<br>
 *
 * @see SimpleRcvListAdapter For adapter without binding support.<br><br>
 *      바인딩이 없는 어댑터는 SimpleRcvListAdapter를 참조하세요.<br>
 *
 * @see SimpleRcvDataBindingListAdapter For adapter with DataBinding support.<br><br>
 *      DataBinding 어댑터는 SimpleRcvDataBindingListAdapter를 참조하세요.<br>
 */
public open class SimpleRcvViewBindingListAdapter<ITEM : Any, BINDING : ViewBinding>(
    private val inflate: (LayoutInflater, ViewGroup, Boolean) -> BINDING,
    listDiffUtil: RcvListDiffUtilCallBack<ITEM>,
    private val onBind: (BaseRcvViewBindingViewHolder<BINDING>, ITEM, position: Int) -> Unit,
) : BaseRcvListAdapter<ITEM, BaseRcvViewBindingViewHolder<BINDING>>(listDiffUtil) {
    /**
     * Creates a new ViewHolder for the given view type.<br><br>
     * 주어진 뷰 타입에 대한 새로운 ViewHolder를 생성합니다.<br>
     */
    override fun createViewHolderInternal(
        parent: ViewGroup,
        viewType: Int,
    ): BaseRcvViewBindingViewHolder<BINDING> {
        val binding = inflate(LayoutInflater.from(parent.context), parent, false)
        return BaseRcvViewBindingViewHolder(binding)
    }

    /**
     * Binds data to the ViewHolder at the specified position.<br>
     * Delegates to the onBind lambda provided in the constructor.<br><br>
     * 지정된 위치의 ViewHolder에 데이터를 바인딩합니다.<br>
     * 생성자에서 제공된 onBind 람다에 위임합니다.<br>
     */
    override fun onBindViewHolder(
        holder: BaseRcvViewBindingViewHolder<BINDING>,
        item: ITEM,
        position: Int,
    ) {
        onBind(holder, item, position)
    }
}
