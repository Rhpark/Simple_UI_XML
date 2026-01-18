package kr.open.library.simple_ui.xml.ui.temp.normal.binding.databind

import androidx.annotation.LayoutRes
import androidx.databinding.ViewDataBinding
import kr.open.library.simple_ui.xml.ui.temp.viewholder.binding.BaseBindingViewHolder
import kr.open.library.simple_ui.xml.ui.temp.viewholder.binding.BaseDataBindingViewHolder
import java.util.concurrent.Executor

/**
 * Simple single-layout DataBinding adapter using a lambda-based binder.<br><br>
 * 람다 기반 바인더를 사용하는 간단 단일 레이아웃 DataBinding 어댑터입니다.<br>
 */
open class SimpleSingleDataBindingAdapter<ITEM : Any, BINDING : ViewDataBinding>(
    /**
     * Layout resource for DataBinding inflation.<br><br>
     * DataBinding 인플레이트에 사용할 레이아웃 리소스입니다.<br>
     */
    @LayoutRes layoutRes: Int,
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
    /**
     * Lambda invoked to bind the holder with item data.<br><br>
     * 아이템 데이터를 홀더에 바인딩하는 람다입니다.<br>
     */
    private val onBindItem: (holder: BaseBindingViewHolder<BINDING>, item: ITEM, position: Int) -> Unit,
) : BaseSingleDataBindingAdapter<ITEM, BINDING>(layoutRes, diffUtilEnabled, diffExecutor) {
    /**
     * Creates a basic DataBinding ViewHolder.<br><br>
     * 기본 DataBinding ViewHolder를 생성합니다.<br>
     */
    override fun getCreateViewHolder(binding: BINDING): BaseDataBindingViewHolder<BINDING> = BaseDataBindingViewHolder(binding)

    /**
     * Binds the holder using the provided lambda.<br><br>
     * 제공된 람다로 홀더를 바인딩합니다.<br>
     */
    override fun onBind(holder: BaseDataBindingViewHolder<BINDING>, item: ITEM, position: Int) {
        onBindItem(holder, item, position)
    }
}
