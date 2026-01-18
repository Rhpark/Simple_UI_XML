package kr.open.library.simple_ui.xml.ui.temp.base.list

import android.view.View
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import kr.open.library.simple_ui.xml.ui.temp.base.list.diffcallback.DefaultDiffCallback
import java.util.concurrent.Executor

/**
 * Base core for non-binding ListAdapter implementations.<br><br>
 * Non-binding ListAdapter 구현을 위한 베이스 코어입니다.<br>
 */
abstract class BaseListNormalAdapterCore<ITEM : Any, VH : RecyclerView.ViewHolder>(
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
) : RootListAdapterCore<ITEM, VH>(diffCallback, diffExecutor) {
    /**
     * Creates a ViewHolder from the given view.<br><br>
     * 주어진 View로 ViewHolder를 생성합니다.<br>
     */
    abstract fun getCreateViewHolder(view: View): VH
}
