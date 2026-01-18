package kr.open.library.simple_ui.xml.ui.temp.list.binding.databind

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.DiffUtil
import kr.open.library.simple_ui.core.logcat.Logx
import kr.open.library.simple_ui.xml.ui.temp.base.list.BaseListDataBindingAdapterCore
import kr.open.library.simple_ui.xml.ui.temp.base.list.diffcallback.DefaultDiffCallback
import kr.open.library.simple_ui.xml.ui.temp.viewholder.binding.BaseDataBindingViewHolder
import java.util.concurrent.Executor

/**
 * Multi-type DataBinding ListAdapter base.<br><br>
 * 다중 타입 DataBinding ListAdapter 베이스입니다.<br>
 */
abstract class BaseMultiDataBindingListAdapter<ITEM : Any, BINDING : ViewDataBinding>(
    /**
     * Layout resource provider by item and position.<br><br>
     * 아이템과 포지션 기준 레이아웃 리소스 제공자입니다.<br>
     */
    private val layoutResProvider: (item: ITEM, position: Int) -> Int,
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
     * Returns the view type for the given position.<br><br>
     * 지정 포지션의 뷰 타입을 반환합니다.<br>
     */
    override fun getItemViewType(position: Int): Int {
        val item = getItem(position)
        val viewType = layoutResProvider(item, position)
        if (viewType <= 0) {
            val message =
                "Invalid viewType from provider. viewType=$viewType, position=$position, item=${item::class.java.name}, " +
                    "adapter=${this::class.java.name}"
            Logx.e(message)
            throw IllegalArgumentException(message)
        }
        return viewType
    }

    /**
     * Creates a DataBinding ViewHolder for the given view type.<br><br>
     * 주어진 뷰 타입에 대한 DataBinding ViewHolder를 생성합니다.<br>
     */
    override fun createViewHolderInternal(parent: ViewGroup, viewType: Int): BaseDataBindingViewHolder<BINDING> {
        // Layout inflater from parent context.<br><br>부모 컨텍스트에서 가져온 레이아웃 인플레이터입니다.<br>
        val inflater = LayoutInflater.from(parent.context)
        // Binding instance inflated from view type layout resource.<br><br>뷰 타입 레이아웃 리소스에서 인플레이트한 바인딩 인스턴스입니다.<br>
        val binding: BINDING = DataBindingUtil.inflate(inflater, viewType, parent, false)
        // ViewHolder created by subclass.<br><br>하위 클래스가 생성한 ViewHolder입니다.<br>
        return getCreateViewHolder(binding)
    }

    /**
     * Binds the item via the simplified onBind callback with view type.<br><br>
     * 뷰 타입을 포함한 단순화된 onBind 콜백으로 아이템을 바인딩합니다.<br>
     */
    final override fun onBindItem(holder: BaseDataBindingViewHolder<BINDING>, position: Int, item: ITEM) {
        onBind(holder, item, position, holder.itemViewType)
    }

    /**
     * Binds the item to the provided DataBinding holder using view type information.<br><br>
     * 뷰 타입 정보를 사용해 제공된 DataBinding 홀더에 아이템을 바인딩합니다.<br>
     */
    protected abstract fun onBind(holder: BaseDataBindingViewHolder<BINDING>, item: ITEM, position: Int, viewType: Int)
}
