package kr.open.library.simple_ui.xml.ui.temp.base.list

import androidx.recyclerview.widget.DiffUtil
import androidx.viewbinding.ViewBinding
import kr.open.library.simple_ui.xml.ui.temp.base.list.diffcallback.DefaultDiffCallback
import kr.open.library.simple_ui.xml.ui.temp.viewholder.binding.BaseViewBindingViewHolder
import java.util.concurrent.Executor

/**
 * Base core for ViewBinding ListAdapter implementations.<br><br>
 * ViewBinding ListAdapter 구현을 위한 베이스 코어입니다.<br>
 */
abstract class BaseListViewBindingAdapterCore<ITEM : Any, BINDING : ViewBinding>(
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
) : RootListAdapterCore<ITEM, BaseViewBindingViewHolder<BINDING>>(diffCallback, diffExecutor) {
    /**
     * Creates a ViewBinding ViewHolder from the given binding instance.<br><br>
     * 주어진 binding 인스턴스로 ViewBinding ViewHolder를 생성합니다.<br>
     */
    abstract fun getCreateViewHolder(binding: BINDING): BaseViewBindingViewHolder<BINDING>
}
