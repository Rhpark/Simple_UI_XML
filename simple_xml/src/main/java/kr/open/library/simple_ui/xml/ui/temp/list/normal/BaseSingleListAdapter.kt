package kr.open.library.simple_ui.xml.ui.temp.list.normal

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.DiffUtil
import kr.open.library.simple_ui.xml.ui.temp.base.list.BaseListNormalAdapterCore
import kr.open.library.simple_ui.xml.ui.temp.base.list.diffcallback.DefaultDiffCallback
import kr.open.library.simple_ui.xml.ui.temp.viewholder.normal.BaseRcvViewHolder
import java.util.concurrent.Executor

/**
 * Single layout ListAdapter base for non-binding items.<br><br>
 * Non-binding 단일 레이아웃 ListAdapter 베이스입니다.<br>
 */
abstract class BaseSingleListAdapter<ITEM : Any>(
    /**
     * Layout resource for item view inflation.<br><br>
     * 아이템 뷰 인플레이트에 사용할 레이아웃 리소스입니다.<br>
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
) : BaseListNormalAdapterCore<ITEM, BaseRcvViewHolder>(diffCallback, diffExecutor) {
    /**
     * Creates a ViewHolder for the given view type.<br><br>
     * 주어진 뷰 타입에 대한 ViewHolder를 생성합니다.<br>
     */
    override fun createViewHolderInternal(parent: ViewGroup, viewType: Int): BaseRcvViewHolder {
        // Inflate the item view from layout resource.<br><br>레이아웃 리소스로 아이템 뷰를 인플레이트합니다.<br>
        val view = LayoutInflater.from(parent.context).inflate(layoutRes, parent, false)
        // ViewHolder created by subclass.<br><br>하위 클래스가 생성한 ViewHolder입니다.<br>
        return getCreateViewHolder(view)
    }

    /**
     * Binds the item via the simplified onBind callback.<br><br>
     * 단순화된 onBind 콜백으로 아이템을 바인딩합니다.<br>
     */
    final override fun onBindItem(holder: BaseRcvViewHolder, position: Int, item: ITEM) {
        onBind(holder.itemView, item, position)
    }

    /**
     * Binds the item to the provided view.<br><br>
     * 제공된 뷰에 아이템을 바인딩합니다.<br>
     */
    protected abstract fun onBind(view: View, item: ITEM, position: Int)
}
