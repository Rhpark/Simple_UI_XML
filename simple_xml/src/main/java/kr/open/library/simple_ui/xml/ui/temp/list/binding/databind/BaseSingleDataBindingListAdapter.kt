package kr.open.library.simple_ui.xml.ui.temp.list.binding.databind

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.DiffUtil
import kr.open.library.simple_ui.xml.ui.temp.base.list.BaseListDataBindingAdapterCore
import kr.open.library.simple_ui.xml.ui.temp.base.list.diffcallback.DefaultDiffCallback
import kr.open.library.simple_ui.xml.ui.temp.viewholder.binding.BaseDataBindingViewHolder
import java.util.concurrent.Executor

/**
 * Single-type DataBinding ListAdapter base.<br><br>
 * 단일 타입 DataBinding ListAdapter 베이스입니다.<br>
 */
abstract class BaseSingleDataBindingListAdapter<ITEM : Any, BINDING : ViewDataBinding>(
    /**
     * Layout resource ID for the binding.<br><br>
     * 바인딩 레이아웃 리소스 ID입니다.<br>
     */
    @LayoutRes private val layoutRes: Int,
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
) : BaseListDataBindingAdapterCore<ITEM, BINDING>(diffCallback, diffExecutor) {
    /**
     * Creates a DataBinding ViewHolder for the item view.<br><br>
     * 아이템 뷰용 DataBinding ViewHolder를 생성합니다.<br>
     */
    override fun createViewHolderInternal(parent: ViewGroup, viewType: Int): BaseDataBindingViewHolder<BINDING> {
        // Layout inflater from parent context.<br><br>부모 컨텍스트에서 가져온 레이아웃 인플레이터입니다.<br>
        val inflater = LayoutInflater.from(parent.context)
        // Binding instance inflated from layout resource.<br><br>레이아웃 리소스에서 인플레이트한 바인딩 인스턴스입니다.<br>
        val binding: BINDING = DataBindingUtil.inflate(inflater, layoutRes, parent, false)
        // ViewHolder created by subclass.<br><br>하위 클래스가 생성한 ViewHolder입니다.<br>
        return getCreateViewHolder(binding)
    }

    /**
     * Routes binding to the simplified onBind callback.<br><br>
     * 단순화된 onBind 콜백으로 바인딩을 전달합니다.<br>
     */
    final override fun onBindItem(holder: BaseDataBindingViewHolder<BINDING>, position: Int, item: ITEM) {
        onBind(holder, item, position)
    }

    /**
     * Binds the item to the provided DataBinding holder.<br><br>
     * 제공된 DataBinding 홀더에 아이템을 바인딩합니다.<br>
     */
    protected abstract fun onBind(holder: BaseDataBindingViewHolder<BINDING>, item: ITEM, position: Int)
}
