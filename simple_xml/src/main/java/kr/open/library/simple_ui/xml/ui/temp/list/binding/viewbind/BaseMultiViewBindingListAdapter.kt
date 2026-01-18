package kr.open.library.simple_ui.xml.ui.temp.list.binding.viewbind

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.viewbinding.ViewBinding
import kr.open.library.simple_ui.core.logcat.Logx
import kr.open.library.simple_ui.xml.ui.temp.base.list.BaseListViewBindingAdapterCore
import kr.open.library.simple_ui.xml.ui.temp.base.list.diffcallback.DefaultDiffCallback
import kr.open.library.simple_ui.xml.ui.temp.viewholder.binding.BaseViewBindingViewHolder
import java.util.concurrent.Executor

/**
 * Multi-type ViewBinding ListAdapter base.<br><br>
 * ViewBinding 다중 타입 ListAdapter 베이스입니다.<br>
 */
abstract class BaseMultiViewBindingListAdapter<ITEM : Any, BINDING : ViewBinding>(
    /**
     * Provides view type based on item and position.<br><br>
     * 아이템과 포지션에 따른 뷰 타입을 제공합니다.<br>
     */
    private val viewTypeProvider: (item: ITEM, position: Int) -> Int,
    /**
     * Inflate function map keyed by view type.<br><br>
     * 뷰 타입 기준의 인플레이트 함수 맵입니다.<br>
     */
    private val inflateMap: Map<Int, (LayoutInflater, ViewGroup, Boolean) -> BINDING>,
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
     * Returns the view type for the given position.<br><br>
     * 지정 포지션의 뷰 타입을 반환합니다.<br>
     */
    override fun getItemViewType(position: Int): Int {
        val item = getItem(position)
        val viewType = viewTypeProvider(item, position)
        if (!inflateMap.containsKey(viewType)) {
            val message =
                "Invalid viewType from provider. viewType=$viewType, position=$position, item=${item::class.java.name}, " +
                    "adapter=${this::class.java.name}, availableViewTypes=${inflateMap.keys.sorted()}"
            Logx.e(message)
            throw IllegalArgumentException(message)
        }
        return viewType
    }

    /**
     * Creates a ViewBinding ViewHolder for the given view type.<br><br>
     * 주어진 뷰 타입에 대한 ViewBinding ViewHolder를 생성합니다.<br>
     */
    override fun createViewHolderInternal(parent: ViewGroup, viewType: Int): BaseViewBindingViewHolder<BINDING> {
        // Inflate function resolved by view type.<br><br>뷰 타입으로 조회한 인플레이트 함수입니다.<br>
        val inflate = inflateMap[viewType]
        if (inflate == null) {
            val message =
                "Missing inflate for viewType=$viewType, adapter=${this::class.java.name}, availableViewTypes=${inflateMap.keys.sorted()}"
            Logx.e(message)
            throw IllegalArgumentException(message)
        }
        // Inflate the binding instance.<br><br>binding 인스턴스를 인플레이트합니다.<br>
        val binding: BINDING = inflate(LayoutInflater.from(parent.context), parent, false)
        // ViewHolder created by subclass.<br><br>하위 클래스가 생성한 ViewHolder입니다.<br>
        return getCreateViewHolder(binding)
    }

    /**
     * Binds the item via the simplified onBind callback with view type.<br><br>
     * 뷰 타입을 포함한 단순화된 onBind 콜백으로 아이템을 바인딩합니다.<br>
     */
    final override fun onBindItem(holder: BaseViewBindingViewHolder<BINDING>, position: Int, item: ITEM) {
        onBind(holder, item, position, holder.itemViewType)
    }

    /**
     * Binds the item to the provided ViewBinding holder using view type information.<br><br>
     * 뷰 타입 정보를 사용해 제공된 ViewBinding 홀더에 아이템을 바인딩합니다.<br>
     */
    protected abstract fun onBind(holder: BaseViewBindingViewHolder<BINDING>, item: ITEM, position: Int, viewType: Int)
}
