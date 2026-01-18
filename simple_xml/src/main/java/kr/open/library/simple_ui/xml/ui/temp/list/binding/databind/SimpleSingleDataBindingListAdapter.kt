package kr.open.library.simple_ui.xml.ui.temp.list.binding.databind

import androidx.annotation.LayoutRes
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.DiffUtil
import kr.open.library.simple_ui.xml.ui.temp.base.list.diffcallback.DefaultDiffCallback
import kr.open.library.simple_ui.xml.ui.temp.viewholder.binding.BaseBindingViewHolder
import kr.open.library.simple_ui.xml.ui.temp.viewholder.binding.BaseDataBindingViewHolder
import java.util.concurrent.Executor

/**
 * Simple single-type DataBinding ListAdapter with bind lambda.<br><br>
 * 바인드 람다를 사용하는 단일 타입 DataBinding ListAdapter입니다.<br>
 */
open class SimpleSingleDataBindingListAdapter<ITEM : Any, BINDING : ViewDataBinding>(
    /**
     * Layout resource ID for the binding.<br><br>
     * 바인딩 레이아웃 리소스 ID입니다.<br>
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
     * Binding callback for items.<br><br>
     * 아이템 바인딩 콜백입니다.<br>
     */
    private val onBindItem: (holder: BaseBindingViewHolder<BINDING>, item: ITEM, position: Int) -> Unit,
) : BaseSingleDataBindingListAdapter<ITEM, BINDING>(layoutRes, diffCallback, diffExecutor) {
    /**
     * Creates the default DataBinding ViewHolder.<br><br>
     * 기본 DataBinding ViewHolder를 생성합니다.<br>
     */
    override fun getCreateViewHolder(binding: BINDING): BaseDataBindingViewHolder<BINDING> =
        BaseDataBindingViewHolder(binding)

    /**
     * Binds the item through the provided lambda.<br><br>
     * 제공된 람다로 아이템을 바인딩합니다.<br>
     */
    override fun onBind(holder: BaseDataBindingViewHolder<BINDING>, item: ITEM, position: Int) {
        onBindItem(holder, item, position)
    }
}
