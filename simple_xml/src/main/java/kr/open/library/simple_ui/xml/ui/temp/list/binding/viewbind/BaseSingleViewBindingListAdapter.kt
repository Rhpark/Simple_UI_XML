package kr.open.library.simple_ui.xml.ui.temp.list.binding.viewbind

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.viewbinding.ViewBinding
import kr.open.library.simple_ui.xml.ui.temp.base.list.BaseListViewBindingAdapterCore
import kr.open.library.simple_ui.xml.ui.temp.base.list.diffcallback.DefaultDiffCallback
import kr.open.library.simple_ui.xml.ui.temp.viewholder.binding.BaseViewBindingViewHolder
import java.util.concurrent.Executor

/**
 * Single layout ViewBinding ListAdapter base.<br><br>
 * ViewBinding 단일 레이아웃 ListAdapter 베이스입니다.<br>
 */
abstract class BaseSingleViewBindingListAdapter<ITEM : Any, BINDING : ViewBinding>(
    /**
     * Inflate function for the ViewBinding.<br><br>
     * ViewBinding 인플레이트 함수입니다.<br>
     */
    private val inflate: (LayoutInflater, ViewGroup, Boolean) -> BINDING,
    /**
     * DiffUtil callback for item comparison.<br><br>
     * 아이템 비교를 위한 DiffUtil 콜백입니다.<br>
     */
    diffCallback: DiffUtil.ItemCallback<ITEM> = DefaultDiffCallback(),
    /**
     * Executor used for background diff computation.<br><br>
     * 백그라운드 diff 계산에 사용하는 Executor입니다.<br>
     */
    diffExecutor: Executor? = null,
) : BaseListViewBindingAdapterCore<ITEM, BINDING>(diffCallback, diffExecutor) {
    /**
     * Creates a ViewBinding ViewHolder for the given view type.<br><br>
     * 주어진 뷰 타입에 대한 ViewBinding ViewHolder를 생성합니다.<br>
     */
    override fun createViewHolderInternal(parent: ViewGroup, viewType: Int): BaseViewBindingViewHolder<BINDING> {
        // Inflate the binding instance.<br><br>binding 인스턴스를 인플레이트합니다.<br>
        val binding = inflate(LayoutInflater.from(parent.context), parent, false)
        // ViewHolder created by subclass.<br><br>하위 클래스가 생성한 ViewHolder입니다.<br>
        return getCreateViewHolder(binding)
    }

    /**
     * Binds the item via the simplified onBind callback.<br><br>
     * 단순화된 onBind 콜백으로 아이템을 바인딩합니다.<br>
     */
    final override fun onBindItem(holder: BaseViewBindingViewHolder<BINDING>, position: Int, item: ITEM) {
        onBind(holder, item, position)
    }

    /**
     * Binds the item to the provided ViewBinding holder.<br><br>
     * 제공된 ViewBinding 홀더에 아이템을 바인딩합니다.<br>
     */
    protected abstract fun onBind(holder: BaseViewBindingViewHolder<BINDING>, item: ITEM, position: Int)
}
