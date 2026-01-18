package kr.open.library.simple_ui.xml.ui.temp.normal.normal

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kr.open.library.simple_ui.core.logcat.Logx
import kr.open.library.simple_ui.xml.ui.temp.base.normal.BaseRcvNormalAdapterCore
import java.util.concurrent.Executor

/**
 * Multi-type RecyclerView.Adapter base for non-binding items.<br><br>
 * Non-binding 다중 타입 RecyclerView.Adapter 베이스입니다.<br>
 */
abstract class BaseMultiAdapter<ITEM : Any, VH : RecyclerView.ViewHolder>(
    /**
     * Provides layout resource based on item and position.<br><br>
     * 아이템과 포지션에 따른 레이아웃 리소스를 제공합니다.<br>
     */
    private val layoutResProvider: (item: ITEM, position: Int) -> Int,
    /**
     * Enables DiffUtil for normal adapter updates; keep OFF for very large replacements.<br><br>
     * 일반 어댑터 업데이트에서 DiffUtil을 활성화하며 대량 치환은 OFF를 권장합니다.<br>
     */
    diffUtilEnabled: Boolean = false,
    /**
     * Executor used for DiffUtil background computation.<br><br>
     * DiffUtil 백그라운드 계산에 사용하는 Executor입니다.<br>
     */
    diffExecutor: Executor? = null,
) : BaseRcvNormalAdapterCore<ITEM, VH>(diffUtilEnabled, diffExecutor) {
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
     * Creates a ViewHolder for the given view type.<br><br>
     * 주어진 뷰 타입에 대한 ViewHolder를 생성합니다.<br>
     */
    override fun createViewHolderInternal(parent: ViewGroup, viewType: Int): VH {
        // Inflate the view for the resolved view type.<br><br>결정된 뷰 타입으로 뷰를 인플레이트합니다.<br>
        val view = LayoutInflater.from(parent.context).inflate(viewType, parent, false)
        // ViewHolder created by subclass.<br><br>하위 클래스가 생성한 ViewHolder입니다.<br>
        return getCreateViewHolder(view)
    }

    /**
     * Binds the item via the simplified onBind callback with view type.<br><br>
     * 뷰 타입을 포함한 단순화된 onBind 콜백으로 아이템을 바인딩합니다.<br>
     */
    final override fun onBindItem(holder: VH, position: Int, item: ITEM) {
        onBind(holder.itemView, item, position, holder.itemViewType)
    }

    /**
     * Binds the item to the provided view using view type information.<br><br>
     * 뷰 타입 정보를 사용해 제공된 뷰에 아이템을 바인딩합니다.<br>
     */
    protected abstract fun onBind(view: View, item: ITEM, position: Int, viewType: Int)
}
