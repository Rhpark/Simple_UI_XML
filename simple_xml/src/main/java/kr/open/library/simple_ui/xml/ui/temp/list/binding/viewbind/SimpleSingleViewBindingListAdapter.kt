package kr.open.library.simple_ui.xml.ui.temp.list.binding.viewbind

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.viewbinding.ViewBinding
import kr.open.library.simple_ui.xml.ui.temp.base.list.diffcallback.DefaultDiffCallback
import kr.open.library.simple_ui.xml.ui.temp.viewholder.binding.BaseViewBindingViewHolder
import java.util.concurrent.Executor

/**
 * Simple single-type ViewBinding ListAdapter with bind lambda.<br><br>
 * 바인드 람다를 사용하는 단일 타입 ViewBinding ListAdapter입니다.<br>
 */
open class SimpleSingleViewBindingListAdapter<ITEM : Any, BINDING : ViewBinding>(
    /**
     * Inflate function for creating the binding.<br><br>
     * 바인딩 생성을 위한 인플레이트 함수입니다.<br>
     */
    inflate: (LayoutInflater, ViewGroup, Boolean) -> BINDING,
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
    private val onBindItem: (holder: BaseViewBindingViewHolder<BINDING>, item: ITEM, position: Int) -> Unit,
) : BaseSingleViewBindingListAdapter<ITEM, BINDING>(inflate, diffCallback, diffExecutor) {
    /**
     * Binds the item through the provided lambda.<br><br>
     * 제공된 람다로 아이템을 바인딩합니다.<br>
     */
    override fun onBind(holder: BaseViewBindingViewHolder<BINDING>, item: ITEM, position: Int) {
        onBindItem(holder, item, position)
    }

    /**
     * Creates the default ViewBinding ViewHolder.<br><br>
     * 기본 ViewBinding ViewHolder를 생성합니다.<br>
     */
    override fun getCreateViewHolder(binding: BINDING): BaseViewBindingViewHolder<BINDING> =
        BaseViewBindingViewHolder(binding)
}
