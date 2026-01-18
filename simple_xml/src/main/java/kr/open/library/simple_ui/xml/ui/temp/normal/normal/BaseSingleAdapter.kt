package kr.open.library.simple_ui.xml.ui.temp.normal.normal

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView
import kr.open.library.simple_ui.xml.ui.temp.base.normal.BaseRcvNormalAdapterCore
import java.util.concurrent.Executor

/**
 * Single layout RecyclerView.Adapter base for non-binding items.<br><br>
 * Non-binding 단일 레이아웃 RecyclerView.Adapter 베이스입니다.<br>
 */
abstract class BaseSingleAdapter<ITEM : Any, VH : RecyclerView.ViewHolder>(
    /**
     * Layout resource for item view inflation.<br><br>
     * 아이템 뷰 인플레이트에 사용할 레이아웃 리소스입니다.<br>
     */
    @LayoutRes private val layoutRes: Int,
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
     * Creates a ViewHolder for the given view type.<br><br>
     * 주어진 뷰 타입에 대한 ViewHolder를 생성합니다.<br>
     */
    override fun createViewHolderInternal(parent: ViewGroup, viewType: Int): VH {
        // Inflate the item view from layout resource.<br><br>레이아웃 리소스로 아이템 뷰를 인플레이트합니다.<br>
        val view = LayoutInflater.from(parent.context).inflate(layoutRes, parent, false)
        // ViewHolder created by subclass.<br><br>하위 클래스가 생성한 ViewHolder입니다.<br>
        return getCreateViewHolder(view)
    }

    /**
     * Binds the item via the simplified onBind callback.<br><br>
     * 단순화된 onBind 콜백으로 아이템을 바인딩합니다.<br>
     */
    final override fun onBindItem(holder: VH, position: Int, item: ITEM) {
        onBind(holder.itemView, item, position)
    }

    /**
     * Binds the item to the provided view.<br><br>
     * 제공된 뷰에 아이템을 바인딩합니다.<br>
     */
    protected abstract fun onBind(view: View, item: ITEM, position: Int)
}
