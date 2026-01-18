package kr.open.library.simple_ui.xml.ui.temp.base.normal

import androidx.viewbinding.ViewBinding
import kr.open.library.simple_ui.xml.ui.temp.viewholder.binding.BaseViewBindingViewHolder
import java.util.concurrent.Executor

/**
 * Base core for ViewBinding RecyclerView adapters.<br><br>
 * ViewBinding RecyclerView 어댑터용 베이스 코어입니다.<br>
 */
abstract class BaseRcvViewBindingAdapterCore<ITEM : Any, BINDING : ViewBinding>(
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
) : RootRcvAdapterCore<ITEM, BaseViewBindingViewHolder<BINDING>>(diffUtilEnabled, diffExecutor) {
    /**
     * Creates a ViewBinding ViewHolder from the given binding instance.<br><br>
     * 주어진 binding 인스턴스로 ViewBinding ViewHolder를 생성합니다.<br>
     */
    abstract fun getCreateViewHolder(binding: BINDING): BaseViewBindingViewHolder<BINDING>
}
