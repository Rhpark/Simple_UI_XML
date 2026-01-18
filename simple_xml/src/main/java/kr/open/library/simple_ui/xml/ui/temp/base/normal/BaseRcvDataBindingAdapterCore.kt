package kr.open.library.simple_ui.xml.ui.temp.base.normal

import androidx.databinding.ViewDataBinding
import kr.open.library.simple_ui.xml.ui.temp.viewholder.binding.BaseDataBindingViewHolder
import java.util.concurrent.Executor

/**
 * Base core for DataBinding RecyclerView adapters.<br><br>
 * DataBinding RecyclerView 어댑터용 베이스 코어입니다.<br>
 */
abstract class BaseRcvDataBindingAdapterCore<ITEM : Any, BINDING : ViewDataBinding>(
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
) : RootRcvAdapterCore<ITEM, BaseDataBindingViewHolder<BINDING>>(diffUtilEnabled, diffExecutor) {
    /**
     * Creates a DataBinding ViewHolder from the given binding instance.<br><br>
     * 주어진 binding 인스턴스로 DataBinding ViewHolder를 생성합니다.<br>
     */
    abstract fun getCreateViewHolder(binding: BINDING): BaseDataBindingViewHolder<BINDING>
}
