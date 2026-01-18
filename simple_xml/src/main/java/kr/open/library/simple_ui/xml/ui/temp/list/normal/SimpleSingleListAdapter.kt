package kr.open.library.simple_ui.xml.ui.temp.list.normal

import android.view.View
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.DiffUtil
import kr.open.library.simple_ui.xml.ui.temp.base.list.diffcallback.DefaultDiffCallback
import kr.open.library.simple_ui.xml.ui.temp.viewholder.normal.BaseRcvViewHolder
import java.util.concurrent.Executor

/**
 * Simple single-layout ListAdapter using a lambda-based binder.<br><br>
 * 람다 기반 바인더를 사용하는 간단 단일 레이아웃 ListAdapter입니다.<br>
 */
open class SimpleSingleListAdapter<ITEM : Any>(
    /**
     * Layout resource for item view inflation.<br><br>
     * 아이템 뷰 인플레이트에 사용할 레이아웃 리소스입니다.<br>
     */
    @LayoutRes layoutRes: Int,
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
    /**
     * Lambda invoked to bind the view with item data.<br><br>
     * 아이템 데이터를 뷰에 바인딩하는 람다입니다.<br>
     */
    private val onBindItem: (view: View, item: ITEM, position: Int) -> Unit,
) : BaseSingleListAdapter<ITEM>(layoutRes, diffCallback, diffExecutor) {
    /**
     * Binds the view using the provided lambda.<br><br>
     * 제공된 람다로 뷰를 바인딩합니다.<br>
     */
    override fun onBind(view: View, item: ITEM, position: Int) {
        onBindItem(view, item, position)
    }

    /**
     * Creates a basic RecyclerView ViewHolder.<br><br>
     * 기본 RecyclerView ViewHolder를 생성합니다.<br>
     */
    override fun getCreateViewHolder(view: View): BaseRcvViewHolder = BaseRcvViewHolder(view)
}
