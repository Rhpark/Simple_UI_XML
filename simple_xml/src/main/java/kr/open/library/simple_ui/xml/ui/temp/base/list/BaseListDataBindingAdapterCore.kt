package kr.open.library.simple_ui.xml.ui.temp.base.list

import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.DiffUtil
import kr.open.library.simple_ui.xml.ui.temp.base.list.diffcallback.DefaultDiffCallback
import kr.open.library.simple_ui.xml.ui.temp.viewholder.binding.BaseDataBindingViewHolder
import java.util.concurrent.Executor

/**
 * Base core for DataBinding ListAdapter implementations.<br><br>
 * DataBinding ListAdapter 구현을 위한 베이스 코어입니다.<br>
 */
abstract class BaseListDataBindingAdapterCore<ITEM : Any, BINDING : ViewDataBinding>(
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
) : RootListAdapterCore<ITEM, BaseDataBindingViewHolder<BINDING>>(diffCallback, diffExecutor) {
    /**
     * Creates a DataBinding ViewHolder from the given binding instance.<br><br>
     * 주어진 binding 인스턴스로 DataBinding ViewHolder를 생성합니다.<br>
     */
    abstract fun getCreateViewHolder(binding: BINDING): BaseDataBindingViewHolder<BINDING>
}
